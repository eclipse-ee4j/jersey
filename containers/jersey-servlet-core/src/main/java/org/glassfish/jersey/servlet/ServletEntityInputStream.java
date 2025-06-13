/*
 * Copyright (c) 2024, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.servlet;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import org.glassfish.jersey.innate.io.StreamListener;
import org.glassfish.jersey.innate.io.StreamListenerCouple;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class ServletEntityInputStream implements StreamListenerCouple {

    private final boolean waitForInputEnable;
    private final long waitForInputTimeOut;

    private final StreamListener listener = new StreamListener() {

        @Override
        public boolean isEmpty() {
            try {
                return getWrappedStream().available() == 0
                        || getWrappedStream().isFinished();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public boolean isReady() {
            return processReadiness();
        }

        boolean processReadiness() {

            final AtomicBoolean ready = new AtomicBoolean(getWrappedStream().isReady());
            if (waitForInputEnable && !ready.get()) {

                final CountDownLatch latch = new CountDownLatch(1);

                getWrappedStream().setReadListener(new ReadListener() {
                    @Override
                    public void onDataAvailable() {
                        ready.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void onAllDataRead() {
                        ready.set(false);
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                        ready.set(false);
                        latch.countDown();
                    }

                });
                if (!ready.get()) {
                    try {
                        latch.await(waitForInputTimeOut, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        ready.set(getWrappedStream().isReady());
                    }
                }
            }
            return ready.get() || getWrappedStream().isReady();
        }

    };

    public ServletEntityInputStream(boolean waitForInputEnable, long waitForInputTimeOut) {
        this.waitForInputEnable = waitForInputEnable;
        this.waitForInputTimeOut = waitForInputTimeOut;
    }

    protected abstract ServletInputStream getWrappedStream();

    @Override
    public StreamListener getListener() {
        return listener;
    }

    @Override
    public InputStream getExternalStream() {
        return getWrappedStream();
    }
}
