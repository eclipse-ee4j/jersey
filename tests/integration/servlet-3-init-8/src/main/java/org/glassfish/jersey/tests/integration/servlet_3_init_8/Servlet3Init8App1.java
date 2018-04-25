/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_3_init_8;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * The application is fully configured in {@code web.xml} ({@code servlet} as well as {@code servlet-mapping}).
 * It means {@code ApplicationPath.value} ({@code /app1ann}) is overridden using a {@code servlet-mapping} element.
 * The application also explicitly register JAX-RS resource.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
@ApplicationPath("/app1ann")
public class Servlet3Init8App1 extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> hashSet = new HashSet<>();
        hashSet.add(HelloWorld1Resource.class);
        return hashSet;
    }
}
