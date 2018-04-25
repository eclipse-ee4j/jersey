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

import org.junit.Test;

/**
 * @author Paul Sandoz
 */
public class KeyComparatorLinkedHashMapTest extends AbstractKeyComparatorHashMapTest {

    @Test
    public void testNull() {
        final KeyComparatorHashMap<String, String> k = new KeyComparatorLinkedHashMap<String, String>(
                new KeyComparator<String>() {
                    @Override
                    public boolean equals(String s1, String s2) {
                        return s1.equals(s2);
                    }

                    @Override
                    public int hash(String s1) {
                        return s1.hashCode();
                    }
                });

        _test(k);
    }
}
