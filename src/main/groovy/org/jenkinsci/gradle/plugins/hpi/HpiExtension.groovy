package org.jenkinsci.gradle.plugins.hpi

import org.gradle.api.Project

/**
 * This gets exposed to the project as 'hpi' to offer additional convenience methods.
 *
 * @author Kohsuke Kawaguchi
 */
class HpiExtension {
    private final Project project;

    HpiExtension(Project project) {
        this.project = project
    }

    /**
     * Set up the Jenkins core dependency (and repositories to resolve it)
     */
    public void dependOnCore(String version) {
        project.repositories {
          mavenLocal()
          mavenCentral()
          maven {
            url "http://maven.glassfish.org/content/groups/public/"
          }
        }

        project.dependencies {
          providedCompile(
            [group: 'org.jenkins-ci.main', name: 'jenkins-core', version: version, ext: 'jar', transitive: true],
            [group: 'javax.servlet', name: 'servlet-api', version: '2.4']
          )
        }

    }
}
