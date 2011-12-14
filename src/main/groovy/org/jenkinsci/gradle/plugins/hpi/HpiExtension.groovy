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


}
