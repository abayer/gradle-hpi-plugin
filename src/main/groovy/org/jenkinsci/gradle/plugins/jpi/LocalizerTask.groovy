package org.jenkinsci.gradle.plugins.jpi

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.OutputDirectory

import org.jvnet.localizer.GeneratorTask

class LocalizerTask extends DefaultTask {
    public static final String TASK_NAME = "localizer"

    @OutputDirectory
    File destinationDir

    @TaskAction
    def generateLocalized() {
        def p = project
        
        def isolatedAnt = services.get(org.gradle.api.internal.project.IsolatedAntBuilder)
        isolatedAnt.execute {
            mkdir(dir: destinationDir.canonicalPath)
            taskdef(name: "generator", classname: "org.jvnet.localizer.GeneratorTask") {
                classpath {
                    pathelement(path: p.buildscript.configurations.classpath.asPath)
                }
            }
            p.sourceSets.main.getResources().getSrcDirs().each { rsrcDir ->
                generator(todir: destinationDir.canonicalPath, dir: rsrcDir)
            }
        }
    }
}
