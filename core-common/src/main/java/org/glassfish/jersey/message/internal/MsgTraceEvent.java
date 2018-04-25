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

package org.glassfish.jersey.message.internal;

/**
 * Common tracing events.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 * @since 2.3
 */
public enum MsgTraceEvent implements TracingLogger.Event {
    /**
     * {@link javax.ws.rs.ext.ReaderInterceptor} invocation before a call to {@code context.proceed()}.
     */
    RI_BEFORE(TracingLogger.Level.TRACE, "RI", "%s BEFORE context.proceed()"),
    /**
     * {@link javax.ws.rs.ext.ReaderInterceptor} invocation after a call to {@code context.proceed()}.
     */
    RI_AFTER(TracingLogger.Level.TRACE, "RI", "%s AFTER context.proceed()"),
    /**
     * {@link javax.ws.rs.ext.ReaderInterceptor} invocation summary.
     */
    RI_SUMMARY(TracingLogger.Level.SUMMARY, "RI", "ReadFrom summary: %s interceptors"),
    /**
     * {@link javax.ws.rs.ext.MessageBodyReader} lookup.
     */
    MBR_FIND(TracingLogger.Level.TRACE, "MBR", "Find MBR for type=[%s] genericType=[%s] mediaType=[%s] annotations=%s"),
    /**
     * {@link javax.ws.rs.ext.MessageBodyReader#isReadable} returned {@code false}.
     */
    MBR_NOT_READABLE(TracingLogger.Level.VERBOSE, "MBR", "%s is NOT readable"),
    /**
     * {@link javax.ws.rs.ext.MessageBodyReader} selected.
     */
    MBR_SELECTED(TracingLogger.Level.TRACE, "MBR", "%s IS readable"),
    /**
     * {@link javax.ws.rs.ext.MessageBodyReader} skipped as higher-priority reader has been selected already.
     */
    MBR_SKIPPED(TracingLogger.Level.VERBOSE, "MBR", "%s is skipped"),
    /**
     * {@link javax.ws.rs.ext.MessageBodyReader#readFrom} invoked.
     */
    MBR_READ_FROM(TracingLogger.Level.TRACE, "MBR", "ReadFrom by %s"),
    /**
     * {@link javax.ws.rs.ext.MessageBodyWriter} lookup.
     */
    MBW_FIND(TracingLogger.Level.TRACE, "MBW", "Find MBW for type=[%s] genericType=[%s] mediaType=[%s] annotations=%s"),
    /**
     * {@link javax.ws.rs.ext.MessageBodyWriter#isWriteable} returned {@code false}.
     */
    MBW_NOT_WRITEABLE(TracingLogger.Level.VERBOSE, "MBW", "%s is NOT writeable"),
    /**
     * {@link javax.ws.rs.ext.MessageBodyWriter#isWriteable} selected.
     */
    MBW_SELECTED(TracingLogger.Level.TRACE, "MBW", "%s IS writeable"),
    /**
     * {@link javax.ws.rs.ext.MessageBodyWriter} skipped as higher-priority writer has been selected already.
     */
    MBW_SKIPPED(TracingLogger.Level.VERBOSE, "MBW", "%s is skipped"),
    /**
     * {@link javax.ws.rs.ext.MessageBodyWriter#writeTo} invoked.
     */
    MBW_WRITE_TO(TracingLogger.Level.TRACE, "MBW", "WriteTo by %s"),
    /**
     * {@link javax.ws.rs.ext.WriterInterceptor} invocation before a call to {@code context.proceed()}.
     */
    WI_BEFORE(TracingLogger.Level.TRACE, "WI", "%s BEFORE context.proceed()"),
    /**
     * {@link javax.ws.rs.ext.WriterInterceptor} invocation after a call to {@code context.proceed()}.
     */
    WI_AFTER(TracingLogger.Level.TRACE, "WI", "%s AFTER context.proceed()"),
    /**
     * {@link javax.ws.rs.ext.ReaderInterceptor} invocation summary.
     */
    WI_SUMMARY(TracingLogger.Level.SUMMARY, "WI", "WriteTo summary: %s interceptors");

    private final TracingLogger.Level level;
    private final String category;
    private final String messageFormat;

    private MsgTraceEvent(TracingLogger.Level level, String category, String messageFormat) {
        this.level = level;
        this.category = category;
        this.messageFormat = messageFormat;
    }

    @Override
    public String category() {
        return category;
    }

    @Override
    public TracingLogger.Level level() {
        return level;
    }

    @Override
    public String messageFormat() {
        return messageFormat;
    }
}
