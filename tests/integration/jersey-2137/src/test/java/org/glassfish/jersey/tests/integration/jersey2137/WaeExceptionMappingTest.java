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

package org.glassfish.jersey.tests.integration.jersey2137;

import java.net.URI;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Reproducer for JERSEY-2137.
 * Ensure that generated {@link WebApplicationException} is propagated
 * via transactional CDI call and mapped to response according to JAX-RS spec.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class WaeExceptionMappingTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new TestApplication();
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path("jersey-2137").build();
    }

    /**
     * Test all {@javax.transaction.Transactional}
     * annotated CDI beans. The test scenario is as follows.
     * Set two accounts via the CDI bean that avoids rollback.
     * Should any rollback happen there, we would not be able
     * to store any data in JPA. Next try to make two transactions,
     * first of them should be finished without errors,
     * during the other one, a rollback is expected.
     * The rollback should avoid partial data to be written
     * to the first account.
     */
    @Test
    public void testTransactions() {

        final WebTarget cdiResource = target().path("cdi-transactional");
        final WebTarget cdiResourceNoRollback = target().path("cdi-transactional-no-rollback");

        Response response;
        String responseBody;

        // account 12 -> insert 1000:
        response = cdiResourceNoRollback.path("12").request().put(Entity.text("1000"));
        assertThat(response.getStatus(), equalTo(200));

        // account 13 -> insert 1000:
        response = cdiResourceNoRollback.path("13").request().put(Entity.text("1000"));
        assertThat(response.getStatus(), equalTo(200));

        // transfer 1000 from 13 to 12:
        response = cdiResource.queryParam("from", "13").queryParam("to", "12").request().post(Entity.text("1000"));
        assertThat(response.getStatus(), equalTo(200));

        // ensure 12 has balance 2000:
        response = cdiResource.path("12").request().get();
        assertThat(response.getStatus(), equalTo(200));
        responseBody = response.readEntity(String.class);
        assertThat(responseBody, equalTo("2000"));

        // try to transfer 1000 from non-existing 8 to 12, this time the transaction should fail:
        response = cdiResource.queryParam("from", "8").queryParam("to", "12").request().post(Entity.text("1000"));
        assertThat(response.getStatus(), equalTo(400));

        // ensure 12 balance has not changed:
        response = cdiResource.path("12").request().get();
        assertThat(response.getStatus(), equalTo(200));
        responseBody = response.readEntity(String.class);
        assertThat(responseBody, equalTo("2000"));
    }
}
