/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model.internal;

import javax.ws.rs.Path;

/**
 * Common model helper methods.
 *
 * @author Michal Gajdos
 * @author Constantino Cronemberger (ccronemberger at yahoo.com.br)
 */
public final class ModelHelper {

    /**
     * Get the class in the provided resource class ancestor hierarchy that
     * is actually annotated with the {@link javax.ws.rs.Path &#64;Path} annotation.
     *
     * @param resourceClass resource class.
     * @return resource class or it's ancestor that is annotated with the {@link javax.ws.rs.Path &#64;Path}
     *         annotation.
     */
    public static Class<?> getAnnotatedResourceClass(final Class<?> resourceClass) {

        Class<?> foundInterface = null;

        // traverse the class hierarchy to find the annotation
        // According to specification, annotation in the super-classes must take precedence over annotation in the
        // implemented interfaces
        Class<?> cls = resourceClass;
        do {
            if (cls.isAnnotationPresent(Path.class)) {
                return cls;
            }

            // if no annotation found on the class currently traversed, check for annotation in the interfaces on this
            // level - if not already previously found
            if (foundInterface == null) {
                for (final Class<?> i : cls.getInterfaces()) {
                    if (i.isAnnotationPresent(Path.class)) {
                        // store the interface reference in case no annotation will be found in the super-classes
                        foundInterface = i;
                        break;
                    }
                }
            }
        } while ((cls = cls.getSuperclass()) != null);

        if (foundInterface != null) {
            return foundInterface;
        }

        return resourceClass;
    }

    /**
     * Prevent instantiation.
     */
    private ModelHelper() {
        throw new AssertionError("Instantiation not allowed.");
    }
}
