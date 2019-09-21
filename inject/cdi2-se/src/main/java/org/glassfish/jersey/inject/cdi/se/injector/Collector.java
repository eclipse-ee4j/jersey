/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.cdi.se.injector;

import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * This class collects errors, and can then also produce a MultiException from those errors if necessary.
 *
 * @author John Wells (john.wells at oracle.com)
 */
public class Collector {

    private LinkedHashSet<Throwable> throwables;

    /**
     * Merges {@link MultiException} with all {@code throwables} registered in it.
     *
     * @param me {@code MultiException} to merge.
     */
    public void addMultiException(MultiException me) {
        if (me == null) {
            return;
        }
        if (throwables == null) {
            throwables = new LinkedHashSet<>();
        }

        throwables.addAll(me.getErrors());
    }

    /**
     * Adds a throwable to the list of throwables in this collector.
     *
     * @param th The throwable to add to the list.
     */
    public void addThrowable(Throwable th) {
        if (th == null) {
            return;
        }
        if (throwables == null) {
            throwables = new LinkedHashSet<>();
        }

        if (th instanceof MultiException) {
            throwables.addAll(((MultiException) th).getErrors());
        } else {
            throwables.add(th);
        }
    }

    /**
     * This method will throw if the list of throwables associated with this collector is not empty.
     *
     * @throws MultiException An exception with all the throwables found in this collector.
     */
    public void throwIfErrors() throws MultiException {
        if (throwables == null || throwables.isEmpty()) {
            return;
        }

        throw new MultiException(new LinkedList<>(throwables));
    }

    /**
     * Returns true if this collector has errors.
     *
     * @return true if the collector has errors.
     */
    public boolean hasErrors() {
        return ((throwables != null) && (!throwables.isEmpty()));
    }
}
