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

package org.glassfish.jersey.tests.integration.jersey2322;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Reproducer tests for JERSEY-2322.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class Jersey2322ITCase extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new Jersey2322();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    /**
     * Server side response is wrapped, needs to be read to wrapper class.
     */
    @Test
    public void testJackson2JsonPut1() {
        final Response response = target("1").request().put(Entity.json(new Issue2322Resource.JsonString1("foo")));

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.readEntity(Wrapper1.class).getJsonString1().getValue(), equalTo("Hello foo"));
    }

    /**
     * Server side response is returned as orig class.
     */
    @Test
    public void testJackson2JsonPut2() {
        final Response response = target("2").request().put(Entity.json(new Issue2322Resource.JsonString2("foo")));

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.readEntity(Issue2322Resource.JsonString2.class).getValue(), equalTo("Hi foo"));
    }


    public static class Wrapper1 {
        @JsonProperty("JsonString1")
        Issue2322Resource.JsonString1 jsonString1;

        public Issue2322Resource.JsonString1 getJsonString1() {
            return jsonString1;
        }

        public void setJsonString1(Issue2322Resource.JsonString1 jsonString1) {
            this.jsonString1 = jsonString1;
        }
    }

}
