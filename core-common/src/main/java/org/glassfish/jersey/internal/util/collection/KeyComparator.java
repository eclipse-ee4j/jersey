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

import java.io.Serializable;

/**
 * A key comparator.
 *
 * @param <K> Key's type
 * @author Paul Sandoz
 */
public interface KeyComparator<K> extends Serializable {

    /**
     * Compare two keys for equality.
     *
     * @param x the first key
     * @param y the second key
     * @return true if the keys are equal.
     */
    boolean equals(K x, K y);

    /**
     * Get the hash code of a key.
     * @param k the key.
     * @return the hash code of the key.
     */
    int hash(K k);
}
