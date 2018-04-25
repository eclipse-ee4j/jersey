/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector.internal;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A stream that invokes {@link FirstCallListener} when any operation is invoked.
 * {@link FirstCallListener} is invoked only once in the stream lifetime.
 *
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class InterceptingOutputStream extends OutputStream {

    private final OutputStream wrappedStream;
    private final FirstCallListener firstCallListener;
    private volatile boolean listenerInvoked = false;

    InterceptingOutputStream(OutputStream wrappedStream, FirstCallListener firstCallListener) {
        this.wrappedStream = wrappedStream;
        this.firstCallListener = firstCallListener;
    }

    @Override
    public void write(byte[] b) throws IOException {
        tryInvokingListener();
        wrappedStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        tryInvokingListener();
        wrappedStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        tryInvokingListener();
        wrappedStream.flush();
    }

    @Override
    public void close() throws IOException {
        tryInvokingListener();
        wrappedStream.close();
    }

    @Override
    public void write(int b) throws IOException {
        tryInvokingListener();
        wrappedStream.write(b);
    }

    private void tryInvokingListener() {
        if (!listenerInvoked) {
            listenerInvoked = true;
            this.firstCallListener.onInvoked();
        }
    }

    interface FirstCallListener {

        void onInvoked();
    }
}
