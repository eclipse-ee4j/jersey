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

import javax.inject.Named;

import org.glassfish.jersey.internal.inject.AnnotationLiteral;

/**
 * This is an implementation of the {@link Named} annotation.
 *
 * @author John Wells (john.wells at oracle.com)
 */
public class NamedImpl extends AnnotationLiteral<Named> implements Named {

    /**
     * For serialization
     */
    private static final long serialVersionUID = 9110325112008963155L;

    private final String name;

    /**
     * Creates a {@link Named} annotation with the given name.
     *
     * @param name The non-null name to give the annotation.
     */
    NamedImpl(String name) {
        this.name = name;
    }

    @Override
    public String value() {
        return name;
    }

    @Override
    public String toString() {
        return "@Named(" + name + ")";
    }
}
