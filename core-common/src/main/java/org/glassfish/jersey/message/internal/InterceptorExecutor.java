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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.InterceptorContext;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.internal.PropertiesDelegate;

/**
 * Abstract class with implementation of {@link InterceptorContext} which is common for {@link ReaderInterceptorContext}
 * and {@link WriterInterceptorContext} implementations.
 *
 * @author Miroslav Fuksa
 */
abstract class InterceptorExecutor<T> implements InterceptorContext, PropertiesDelegate {

    private final PropertiesDelegate propertiesDelegate;
    private Annotation[] annotations;
    private Class<?> type;
    private Type genericType;
    private MediaType mediaType;

    private final TracingLogger tracingLogger;
    private InterceptorTimestampPair<T> lastTracedInterceptor;

    /**
     * Holder of interceptor instance and timestamp of the interceptor invocation (in ns).
     */
    private static class InterceptorTimestampPair<T> {

        private final T interceptor;
        private final long timestamp;

        private InterceptorTimestampPair(final T interceptor, final long timestamp) {
            this.interceptor = interceptor;
            this.timestamp = timestamp;
        }

        private T getInterceptor() {
            return interceptor;
        }

        private long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Constructor initializes common properties of this abstract class.
     *
     * @param rawType            raw Java entity type.
     * @param type               generic Java entity type.
     * @param annotations        Annotations on the formal declaration of the resource
     *                           method parameter that is the target of the message body
     *                           conversion. See {@link InterceptorContext#getAnnotations()}.
     * @param mediaType          MediaType of HTTP entity. See {@link InterceptorContext#getMediaType()}.
     * @param propertiesDelegate request-scoped properties delegate.
     */
    public InterceptorExecutor(final Class<?> rawType, final Type type, final Annotation[] annotations, final MediaType mediaType,
                               final PropertiesDelegate propertiesDelegate) {
        super();
        this.type = rawType;
        this.genericType = type;
        this.annotations = annotations;
        this.mediaType = mediaType;
        this.propertiesDelegate = propertiesDelegate;
        this.tracingLogger = TracingLogger.getInstance(propertiesDelegate);
    }

    @Override
    public Object getProperty(final String name) {
        return propertiesDelegate.getProperty(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return propertiesDelegate.getPropertyNames();
    }

    @Override
    public void setProperty(final String name, final Object object) {
        propertiesDelegate.setProperty(name, object);
    }

    @Override
    public void removeProperty(final String name) {
        propertiesDelegate.removeProperty(name);
    }

    /**
     * Get tracing logger instance configured in via properties.
     *
     * @return tracing logger instance.
     */
    protected final TracingLogger getTracingLogger() {
        return tracingLogger;
    }

    /**
     * Tracing support - log invocation of interceptor BEFORE context.proceed() call.
     *
     * @param interceptor invoked interceptor
     * @param event       event type to be tested
     */
    protected final void traceBefore(final T interceptor, final TracingLogger.Event event) {
        if (tracingLogger.isLogEnabled(event)) {
            if ((lastTracedInterceptor != null) && (interceptor != null)) {
                tracingLogger.logDuration(event, lastTracedInterceptor.getTimestamp(), lastTracedInterceptor.getInterceptor());
            }
            lastTracedInterceptor = new InterceptorTimestampPair<T>(interceptor, System.nanoTime());
        }
    }

    /**
     * Tracing support - log invocation of interceptor AFTER context.proceed() call.
     *
     * @param interceptor invoked interceptor
     * @param event       event type to be tested
     */
    protected final void traceAfter(final T interceptor, final TracingLogger.Event event) {
        if (tracingLogger.isLogEnabled(event)) {
            if ((lastTracedInterceptor != null) && (lastTracedInterceptor.getInterceptor() != null)) {
                tracingLogger.logDuration(event, lastTracedInterceptor.getTimestamp(), interceptor);
            }
            lastTracedInterceptor = new InterceptorTimestampPair<T>(interceptor, System.nanoTime());
        }
    }

    /**
     * Clear last traced interceptor information.
     */
    protected final void clearLastTracedInterceptor() {
        lastTracedInterceptor = null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations;
    }

    @Override
    public void setAnnotations(final Annotation[] annotations) {
        if (annotations == null) {
            throw new NullPointerException("Annotations must not be null.");
        }
        this.annotations = annotations;
    }

    @Override
    public Class getType() {
        return this.type;
    }

    @Override
    public void setType(final Class type) {
        this.type = type;
    }

    @Override
    public Type getGenericType() {
        return genericType;
    }

    @Override
    public void setGenericType(final Type genericType) {
        this.genericType = genericType;
    }

    @Override
    public MediaType getMediaType() {
        return mediaType;
    }

    @Override
    public void setMediaType(final MediaType mediaType) {
        this.mediaType = mediaType;
    }

}
