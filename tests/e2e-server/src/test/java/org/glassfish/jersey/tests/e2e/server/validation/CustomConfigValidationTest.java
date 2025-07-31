/*
 * Copyright (c) 2012, 2025 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Clock;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.validation.ClockProvider;
import jakarta.validation.constraints.Future;
import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.ValueExtractor;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ContextResolver;

import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.validation.ValidationConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.eclipse.persistence.jaxb.BeanValidationMode;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Michal Gajdos
 */
public class CustomConfigValidationTest extends JerseyTest {
    private static final AtomicReference<Boolean> CLOCK_HIT = new AtomicReference<>(false);

    @jakarta.ws.rs.Path("customconfigvalidation/{path: .*}")
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

    @jakarta.ws.rs.Path("customclockvalidation")
    public static class CustomClockResource {
        @POST
        @Valid
        public String echo(@Future Date date) {
            return "OK";
        }
    }

    @jakarta.ws.rs.Path("customextractorvalidation")
    public static class CustomExtractorResource {
        @POST
        @Valid
        public String validate(@Size(min = 5) LikeString string) {
            return "OK";
        }
    }

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        final ResourceConfig resourceConfig = new ResourceConfig(CustomConfigResource.class)
                .register(CustomClockResource.class)
                .register(CustomExtractorResource.class)
                .register(new DateMessageProvider())
                .register(new LikeStringMessageProvider());

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

    @Test
    public void testCustomClock() {
        Assertions.assertFalse(CLOCK_HIT.get());
        Date before = new Date(System.currentTimeMillis() - 1_000_000_000);
        Date after = new Date(System.currentTimeMillis() + 1_000_000_000);
        try (Response response = target("customclockvalidation")
                .register(new DateMessageProvider())
                .request()
                .post(Entity.entity(after, MediaType.TEXT_PLAIN_TYPE))) {
            Assertions.assertEquals("OK", response.readEntity(String.class));
            Assertions.assertTrue(CLOCK_HIT.get());
        }

        try (Response response = target("customclockvalidation")
                .register(new DateMessageProvider())
                .request()
                .post(Entity.entity(before, MediaType.TEXT_PLAIN_TYPE))) {
            Assertions.assertEquals(400, response.getStatus());
        }
    }

    @Test
    public void testCustomExtractor() {
        try (Response response = target("customextractorvalidation")
                .register(new LikeStringMessageProvider())
                .request()
                .post(Entity.entity(new LikeString("123456"), MediaType.TEXT_PLAIN_TYPE))) {
            Assertions.assertEquals("OK", response.readEntity(String.class));
        }

        try (Response response = target("customextractorvalidation")
                .register(new LikeStringMessageProvider())
                .request()
                .post(Entity.entity(new LikeString("1234"), MediaType.TEXT_PLAIN_TYPE))) {
            Assertions.assertEquals(400, response.getStatus());
        }

    }

    public static class ValidationConfigurationContextResolver implements ContextResolver<ValidationConfig> {

        private final ValidationConfig config;

        public ValidationConfigurationContextResolver() {
            config = new ValidationConfig();

            // ConstraintValidatorFactory is set by default.
            config.messageInterpolator(new CustomMessageInterpolator());
            config.parameterNameProvider(new CustomParameterNameProvider());
            config.traversableResolver(new CustomTraversableResolver());
            config.clockProvider(new CustomClockProvider());
            config.addValueExtractor(new LikeStringExtractor());
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

    private static class CustomClockProvider implements ClockProvider {
        @Override
        public Clock getClock() {
            CLOCK_HIT.set(true);
            return Clock.systemUTC();
        }
    }

    private static class DateMessageProvider implements MessageBodyWriter<Date>, MessageBodyReader<Date> {
        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Date.class.equals(type);
        }

        @Override
        public Date readFrom(Class<Date> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                             MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
                throws IOException, WebApplicationException {
            String date = ReaderWriter.readFromAsString(new InputStreamReader(entityStream));
            return new Date(Long.parseLong(date));
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Date.class.equals(type);
        }

        @Override
        public void writeTo(Date date, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                throws IOException, WebApplicationException {
            entityStream.write(String.valueOf(date.getTime()).getBytes());
        }
    }

    private static class LikeString {
        private final String inner;

        private LikeString(String inner) {
            this.inner = inner;
        }
    }

    private static class LikeStringMessageProvider implements MessageBodyReader<LikeString>, MessageBodyWriter<LikeString> {

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return LikeString.class.equals(type);
        }

        @Override
        public LikeString readFrom(Class<LikeString> type, Type genericType, Annotation[] annotations,
                                   MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
                throws IOException, WebApplicationException {
            return new LikeString(ReaderWriter.readFromAsString(entityStream, MediaType.WILDCARD_TYPE));
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return LikeString.class.equals(type);
        }

        @Override
        public void writeTo(LikeString likeString, Class<?> type, Type genericType, Annotation[] annotations,
                            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                throws IOException, WebApplicationException {
            entityStream.write(likeString.inner.getBytes());
        }
    }

    @UnwrapByDefault
    private static class LikeStringExtractor implements ValueExtractor<@ExtractedValue(type = String.class) LikeString> {
        @Override
        public void extractValues(@ExtractedValue(type = String.class) LikeString originalValue, ValueReceiver receiver) {
            receiver.value(null, originalValue.inner);
        }
    }
}
