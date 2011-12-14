package org.jenkinsci.gradle.plugins.hpi

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.util.jar.JarFile

/**
 * Task that starts Jenkins in place with the current plugin.
 *
 * @author Kohsuke Kawaguchi
 */
class ServerTask extends DefaultTask {
    
    @TaskAction
    def start() {
        def c = project.configurations.getByName(HpiPlugin.WAR_DEPENDENCY_CONFIGURATION_NAME)
        def files = c.resolve();
        if (files.isEmpty())
            throw new Error("No jenkins.war dependency is specified");
        File war = files.toArray()[0]

        def conv = project.extensions.getByType(HpiExtension)
        System.setProperty("JENKINS_HOME",conv.workDir.getAbsolutePath())
        System.setProperty("stapler.trace","true")
        System.setProperty("debug.YUI","true")

        def cl = new URLClassLoader([war.toURI().toURL()] as URL[])
        def mainClass = new JarFile(war).getManifest().mainAttributes.getValue("Main-Class")
        //cl.loadClass(mainClass).getMethod("main",String[].class).invoke(null,new Object[0]);
        cl.loadClass(mainClass).main();
        
        // make the thread hang
        synchronized (this) { wait(); }
    }

    public static final String TASK_NAME = "server";
}
