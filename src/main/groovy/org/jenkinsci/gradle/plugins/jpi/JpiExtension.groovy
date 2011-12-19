package org.jenkinsci.gradle.plugins.jpi

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.SourceSet

/**
 * This gets exposed to the project as 'jpi' to offer additional convenience methods.
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

    private String fileExtension

    /**
     * File extension for plugin archives.
     */
    String getFileExtension() {
        return fileExtension ?: "hpi"
    }

    void setFileExtension(String s) {
        this.fileExtension = s
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
                name "m.g.o"
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

    private String repoUrl

    /**
     * The URL for the Maven repository to deploy the built plugin to.
     */
    String getRepoUrl() {
        return repoUrl ?: 'http://maven.jenkins-ci.org:8081/content/repositories/releases'
    }

    void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl
    }

    private String snapshotRepoUrl
    
    /**
     * The URL for the Maven snapshot repository to deploy the built plugin to.
     */
    String getSnapshotRepoUrl() {
        return repoUrl ?: 'http://maven.jenkins-ci.org:8081/content/repositories/snapshots'
    }

    void setSnapshotRepoUrl(String snapshotRepoUrl) {
        this.snapshotRepoUrl = snapshotRepoUrl
    }


    /**
     * Maven repo deployment credentials.
     */
    String getJpiDeployUser() {
        if (project.hasProperty("jpi.deploy.user")) {
            return project.property("jpi.deploy.user")
        } else {
            return ''
        }
    }
    
    String getJpiDeployPassword() {
        if (project.hasProperty("jpi.deploy.password")) {
            return project.property("jpi.deploy.password")
        } else {
            return ''
        }
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
