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


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.plugins.WarPluginConvention

/**
 * Loads HPI related tasks into the current project.
 *
 * @author Hans Dockter
 * @author Kohsuke Kawaguchi
 */
public class HpiPlugin implements Plugin<Project> {
    /**
     * Represents the dependency to the Jenkins core.
     */
    public static final String CORE_DEPENDENCY_CONFIGURATION_NAME = "jenkinsCore";

    /**
     * Represents the dependency to the Jenkins war. Test scope.
     */
    public static final String WAR_DEPENDENCY_CONFIGURATION_NAME = "jenkinsWar";


    public static final String WEB_APP_GROUP = "web application";

    public void apply(final Project project) {
        project.plugins.apply(JavaPlugin);
        project.plugins.apply(WarPlugin);
        def pluginConvention = new HpiPluginConvention();
        project.convention.plugins["hpi"] = pluginConvention

        def warConvention = project.convention.getPlugin(WarPluginConvention);

        def ext = new HpiExtension(project)
        project.extensions.jenkinsPlugin = ext;

        project.tasks.withType(Hpi) { Hpi task ->
            task.from {
                return warConvention.webAppDir;
            }
            task.dependsOn {
                ext.mainSourceTree().runtimeClasspath
            }
            task.classpath {
                ext.runtimeClasspath;
            }
            task.archiveName = "${ext.shortName}.hpi";
        }
        project.tasks.withType(ServerTask) { ServerTask task ->
            task.dependsOn {
                ext.mainSourceTree().runtimeClasspath
            }
        }

        def hpi = project.tasks.add(Hpi.TASK_NAME, Hpi);
        hpi.description = "Generates the HPI package";
        hpi.group = BasePlugin.BUILD_GROUP;
        project.extensions.getByType(DefaultArtifactPublicationSet).addCandidate(new ArchivePublishArtifact(hpi));

        def server = project.tasks.add(ServerTask.TASK_NAME, ServerTask);
        server.description = "Run Jenkins in place with the plugin being developed";
        server.group = BasePlugin.BUILD_GROUP; // TODO

        configureConfigurations(project.configurations);
    }

    public void configureConfigurations(ConfigurationContainer cc) {
        Configuration jenkinsCoreConfiguration = cc.add(CORE_DEPENDENCY_CONFIGURATION_NAME).setVisible(false).
                setDescription("Jenkins core that your plugin is built against");
        cc.getByName(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME).extendsFrom(jenkinsCoreConfiguration);

        cc.add(WAR_DEPENDENCY_CONFIGURATION_NAME).setVisible(false).
                setDescription("Jenkins war that corresponds to the Jenkins core");
    }

}
