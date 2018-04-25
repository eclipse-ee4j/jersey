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

package org.glassfish.jersey.tests.integration.jersey2892;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests whether classes repeating in the object graph are filtered out correctly.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public abstract class AbstractJerseyEntityFilteringITCase extends JerseyTest {

    @Override
    protected Application configure() {
        return new TestApplication();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    /**
     * Tests whether sub-sub-field, {@link TestResource.Street} in particular,
     * is not filtered out.
     * <p/>
     * This corresponds with the JERSEY-2892 reported case.
     */
    @Test
    public void testWhetherSubSubFiledIsNotFilteredOut() {
        Response response = target(provider() + "/test").request(MediaType.APPLICATION_JSON_TYPE).get();

        final TestResource.Persons persons = response.readEntity(TestResource.Persons.class);

        Assert.assertEquals("Amphitheatre Pkwy", persons.first.address.street.name);
        Assert.assertEquals("Microsoft Way", persons.second.address.street.name);
    }

    /**
     * Tests whether a de-referenced case of the reported problem is still correctly not filtered out. In particular, a
     * sub-sub-sub-field of the same class is not filtered out.
     */
    @Test
    public void testWhetherSubSubSubFieldIsNotFilteredOut() {
        Response response = target(provider() + "/pointer").request(MediaType.APPLICATION_JSON_TYPE).get();

        final TestResource.Pointer pointer = response.readEntity(TestResource.Pointer.class);

        Assert.assertEquals("Amphitheatre Pkwy", pointer.persons.first.address.street.name);
        Assert.assertEquals("Microsoft Way", pointer.persons.second.address.street.name);
    }

    /**
     * Tests whether a reference cycle is detected and infinite recursion is prevented.
     */
    @Test
    public void testWhetherReferenceCycleIsDetected() {
        Response response = target(provider() + "/recursive").request(MediaType.APPLICATION_JSON_TYPE).get();

        final TestResource.Recursive recursive = response.readEntity(TestResource.Recursive.class);

        Assert.assertEquals("c", recursive.subField.subSubField.idSubSubField);
    }

    /**
     * Jersey Entity filtering feature provider.
     *
     * @return The provider string to match with appropriate Jersey app configured in web.xml.
     */
    protected abstract String provider();

}
