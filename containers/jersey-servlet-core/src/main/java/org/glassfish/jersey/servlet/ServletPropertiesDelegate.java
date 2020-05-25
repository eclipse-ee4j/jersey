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

package org.glassfish.jersey.servlet;

import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.glassfish.jersey.internal.PropertiesDelegate;

/**
 * @author Martin Matula
 */
class ServletPropertiesDelegate implements PropertiesDelegate {
    private final HttpServletRequest request;

    public ServletPropertiesDelegate(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Object getProperty(String name) {
        return request.getAttribute(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        //noinspection unchecked
        return Collections.list(request.getAttributeNames());
    }

    @Override
    public void setProperty(String name, Object object) {
        request.setAttribute(name, object);
    }

    @Override
    public void removeProperty(String name) {
        request.removeAttribute(name);
    }
}
