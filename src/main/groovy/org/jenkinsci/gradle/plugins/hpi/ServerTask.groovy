package org.jenkinsci.gradle.plugins.hpi

import java.util.jar.JarFile
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.WarPluginConvention
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
        def files = c.resolve();
        if (files.isEmpty())
            throw new Error("No jenkins.war dependency is specified");
        File war = files.toArray()[0]

        generateHpl();

        def conv = project.extensions.getByType(HpiExtension)
        System.setProperty("JENKINS_HOME",conv.workDir.getAbsolutePath())
        System.setProperty("stapler.trace","true")
        System.setProperty("debug.YUI","true")

        def cl = new URLClassLoader([war.toURI().toURL()] as URL[])
        def mainClass = new JarFile(war).getManifest().mainAttributes.getValue("Main-Class")
        cl.loadClass(mainClass).main();
        
        // make the thread hang
        synchronized (this) { wait(); }
    }

    void generateHpl() {
        def conv = project.extensions.getByType(HpiExtension)

        def m = new HpiManifest(project)

        // src/main/webApp
        def warconv = project.convention.getPlugin(WarPluginConvention);
        m["Resource-Path"] = warconv.webAppDir.getAbsolutePath()

        // add resource directories directly so that we can pick up the source, then add all the jars and class path
        m["Libraries"] = (conv.mainSourceTree().resources.srcDirs + conv.runtimeClasspath.getFiles()).join(",")

        def hpl = new File(conv.workDir, "plugins/${conv.shortName}.hpl")
        hpl.parentFile.mkdirs()
        m.writeTo(hpl)
    }

    public static final String TASK_NAME = "server";
}
