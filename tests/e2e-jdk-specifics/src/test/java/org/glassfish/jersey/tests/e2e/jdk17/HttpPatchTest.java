/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.jdk17;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class HttpPatchTest extends JerseyTest {

    public static final String PATCH_ENTITY = "HelloPatch";

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @HttpMethod(HttpMethod.PATCH)
    public @interface PATCH {

    }

    @Path("/")
    public static class HttpPatchResource {
        @PATCH
        public String patchEcho(String entity) {
            return entity;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(HttpPatchResource.class);
    }

    @Test
    void testPatchWithHttpUrlConnector() {
        try (Response response = target()
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request().method(HttpMethod.PATCH, Entity.text(PATCH_ENTITY))) {
            MatcherAssert.assertThat(200, Matchers.equalTo(response.getStatus()));
            response.bufferEntity();
            System.out.println(response.readEntity(String.class));
            MatcherAssert.assertThat(PATCH_ENTITY, Matchers.equalTo(response.readEntity(String.class)));
        }
    }
}
