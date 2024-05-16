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

/**
 * Utility class for the virtual thread support.
 */
public final class VirtualThreadSupport {

    /**
     * Do not instantiate.
     */
    private VirtualThreadSupport() {
        throw new IllegalStateException();
    }

    /**
     * Informs whether the given {@link Thread} is virtual.
     * @return true when the current thread is virtual.
     */
    public static boolean isVirtualThread() {
        return false;
    }
}
