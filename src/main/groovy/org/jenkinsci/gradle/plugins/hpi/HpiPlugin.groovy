/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jenkinsci.gradle.plugins.hpi;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact;
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;


import java.util.concurrent.Callable;

/**
 * <p>A {@link Plugin} which extends the {@link JavaPlugin} to add tasks which assemble a web application into a WAR
 * file.</p>
 *
 * @author Hans Dockter
 */
public class HpiPlugin implements Plugin<Project> {
    public static final String CORE_DEPENDENCY_CONFIGURATION_NAME = "jenkinsCore";
    public static final String HPI_TASK_NAME = "hpi";
    public static final String WEB_APP_GROUP = "web application";

    public void apply(final Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(WarPlugin.class);
        final HpiPluginConvention pluginConvention = new HpiPluginConvention(project);
        project.getConvention().getPlugins().put("hpi", pluginConvention);

        final WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);
        
        
        project.getTasks().withType(Hpi.class, new Action<Hpi>() {
            public void execute(Hpi task) {
                task.from(new Callable() {
                    public Object call() throws Exception {
                        return warConvention.getWebAppDir();
                    }
                });
                task.dependsOn(new Callable() {
                    public Object call() throws Exception {
                        return project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName(
                                SourceSet.MAIN_SOURCE_SET_NAME).getRuntimeClasspath();
                    }
                });
                task.classpath(new Callable() {
                    public Object call() throws Exception {
                        FileCollection runtimeClasspath = project.getConvention().getPlugin(JavaPluginConvention.class)
                                .getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getRuntimeClasspath();
                        Configuration providedRuntime = project.getConfigurations().getByName(
                                WarPlugin.PROVIDED_RUNTIME_CONFIGURATION_NAME);
                        return runtimeClasspath.minus(providedRuntime);
                    }
                });
                task.setArchiveName(pluginConvention.getShortName()+".hpi");
                task.configureManifest();
            }
        });
        
        Hpi war = project.getTasks().add(HPI_TASK_NAME, Hpi.class);
        war.setDescription("Generates the HPI package");
        war.setGroup(BasePlugin.BUILD_GROUP);
        project.getExtensions().getByType(DefaultArtifactPublicationSet.class).addCandidate(new ArchivePublishArtifact(war));
        configureConfigurations(project.getConfigurations());

        project.getExtensions().add("hpi", new HpiExtension(project));
    }

    public void configureConfigurations(ConfigurationContainer configurationContainer) {
        Configuration jenkinsCoreConfiguration = configurationContainer.add(CORE_DEPENDENCY_CONFIGURATION_NAME).setVisible(false).
                setDescription("Jenkins core that your plugin is built against");
        configurationContainer.getByName(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME).extendsFrom(jenkinsCoreConfiguration);
    }

}
