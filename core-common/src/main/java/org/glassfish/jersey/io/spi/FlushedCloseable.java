/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.io.spi;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A marker interface that the stream provided to Jersey can implement,
 * noting that the stream does not need to call {@link #flush()} prior to {@link #close()}.
 * That way, {@link #flush()} method is not called twice.
 *
 * <p>
 *     Usable by {@link javax.ws.rs.client.ClientRequestContext#setEntityStream(OutputStream)}.
 *     Usable by {@link javax.ws.rs.container.ContainerResponseContext#setEntityStream(OutputStream)}.
 * </p>
 *
 * <p>
 *     This marker interface can be useful for the customer OutputStream to know the {@code flush} did not come from
 *     Jersey before close. By default, when the entity stream is to be closed by Jersey, {@code flush} is called first.
 * </p>
 */
public interface FlushedCloseable extends Flushable, Closeable {
    /**
     * Flushes this stream by writing any buffered output to the underlying stream.
     * Then closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException;
}
