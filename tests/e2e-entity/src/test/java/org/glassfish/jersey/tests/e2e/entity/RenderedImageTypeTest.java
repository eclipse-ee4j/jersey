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

package org.glassfish.jersey.tests.e2e.entity;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
public class RenderedImageTypeTest extends JerseyTest {

    @Path("/")
    public static class ImageResource {

        @Consumes("image/gif")
        @Produces("image/png")
        @POST
        public RenderedImage postGif(final RenderedImage image) {
            return image;
        }

        @Consumes("image/png")
        @Produces("image/png")
        @POST
        public RenderedImage postPng(final RenderedImage image) {
            return image;
        }

        @Path("sub")
        @Consumes("application/octet-stream")
        @Produces("image/png")
        @POST
        public RenderedImage postUndefined(final BufferedImage image) {
            return image;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ImageResource.class);
    }

    @Test
    public void testPostPng() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("Jersey_yellow.png");
        Response response = target().request().post(Entity.entity(stream, "image/png"));
        assertThat(Long.valueOf(response.getHeaderString("Content-Length")), greaterThan(0L));

        final RenderedImage image = response.readEntity(RenderedImage.class);
        assertThat(image, notNullValue());

        response = target().request().post(Entity.entity(image, "image/png"));
        assertThat(response.readEntity(RenderedImage.class), notNullValue());
        assertThat(Long.valueOf(response.getHeaderString("Content-Length")), greaterThan(0L));
    }

    @Test
    public void testPostGif() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("duke_rocket.gif");
        Response response = target().request().post(Entity.entity(stream, "image/gif"));
        assertThat(Long.valueOf(response.getHeaderString("Content-Length")), greaterThan(0L));

        final RenderedImage image = response.readEntity(RenderedImage.class);
        assertThat(image, notNullValue());

        response = target().request().post(Entity.entity(image, "image/png"));
        assertThat(response.readEntity(RenderedImage.class), notNullValue());
        assertThat(Long.valueOf(response.getHeaderString("Content-Length")), greaterThan(0L));
    }

    @Test
    public void testPostUndefined() throws Exception {
        final InputStream stream = getClass().getResourceAsStream("duke_rocket.gif");
        Response response = target("sub").request().post(Entity.entity(stream, "application/octet-stream"));
        assertThat(Long.valueOf(response.getHeaderString("Content-Length")), greaterThan(0L));

        final RenderedImage image = response.readEntity(RenderedImage.class);
        assertThat(image, notNullValue());

        response = target().request().post(Entity.entity(image, "image/png"));
        assertThat(response.readEntity(RenderedImage.class), notNullValue());
        assertThat(Long.valueOf(response.getHeaderString("Content-Length")), greaterThan(0L));
    }
}
