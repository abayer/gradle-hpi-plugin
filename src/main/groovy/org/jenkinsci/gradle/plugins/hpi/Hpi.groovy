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

    @Override
    protected void copy() {
        manifest.attributes(new HpiManifest(project))
        super.copy()
    }

    public static final String TASK_NAME = "hpi";
}
