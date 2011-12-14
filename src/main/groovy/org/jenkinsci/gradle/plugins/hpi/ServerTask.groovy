package org.jenkinsci.gradle.plugins.hpi

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Task that starts Jenkins in place with the current plugin.
 *
 * @author Kohsuke Kawaguchi
 */
class ServerTask extends DefaultTask {
    
    @TaskAction
    def start() {
        def c = project.configurations.getByName(HpiPlugin.WAR_DEPENDENCY_CONFIGURATION_NAME)
        System.out.println(c.resolve())
    }

    public static final String TASK_NAME = "server";
}
