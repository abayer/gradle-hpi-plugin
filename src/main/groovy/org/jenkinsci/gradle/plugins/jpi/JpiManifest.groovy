package org.jenkinsci.gradle.plugins.jpi

import java.text.SimpleDateFormat
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

/**
 * Encapsulates the Jenkins plugin manifest and its generation.
 *
 * @author Kohsuke Kawaguchi
 */
class JpiManifest extends HashMap<String,Object> {
    private final Project project;

    JpiManifest(Project project) {
        this.project = project

        def conv = project.extensions.getByType(JpiExtension)
        def classDir = project.convention.getPlugin(JavaPluginConvention).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).output.classesDir;

        File pluginImpl = new File(classDir, "META-INF/services/hudson.Plugin");
        if (pluginImpl.exists()) {
            this["Plugin-Class"] = pluginImpl.readLines("UTF-8")[0]
        }

        this["Group-Id"] = project.group;
        this["Short-Name"] = conv.shortName;
        this["Long-Name"] = conv.displayName;
        this["Url"] = conv.url;
        this["Compatible-Since-Version"] = conv.compatibleSinceVersion;
        if (conv.sandboxStatus)
            this["Sandbox-Status"] = conv.sandboxStatus;

        v = project.version
        if (v==Project.DEFAULT_VERSION)     v = "1.0-SNAPSHOT";
        if(v.toString().endsWith("-SNAPSHOT")) {
            String dt = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date());
            v += " (private-"+dt+"-"+System.getProperty("user.name")+")";
        }
        this["Plugin-Version"] = v;

        this["Jenkins-Version"] = conv.coreVersion;

        this["Mask-Classes"] = conv.maskClasses;

        // TODO
        // String dep = findDependencyProjects();
        // if(dep.length()>0)
        //    attrs["Plugin-Dependencies"] = dep;

        // more TODO
/*
        if(pluginFirstClassLoader)
            mainSection.addAttributeAndCheck( new Attribute( "PluginFirstClassLoader", "true" ) );

        if (project.getDevelopers() != null) {
            mainSection.addAttributeAndCheck(new Attribute("Plugin-Developers",getDevelopersForManifest()));
        }

        Boolean b = isSupportDynamicLoading();
        if (b!=null)
            mainSection.addAttributeAndCheck(new Attribute("Support-Dynamic-Loading",b.toString()));
*/
        // remove null values
        for (Iterator itr = this.entrySet().iterator(); itr.hasNext();) {
            if (itr.next().value==null) itr.remove();
        }
    }

    public void writeTo(File f) {
        def m = new java.util.jar.Manifest()
        m.mainAttributes.putValue("Manifest-Version","1.0")
        this.each {k,v -> m.mainAttributes.putValue(k,v.toString())}
        f.withOutputStream { o -> m.write(o); }
    }
}
