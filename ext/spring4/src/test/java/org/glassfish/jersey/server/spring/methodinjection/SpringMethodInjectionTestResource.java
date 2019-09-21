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

package org.glassfish.jersey.server.spring.methodinjection;

import org.glassfish.jersey.server.spring.NoComponent;
import org.glassfish.jersey.server.spring.TestComponent1;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class SpringMethodInjectionTestResource {

    private TestComponent1 testComponent1;
    private List<org.glassfish.jersey.server.spring.TestComponent2> testComponent2List;
    private Set<org.glassfish.jersey.server.spring.TestComponent2> testComponent2Set;
    private NoComponent noComponent;

    @Autowired
    public void setTestComponent1(TestComponent1 testComponent1) {
        this.testComponent1 = testComponent1;
    }

    @Autowired
    public void setTestComponent2List(List<org.glassfish.jersey.server.spring.TestComponent2> testComponent2List) {
        this.testComponent2List = testComponent2List;
    }

    @Autowired
    public void setTestComponent2Set(Set<org.glassfish.jersey.server.spring.TestComponent2> testComponent2Set) {
        this.testComponent2Set = testComponent2Set;
    }

    @Autowired(required = false)
    public void setNoComponent(NoComponent noComponent) {
        this.noComponent = noComponent;
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
        java.util.Iterator<org.glassfish.jersey.server.spring.TestComponent2> iterator = testComponent2Set.iterator();
        return (testComponent2Set.size() == 2 && "test ok".equals(iterator.next().result())
                && "test ok".equals(iterator.next().result())) ? "test ok" : "test failed";
    }

    @Path("JERSEY-2643")
    @GET
    public String JERSEY_2643() {
        return noComponent == null ? "test ok" : "test failed";
    }

}
