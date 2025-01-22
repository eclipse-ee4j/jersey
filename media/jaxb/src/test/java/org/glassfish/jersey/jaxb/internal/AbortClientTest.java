/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jaxb.internal;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class AbortClientTest {
    public static final String MESSAGE = "hello";
    @Test
    void testAbortWithJaxbEntity() {
        Client client = ClientBuilder.newBuilder()
                .register(AbortRequestFilter.class)
                .build();

        try {
            JaxbEntity entity = client.target("http://localhost:8080")
                    .request()
                    .get()
                    .readEntity(JaxbEntity.class);
            MatcherAssert.assertThat(entity.getStr(), Matchers.is(MESSAGE));
        } finally {
            client.close();
        }
    }

    public static class AbortRequestFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext requestContext) {
            requestContext.abortWith(Response.ok(new JaxbEntity(MESSAGE)).build());
        }

    }

    @XmlRootElement
    public static class JaxbEntity {

        @XmlElement
        private String str;

        public JaxbEntity() {}

        public JaxbEntity(String str) {
            this.str = str;
        }

        public String getStr() {
            return str;
        }
    }
}
