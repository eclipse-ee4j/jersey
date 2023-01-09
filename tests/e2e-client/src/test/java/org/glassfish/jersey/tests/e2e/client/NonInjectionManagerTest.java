/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.jersey.inject.injectless.NonInjectionManagerFactory;

public class NonInjectionManagerTest {
    @Test
    public void testNonInjectionManagerIsUsed() {
        String value = System.getProperty("jersey.injectionmanager.hk2");
        if ("true".equals(value)) {
            return;
        }

        Records records = new Records();
        Logger logger = Logger.getLogger(new NonInjectionManagerFactory().create(null).getClass().getName());
        Level oldLevel = logger.getLevel();
        logger.setLevel(Level.FINEST);
        logger.addHandler(records);

        ClientBuilder.newClient().register((ClientRequestFilter) requestContext -> {
            requestContext.abortWith(Response.ok().build());
        }).target("http://localhost:9998/nevermind").request().get();

        logger.removeHandler(records);
        logger.setLevel(oldLevel);

        LogRecord warning = records.records.get(0);
        Assertions.assertTrue(warning.getMessage().contains("injection-less"));
    }

    private static class Records extends Handler {
        private List<LogRecord> records = new LinkedList<>();
        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
