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

package org.glassfish.jersey.tests.integration.jersey2184;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import javax.servlet.ServletContext;

/**
 * Test Application subclass for JERSEY-2184 integration test.
 *
 * Tests the ability to inject {@link ServletContext} into application subclass constructor
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class App extends Application {

    /** constructor-injected servletContext */
    private ServletContext ctx;

    public App(@Context ServletContext servletContext) {
        this.ctx = servletContext;
    }

    @Override
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<>();
        String dynamicClassName = ctx.getInitParameter("dynamicClassName");
        Class<?> clazz = null;
        if (classes.isEmpty()) {
            try {
                clazz = Class.forName(dynamicClassName);
            } catch (ClassNotFoundException e) {
                // swallow the exception - if class is not loaded, the integration test will fail
            }

            if (clazz != null) {
                classes.add(clazz);
            }
        }
        return classes;
    }
}
