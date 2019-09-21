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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Link;

/**
 * Utility class for working with {@link InjectLinks} annotated fields.
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
class InjectLinksFieldDescriptor extends FieldDescriptor {

    private final InjectLinks link;
    private final Class<?> type;

    /**
     * C'tor
     *
     * @param f the field to inject
     * @param l the InjectLinks annotation
     * @param t the class that contains field f
     */
    InjectLinksFieldDescriptor(Field f, InjectLinks l, Class<?> t) {
        super(f);
        link = l;
        type = t;
    }

    /**
     * Injects the Link list into the instance.
     *
     * If the field is {@code null} then it is replaced with the list.
     * If the field already contains links, then the content is merged
     * with this list into a new list and injected.
     *
     * @param instance the instance that contains the field f
     * @param list the list of links to inject
     */
    public void setPropertyValue(Object instance, List<Link> list) {
        setAccessibleField(field);
        try {
            List<Link> merged = mergeWithExistingField(instance, list);

            Object value;
            if (Objects.equals(List.class, type)) {
                value = merged;
            } else if (type.isArray()) {
                value = merged.toArray((Object[]) Array.newInstance(type.getComponentType(), merged.size()));
            } else {
                throw new IllegalArgumentException("Field type " + type + " not one of supported List<Link> or Link[]");
            }

            field.set(instance, value);


        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(InjectLinksFieldDescriptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List<Link> mergeWithExistingField(Object instance, List<Link> list) throws IllegalAccessException {
        Object existing = field.get(instance);
        if (existing != null) {
            if (Collection.class.isAssignableFrom(existing.getClass()) && !((Collection) existing).isEmpty()) {
                List<Link> merged  = new ArrayList<>(list);
                merged.addAll((Collection<Link>) existing);
                return merged;
            } else if (existing.getClass().isArray() && existing.getClass().isAssignableFrom(Link[].class)) {
                List<Link> merged = new ArrayList<>(list);
                merged.addAll(Arrays.asList((Link[]) existing));
                return merged;
            }
        }
        return list;
    }

    /**
     * Creates {@link InjectLinkFieldDescriptor} for each link to inject.
     */
    InjectLinkFieldDescriptor[] getLinksToInject() {
        final InjectLink[] listOfLinks = link.value();
        InjectLinkFieldDescriptor[] fields = new InjectLinkFieldDescriptor[listOfLinks.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new InjectLinkFieldDescriptor(field, listOfLinks[i], Link.class);
        }
        return fields;
    }
}
