/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_3_async;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Asynchronous servlet-deployed resource application.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@ApplicationPath("/")
public class Servlet3Async extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> hashSet = new HashSet<>();
        hashSet.add(AsyncServletResource.class);
        hashSet.add(AsyncResumeTimeoutResource.class);
        hashSet.add(AsyncCancelTimeoutResource.class);
        return hashSet;
    }
}
