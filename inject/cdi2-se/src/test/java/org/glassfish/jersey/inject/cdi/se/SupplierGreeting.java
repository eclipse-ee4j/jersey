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

package org.glassfish.jersey.inject.cdi.se;

import java.util.function.Supplier;

import javax.enterprise.inject.Vetoed;

/**
 * @author Petr Bouda
 */
@Vetoed
public class SupplierGreeting implements Supplier<Greeting> {

    private final String greetingType;

    /**
     * Default constructor.
     */
    public SupplierGreeting() {
        this(CzechGreeting.GREETING);
    }

    /**
     * Supplier's constructor.
     *
     * @param greetingType greetingType in a specific language.
     */
    public SupplierGreeting(String greetingType) {
        this.greetingType = greetingType;
    }

    @Override
    public Greeting get() {
        if (CzechGreeting.GREETING.equals(greetingType)) {
            return new CzechGreeting();
        } else {
            return new EnglishGreeting();
        }
    }
}
