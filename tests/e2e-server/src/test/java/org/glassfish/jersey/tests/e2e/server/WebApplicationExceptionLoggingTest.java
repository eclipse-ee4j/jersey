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

package org.glassfish.jersey.tests.e2e.server;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests that {@link WebApplicationException} is logged on the correct level.
 *
 * @author Miroslav Fuksa
 */
public class WebApplicationExceptionLoggingTest extends JerseyTest {

    @Path("/test")
    public static class StatusResource {

        @GET
        @Produces("text/plain")
        public String test(@NotNull @QueryParam("id") final String id) {
            return "ok";
        }

        @GET
        @Path("WAE-no-entity")
        @Produces("text/plain")
        public String testWithoutEntity() {
            throw new WebApplicationException("WAE without entity", Response.status(400).build());
        }

        @GET
        @Path("WAE-entity")
        @Produces("text/plain")
        public String testWithEntity() {
            throw new WebApplicationException("WAE with entity", Response.status(400).entity("WAE with entity").build());
        }
    }

    @Provider
    public static class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

        @Override
        public Response toResponse(final ValidationException ex) {
            return Response.status(200).entity("Error mapped: " + ex.toString()).type("text/plain").build();
        }
    }

    @Override
    protected Application configure() {
        set(TestProperties.RECORD_LOG_LEVEL, Level.FINER.intValue());

        return new ResourceConfig(StatusResource.class, ValidationExceptionMapper.class);
    }

    private LogRecord getLogRecord(final String messagePrefix) {
        for (final LogRecord logRecord : getLoggedRecords()) {
            if (logRecord.getMessage() != null && logRecord.getMessage().startsWith(messagePrefix)) {
                return logRecord;
            }
        }
        return null;
    }

    @Test
    public void testValidationException() {
        final Response response = target().path("test").request().get();
        assertEquals(200, response.getStatus());

        final String entity = response.readEntity(String.class);
        assertTrue(entity.startsWith("Error mapped:"));

        // check logs
        final LogRecord logRecord = this.getLogRecord("Starting mapping of the exception");
        assertNotNull(logRecord);
        assertEquals(Level.FINER, logRecord.getLevel());

        // check that there is no exception logged on the level higher than FINE
        for (final LogRecord record : getLoggedRecords()) {
            if (record.getThrown() != null) {
                assertTrue(record.getLevel().intValue() <= Level.FINE.intValue());
            }
        }

    }

    @Test
    public void testWAEWithEntity() {
        final Response response = target().path("test/WAE-entity").request().get();
        assertEquals(400, response.getStatus());
        final String entity = response.readEntity(String.class);
        assertEquals("WAE with entity", entity);

        // check logs
        LogRecord logRecord = this.getLogRecord("Starting mapping of the exception");
        assertNotNull(logRecord);
        assertEquals(Level.FINER, logRecord.getLevel());

        logRecord = this.getLogRecord("WebApplicationException (WAE) with non-null entity thrown.");
        assertNotNull(logRecord);
        assertEquals(Level.FINE, logRecord.getLevel());
        assertTrue(logRecord.getThrown() instanceof WebApplicationException);
        logRecord.getThrown().printStackTrace();
    }

    @Test
    public void testWAEWithoutEntity() {
        final Response response = target().path("test/WAE-no-entity").request().get();
        assertEquals(400, response.getStatus());
        assertFalse(response.hasEntity());

        // check logs
        LogRecord logRecord = this.getLogRecord("Starting mapping of the exception");
        assertNotNull(logRecord);
        assertEquals(Level.FINER, logRecord.getLevel());

        logRecord = this.getLogRecord("WebApplicationException (WAE) with no entity thrown and no");
        assertNotNull(logRecord);
        assertEquals(Level.FINE, logRecord.getLevel());
        assertTrue(logRecord.getThrown() instanceof WebApplicationException);
        logRecord.getThrown().printStackTrace();
    }
}
