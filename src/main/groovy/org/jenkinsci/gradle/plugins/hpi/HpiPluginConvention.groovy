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
  /**
   * The name of the destination directory for generated Stapler stubs, relative to the project build directory.
   */
  String staplerStubDir
  final Project project
  
  def HpiPluginConvention(Project project) {
    this.project = project
    staplerStubDir = 'generated-src/stubs'
  }

  /**
   * Returns the Stapler stubs directory.
   */
  File getStaplerStubDir() {
    project.file("${project.buildDir}/${staplerStubDir}")
  }
  
}