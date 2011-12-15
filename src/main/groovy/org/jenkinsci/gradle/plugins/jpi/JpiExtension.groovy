package org.jenkinsci.gradle.plugins.jpi

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.SourceSet

/**
 * This gets exposed to the project as 'hpi' to offer additional convenience methods.
 *
 * @author Kohsuke Kawaguchi
 */
class JpiExtension {
    final Project project

    def JpiExtension(Project project) {
        this.project = project
    }

    private String shortName;

    /**
     * Short name of the plugin is the ID that uniquely identifies a plugin.
     * If unspecified, we use the project name except the trailing "-plugin"
     */
    String getShortName() {
        return shortName ?: trimOffPluginSuffix(project.name)
    }

    private String trimOffPluginSuffix(String s) {
        if (s.endsWith("-plugin"))
            s = s[0..-8]
        return s;
    }

    private String displayName;

    /**
     * One-line display name of this plugin. Should be human readable.
     * For example, "Git plugin", "Acme Executor plugin", etc.
     */
    String getDisplayName() {
        return displayName ?: getShortName()
    }

    void setDisplayName(String s) {
        this.displayName = s;
    }

    /**
     * URL that points to the home page of this plugin.
     */
    public String url;

    /**
     * TODO: document
     */
    public String compatibleSinceVersion;

    /**
     * TODO: document
     */
    public boolean sandboxStatus;
    
    /**
     * TODO: document
     */
    public String maskClasses;

    /**
     * Version of core that we depend on.
     */
    private String coreVersion;

    String getCoreVersion() {
        return coreVersion
    }

    void setCoreVersion(String v) {
        this.coreVersion = v

        project.repositories {
            mavenLocal()
            mavenCentral()
            maven {
                delegate.url("http://maven.glassfish.org/content/groups/public/")
            }
        }

        project.dependencies {
            jenkinsCore(
                [group: 'org.jenkins-ci.main', name: 'jenkins-core', version: v, ext: 'jar', transitive: true],
                [group: 'javax.servlet', name: 'servlet-api', version: '2.4']
            )

            jenkinsWar(group: 'org.jenkins-ci.main', name: 'jenkins-war', version: v, ext: 'war')
        }
    }
    
    private String staplerStubDir

    /**
     * Sets the stapler stubs output directory
     */
    void setStaplerStubDir(String staplerStubDir) {
        this.staplerStubDir = staplerStubDir
    }
    
    /**
     * Returns the Stapler stubs directory.
     */
    File getStaplerStubDir() {
        def stubDir = staplerStubDir ?: 'generated-src/stubs'
        project.file("${project.buildDir}/${stubDir}")
    }
    
    private File workDir;
    
    File getWorkDir() {
        return workDir ?: new File(project.rootDir,"work");
    }

    /**
     * Work directory to run Jenkins.war with.
     */
    void setWorkDir(File workDir) {
        this.workDir = workDir
    }

    /**
     * Runtime dependencies
     */
    public FileCollection getRuntimeClasspath() {
        def providedRuntime = project.configurations.getByName(WarPlugin.PROVIDED_RUNTIME_CONFIGURATION_NAME);
        return mainSourceTree().runtimeClasspath.minus(providedRuntime)
    }

    public SourceSet mainSourceTree() {
        return project.convention.getPlugin(JavaPluginConvention)
                .sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
    }


}
