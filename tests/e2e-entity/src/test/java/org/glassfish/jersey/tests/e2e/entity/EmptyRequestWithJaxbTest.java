/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jettison.JettisonConfig;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.jettison.JettisonJaxbContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

/**
 * @author Paul Sandoz
 * @author Martin Matula
 */
@RunWith(Enclosed.class)
public class EmptyRequestWithJaxbTest {

    @SuppressWarnings("UnusedParameters")
    @Path("/")
    @Ignore("This class is not a test class & must be ignored by the Enclosed test runner.")
    public static class Resource {

        @POST
        public void bean(JaxbBean b) {
        }

        @Path("type")
        @POST
        public void type(JaxbBeanType b) {
        }

        @Path("list-bean")
        @POST
        public void listBean(List<JaxbBean> b) {
        }

        @Path("list-type")
        @POST
        public void listType(List<JaxbBeanType> b) {
        }

        @Path("array-bean")
        @POST
        public void arrayBean(JaxbBean[] b) {
        }

        @Path("array-type")
        @POST
        public void arrayType(JaxbBeanType[] b) {
        }

    }

    public static class EmptyRequestTest extends JerseyTest {

        @Override
        protected Application configure() {
            return new ResourceConfig(Resource.class).register(new JettisonFeature());
        }

        @Override
        protected void configureClient(ClientConfig config) {
            config.register(JettisonFeature.class);
        }

        @Test
        public void testEmptyJsonRequestMapped() {
            _test(target());
        }

        @Test
        public void testEmptyXmlRequest() {
            WebTarget r = target();

            Response cr = r.request().post(Entity.entity(null, "application/xml"));
            assertEquals(400, cr.getStatus());

            cr = r.path("type").request().post(Entity.entity(null, "application/xml"));
            assertEquals(400, cr.getStatus());

            cr = r.path("list-bean").request().post(Entity.entity(null, "application/xml"));
            assertEquals(400, cr.getStatus());

            cr = r.path("list-type").request().post(Entity.entity(null, "application/xml"));
            assertEquals(400, cr.getStatus());

            cr = r.path("array-bean").request().post(Entity.entity(null, "application/xml"));
            assertEquals(400, cr.getStatus());

            cr = r.path("array-type").request().post(Entity.entity(null, "application/xml"));
            assertEquals(400, cr.getStatus());
        }
    }

    @Ignore("This class is not a test class & must be ignored by the Enclosed test runner.")
    public abstract static class CR implements ContextResolver<JAXBContext> {

        private final JAXBContext context;

        private final Class[] classes = {JaxbBean.class, JaxbBeanType.class};

        private final Set<Class> types = new HashSet<>(Arrays.asList(classes));

        public CR() {
            try {
                context = configure(classes);
            } catch (JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }

        protected abstract JAXBContext configure(Class[] classes) throws JAXBException;

        public JAXBContext getContext(Class<?> objectType) {
            return (types.contains(objectType)) ? context : null;
        }
    }

    public static class MappedJettisonCRTest extends JerseyTest {

        @Override
        protected Application configure() {
            return new ResourceConfig(MappedJettisonCR.class, Resource.class).register(new JettisonFeature());
        }

        @Override
        protected void configureClient(ClientConfig config) {
            config.register(JettisonFeature.class);
        }

        public static class MappedJettisonCR extends CR {

            protected JAXBContext configure(Class[] classes) throws JAXBException {
                return new JettisonJaxbContext(JettisonConfig.mappedJettison().build(), classes);
            }
        }

        @Test
        public void testMappedJettisonCR() {
            _test(target());
        }
    }

    public static class BadgerFishCRTest extends JerseyTest {

        @Override
        protected Application configure() {
            return new ResourceConfig(BadgerFishCR.class, Resource.class).register(new JettisonFeature());
        }

        @Override
        protected void configureClient(ClientConfig config) {
            config.register(JettisonFeature.class);
        }

        public static class BadgerFishCR extends CR {

            protected JAXBContext configure(Class[] classes) throws JAXBException {
                return new JettisonJaxbContext(JettisonConfig.badgerFish().build(), classes);
            }
        }

        @Test
        public void testBadgerFishCR() {
            _test(target());
        }
    }

    public static void _test(WebTarget target) {
        Response cr = target.request().post(Entity.entity(null, "application/json"));
        assertEquals(400, cr.getStatus());

        cr = target.path("type").request().post(Entity.entity(null, "application/json"));
        assertEquals(400, cr.getStatus());

        cr = target.path("list-bean").request().post(Entity.entity(null, "application/json"));
        assertEquals(400, cr.getStatus());

        cr = target.path("list-type").request().post(Entity.entity(null, "application/json"));
        assertEquals(400, cr.getStatus());

        cr = target.path("array-bean").request().post(Entity.entity(null, "application/json"));
        assertEquals(400, cr.getStatus());

        cr = target.path("array-type").request().post(Entity.entity(null, "application/json"));
        assertEquals(400, cr.getStatus());
    }
}
