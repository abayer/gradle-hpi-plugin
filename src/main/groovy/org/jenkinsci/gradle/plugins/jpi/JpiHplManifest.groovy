package org.jenkinsci.gradle.plugins.jpi;

import org.gradle.api.Project;
import org.gradle.api.plugins.WarPluginConvention;

/**
 * @author Kohsuke Kawaguchi
 */
class JpiHplManifest extends JpiManifest {
    JpiHplManifest(Project project) {
        super(project);

        def conv = project.extensions.getByType(JpiExtension)

        // src/main/webApp
        def warconv = project.convention.getPlugin(WarPluginConvention)
        this["Resource-Path"] = warconv.webAppDir.absolutePath

        // add resource directories directly so that we can pick up the source, then add all the jars and class path
        this["Libraries"] = (conv.mainSourceTree().resources.srcDirs + conv.runtimeClasspath.files).join(",")
    }
}
