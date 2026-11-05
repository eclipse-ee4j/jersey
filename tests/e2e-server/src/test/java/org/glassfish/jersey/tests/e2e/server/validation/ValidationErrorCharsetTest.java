/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.jersey.tests.e2e.server.validation;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests that validation errors are sent in the response when a filter adds a charset to the media type of the response.
 */
public class ValidationErrorCharsetTest extends JerseyTest {

    @Path("charset")
    public static class CharsetResource {

        @GET
        public String get(@NotNull @QueryParam("name") final String name) {
            return name;
        }
    }

    public static class CharsetFilter implements ContainerResponseFilter {

        @Override
        public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
            final MediaType mediaType = responseContext.getMediaType();
            if (mediaType != null) {
                responseContext.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, mediaType.withCharset("UTF-8"));
            }
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(CharsetResource.class, CharsetFilter.class)
                .property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
    }

    @Test
    public void testPlainTextValidationErrorWithCharset() {
        final Response response = target("charset").request(MediaType.TEXT_PLAIN_TYPE).get();

        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.getMediaType(), equalTo(MediaType.valueOf("text/plain;charset=UTF-8")));
        assertThat(response.readEntity(String.class), containsString("invalidValue = null"));
    }

    @Test
    public void testHtmlValidationErrorWithCharset() {
        final Response response = target("charset").request(MediaType.TEXT_HTML_TYPE).get();

        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.getMediaType(), equalTo(MediaType.valueOf("text/html;charset=UTF-8")));
        assertThat(response.readEntity(String.class), containsString("<div class=\"validation-errors\">"));
    }
}
