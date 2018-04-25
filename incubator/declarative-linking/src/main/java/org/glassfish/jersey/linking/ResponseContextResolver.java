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

import java.beans.FeatureDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotWritableException;

/**
 * The initial context resolver that resolves the entity and resource
 * objects used at the start of an EL expression.
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
class ResponseContextResolver extends ELResolver {

    private Map<String, Object> responseObjects;
    private static final String ENTITY_OBJECT = "entity";
    private static final String RESOURCE_OBJECT = "resource";
    static final String INSTANCE_OBJECT = "instance";

    ResponseContextResolver(Object entity, Object resource, Object instance) {
        responseObjects = new HashMap<>();
        responseObjects.put(ENTITY_OBJECT, entity);
        responseObjects.put(RESOURCE_OBJECT, resource);
        responseObjects.put(INSTANCE_OBJECT, instance);
    }

    private boolean isHandled(ELContext elc, Object base, Object property) {
        if (base != null) {
            return false;
        }
        if (responseObjects.containsKey(property.toString())) {
            elc.setPropertyResolved(true);
            return true;
        }
        return false;
    }

    @Override
    public Object getValue(ELContext elc, Object base, Object property) {
        if (isHandled(elc, base, property)) {
            return responseObjects.get(property.toString());
        }
        return null;
    }

    @Override
    public Class<?> getType(ELContext elc, Object o, Object o1) {
        if (isHandled(elc, o, o1)) {
            return getValue(elc, o, o1).getClass();
        }
        return null;
    }

    @Override
    public void setValue(ELContext elc, Object o, Object o1, Object o2) {
        throw new PropertyNotWritableException(o2.toString());
    }

    @Override
    public boolean isReadOnly(ELContext elc, Object o, Object o1) {
        if (isHandled(elc, o, o1)) {
            return true;
        }
        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext elc, Object o) {
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext elc, Object o) {
        return Object.class;
    }
}
