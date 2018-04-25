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

package org.glassfish.jersey.linking;

import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

/**
 * An ELContext that encapsulates the response information for use by the
 * expression evaluator.
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
class LinkELContext extends ELContext {

    private Object entity;
    private Object resource;
    private Object instance;

    /**
     * Convenience constructor for the common case where a context where
     * the entity and instance are the same. Equivalent to
     * {@link #LinkELContext(Object, Object, Object)}.
     *
     * @param entity
     * @param resource
     */
    LinkELContext(Object entity, Object resource) {
        this.entity = entity;
        this.resource = resource;
        this.instance = entity;
    }

    /**
     * Construct a new context
     * @param entity the entity returned from the resource method
     * @param resource the resource class instance that returned the entity
     * @param instance the instance that contains the entity, e.g. the value of
     * a field within an entity class.
     */
    LinkELContext(Object entity, Object resource, Object instance) {
        this.entity = entity;
        this.resource = resource;
        this.instance = instance;
    }

    @Override
    public ELResolver getELResolver() {
        CompositeELResolver resolver = new CompositeELResolver();
        resolver.add(new ResponseContextResolver(entity, resource, instance));
        resolver.add(new BeanELResolver(true));
        return resolver;
    }

    @Override
    public FunctionMapper getFunctionMapper() {
        return null;
    }

    @Override
    public VariableMapper getVariableMapper() {
        return null;
    }

}
