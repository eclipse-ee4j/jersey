/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerSupplier;
import org.glassfish.jersey.message.MessageBodyWorkers;

/**
 * Represents reader interceptor chain executor for both client and server side.
 * It constructs wrapped interceptor chain and invokes it. At the end of the chain
 * a {@link MessageBodyReader message body reader} execution interceptor is inserted,
 * which finally reads an entity from the output stream provided by the chain.
 *
 * @author Miroslav Fuksa
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public final class ReaderInterceptorExecutor extends InterceptorExecutor<ReaderInterceptor>
        implements ReaderInterceptorContext, InjectionManagerSupplier {

    private static final Logger LOGGER = Logger.getLogger(ReaderInterceptorExecutor.class.getName());

    private final MultivaluedMap<String, String> headers;
    private final Iterator<ReaderInterceptor> interceptors;
    private final MessageBodyWorkers workers;
    private final boolean translateNce;

    private final InjectionManager injectionManager;

    private InputStream inputStream;
    private int processedCount;

    /**
     * Constructs a new executor to read given type from provided {@link InputStream entityStream}.
     *
     * @param rawType            raw Java entity type.
     * @param type               generic Java entity type.
     * @param annotations        array of annotations on the declaration of the artifact
     *                           that will be initialized with the produced instance. E.g. if the message
     *                           body is to be converted into a method parameter, this will be the annotations
     *                           on that parameter returned by {@code Method.getParameterAnnotations}.
     * @param mediaType          media type of the HTTP entity.
     * @param headers            mutable message headers.
     * @param propertiesDelegate request-scoped properties delegate.
     * @param inputStream        entity input stream.
     * @param workers            {@link org.glassfish.jersey.message.MessageBodyWorkers Message body workers}.
     * @param readerInterceptors Reader interceptor that are to be used to intercept the reading of an entity.
     *                           The interceptors will be executed in the same order as given in this parameter.
     * @param translateNce       if {@code true}, the {@link javax.ws.rs.core.NoContentException} thrown by a selected message
     *                           body
     *                           reader will be translated into a {@link javax.ws.rs.BadRequestException} as required by
     * @param injectionManager   injection manager.
     */
    ReaderInterceptorExecutor(final Class<?> rawType, final Type type,
                              final Annotation[] annotations,
                              final MediaType mediaType,
                              final MultivaluedMap<String, String> headers,
                              final PropertiesDelegate propertiesDelegate,
                              final InputStream inputStream,
                              final MessageBodyWorkers workers,
                              final Iterable<ReaderInterceptor> readerInterceptors,
                              final boolean translateNce,
                              final InjectionManager injectionManager) {

        super(rawType, type, annotations, mediaType, propertiesDelegate);
        this.headers = headers;
        this.inputStream = inputStream;
        this.workers = workers;
        this.translateNce = translateNce;
        this.injectionManager = injectionManager;

        final List<ReaderInterceptor> effectiveInterceptors = StreamSupport.stream(readerInterceptors.spliterator(), false)
                                                                           .collect(Collectors.toList());
        effectiveInterceptors.add(new TerminalReaderInterceptor());

        this.interceptors = effectiveInterceptors.iterator();
        this.processedCount = 0;
    }

    /**
     * Starts the interceptor chain execution.
     *
     * @return an entity read from the stream.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object proceed() throws IOException {
        if (!interceptors.hasNext()) {
            throw new ProcessingException(LocalizationMessages.ERROR_INTERCEPTOR_READER_PROCEED());
        }
        final ReaderInterceptor interceptor = interceptors.next();
        traceBefore(interceptor, MsgTraceEvent.RI_BEFORE);
        try {
            return interceptor.aroundReadFrom(this);
        } finally {
            processedCount++;
            traceAfter(interceptor, MsgTraceEvent.RI_AFTER);
        }
    }

    @Override
    public InputStream getInputStream() {
        return this.inputStream;
    }

    @Override
    public void setInputStream(final InputStream is) {
        this.inputStream = is;

    }

    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * Get number of processed interceptors.
     *
     * @return number of processed interceptors.
     */
    int getProcessedCount() {
        return processedCount;
    }

    @Override
    public InjectionManager getInjectionManager() {
        return injectionManager;
    }

    /**
     * Terminal reader interceptor which choose the appropriate {@link MessageBodyReader}
     * and reads the entity from the input stream. The order of actions is the following: <br>
     * 1. choose the appropriate {@link MessageBodyReader} <br>
     * 3. reads the entity from the output stream <br>
     */
    private class TerminalReaderInterceptor implements ReaderInterceptor {

        @Override
        @SuppressWarnings("unchecked")
        public Object aroundReadFrom(final ReaderInterceptorContext context) throws IOException, WebApplicationException {
            processedCount--; //this is not regular interceptor -> count down

            traceBefore(null, MsgTraceEvent.RI_BEFORE);
            try {
                final TracingLogger tracingLogger = getTracingLogger();
                if (tracingLogger.isLogEnabled(MsgTraceEvent.MBR_FIND)) {
                    tracingLogger.log(MsgTraceEvent.MBR_FIND,
                            context.getType().getName(),
                            (context.getGenericType() instanceof Class
                                    ? ((Class) context.getGenericType()).getName() : context.getGenericType()),
                            String.valueOf(context.getMediaType()), java.util.Arrays.toString(context.getAnnotations()));
                }

                final MessageBodyReader bodyReader = workers.getMessageBodyReader(
                        context.getType(),
                        context.getGenericType(),
                        context.getAnnotations(),
                        context.getMediaType(),
                        ReaderInterceptorExecutor.this);

                final EntityInputStream input = new EntityInputStream(context.getInputStream());

                if (bodyReader == null) {
                    if (input.isEmpty() && !context.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE)) {
                        return null;
                    } else {
                        LOGGER.log(Level.FINE, LocalizationMessages.ERROR_NOTFOUND_MESSAGEBODYREADER(context.getMediaType(),
                                context.getType(), context.getGenericType()));
                        throw new MessageBodyProviderNotFoundException(LocalizationMessages.ERROR_NOTFOUND_MESSAGEBODYREADER(
                                context.getMediaType(), context.getType(), context.getGenericType()));
                    }
                }
                Object entity = invokeReadFrom(context, bodyReader, input);

                if (bodyReader instanceof CompletableReader) {
                    entity = ((CompletableReader) bodyReader).complete(entity);
                }
                return entity;
            } finally {
                clearLastTracedInterceptor();
                traceAfter(null, MsgTraceEvent.RI_AFTER);
            }
        }

        @SuppressWarnings("unchecked")
        private Object invokeReadFrom(final ReaderInterceptorContext context, final MessageBodyReader reader,
                                      final EntityInputStream input) throws WebApplicationException, IOException {

            final TracingLogger tracingLogger = getTracingLogger();
            final long timestamp = tracingLogger.timestamp(MsgTraceEvent.MBR_READ_FROM);
            final InputStream stream = new UnCloseableInputStream(input, reader);

            try {
                return reader.readFrom(context.getType(), context.getGenericType(), context.getAnnotations(),
                                       context.getMediaType(), context.getHeaders(), stream);
            } catch (final NoContentException ex) {
                if (translateNce) {
                    throw new BadRequestException(ex);
                } else {
                    throw ex;
                }
            } finally {
                tracingLogger.logDuration(MsgTraceEvent.MBR_READ_FROM, timestamp, reader);
            }
        }
    }

    /**
     * {@link javax.ws.rs.ext.MessageBodyReader}s should not close the given {@link java.io.InputStream stream}. This input
     * stream makes sure that the stream is not closed even if MBR tries to do it.
     */
    private static class UnCloseableInputStream extends InputStream {

        private final InputStream original;
        private final MessageBodyReader reader;

        private UnCloseableInputStream(final InputStream original, final MessageBodyReader reader) {
            this.original = original;
            this.reader = reader;
        }

        @Override
        public int read() throws IOException {
            return original.read();
        }

        @Override
        public int read(final byte[] b) throws IOException {
            return original.read(b);
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            return original.read(b, off, len);
        }

        @Override
        public long skip(final long l) throws IOException {
            return original.skip(l);
        }

        @Override
        public int available() throws IOException {
            return original.available();
        }

        @Override
        public synchronized void mark(final int i) {
            original.mark(i);
        }

        @Override
        public synchronized void reset() throws IOException {
            original.reset();
        }

        @Override
        public boolean markSupported() {
            return original.markSupported();
        }

        @Override
        public void close() throws IOException {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, LocalizationMessages.MBR_TRYING_TO_CLOSE_STREAM(reader.getClass()));
            }
        }

        private InputStream unwrap() {
            return original;
        }
    }

    /**
     * Make the {@link InputStream} able to close.
     * <p/>
     * The purpose of this utility method is to undo effect of {@link ReaderInterceptorExecutor.UnCloseableInputStream}.
     *
     * @param inputStream Potential {@link ReaderInterceptorExecutor.UnCloseableInputStream} to undo its effect
     * @return Input stream that is possible to close
     */
    public static InputStream closeableInputStream(InputStream inputStream) {
        if (inputStream instanceof UnCloseableInputStream) {
            return ((UnCloseableInputStream) inputStream).unwrap();
        } else {
            return inputStream;
        }
    }
}
