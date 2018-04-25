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

package org.glassfish.jersey.model;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Model component that can be name bound.
 *
 * A component implementing this interface provides additional information about
 * the name bindings attached to it.
 *
 * @author Martin Matula
 * @see javax.ws.rs.NameBinding
 */
public interface NameBound {
    /**
     * Check if the component is bound or not.
     *
     * @return {@code true} if the component is bound, {@code false} otherwise.
     */
    public boolean isNameBound();

    /**
     * Get the collection of name bindings attached to this component.
     *
     * @return collection of name binding annotation types.
     */
    Collection<Class<? extends Annotation>> getNameBindings();
}
