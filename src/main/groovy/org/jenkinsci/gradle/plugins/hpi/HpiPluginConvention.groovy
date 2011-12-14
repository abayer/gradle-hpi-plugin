/*
 * Copyright 2009 the original author or authors.
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

import org.gradle.api.Project

public class HpiPluginConvention {
    final Project project

    def HpiPluginConvention(Project project) {
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
                delegate.url("http://maven.glassfish.org/content/groups/public/")
            }
        }

        project.dependencies {
            jenkinsCore(
                [group: 'org.jenkins-ci.main', name: 'jenkins-core', version: v, ext: 'jar', transitive: true],
                [group: 'javax.servlet', name: 'servlet-api', version: '2.4']
            )
        }
    }


}