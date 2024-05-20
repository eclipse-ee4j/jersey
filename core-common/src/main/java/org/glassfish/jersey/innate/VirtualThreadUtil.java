/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.innate;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.innate.virtual.LoomishExecutors;

import javax.ws.rs.core.Configuration;
import java.util.concurrent.ThreadFactory;

/**
 * Factory class to provide JDK specific implementation of bits related to the virtual thread support.
 */
public final class VirtualThreadUtil {
    /**
     * Do not instantiate.
     */
    private VirtualThreadUtil() {
        throw new IllegalStateException();
    }

    /**
     * Return an instance of {@link LoomishExecutors} based on a configuration property.
     * @param config the {@link Configuration}
     * @return the {@link LoomishExecutors} instance.
     */
    public static LoomishExecutors withConfig(Configuration config) {
        boolean bUseVirtualThreads = false;
        ThreadFactory tfThreadFactory = null;

        if (config != null) {
            Object useVirtualThread = config.getProperty(CommonProperties.USE_VIRTUAL_THREADS);
            if (useVirtualThread != null && Boolean.class.isInstance(useVirtualThread)) {
                bUseVirtualThreads = (boolean) useVirtualThread;
            }
            if (useVirtualThread != null && String.class.isInstance(useVirtualThread)) {
                bUseVirtualThreads = Boolean.parseBoolean(useVirtualThread.toString());
            }

            Object threadFactory = config.getProperty(CommonProperties.THREAD_FACTORY);
            if (threadFactory != null && ThreadFactory.class.isInstance(threadFactory)) {
                tfThreadFactory = (ThreadFactory) threadFactory;
            }

        }
        return tfThreadFactory == null
                ? VirtualThreadSupport.allowVirtual(bUseVirtualThreads)
                : VirtualThreadSupport.allowVirtual(bUseVirtualThreads, tfThreadFactory);
    }
}
