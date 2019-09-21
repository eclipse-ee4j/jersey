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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for working with class fields
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
class FieldDescriptor {

    protected Field field;

    FieldDescriptor(Field f) {
        this.field = f;
    }

    Object getFieldValue(Object instance) {
        setAccessibleField(field);
        Object value = null;
        try {
            value = field.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(FieldDescriptor.class.getName()).log(Level.FINE, null, ex);
        }
        return value;
    }

    public String getFieldName() {
        return field.getName();
    }

    static void setAccessibleField(final Field f) {
        if (Modifier.isPublic(f.getModifiers())) {
            return;
        }

        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                return f;
            }
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldDescriptor other = (FieldDescriptor) obj;
        if (this.field != other.field && (this.field == null || !this.field.equals(other.field))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.field != null ? this.field.hashCode() : 0);
        return hash;
    }

}
