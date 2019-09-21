/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.test.maven.runner

import org.apache.maven.plugin.Mojo
import org.apache.maven.plugins.annotations.Parameter

/**
 * This trait serves as a field container only as a workaround for failing Traits with more than 10 fields (see
 * http://stackoverflow.com/questions/27259633/using-groovy-trait-in-grails-test-fails)
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
trait SuperRunnerMojo implements Mojo {

    /**
     * The number of lines of stderr/stdout to print when an execution failure occurs.
     */
    @Parameter(defaultValue = "10", name = "lastLinesCount")
    int lastLinesCount

    void setLastLinesCount(final int lastLinesCount) {
        this.lastLinesCount = lastLinesCount
    }
}
