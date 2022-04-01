/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.annotation.Priority;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.hamcrest.Matcher;
import org.hamcrest.core.SubstringMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

/**
 * {@link LoggingFeature} end-to-end tests.
 *
 * @author Michal Gajdos
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        LoggingFeatureTest.ClientTest.class,
        LoggingFeatureTest.ContainerTest.class,
        LoggingFeatureTest.ContainerAutodiscoveryTest.class,
        LoggingFeatureTest.FiltersOrderTest.class
})
public class LoggingFeatureTest {

    private static final String LOGGER_NAME = "org.glassfish.jersey.logging.feature";
    private static final String BINARY_MEDIA_TYPE = "application/binary";
    private static final String TEXT_MEDIA_TYPE = MediaType.TEXT_PLAIN;
    private static final String ENTITY = "This entity must (not) be logged";
    private static final String SEPARATOR = "!-------!";

    @Path("/")
    public static class MyResource {

        @GET
        @Produces(BINARY_MEDIA_TYPE)
        public Response getHeadersAndBinaryPayload() {
            return Response
                    .ok(ENTITY)
                    .header("001", "First Header Value")
                    .header("002", "Second Header Value")
                    .header("003", "Third Header Value")
                    .header("004", "Fourth Header Value")
                    .header("005", "Fifth Header Value")
                    .build();
        }

        @Path("/text")
        @GET
        @Produces(TEXT_MEDIA_TYPE)
        public Response getHeadersAndTextPayload() {
            return Response
                    .ok(ENTITY)
                    .header("001", "First Header Value")
                    .header("002", "Second Header Value")
                    .header("003", "Third Header Value")
                    .header("004", "Fourth Header Value")
                    .header("005", "Fifth Header Value")
                    .build();
        }

        @Path("/text")
        @POST
        @Produces(TEXT_MEDIA_TYPE)
        public Response post(String text) {
            return Response
                    .ok(ENTITY)
                    .build();
        }

        @Path("/echo-headers")
        @GET
        @Produces(TEXT_MEDIA_TYPE)
        public Response getSameHeadersAsRequest(@Context HttpHeaders httpHeaders) {
            Response.ResponseBuilder responseBuilder = Response.ok(ENTITY);
            httpHeaders.getRequestHeaders().forEach(
                    (key, values) -> values.forEach(
                            value -> responseBuilder.header(key, value)));
            return responseBuilder.build();
        }
    }

    /**
     * General client side tests.
     */
    public static class ClientTest extends JerseyTest {

        @Override
        protected Application configure() {
            set(TestProperties.RECORD_LOG_LEVEL, Level.FINE.intValue());

            return new ResourceConfig(MyResource.class);
        }

        @Test
        public void testFilterAsClientRequestFilter() throws Exception {
            final Response response = target()
                    .register(new LoggingFeature(Logger.getLogger(LOGGER_NAME)))
                    .request()
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for proper id.
            assertThat(getLoggingFilterRequestLogRecord(getLoggedRecords()).getMessage(), containsString("1 *"));
        }

        @Test
        public void testOrderOfHeadersOnClient() throws Exception {
            final Response response = target()
                    .register(new LoggingFeature(Logger.getLogger(LOGGER_NAME)))
                    .request()
                    .get();
            assertThat(response.readEntity(String.class), equalTo(ENTITY));

            final LogRecord record = getLoggingFilterResponseLogRecord(getLoggedRecords());
            final String message = record.getMessage();

            int i = 1;
            do {
                final String h1 = "00" + i++;
                final String h2 = "00" + i;

                final int i1 = message.indexOf(h1);
                final int i2 = message.indexOf(h2);

                assertThat("Header " + h1 + " has been logged sooner than header " + h2, i1, lessThan(i2));
            } while (i < 5);
        }

        @Test
        public void testVerbosityAnyPayload() throws Exception {
            final Response response = target()
                    .register(new LoggingFeature(Logger.getLogger(LOGGER_NAME), LoggingFeature.Verbosity.PAYLOAD_ANY))
                    .request()
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for proper id.
            assertThat(getLoggingFilterLogRecord(getLoggedRecords()).get(1).getMessage(), containsString(ENTITY));
        }

        @Test
        public void testVerbosityAnyPayloadSetVerbosityAsText() throws Exception {
            final Response response = target()
                    .register(new LoggingFeature(Logger.getLogger(LOGGER_NAME)))
                    .property(LoggingFeature.LOGGING_FEATURE_VERBOSITY, "PAYLOAD_ANY")
                    .request()
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for proper id.
            assertThat(getLoggingFilterLogRecord(getLoggedRecords()).get(1).getMessage(), containsString(ENTITY));
        }

        @Test
        public void testVerbosityTextPayloadBinaryFiltered() throws Exception {
            final Response response = target()
                    .register(new LoggingFeature(Logger.getLogger(LOGGER_NAME), LoggingFeature.Verbosity.PAYLOAD_TEXT))
                    .request()
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for proper id.
            assertThat(getLoggingFilterLogRecord(getLoggedRecords()).get(1).getMessage(), not(containsString(ENTITY)));
        }

        @Test
        public void testVerbosityTextPayload() throws Exception {
            final Response response = target("/text")
                    .register(new LoggingFeature(Logger.getLogger(LOGGER_NAME), LoggingFeature.Verbosity.PAYLOAD_TEXT))
                    .request()
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for proper id.
            assertThat(getLoggingFilterLogRecord(getLoggedRecords()).get(1).getMessage(), containsString(ENTITY));
        }

        @Test
        public void testVerbosityHeadersPayload() throws Exception {
            final Response response = target()
                    .register(new LoggingFeature(Logger.getLogger(LOGGER_NAME), LoggingFeature.Verbosity.HEADERS_ONLY))
                    .request()
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for proper id.
            assertThat(getLoggingFilterLogRecord(getLoggedRecords()).get(1).getMessage(), not(containsString(ENTITY)));
        }

        @Test
        public void testPostedEntityLogged() throws Exception {
            final Response response = target("/text")
                    .register(new LoggingFeature(Logger.getLogger(LOGGER_NAME), LoggingFeature.Verbosity.PAYLOAD_TEXT))
                    .request()
                    .post(Entity.text(ENTITY));

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for proper id.
            assertThat(getLoggingFilterLogRecord(getLoggedRecords()).get(0).getMessage(), containsString(ENTITY));
        }

        @Test
        public void testLoggingFeatureBuilderSeparator() {
            final Response response = target("/text")
                    .register(LoggingFeature
                            .builder()
                            .withLogger(Logger.getLogger(LOGGER_NAME))
                            .verbosity(LoggingFeature.Verbosity.PAYLOAD_ANY)
                            .separator(SEPARATOR)
                            .build()
                    ).request()
                    .post(Entity.text(ENTITY));

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for proper separator.
            final LogRecord record = getLoggingFilterResponseLogRecord(getLoggedRecords());
            assertThat(record.getMessage(), containsString(SEPARATOR));

        }

        @Test
        public void testLoggingFeatureBuilderProperty() {
            final Response response = target("/text")
                    .register(LoggingFeature.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LOGGER_NAME)
                    .property(LoggingFeature.LOGGING_FEATURE_SEPARATOR, SEPARATOR)
                    .request()
                    .post(Entity.text(ENTITY));

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for proper separator.
            final LogRecord record = getLoggingFilterResponseLogRecord(getLoggedRecords());
            assertThat(record.getMessage(), containsString(SEPARATOR));
        }

        @Test
        public void testLoggingFeatureMaxEntitySize() {
            final Response response = target("/text")
                    .register(LoggingFeature.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LOGGER_NAME)
                    .property(LoggingFeature.LOGGING_FEATURE_MAX_ENTITY_SIZE, 1)
                    .request()
                    .post(Entity.text(ENTITY));

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for trimmedEntity.
            String trimmedEntity = ENTITY.charAt(0) + "...more...";
            List<LogRecord> logRecords = getLoggedRecords();
            assertThat(getLoggingFilterRequestLogRecord(logRecords).getMessage(), containsString(trimmedEntity));
            assertThat(getLoggingFilterResponseLogRecord(logRecords).getMessage(), containsString(trimmedEntity));
        }

        @Test
        public void testSingleValuedHeader() {
            String headerName = "X-Single-Valued-Header";
            String headerValue = "test-value";
            final Response response = target("/echo-headers")
                    .register(LoggingFeature.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LOGGER_NAME)
                    .request()
                    .header(headerName, headerValue)
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for header
            List<LogRecord> logRecords = getLoggedRecords();
            Matcher<String> matcher = new ContainsHeaderMatcher(headerName, headerValue);
            assertThat(getLoggingFilterRequestLogRecord(logRecords).getMessage().toLowerCase(), matcher);
            assertThat(getLoggingFilterResponseLogRecord(logRecords).getMessage().toLowerCase(), matcher);
        }

        @Test
        public void testMultivaluedHeader() {
            String headerName = "X-Multi-Valued-Header";
            String firstHeaderValue = "first-value";
            String secondHeaderValue = "second-value";
            final Response response = target("/echo-headers")
                    .register(LoggingFeature.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LOGGER_NAME)
                    .request()
                    .header(headerName, firstHeaderValue)
                    .header(headerName, secondHeaderValue)
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for header
            List<LogRecord> logRecords = getLoggedRecords();
            Matcher<String> matcher = new ContainsHeaderMatcher(headerName, firstHeaderValue, secondHeaderValue);
            assertThat(getLoggingFilterRequestLogRecord(logRecords).getMessage().toLowerCase(), matcher);
            assertThat(getLoggingFilterResponseLogRecord(logRecords).getMessage().toLowerCase(), matcher);
        }

        @Test
        public void testMultipleHeaders() {
            String firstHeaderName = "X-First-Header";
            String firstHeaderValue = "first-value";
            String secondHeaderName = "X-Second-Header";
            String secondHeaderValue = "second-value";
            final Response response = target("/echo-headers")
                    .register(LoggingFeature.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LOGGER_NAME)
                    .request()
                    .header(firstHeaderName, firstHeaderValue)
                    .header(secondHeaderName, secondHeaderValue)
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for header
            List<LogRecord> logRecords = getLoggedRecords();
            Matcher<String> matcher = allOf(
                    new ContainsHeaderMatcher(firstHeaderName, firstHeaderValue),
                    new ContainsHeaderMatcher(secondHeaderName, secondHeaderValue));
            assertThat(getLoggingFilterRequestLogRecord(logRecords).getMessage().toLowerCase(), matcher);
            assertThat(getLoggingFilterResponseLogRecord(logRecords).getMessage().toLowerCase(), matcher);
        }

        @Test(expected = AssertionError.class)
        public void testAuthorizationHeaderRedactedByDefault() {
            String headerName = HttpHeaders.AUTHORIZATION;
            String headerValue = "username:password";
            final Response response = target("/echo-headers")
                    .register(LoggingFeature.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LOGGER_NAME)
                    .request()
                    .header(headerName, headerValue)
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for header
            List<LogRecord> logRecords = getLoggedRecords();
            Matcher<String> matcher = allOf(
                    new ContainsHeaderMatcher(headerName, "[redacted]"),
                    not(containsString(headerValue)));
            assertThat(getLoggingFilterRequestLogRecord(logRecords).getMessage().toLowerCase(), matcher);
            assertThat(getLoggingFilterResponseLogRecord(logRecords).getMessage().toLowerCase(), matcher);
        }

        @Test(expected = AssertionError.class)
        public void testLoggingFeatureRedactOneHeader() {
            String headerName = "X-Redact-This-Header";
            String headerValue = "sensitive-info";
            final Response response = target("/echo-headers")
                    .register(LoggingFeature.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LOGGER_NAME)
                    .property("LOGGING_FEATURE_REDACT_HEADERS", headerName)
                    .request()
                    .header(headerName, headerValue)
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for header
            List<LogRecord> logRecords = getLoggedRecords();
            Matcher<String> matcher = allOf(
                    new ContainsHeaderMatcher(headerName, "[redacted]"),
                    not(containsString(headerValue)));
            assertThat(getLoggingFilterRequestLogRecord(logRecords).getMessage().toLowerCase(), matcher);
            assertThat(getLoggingFilterResponseLogRecord(logRecords).getMessage().toLowerCase(), matcher);
        }

        @Test(expected = AssertionError.class)
        public void testLoggingFeatureRedactOneHeaderNormalizing() {
            String headerName = "X-Redact-This-Header";
            String headerValue = "sensitive-info";
            final Response response = target("/echo-headers")
                    .register(LoggingFeature.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LOGGER_NAME)
                    .property("LOGGING_FEATURE_REDACT_HEADERS", " " + headerName.toUpperCase(Locale.ROOT) + " ")
                    .request()
                    .header(headerName, headerValue)
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for header
            List<LogRecord> logRecords = getLoggedRecords();
            Matcher<String> matcher = allOf(
                    new ContainsHeaderMatcher(headerName, "[redacted]"),
                    not(containsString(headerValue)));
            assertThat(getLoggingFilterRequestLogRecord(logRecords).getMessage().toLowerCase(), matcher);
            assertThat(getLoggingFilterResponseLogRecord(logRecords).getMessage().toLowerCase(), matcher);
        }

        @Test(expected = AssertionError.class)
        public void testLoggingFeatureRedactMultivaluedHeader() {
            String headerName = "X-Redact-This-Header";
            String firstHeaderValue = "sensitive-info";
            String secondHeaderValue = "additional-info";
            final Response response = target("/echo-headers")
                    .register(LoggingFeature.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LOGGER_NAME)
                    .property("LOGGING_FEATURE_REDACT_HEADERS", headerName)
                    .request()
                    .header(headerName, firstHeaderValue)
                    .header(headerName, secondHeaderValue)
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for header
            List<LogRecord> logRecords = getLoggedRecords();
            Matcher<String> matcher = allOf(
                    new ContainsHeaderMatcher(headerName, "[redacted]"),
                    not(containsString(firstHeaderValue)),
                    not(containsString(secondHeaderValue)));
            assertThat(getLoggingFilterRequestLogRecord(logRecords).getMessage().toLowerCase(), matcher);
            assertThat(getLoggingFilterResponseLogRecord(logRecords).getMessage().toLowerCase(), matcher);
        }

        @Test(expected = AssertionError.class)
        public void testLoggingFeatureRedactMultipleHeaders() {
            String firstHeaderName = "X-Redact-This-Header";
            String firstHeaderValue = "sensitive-info";
            String secondHeaderName = "X-Also-Redact-This-Header";
            String secondHeaderValue = "additional-info";
            final Response response = target("/echo-headers")
                    .register(LoggingFeature.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LOGGER_NAME)
                    .property("LOGGING_FEATURE_REDACT_HEADERS", firstHeaderName + "," + secondHeaderName)
                    .request()
                    .header(firstHeaderName, firstHeaderValue)
                    .header(secondHeaderName, secondHeaderValue)
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for header
            List<LogRecord> logRecords = getLoggedRecords();
            Matcher<String> matcher = allOf(
                    new ContainsHeaderMatcher(firstHeaderName, "[redacted]"),
                    not(containsString(firstHeaderValue)),
                    new ContainsHeaderMatcher(secondHeaderName, "[redacted]"),
                    not(containsString(secondHeaderValue)));
            assertThat(getLoggingFilterRequestLogRecord(logRecords).getMessage().toLowerCase(), matcher);
            assertThat(getLoggingFilterResponseLogRecord(logRecords).getMessage().toLowerCase(), matcher);
        }

        @Test
        public void testLoggingFeatureRedactZeroHeaders() {
            String headerName = HttpHeaders.AUTHORIZATION;
            String headerValue = "username:password";
            final Response response = target("/echo-headers")
                    .register(LoggingFeature.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LOGGER_NAME)
                    .property("LOGGING_FEATURE_REDACT_HEADERS", "")
                    .request()
                    .header(headerName, headerValue)
                    .get();

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for header
            List<LogRecord> logRecords = getLoggedRecords();
            Matcher<String> matcher = allOf(
                    new ContainsHeaderMatcher(headerName, headerValue),
                    not(containsString("[redacted]")));
            assertThat(getLoggingFilterRequestLogRecord(logRecords).getMessage().toLowerCase(), matcher);
            assertThat(getLoggingFilterResponseLogRecord(logRecords).getMessage().toLowerCase(), matcher);
        }
    }

    /**
     * General client side tests.
     */
    public static class ContainerTest extends JerseyTest {

        @Override
        protected Application configure() {
            set(TestProperties.RECORD_LOG_LEVEL, Level.FINE.intValue());

            return new ResourceConfig(MyResource.class)
                    .register(LoggingFeature.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME_SERVER, LOGGER_NAME);
        }

        @Test
        public void testLoggingAsContainer() throws Exception {
            // Correct response status.
            assertThat(target().request().get().getStatus(), is(Response.Status.OK.getStatusCode()));

            // Check logs for proper id.
            assertThat(getLoggingFilterRequestLogRecord(getLoggedRecords()).getMessage(), containsString("1 *"));
            assertThat(getLoggingFilterResponseLogRecord(getLoggedRecords()).getMessage(), containsString("1 *"));
        }

        @Test
        public void testLoggingAsContainerTextPayload() throws Exception {
            // Correct response status.
            assertThat(target("/text").request().get().getStatus(), is(Response.Status.OK.getStatusCode()));

            // Check logs for proper id.
            assertThat(getLoggingFilterRequestLogRecord(getLoggedRecords()).getMessage(), containsString("1 *"));
            assertThat(getLoggingFilterResponseLogRecord(getLoggedRecords()).getMessage(), containsString("1 *"));
            assertThat(getLoggingFilterLogRecord(getLoggedRecords()).get(1).getMessage(), containsString(ENTITY));
        }

        @Test
        public void testLoggingAsContainerBinaryPayload() throws Exception {
            // Correct response status.
            assertThat(target().request().get().getStatus(), is(Response.Status.OK.getStatusCode()));

            // Check logs for proper id.
            assertThat(getLoggingFilterRequestLogRecord(getLoggedRecords()).getMessage(), containsString("1 *"));
            assertThat(getLoggingFilterResponseLogRecord(getLoggedRecords()).getMessage(), containsString("1 *"));
            assertThat(getLoggingFilterLogRecord(getLoggedRecords()).get(1).getMessage(), not(containsString(ENTITY)));
        }

        @Test
        public void testPostedEntityLogged() throws Exception {
            final Response response = target("/text")
                    .request()
                    .post(Entity.text(ENTITY));

            // Correct response status.
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            // Check logs for proper id.
            assertThat(getLoggingFilterLogRecord(getLoggedRecords()).get(0).getMessage(), containsString(ENTITY));

        }
    }

    /**
     * General client side tests.
     */
    public static class ContainerAutodiscoveryTest extends JerseyTest {

        @Override
        protected Application configure() {
            set(TestProperties.RECORD_LOG_LEVEL, Level.INFO.intValue());

            return new ResourceConfig(MyResource.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME_SERVER, LOGGER_NAME)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_SERVER, "INFO");
        }

        @Test
        public void testFilterAsContainerFilter() throws Exception {
            // Correct response status.
            assertThat(target().request().get().getStatus(), is(Response.Status.OK.getStatusCode()));

            // Check logs for proper id.
            assertThat(getLoggingFilterRequestLogRecord(getLoggedRecords()).getMessage(), containsString("1 *"));
            assertThat(getLoggingFilterResponseLogRecord(getLoggedRecords()).getMessage(), containsString("1 *"));
        }
    }

    private static LogRecord getLoggingFilterRequestLogRecord(final List<LogRecord> records) {
        return getLoggingFilterLogRecord(records, true);
    }

    private static LogRecord getLoggingFilterResponseLogRecord(final List<LogRecord> records) {
        return getLoggingFilterLogRecord(records, false);
    }

    private static LogRecord getLoggingFilterLogRecord(final List<LogRecord> records, final boolean requestQuery) {
        for (final LogRecord record : getLoggingFilterLogRecord(records)) {
            if (record.getMessage().contains(requestQuery ? "request" : "response")) {
                return record;
            }
        }

        throw new AssertionError("Unable to find proper log record.");
    }

    private static List<LogRecord> getLoggingFilterLogRecord(final List<LogRecord> records) {
        final List<LogRecord> loggingFilterRecords = new ArrayList<>(records.size());

        for (final LogRecord record : records) {
            if (record.getLoggerName().startsWith(LOGGER_NAME)) {
                loggingFilterRecords.add(record);
            }
        }

        return loggingFilterRecords;
    }

    public static class FiltersOrderTest extends JerseyTest {

        @Priority(1000)
        private static class CustomFilter implements ClientRequestFilter, ClientResponseFilter,
                                                     ContainerRequestFilter, ContainerResponseFilter {

            static final String CUSTOM_HEADER = "custom_header";

            @Override
            public void filter(final ClientRequestContext requestContext) throws IOException {
                requestContext.getHeaders().add(CUSTOM_HEADER, "client/request");
            }

            @Override
            public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext)
                    throws IOException {
                responseContext.getHeaders().add(CUSTOM_HEADER, "client/response");
            }

            @Override
            public void filter(final ContainerRequestContext requestContext) throws IOException {
                requestContext.getHeaders().add(CUSTOM_HEADER, "container/request");
            }

            @Override
            public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
                    throws IOException {
                responseContext.getHeaders().add(CUSTOM_HEADER, "container/response");
            }
        }

        @Override
        protected Application configure() {
            set(TestProperties.RECORD_LOG_LEVEL, Level.INFO.intValue());

            return new ResourceConfig(MyResource.class)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LOGGER_NAME)
                    .property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL, "INFO")
                    .property(ServerProperties.WADL_FEATURE_DISABLE, true)
                    .register(CustomFilter.class);
        }

        @Test
        public void testFilterAsContainerFilter() throws Exception {
            // Correct response status.
            assertThat(target()
                    .register(CustomFilter.class)
                    .register(new LoggingFeature(Logger.getLogger(LOGGER_NAME),
                            Level.INFO,
                            LoggingFeature.Verbosity.HEADERS_ONLY,
                            0))
                    .request().get().getStatus(), is(Response.Status.OK.getStatusCode()));

            for (LogRecord record : getLoggedRecords()) {
                System.out.println(record.getMessage());
            }

            // --- client request log entry
            // client added header before request has sent (and logged)
            assertThat(getLoggedRecords().get(0).getMessage(),
                    containsString("1 > custom_header: client/request\n"));


            // --- container request log entry
            // container receives header from client request
            assertThat(getLoggedRecords().get(1).getMessage(),
                    containsString("1 > custom_header: client/request\n"));
            // container has added its own header after logging filter logged message
            assertThat(getLoggedRecords().get(1).getMessage(),
                    not(containsString("1 > custom_header: container/request\n")));


            // --- container response log entry
            // container added header to the response and it was logged
            assertThat(getLoggedRecords().get(2).getMessage(),
                    containsString("1 < custom_header: container/response\n"));

            // --- client response log entry
            // client received header
            assertThat(getLoggedRecords().get(3).getMessage(),
                    containsString("1 < custom_header: container/response\n"));
            assertThat(getLoggedRecords().get(3).getMessage(),
                    not(containsString("1 < custom_header: client/response\n")));

        }

    }

    private static final class ContainsHeaderMatcher extends SubstringMatcher {

        private static final boolean ORDER_OF_HEADER_VALUES_IS_GUARANTEED = true;

        ContainsHeaderMatcher(String headerName, String... headerValues) {
            super(makeRegex(headerName, Arrays.asList(headerValues)));
        }

        private static String makeRegex(String headerName, List<String> headerValues) {
            StringBuilder stringBuilder = new StringBuilder("^[\\s\\S]*")
                    // Header name is case insensitive
                    .append("(?i)").append(quote(headerName)).append("(?-i): ");

            if (headerValues.size() == 1) {
                stringBuilder.append(quote(headerValues.get(0)));
            } else if (headerValues.size() > 1) {
                if (ORDER_OF_HEADER_VALUES_IS_GUARANTEED) {
                    stringBuilder.append(String.join(",", headerValues));
                } else {
                    headerValues.forEach(headerValue -> stringBuilder
                            .append("(?=.*").append(quote(headerValue)).append(",?)"));
                }
            }

            return stringBuilder.append("[\\s\\S]*$").toString();
        }

        private static String quote(String input) {
            return Pattern.quote(input);
        }

        @Override
        protected boolean evalSubstringOf(String string) {
            return string.matches(substring);
        }

        @Override
        protected String relationship() {
            return "matching regex";
        }
    }
}
