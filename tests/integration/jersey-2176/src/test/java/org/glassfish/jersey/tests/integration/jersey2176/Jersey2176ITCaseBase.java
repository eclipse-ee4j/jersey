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

package org.glassfish.jersey.tests.integration.jersey2176;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Reproducer tests for JERSEY-2176.
 *
 * @author Libor Kramolis
 */
public abstract class Jersey2176ITCaseBase extends JerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testGetContent222() {
        testGetContent(222, true);
    }

    @Test
    public void testGetContent333() {
        testGetContent(333, true);
    }

    @Test
    public void testGetContent444() {
        testGetContent(444, true);
    }

    @Test
    public void testGetContent555() {
        testGetContent(555, true);
    }

    @Test
    public void testGetContent222NoResponseEntity() {
        testGetContent(222, false);
    }

    @Test
    public void testGetContent333NoResponseEntity() {
        testGetContent(333, false);
    }

    @Test
    public void testGetContent444NoResponseEntity() {
        testGetContent(444, false);
    }

    @Test
    public void testGetContent555NoResponseEntity() {
        testGetContent(555, false);
    }

    @Test
    public void testGetException_1() {
        testGetException(-1, 500, false);
    }

    @Test
    public void testGetException_2() {
        testGetException(-2, 500, false);
    }

    @Test
    public void testGetException_3() {
        testGetException(-3, 321, false);
    }

    @Test
    public void testGetException_4() {
        testGetException(-4, 432, false);
    }

    @Test
    public void testGetException222() {
        testGetException(222, 500, true);
    }

    private void testGetContent(int uc, boolean responseEntity) {
        String expectedContent = "ENTITY";
        expectedContent = "[INTERCEPTOR]" + expectedContent + "[/INTERCEPTOR]";
        expectedContent = "[FILTER]" + expectedContent + "[/FILTER]";

        Invocation.Builder builder = target().path("/resource/" + uc).request();
        if (responseEntity) {
            builder.header(Issue2176ReproducerResource.X_RESPONSE_ENTITY_HEADER, true);
        } else {
            builder.header(TraceResponseFilter.X_NO_FILTER_HEADER, true);
        }

        final Response response = builder.get();
        final String assertMessage = uc + "|" + responseEntity;

        Assertions.assertEquals(uc, response.getStatus(), assertMessage);
        if (!sendErrorExpected(uc, responseEntity)) {
            Assertions.assertEquals("OK", response.getHeaderString(TraceResponseFilter.X_STATUS_HEADER), assertMessage);
            Assertions.assertNotNull(response.getHeaderString(TraceResponseFilter.X_SERVER_DURATION_HEADER), assertMessage);
            if (responseEntity) {
                Assertions.assertEquals(expectedContent, response.readEntity(String.class), assertMessage);
                Assertions.assertEquals(String.valueOf(expectedContent.length()),
                        response.getHeaderString(HttpHeaders.CONTENT_LENGTH), assertMessage);
            }
        } else {
            Assertions.assertNull(response.getHeaderString(TraceResponseFilter.X_STATUS_HEADER), assertMessage);
            Assertions.assertNull(response.getHeaderString(TraceResponseFilter.X_SERVER_DURATION_HEADER), assertMessage);
        }
    }

    private void testGetException(int uc, int expectedStatus, boolean fail) {
        Invocation.Builder builder = target().path("/resource/" + uc).request();
        builder = builder.header(Issue2176ReproducerResource.X_RESPONSE_ENTITY_HEADER, true);
        if (fail) {
            builder = builder.header(Issue2176ReproducerResource.X_FAIL_HEADER, true);
        }

        final Response response = builder.get();

        final String expectedContent = "[FILTER][/FILTER]";
        final String assertMessage = uc + ":" + expectedStatus + "|" + fail;

        Assertions.assertEquals(expectedStatus, response.getStatus(), assertMessage);
        if (!sendErrorExpected(expectedStatus, false)) {
            Assertions.assertEquals(expectedStatus == 500 ? "FAIL" : "OK",
                    response.getHeaderString(TraceResponseFilter.X_STATUS_HEADER), assertMessage);
            Assertions.assertNotNull(response.getHeaderString(TraceResponseFilter.X_SERVER_DURATION_HEADER), assertMessage);
            Assertions.assertEquals(String.valueOf(expectedContent.length()),
                    response.getHeaderString(HttpHeaders.CONTENT_LENGTH), assertMessage);
        } else {
            Assertions.assertNull(response.getHeaderString(TraceResponseFilter.X_STATUS_HEADER), assertMessage);
            Assertions.assertNull(response.getHeaderString(TraceResponseFilter.X_SERVER_DURATION_HEADER), assertMessage);
        }
    }

    private boolean sendErrorExpected(final int uc, final boolean responseEntity) {
        return !((Jersey2176App) configure()).isSetStatusOverSendError() && (uc >= 400) && !responseEntity;
    }

}
