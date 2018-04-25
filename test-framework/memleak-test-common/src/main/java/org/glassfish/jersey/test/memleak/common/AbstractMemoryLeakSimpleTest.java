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

package org.glassfish.jersey.test.memleak.common;

import java.io.File;
import java.lang.management.ManagementFactory;

import org.junit.Before;

/**
 * An abstract test class that adds support for dump heap at the beginning of the test execution.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class AbstractMemoryLeakSimpleTest {

    @Before
    public void dumpTheHeap() throws Exception {
        final String heapDumpPath = System.getProperty(MemoryLeakUtils.JERSEY_CONFIG_TEST_MEMLEAK_HEAP_DUMP_PATH);
        String heapDumpFile = "java_start_pid" + guessPid() + ".hprof";
        if (heapDumpPath != null) {
            heapDumpFile = heapDumpPath + File.separator + heapDumpFile;
        }
        MemoryLeakUtils.dumpHeap(heapDumpFile, true);
    }

    private String guessPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();

        if (name == null) {
            return "";
        }
        if (name.contains("@")) {
            name = name.substring(0, name.indexOf("@"));
        }
        return name.trim();
    }

}
