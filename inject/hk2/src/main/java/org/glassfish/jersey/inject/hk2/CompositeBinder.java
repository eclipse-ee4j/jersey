/*
 * Copyright (c) 2017, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.hk2;

import org.glassfish.jersey.internal.inject.Binder;

import java.util.Collection;

/**
 * Utility class which is able to install several binders and register them as a whole.
 * <p>
 * Created {@code Binder} is able to recursively register all injection binding descriptions in all installed binders.
 *
 * @author Petr Bouda
 */
public class CompositeBinder {

    /**
     * Creates {@code Binder} with provided binders.
     *
     * @param binders provided binder to install as a collection.
     * @return composite binder.
     */
    public static Binder wrap(Collection<Binder> binders) {
        return org.glassfish.jersey.innate.inject.CompositeBinder.wrap(binders);
    }

    /**
     * Creates {@code Binder} with provided binders.
     *
     * @param binders provided binder to install as an array.
     * @return composite binder.
     */
    public static Binder wrap(Binder... binders) {
        return org.glassfish.jersey.innate.inject.CompositeBinder.wrap(binders);
    }
}
