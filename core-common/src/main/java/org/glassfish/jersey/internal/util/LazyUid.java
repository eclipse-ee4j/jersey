/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.util;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lazily initialized, thread-safe, random UUID. Useful for identifying instances
 * for logging & debugging purposes.
 * <p />
 * The UUID value gets initialized with the first call to {@link #value()} method.
 * Once initialized, the UUID value stays the same.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class LazyUid implements Serializable {
    private static final long serialVersionUID = 4618609413877136867L;

    private final AtomicReference<String> uid = new AtomicReference<String>();

    /**
     * Return UUID value. The returned value is never {@code null}.
     *
     * @return UUID value.
     */
    public String value() {
        if (uid.get() == null) {
            uid.compareAndSet(null, UUID.randomUUID().toString());
        }

        return uid.get();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        final LazyUid other = (LazyUid) that;
        return this.value().equals(other.value());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.value().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return value();
    }
}
