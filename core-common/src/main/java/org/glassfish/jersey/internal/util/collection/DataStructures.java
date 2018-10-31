/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.util.collection;

/**
 * Utility class, which tries to pickup the best collection implementation depending
 * on running environment.
 *
 * @author Gustav Trede
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @since 2.3
 */
public final class DataStructures {

    /**
     * Default concurrency level calculated based on the number of available CPUs.
     */
    public static final int DEFAULT_CONCURENCY_LEVEL = ceilingNextPowerOfTwo(Runtime.getRuntime().availableProcessors());

    private static int ceilingNextPowerOfTwo(final int x) {
        // Hacker's Delight, Chapter 3, Harry S. Warren Jr.
        return 1 << (Integer.SIZE - Integer.numberOfLeadingZeros(x - 1));
    }
}
