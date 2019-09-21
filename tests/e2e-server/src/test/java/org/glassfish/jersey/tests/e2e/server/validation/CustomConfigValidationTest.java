/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;

import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.validation.ValidationConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.eclipse.persistence.jaxb.BeanValidationMode;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Michal Gajdos
 */
public class CustomConfigValidationTest extends JerseyTest {

    @javax.ws.rs.Path("customconfigvalidation/{path: .*}")
    public static class CustomConfigResource {

        @POST
        @Consumes("application/xml")
        @Produces("application/xml")
        @NotNull
        @Valid
        public CustomBean post(@PathParam("path") final String path, final CustomBean beanParameter,
                               @Size(min = 5) @HeaderParam("myHeader") final String header) {
            if ("".equals(path)) {
                beanParameter.setPath(null);
                beanParameter.setValidate(false);
            } else {
                beanParameter.setPath(path);
                beanParameter.setValidate(true);
            }
            return beanParameter;
        }
    }

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        final ResourceConfig resourceConfig = new ResourceConfig(CustomConfigResource.class);

        // Turn off BV in MOXy otherwise the entities on server would be validated at incorrect times.
        resourceConfig.register(moxyXmlFeature());
        resourceConfig.register(ValidationConfigurationContextResolver.class);

        resourceConfig.property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        return resourceConfig;
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        super.configureClient(config);

        // Turn off BV in MOXy otherwise the entities on client would be validated as well.
        config.register(moxyXmlFeature());
    }

    private MoxyXmlFeature moxyXmlFeature() {
        return new MoxyXmlFeature(new HashMap<String, Object>() {{
                    put(MarshallerProperties.BEAN_VALIDATION_MODE, BeanValidationMode.NONE);
                }},
                Thread.currentThread().getContextClassLoader(),
                false
        );
    }

    @Test
    public void testPositive() throws Exception {
        final Response response = target("customconfigvalidation")
                .path("ok")
                .request()
                .header("myHeader", "12345")
                .post(Entity.entity(new CustomBean(), MediaType.APPLICATION_XML_TYPE));

        assertEquals(200, response.getStatus());
        assertEquals("ok", response.readEntity(CustomBean.class).getPath());
    }

    @Test
    public void testParameterNameWithInterpolator() throws Exception {
        final Response response = target("customconfigvalidation")
                .path("ok")
                .request()
                .header("myHeader", "1234")
                .post(Entity.entity(new CustomBean(), MediaType.APPLICATION_XML_TYPE));

        assertEquals(400, response.getStatus());

        final String message = response.readEntity(String.class);

        assertFalse(message.contains("arg2"));
        assertTrue(message.contains("header"));
        assertFalse(message.contains("size must be between"));
        assertTrue(message.contains("message"));
    }

    @Test
    public void testTraversableResolver() throws Exception {
        final Response response = target("customconfigvalidation/")
                .request()
                .header("myHeader", "12345")
                .post(Entity.entity(new CustomBean(), MediaType.APPLICATION_XML_TYPE));

        assertEquals(200, response.getStatus());
        // return value passed validation because of "corrupted" traversableresolver
        assertEquals(null, response.readEntity(CustomBean.class).getPath());
    }

    public static class ValidationConfigurationContextResolver implements ContextResolver<ValidationConfig> {

        private final ValidationConfig config;

        public ValidationConfigurationContextResolver() {
            config = new ValidationConfig();

            // ConstraintValidatorFactory is set by default.
            config.messageInterpolator(new CustomMessageInterpolator());
            config.parameterNameProvider(new CustomParameterNameProvider());
            config.traversableResolver(new CustomTraversableResolver());
        }

        @Override
        public ValidationConfig getContext(final Class<?> type) {
            return ValidationConfig.class.isAssignableFrom(type) ? config : null;
        }
    }

    private static class CustomMessageInterpolator implements MessageInterpolator {

        @Override
        public String interpolate(final String messageTemplate, final Context context) {
            return "message";
        }

        @Override
        public String interpolate(final String messageTemplate, final Context context, final Locale locale) {
            return "localized message";
        }
    }

    private static class CustomParameterNameProvider implements ParameterNameProvider {

        private final ParameterNameProvider nameProvider;

        public CustomParameterNameProvider() {
            nameProvider = Validation.byDefaultProvider().configure().getDefaultParameterNameProvider();
        }

        @Override
        public List<String> getParameterNames(final Constructor<?> constructor) {
            return nameProvider.getParameterNames(constructor);
        }

        @Override
        public List<String> getParameterNames(final Method method) {
            try {
                final Method post = CustomConfigResource.class.getMethod("post", String.class, CustomBean.class, String.class);

                if (method.equals(post)) {
                    return Arrays.asList("path", "beanParameter", "header");
                }
            } catch (final NoSuchMethodException e) {
                // Do nothing.
            }
            return nameProvider.getParameterNames(method);
        }
    }

    private static class CustomTraversableResolver implements TraversableResolver {

        @Override
        public boolean isReachable(final Object traversableObject,
                                   final Path.Node traversableProperty,
                                   final Class<?> rootBeanType,
                                   final Path pathToTraversableObject,
                                   final ElementType elementType) {
            return false;
        }

        @Override
        public boolean isCascadable(final Object traversableObject,
                                    final Path.Node traversableProperty,
                                    final Class<?> rootBeanType,
                                    final Path pathToTraversableObject,
                                    final ElementType elementType) {
            return false;
        }
    }
}
