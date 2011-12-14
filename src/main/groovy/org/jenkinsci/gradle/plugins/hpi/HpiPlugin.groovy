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


import java.util.concurrent.Callable
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.plugins.WarPluginConvention
import org.gradle.api.tasks.SourceSet

/**
 * Loads HPI related tasks into the current project.
 *
 * @author Hans Dockter
 * @author Kohsuke Kawaguchi
 */
public class HpiPlugin implements Plugin<Project> {
    public static final String CORE_DEPENDENCY_CONFIGURATION_NAME = "jenkinsCore";
    public static final String HPI_TASK_NAME = "hpi";
    public static final String WEB_APP_GROUP = "web application";

    public void apply(final Project project) {
        project.plugins.apply(JavaPlugin);
        project.plugins.apply(WarPlugin);
        def pluginConvention = new HpiPluginConvention(project);
        project.convention.hpi = pluginConvention

        def warConvention = project.convention.getPlugin(WarPluginConvention);
        
        
        project.getTasks().withType(Hpi.class, new Action<Hpi>() {
            public void execute(Hpi task) {
                task.from(new Callable() {
                    public Object call() throws Exception {
                        return warConvention.webAppDir;
                    }
                });
                task.dependsOn(new Callable() {
                    public Object call() throws Exception {
                        return project.convention.getPlugin(JavaPluginConvention).sourceSets.getByName(
                                SourceSet.MAIN_SOURCE_SET_NAME).runtimeClasspath;
                    }
                });
                task.classpath(new Callable() {
                    public Object call() throws Exception {
                        def runtimeClasspath = project.convention.getPlugin(JavaPluginConvention)
                                .sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).runtimeClasspath;
                        def providedRuntime = project.configurations.getByName(
                                WarPlugin.PROVIDED_RUNTIME_CONFIGURATION_NAME);
                        return runtimeClasspath.minus(providedRuntime);
                    }
                });
                task.archiveName = "${pluginConvention.shortName}.hpi";
                task.configureManifest();
            }
        });
        
        def war = project.tasks.add(HPI_TASK_NAME, Hpi);
        war.description = "Generates the HPI package";
        war.group = BasePlugin.BUILD_GROUP;
        project.extensions.getByType(DefaultArtifactPublicationSet).addCandidate(new ArchivePublishArtifact(war));
        configureConfigurations(project.configurations);

        project.extensions.hpi = new HpiExtension(project);
    }

    public void configureConfigurations(ConfigurationContainer configurationContainer) {
        Configuration jenkinsCoreConfiguration = configurationContainer.add(CORE_DEPENDENCY_CONFIGURATION_NAME).setVisible(false).
                setDescription("Jenkins core that your plugin is built against");
        configurationContainer.getByName(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME).extendsFrom(jenkinsCoreConfiguration);
    }

}
