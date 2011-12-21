# Gradle JPI plugin

This is a Gradle plugin for building [Jenkins](http://jenkins-ci.org)
plugins, written in Groovy or Java.

## Configuration

Add the following to your build.gradle:

>        buildscript {
>            mavenCentral()
>            dependencies {
>                classpath 'org.jenkins-ci.tools:gradle-jpi-plugin:0.1'
>            }
>        }
>        apply plugin: 'jpi'
>        // Whatever other plugins you need to load.
>
>        groupId = "org.jenkins-ci.plugins"
>        version = "0.0.1-SNAPSHOT"    // Or whatever your version is.
>        description = "A description of your plugin"
>
>        jenkinsPlugin {
>            coreVersion = '1.409'                                               // Version of Jenkins core this plugin depends on.
>            displayName = 'Hello World plugin built with Gradle'                // Human-readable name of plugin.
>            url = 'http://wiki.jenkins-ci.org/display/JENKINS/SomePluginPage'   // URL for plugin on Jenkins wiki or elsewhere.
>        }

Be sure to add the jenkinsPlugin { ... } section before any additional
repositories are defined in your build.gradle.

## Usage

* 'gradle jpi' - Build the Jenkins plugin file, which can then be
  found in the build directory. The file will currently end in ".hpi".
* 'gradle install' - Build the Jenkins plugin and install it into your
  local Maven repository.
* 'gradle uploadArchives' (or 'gradle deploy') - Deploy your plugin to
  the Jenkins Maven repository to be included in the Update Center.
