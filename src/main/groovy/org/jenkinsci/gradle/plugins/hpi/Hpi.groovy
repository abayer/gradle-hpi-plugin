/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jenkinsci.gradle.plugins.hpi

import java.text.SimpleDateFormat
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.War

/**
 * Assembles an hpi archive.
 *
 * @author Kohsuke Kawaguchi
 */
class Hpi extends War {
    public static final String HPI_EXTENSION = 'hpi'

    Hpi() {
        extension = HPI_EXTENSION
    }

    /**
     * Configures the manifest generation.
     */
    protected void configureManifest() {
        def conv = project.convention.getPlugin(HpiPluginConvention)
        def classDir = project.convention.getPlugin(JavaPluginConvention).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).output.classesDir;

        def attrs = [:]

        File pluginImpl = new File(classDir, "META-INF/services/hudson.Plugin");
        if (pluginImpl.exists()) {
            attrs["Plugin-Class"] = pluginImpl.readLines("UTF-8")[0]
        }

        attrs["Group-Id"] = project.group;
        attrs["Short-Name"] = conv.shortName;
        attrs["Long-Name"] = conv.displayName;
        attrs["Url"] = conv.url;
        attrs["Compatible-Since-Version"] = conv.compatibleSinceVersion;
        if (conv.sandboxStatus)
            attrs["Sandbox-Status"] = conv.sandboxStatus;

        v = project.version
        if (v==Project.DEFAULT_VERSION)     v = "1.0-SNAPSHOT";
        if(v.toString().endsWith("-SNAPSHOT")) {
            String dt = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date());
            v += " (private-"+dt+"-"+System.getProperty("user.name")+")";
        }
        attrs["Plugin-Version"] = v;

        attrs["Jenkins-Version"] = conv.coreVersion;

        attrs["Mask-Classes"] = conv.maskClasses;

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
        for (Iterator itr = attrs.entrySet().iterator(); itr.hasNext();) {
            if (itr.next().value==null) itr.remove();
        }

        manifest.attributes(attrs)
    }
}
