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

package org.glassfish.jersey.server.model;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Abstraction for a resource handler class constructor.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class HandlerConstructor implements Parameterized, ResourceModelComponent {

    private final Constructor<?> constructor;
    private final List<Parameter> parameters;

    /**
     * Creates a new instance of ResourceConstructor.
     *
     * @param constructor underlying Java constructor.
     * @param parameters constructor parameters.
     */
    HandlerConstructor(Constructor<?> constructor, List<Parameter> parameters) {
        this.constructor = constructor;
        this.parameters = parameters;
    }

    /**
     * Get the underlying java constructor.
     *
     * @return underlying java constructor.
     */
    public Constructor<?> getConstructor() {
        return constructor;
    }

    @Override
    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public boolean requiresEntity() {
        for (Parameter p : getParameters()) {
            if (Parameter.Source.ENTITY == p.getSource()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void accept(ResourceModelVisitor visitor) {
        visitor.visitResourceHandlerConstructor(this);
    }

    @Override
    public List<ResourceModelComponent> getComponents() {
        return null;
    }
}
