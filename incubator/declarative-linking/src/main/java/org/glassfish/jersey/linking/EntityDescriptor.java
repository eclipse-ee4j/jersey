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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Link;

/**
 * Describes an entity in terms of its fields, bean properties and {@link InjectLink}
 * annotated fields.
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
class EntityDescriptor {

    // Maintains an internal static cache to optimize processing
    private static final Map<Class<?>, EntityDescriptor> descriptors = new HashMap<>();

    static synchronized EntityDescriptor getInstance(Class<?> entityClass) {
        if (descriptors.containsKey(entityClass)) {
            return descriptors.get(entityClass);
        } else {
            EntityDescriptor descriptor = new EntityDescriptor(entityClass);
            descriptors.put(entityClass, descriptor);
            return descriptor;
        }
    }

    // instance

    private Map<String, FieldDescriptor> nonLinkFields;
    private Map<String, FieldDescriptor> linkFields;
    private List<LinkHeaderDescriptor> linkHeaders;

    /**
     * Construct an new descriptor by inspecting the supplied class.
     *
     * @param entityClass
     */
    private EntityDescriptor(Class<?> entityClass) {
        // create a list of link headers
        this.linkHeaders = new ArrayList<>();
        findLinkHeaders(entityClass);
        this.linkHeaders = Collections.unmodifiableList(linkHeaders);

        // create a list of field names
        this.nonLinkFields = new HashMap<>();
        this.linkFields = new HashMap<>();
        findFields(entityClass);
        this.nonLinkFields = Collections.unmodifiableMap(this.nonLinkFields);
        this.linkFields = Collections.unmodifiableMap(this.linkFields);
    }

    Collection<FieldDescriptor> getLinkFields() {
        return linkFields.values();
    }

    Collection<FieldDescriptor> getNonLinkFields() {
        return nonLinkFields.values();
    }

    List<LinkHeaderDescriptor> getLinkHeaders() {
        return linkHeaders;
    }

    /**
     * Find and cache the fields of the supplied class and its superclasses and
     * interfaces.
     *
     * @param entityClass the class
     */
    private void findFields(Class<?> entityClass) {
        for (Field f : entityClass.getDeclaredFields()) {
            InjectLink a = f.getAnnotation(InjectLink.class);
            Class<?> t = f.getType();
            if (a != null) {
                if (t.equals(String.class) || t.equals(URI.class) || Link.class.isAssignableFrom(t)) {
                    if (!linkFields.containsKey(f.getName())) {
                        linkFields.put(f.getName(), new InjectLinkFieldDescriptor(f, a, t));
                    }
                } else {
                    // TODO unsupported type
                }
            } else if (f.isAnnotationPresent(InjectLinks.class)) {

                if (List.class.isAssignableFrom(t)
                        || t.isArray() && Link.class.isAssignableFrom(t.getComponentType())) {

                    InjectLinks a2 = f.getAnnotation(InjectLinks.class);
                    linkFields.put(f.getName(), new InjectLinksFieldDescriptor(f, a2, t));
                } else {
                    throw new IllegalArgumentException("Can only inject links onto a List<Link> or Link[] object");
                }

            } else {
                // see issue http://java.net/jira/browse/JERSEY-625
                if ((f.getModifiers() & Modifier.STATIC) > 0
                        || f.getName().startsWith("java.")
                        || f.getName().startsWith("javax.")) {
                    continue;
                }
                nonLinkFields.put(f.getName(), new FieldDescriptor(f));
            }
        }

        // look for nonLinkFields in superclasses
        Class<?> sc = entityClass.getSuperclass();
        if (sc != null && sc != Object.class) {
            findFields(sc);
        }

        // look for nonLinkFields in interfaces
        for (Class<?> ic : entityClass.getInterfaces()) {
            findFields(ic);
        }
    }

    private void findLinkHeaders(Class<?> entityClass) {
        InjectLink linkHeaderAnnotation = entityClass.getAnnotation(InjectLink.class);
        if (linkHeaderAnnotation != null) {
            linkHeaders.add(new LinkHeaderDescriptor(linkHeaderAnnotation));
        }
        InjectLinks linkHeadersAnnotation = entityClass.getAnnotation(InjectLinks.class);
        if (linkHeadersAnnotation != null) {
            for (InjectLink linkHeader : linkHeadersAnnotation.value()) {
                linkHeaders.add(new LinkHeaderDescriptor(linkHeader));
            }
        }

        // look in superclasses
        Class<?> sc = entityClass.getSuperclass();
        if (sc != null && sc != Object.class) {
            findLinkHeaders(sc);
        }

        // look in interfaces
        for (Class<?> ic : entityClass.getInterfaces()) {
            findLinkHeaders(ic);
        }
    }
}
