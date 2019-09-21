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

package org.glassfish.jersey.server.spring.parameterinjection;

import org.glassfish.jersey.server.spring.TestComponent1;
import org.glassfish.jersey.server.spring.TestComponent2;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Path("/")
public class SpringParameterInjectionTestResource {

    private final TestComponent1 testComponent1;
    private final List<TestComponent2> testComponent2List;
    private final Set<TestComponent2> testComponent2Set;

    @Autowired
    public SpringParameterInjectionTestResource(final TestComponent1 testComponent1,
                                                final List<TestComponent2> testComponent2List,
                                                final Set<TestComponent2> testComponent2Set) {
        this.testComponent1 = testComponent1;
        this.testComponent2List = testComponent2List;
        this.testComponent2Set = testComponent2Set;
    }

    @Path("test1")
    @GET
    public String test1() {
        return testComponent1.result();
    }

    @Path("test2")
    @GET
    public String test2() {
        return (testComponent2List.size() == 2 && "test ok".equals(testComponent2List.get(0).result())
                && "test ok".equals(testComponent2List.get(1).result())) ? "test ok" : "test failed";
    }

    @Path("test3")
    @GET
    public String test3() {
        Iterator<TestComponent2> iterator = testComponent2Set.iterator();
        return (testComponent2Set.size() == 2 && "test ok".equals(iterator.next().result())
                && "test ok".equals(iterator.next().result())) ? "test ok" : "test failed";
    }
}
