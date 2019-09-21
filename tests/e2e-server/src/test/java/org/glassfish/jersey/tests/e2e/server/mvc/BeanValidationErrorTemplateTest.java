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

package org.glassfish.jersey.tests.e2e.server.mvc;

import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.ErrorTemplate;
import org.glassfish.jersey.server.mvc.beanvalidation.MvcBeanValidationFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.tests.e2e.server.mvc.provider.TestViewProcessor;

import org.hibernate.validator.constraints.Length;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Michal Gajdos
 */
public class BeanValidationErrorTemplateTest extends JerseyTest {

    private Properties props;

    @Before
    public void setUp() throws Exception {
        props = new Properties();

        super.setUp();
    }

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(ErrorTemplateResource.class)
                .register(MvcBeanValidationFeature.class)
                .register(TestViewProcessor.class);
    }

    @Path("/")
    @Consumes("text/plain")
    public static class ErrorTemplateResource {

        @POST
        @Path("params")
        @ErrorTemplate
        public String invalidParams(@Length(min = 5) final String value) {
            fail("Should fail on Bean Validation!");
            return value;
        }

        @POST
        @Path("return")
        @ErrorTemplate
        @Length(min = 5)
        public String invalidReturnValue(final String value) {
            return value;
        }
    }

    @Test
    public void testInvalidParams() throws Exception {
        final Response response = target("params").request().post(Entity.text("foo"));
        props.load(response.readEntity(InputStream.class));

        assertThat(response.getStatus(), equalTo(400));
        assertThat(props.getProperty("model"),
                equalTo("{org.hibernate.validator.constraints.Length.message}_ErrorTemplateResource.invalidParams.arg0_foo"));
    }

    @Test
    public void testInvalidReturnValue() throws Exception {
        final Response response = target("return").request().post(Entity.text("foo"));
        props.load(response.readEntity(InputStream.class));

        assertThat(response.getStatus(), equalTo(500));
        assertThat(props.getProperty("model"),
                equalTo("{org.hibernate.validator.constraints.Length.message}_ErrorTemplateResource.invalidReturnValue."
                        + "<return value>_foo"));
    }
}
