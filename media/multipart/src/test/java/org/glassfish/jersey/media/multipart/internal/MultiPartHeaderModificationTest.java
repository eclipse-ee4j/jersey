/*
 * Copyright (c) 2014, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.multipart.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestHelper;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JERSEY-2123 reproducer.
 * <p/>
 * Delete this test when JERSEY-2341 fixed.
 *
 * @author Libor Kramolis
 */
public class MultiPartHeaderModificationTest {

    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                {new HttpUrlConnectorProvider(), false},
                {new GrizzlyConnectorProvider(), true},
//                {new JettyConnectorProvider(), true},
                {new ApacheConnectorProvider(), true},
        });
    }

    @TestFactory
    public Collection<DynamicContainer> generateTests() {
        Collection<DynamicContainer> tests = new ArrayList<>();
        testData().forEach(arr -> {
            MultiPartJerseyTemplateTest test = new MultiPartJerseyTemplateTest((ConnectorProvider) arr[0], (boolean) arr[1]) {};
            tests.add(TestHelper.toTestContainer(test, arr[0].getClass().getSimpleName() + ", " + arr[1]));
        });
        return tests;
    }

    public abstract static class MultiPartJerseyTemplateTest extends MultiPartJerseyTest {

        private final ConnectorProvider connectorProvider;
        private final boolean messageLogged;

        public MultiPartJerseyTemplateTest(ConnectorProvider connectorProvider, boolean messageLogged) {
            this.connectorProvider = connectorProvider;
            this.messageLogged = messageLogged;
        }

        @Override
        protected Set<Class<?>> getResourceClasses() {
            final HashSet<Class<?>> classes = new HashSet<Class<?>>();
            classes.add(MultiPartResource.class);
            return classes;
        }

        @Override
        protected Application configure() {
            set(TestProperties.RECORD_LOG_LEVEL, Level.WARNING.intValue());
            return super.configure();
        }

        @Override
        protected void configureClient(ClientConfig clientConfig) {
            super.configureClient(clientConfig);
            clientConfig.connectorProvider(connectorProvider);
        }

        @Test
        public void testLogMessage() {
            final WebTarget target = target().path("multipart/ten");

            MultiPartBean bean = new MultiPartBean("myname", "myvalue");
            MultiPart entity = new MultiPart()
                    .bodyPart(bean, new MediaType("x-application", "x-format"))
                    .bodyPart("", MediaType.APPLICATION_OCTET_STREAM_TYPE);

            final String UNSENT_HEADER_CHANGES = "Unsent header changes";
            try {
                target.request("text/plain").put(Entity.entity(entity, "multipart/mixed"), String.class);
                assertFalse(messageLogged, "BadRequestException can not be thrown just in case JERSEY-2341 is not fixed.");
                LogRecord logRecord = findLogRecord(UNSENT_HEADER_CHANGES);
                assertNull(logRecord);
            } catch (BadRequestException brex) {
                assertTrue(messageLogged,
                        "BadRequestException can be thrown just in case JERSEY-2341 is not fixed.");
                LogRecord logRecord = findLogRecord(UNSENT_HEADER_CHANGES);
                assertNotNull(logRecord, "Missing LogRecord for message '" + UNSENT_HEADER_CHANGES + "'.");
                assertThat(logRecord.getMessage(), containsString("MIME-Version"));
                assertThat(logRecord.getMessage(), containsString("Content-Type"));
            }
        }

        private LogRecord findLogRecord(String messageContains) {
            for (final LogRecord record : getLoggedRecords()) {
                if (record.getMessage().contains(messageContains)) {
                    return record;
                }
            }
            return null;
        }
    }
}
