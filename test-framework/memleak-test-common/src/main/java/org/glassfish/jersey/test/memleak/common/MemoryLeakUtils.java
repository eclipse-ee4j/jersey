/*
 * Copyright (c) 2015, 2022 Oracle and/or its affiliates. All rights reserved.
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
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.management.MBeanServer;

/**
 * Utility class for memory leak test infrastructure.
 *
 * @author Stepan Vavra
 */
public class MemoryLeakUtils {

    /**
     * The log file where the output (stdout/stderr) of container is located.
     * <p/>
     * For instance, this file is used for detection of {@link OutOfMemoryError} exception records.
     */
    public static final String JERSEY_CONFIG_TEST_CONTAINER_LOGFILE = "jersey.config.test.container.logfile";

    /**
     * The memory leak timeout denotes successful end of the memory leak test. That is, if the memory leak didn't occur during the
     * specified timeout, the test successfully finishes.
     */
    public static final String JERSEY_CONFIG_TEST_MEMLEAK_TIMEOUT = "jersey.config.test.memleak.timeout";

    /**
     * The context root where the deployed application will be accessible.
     */
    public static final String JERSEY_CONFIG_TEST_CONTAINER_CONTEXT_ROOT = "jersey.config.test.container.contextRoot";

    /**
     * The path where to create heap dump files.
     */
    public static final String JERSEY_CONFIG_TEST_MEMLEAK_HEAP_DUMP_PATH = "jersey.config.test.memleak.heapDumpPath";

    private MemoryLeakUtils() {
    }

    private static final Pattern PATTERN = Pattern.compile(".*java\\.lang\\.OutOfMemoryError.*");

    /**
     * Scans the file denoted by {@link #JERSEY_CONFIG_TEST_CONTAINER_LOGFILE} for {@link OutOfMemoryError} records.
     *
     * @throws IOException           In case of I/O error.
     * @throws IllegalStateException In case the {@link OutOfMemoryError} record was found.
     */
    public static void verifyNoOutOfMemoryOccurred() throws IOException {

        final String logFileName = System.getProperty(JERSEY_CONFIG_TEST_CONTAINER_LOGFILE);
        System.out.println("Verifying whether OutOfMemoryError occurred in log file: " + logFileName);

        if (logFileName == null) {
            return;
        }
        final File logFile = new File(logFileName);
        if (!logFile.exists()) {
            return;
        }

        final List<String> lines = Files.lines(logFile.toPath(), Charset.defaultCharset())
                .filter(line -> PATTERN.matcher(line).matches()).collect(Collectors.toList());

        if (lines.size() > 0) {
            throw new IllegalStateException(
                    "OutOfMemoryError detected in '" + logFileName + "': " + Arrays.toString(lines.toArray()));
        }
    }

    /**
     * The name of the HotSpot Diagnostic MXBean
     */
    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";

    /**
     * The class name of HotSpot Diagnostic MXBean
     */
    private static final String HOT_SPOT_DIAGNOSTIC_MXBEAN_CLASSNAME = "com.sun.management.HotSpotDiagnosticMXBean";

    /**
     * Hotspot diagnostic MBean singleton
     */
    private static volatile Object hotSpotDiagnosticMBean;

    private static volatile Method dumpHeapMethod;

    /**
     * Create a heap dump into a given file.
     *
     * @param fileName name of the heap dump file
     * @param live     whether to dump only the live objects
     */
    static void dumpHeap(String fileName, boolean live)
            throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, IOException {
        conditionallyInitHotSpotDiagnosticMXBean();
        try {
            java.nio.file.Files.deleteIfExists(Paths.get(fileName));
        } catch (IOException e) {
            // do nothing and try to go further
        }
        dumpHeapMethod.invoke(hotSpotDiagnosticMBean, fileName, live);
    }

    /**
     * Initialize the HotSpot diagnostic MBean
     */
    private static void conditionallyInitHotSpotDiagnosticMXBean()
            throws IOException, ClassNotFoundException, NoSuchMethodException {
        if (hotSpotDiagnosticMBean == null) {
            synchronized (MemoryLeakUtils.class) {
                if (hotSpotDiagnosticMBean == null) {
                    Class clazz = Class.forName(HOT_SPOT_DIAGNOSTIC_MXBEAN_CLASSNAME);
                    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                    hotSpotDiagnosticMBean = ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, clazz);
                    dumpHeapMethod = clazz.getMethod("dumpHeap", String.class, boolean.class);
                }
            }
        }
    }
}
