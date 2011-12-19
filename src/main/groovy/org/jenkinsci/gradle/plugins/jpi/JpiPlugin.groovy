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

package org.jenkinsci.gradle.plugins.jpi;


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
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.plugins.MavenPluginConvention
import org.gradle.api.artifacts.maven.Conf2ScopeMappingContainer;
import org.gradle.api.artifacts.maven.MavenResolver
import org.gradle.api.artifacts.maven.MavenPom;

/**
 * Loads HPI related tasks into the current project.
 *
 * @author Hans Dockter
 * @author Kohsuke Kawaguchi
 */
public class JpiPlugin implements Plugin<Project> {
    /**
     * Represents the dependency to the Jenkins core.
     */
    public static final String CORE_DEPENDENCY_CONFIGURATION_NAME = "jenkinsCore";

    /**
     * Represents the dependency to the Jenkins war. Test scope.
     */
    public static final String WAR_DEPENDENCY_CONFIGURATION_NAME = "jenkinsWar";


    public static final String WEB_APP_GROUP = "web application";

    public void apply(final Project gradleProject) {
        gradleProject.plugins.apply(JavaPlugin);
        gradleProject.plugins.apply(WarPlugin);
        gradleProject.plugins.apply(MavenPlugin);
        gradleProject.plugins.apply(GroovyPlugin);
        def pluginConvention = new JpiPluginConvention();
        gradleProject.convention.plugins["jpi"] = pluginConvention

        def warConvention = gradleProject.convention.getPlugin(WarPluginConvention);

        // never run war as it's useless
        gradleProject.tasks.getByName("war").onlyIf { false }

        def ext = new JpiExtension(gradleProject)
        gradleProject.extensions.jenkinsPlugin = ext;

        gradleProject.tasks.withType(Jpi) { Jpi task ->
            task.from {
                return warConvention.webAppDir;
            }
            task.dependsOn {
                ext.mainSourceTree().runtimeClasspath
            }
            task.classpath {
                ext.runtimeClasspath;
            }
            task.archiveName = "${ext.shortName}.${ext.fileExtension}";
        }
        gradleProject.tasks.withType(ServerTask) { ServerTask task ->
            task.dependsOn {
                ext.mainSourceTree().runtimeClasspath
            }
        }
        gradleProject.tasks.withType(StaplerGroovyStubsTask) { StaplerGroovyStubsTask task ->
            task.destinationDir = ext.getStaplerStubDir()
        }

        def jpi = gradleProject.tasks.add(Jpi.TASK_NAME, Jpi);
        jpi.description = "Generates the JPI package";
        jpi.group = BasePlugin.BUILD_GROUP;
        gradleProject.extensions.getByType(DefaultArtifactPublicationSet).addCandidate(new ArchivePublishArtifact(jpi));

        def server = gradleProject.tasks.add(ServerTask.TASK_NAME, ServerTask);
        server.description = "Run Jenkins in place with the plugin being developed";
        server.group = BasePlugin.BUILD_GROUP; // TODO

        def stubs = gradleProject.tasks.add(StaplerGroovyStubsTask.TASK_NAME, StaplerGroovyStubsTask)
        stubs.description = "Generates the Java stubs from Groovy source to enable Stapler annotation processing."
        stubs.group = BasePlugin.BUILD_GROUP

        gradleProject.tasks.compileJava.dependsOn(StaplerGroovyStubsTask.TASK_NAME)
        configureConfigurations(gradleProject.configurations);

        def mvnConvention = gradleProject.convention.getPlugin(MavenPluginConvention)
        mvnConvention.getConf2ScopeMappings().addMapping(MavenPlugin.PROVIDED_COMPILE_PRIORITY,
                                                         gradleProject.configurations[CORE_DEPENDENCY_CONFIGURATION_NAME],
                                                         Conf2ScopeMappingContainer.PROVIDED)

        def installer = gradleProject.tasks.getByName("install")

        
        // default configuration of uploadArchives Maven task
        def uploadArchives = gradleProject.tasks.getByName("uploadArchives")
        uploadArchives.doFirst {
            repositories {
                mavenDeployer {
                    // configure this only when the user didn't give any explicit configuration
                    // whatever in build.gradle should win what we have here
                    if (repository==null && snapshotRepository==null) {
                        System.out.println("Deploying to the Jenkins community repository")
                        def props = loadDotJenkinsOrg()

                        repository(url: ext.repoUrl) {
                            authentication(userName:props["userName"], password:props["password"])
                        }
                        snapshotRepository(url:ext.snapshotRepoUrl) {
                            authentication(userName:props["userName"], password:props["password"])
                        }
                    }
                }
            }
        }

        def mavenUploaders = [installer,uploadArchives]*.repositories*.find { it instanceof MavenResolver }

        mavenUploaders.each { m ->
            if (m != null) { 
                m.pom.whenConfigured { p -> 
                    p.project { 
                        parent {
                            groupId 'org.jenkins-ci.plugins'
                            artifactId 'plugin'
                            version ext.getCoreVersion()
                        }
                        repositories { 
                            gradleProject.repositories.each { repo ->
                                if (repo.name == 'MavenRepo' || repo.name == 'MavenLocal') {
                                    // do not include the Maven Central repository or the local cache.
                                    return
                                }
                                repository {
                                    id = repo.name
                                    url = repo.url
                                }
                            }
                        }
                    }
                }
            }
        }
                

        // creating alias for making migration from Maven easy.
        gradleProject.tasks.create("deploy").dependsOn(uploadArchives)
    }
    
    private Properties loadDotJenkinsOrg() {
        Properties props = new Properties()
        def dot = new File(new File(System.getProperty("user.home")), ".jenkins-ci.org")
        if (!dot.exists())
            throw new Exception("Trying to deploy to Jenkins community repository but there's no credential file ${dot}. See https://wiki.jenkins-ci.org/display/JENKINS/Dot+Jenkins+Ci+Dot+Org")
        dot.withInputStream { i -> props.load(i) }
        return props
    }

    public void configureConfigurations(ConfigurationContainer cc) {
        Configuration jenkinsCoreConfiguration = cc.add(CORE_DEPENDENCY_CONFIGURATION_NAME).setVisible(false).
                setDescription("Jenkins core that your plugin is built against");
        cc.getByName(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME).extendsFrom(jenkinsCoreConfiguration);

        cc.add(WAR_DEPENDENCY_CONFIGURATION_NAME).setVisible(false).
                setDescription("Jenkins war that corresponds to the Jenkins core");
    }

}
