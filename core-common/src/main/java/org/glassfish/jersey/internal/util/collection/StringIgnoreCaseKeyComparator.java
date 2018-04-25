/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * Case insensitive String key comparator.
 *
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class StringIgnoreCaseKeyComparator implements KeyComparator<String> {

    private static final long serialVersionUID = 9106900325469360723L;

    public static final StringIgnoreCaseKeyComparator SINGLETON = new StringIgnoreCaseKeyComparator();

    @Override
    public int hash(String k) {
        return k.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(String x, String y) {
        return x.equalsIgnoreCase(y);
    }

}
