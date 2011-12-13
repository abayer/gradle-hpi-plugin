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

import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile;
import org.gradle.util.ConfigureUtil
import org.gradle.api.internal.file.copy.CopySpecImpl
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.GUtil;
import org.gradle.api.plugins.BasePluginConvention

/**
 * Assembles an hpi archive.
 *
 * @author Hans Dockter
 * @author Andrew Bayer
 */

class Hpi extends Jar {
    public static final String HPI_EXTENSION = 'hpi'

    private File webXml

    private FileCollection classpath
    private final CopySpecImpl webInf

    Hpi() {
        extension = HPI_EXTENSION
        // Add these as separate specs, so they are not affected by the changes to the main spec
        webInf = copyAction.rootSpec.addChild().into('WEB-INF')
        webInf.into('classes') {
            from {
                def classpath = getClasspath()
                classpath ? classpath.filter {File file -> file.isDirectory()} : []
            }
        }
        webInf.into('lib') {
            from {
                def classpath = getClasspath()
                classpath ? classpath.filter {File file -> file.isFile()} : []
            }
        }
        webInf.into('') {
            from {
                getWebXml()
            }
            rename {
                'web.xml'
            }
        }
    }

    CopySpec getWebInf() {
        return webInf.addChild()
    }

  /**
     * The path where the archive is constructed. The path is simply the {@code destinationDir} plus the {@code archiveName}.
     *
     * @return a File object with the path to the archive
     */
    @OutputFile
    public File getArchivePath() {
      return new File(getDestinationDir(), GUtil.elvis(project.archivesBaseName, "") + "." + extension)
    }

    /**
     * Adds some content to the {@code WEB-INF} directory for this hpi archive.
     *
     * <p>The given closure is executed to configure a {@link CopySpec}. The {@code CopySpec} is passed to the closure
     * as its delegate.
     *
     * @param configureClosure The closure to execute
     * @return The newly created {@code CopySpec}.
     */
    CopySpec webInf(Closure configureClosure) {
        return ConfigureUtil.configure(configureClosure, getWebInf())
    }

    /**
     * Returns the classpath to include in the hpi archive. Any JAR or ZIP files in this classpath are included in the
     * {@code WEB-INF/lib} directory. Any directories in this classpath are included in the {@code WEB-INF/classes}
     * directory.
     *
     * @return The classpath. Returns an empty collection when there is no classpath to include in the hpi.
     */
    @InputFiles @Optional
    FileCollection getClasspath() {
        return classpath
    }

    /**
     * Sets the classpath to include in the hpi archive.
     *
     * @param classpath The classpath. Must not be null.
     */
    void setClasspath(Object classpath) {
        this.classpath = project.files(classpath)
    }

    /**
     * Adds files to the classpath to include in the hpi archive.
     *
     * @param classpath The files to add. These are evaluated as for {@link org.gradle.api.Project#files(Object [])}
     */
    void classpath(Object... classpath) {
        FileCollection oldClasspath = getClasspath()
        this.classpath = project.files(oldClasspath ?: [], classpath)
    }

    /**
     * Returns the {@code web.xml} file to include in the hpi archive. When {@code null}, no {@code web.xml} file is
     * included in the hpi.
     *
     * @return The {@code web.xml} file.
     */
    @InputFile @Optional
    public File getWebXml() {
        return webXml;
    }

    /**
     * Sets the {@code web.xml} file to include in the hpi archive. When {@code null}, no {@code web.xml} file is
     * included in the hpi.
     *
     * @param webXml The {@code web.xml} file. Maybe null.
     */
    public void setWebXml(File webXml) {
        this.webXml = webXml;
    }
}
