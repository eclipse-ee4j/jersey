/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.sse.internal;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseEventSink;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * {@link javax.ws.rs.sse.SseBroadcaster} test.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class JerseySseBroadcasterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String TEST_EXCEPTION_MSG = "testException";

    @Test
    public void testOnErrorNull() {
        try (JerseySseBroadcaster broadcaster = new JerseySseBroadcaster()) {

            thrown.expect(IllegalArgumentException.class);
            broadcaster.onError(null);
        }
    }

    @Test
    public void testOnCloseNull() {
        try (JerseySseBroadcaster jerseySseBroadcaster = new JerseySseBroadcaster()) {

            thrown.expect(IllegalArgumentException.class);
            jerseySseBroadcaster.onClose(null);
        }
    }

    @Test
    public void testOnErrorFromOnNext() throws InterruptedException {
        try (JerseySseBroadcaster broadcaster = new JerseySseBroadcaster()) {

            final CountDownLatch latch = new CountDownLatch(1);


            broadcaster.onError((subscriber, throwable) -> {
                if (TEST_EXCEPTION_MSG.equals(throwable.getMessage())) {
                    latch.countDown();
                }
            });

            broadcaster.register(new SseEventSink() {
                @Override
                public boolean isClosed() {
                    return false;
                }

                @Override
                public CompletionStage<?> send(OutboundSseEvent event) {
                    throw new RuntimeException(TEST_EXCEPTION_MSG);
                }

                @Override
                public void close() {

                }
            });

            broadcaster.broadcast(new JerseySse().newEvent("ping"));
            Assert.assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
        }
    }

    @Test
    public void testOnClose() throws InterruptedException {
        try (JerseySseBroadcaster broadcaster = new JerseySseBroadcaster()) {

            final CountDownLatch latch = new CountDownLatch(1);

            final SseEventSink eventSink = new SseEventSink() {
                @Override
                public boolean isClosed() {
                    return false;
                }

                @Override
                public CompletionStage<?> send(OutboundSseEvent event) {
                    return null;
                }

                @Override
                public void close() {

                }
            };
            broadcaster.register(eventSink);

            broadcaster.onClose((s) -> {
                if (s.equals(eventSink)) {
                    latch.countDown();
                }
            });

            broadcaster.close();
            Assert.assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));
        }
    }

}
