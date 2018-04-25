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

package org.glassfish.jersey.tests.memleaks.threadlocal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet that introduces a memory leak with a single call. All the classes loaded by current classloader won't get GCed even
 * after this application is undeployed.
 * <p/>
 * This servlet demonstrates that fix HK2-247 when {@link ThreadLocal} reference is changed from a static to an instance, it does
 * not always solve memory leak issues as long as a static reference (in this case {@link StaticReferenceClass#STATIC_HOLDER})
 * holds (even transitively) the {@link ThreadLocal} instance.
 * <p/>
 * To revert this case to the simple one (the original one), change the {@link #threadLocal} instance should be static and than no
 * {@link StaticReferenceClass#STATIC_HOLDER} is needed. The memory leak occurs without any other special actions.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class ThreadLocalMemoryLeakServlet extends HttpServlet {

    /**
     * This {@link ThreadLocal} reference is held by instance reference and might be GCed; however, the class {@link SomeClass}
     * has a reference to its classloader which has a reference to {@link StaticReferenceClass} class which has a static reference
     * to this instance. As a result, this {@link ThreadLocal} instance is never GCed in a thread pool environment.
     * <p/>
     * If this field was changed to a static reference, the memory leak would occur even without the {@link
     * StaticReferenceClass#STATIC_HOLDER} holding this instance (see {@link #doGet(HttpServletRequest, HttpServletResponse)}
     * method bellow).
     */
    final ThreadLocal<Class> threadLocal = new ThreadLocal<Class>() {
        @Override
        protected Class initialValue() {
            return SomeClass.class;
        }
    };

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StaticReferenceClass.STATIC_HOLDER.add(threadLocal);
        response.getWriter().write("Greeting: " + SomeClass.hello() + "\n");
        response.getWriter().write("Thread Locals Content: " + threadLocal.get().getCanonicalName() + "\n");
        response.getWriter().write("Holder size: " + StaticReferenceClass.STATIC_HOLDER.size());
    }
}
