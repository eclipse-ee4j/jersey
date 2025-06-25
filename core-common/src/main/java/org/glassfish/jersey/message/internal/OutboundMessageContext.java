/*
 * Copyright (c) 2012, 2025 Oracle and/or its affiliates. All rights reserved.
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
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.RuntimeDelegateDecorator;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.GuardianStringKeyMultivaluedMap;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.io.spi.FlushedCloseable;

/**
 * Base outbound message context implementation.
 *
 * @author Marek Potociar
 */
public class OutboundMessageContext extends MessageHeaderMethods {
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    private static final List<MediaType> WILDCARD_ACCEPTABLE_TYPE_SINGLETON_LIST =
            Collections.<MediaType>singletonList(MediaTypes.WILDCARD_ACCEPTABLE_TYPE);

    private final GuardianStringKeyMultivaluedMap<Object> headers;
    private final CommittingOutputStream committingOutputStream;
    private Configuration configuration;
    private LazyValue<MediaType> mediaTypeCache;

    private Object entity;
    private GenericType<?> entityType;
    private Annotation[] entityAnnotations = EMPTY_ANNOTATIONS;
    private OutputStream entityStream;



    /**
     * The callback interface which is used to get the terminal output stream into which the entity should be
     * written and to inform the implementation about the entity size.
     */
    public static interface StreamProvider {
        /**
         * Get the output stream. This method will be called after all the
         * {@link jakarta.ws.rs.ext.WriterInterceptor writer interceptors} are called and written entity is buffered
         * into the buffer or the buffer exceeds.
         *
         * @param contentLength the size of the buffered entity or -1 if the entity exceeded the maximum buffer
         *                      size or if the buffering is disabled.
         * @return the adapted output stream into which the serialized entity should be written. May return null
         * which will cause ignoring the written entity (in that case the entity will
         * still be written by {@link jakarta.ws.rs.ext.MessageBodyWriter message body writers}
         * but the output will be ignored).
         * @throws java.io.IOException in case of an IO error.
         */
        public OutputStream getOutputStream(int contentLength) throws IOException;
    }

    /**
     * Create new outbound message context.
     * @param configuration the client/server {@link Configuration}. If {@code null}, the default behaviour is expected.
     */
    public OutboundMessageContext(Configuration configuration) {
        super(configuration);
        this.configuration = configuration;
        this.headers = new GuardianStringKeyMultivaluedMap<>(HeaderUtils.createOutbound());
        this.committingOutputStream = new CommittingOutputStream();
        this.entityStream = committingOutputStream;
        this.mediaTypeCache = mediaTypeCache();

        headers.setGuard(HttpHeaders.CONTENT_TYPE);
    }

    /**
     * Create new outbound message context copying the content
     * of another context.
     *
     * @param original the original outbound message context.
     */
    public OutboundMessageContext(OutboundMessageContext original) {
        super(original);
        this.headers = new GuardianStringKeyMultivaluedMap<>(HeaderUtils.createOutbound());
        this.headers.setGuard(HttpHeaders.CONTENT_TYPE);
        this.headers.putAll(original.headers);
        this.committingOutputStream = new CommittingOutputStream();
        this.entityStream = committingOutputStream;

        this.entity = original.entity;
        this.entityType = original.entityType;
        this.entityAnnotations = original.entityAnnotations;
        this.configuration = original.configuration;
        this.mediaTypeCache = mediaTypeCache();
    }

    /**
     * Create new outbound message context.
     *
     * @see #OutboundMessageContext(Configuration)
     */
    @Deprecated
    public OutboundMessageContext() {
        this ((Configuration) null);
    }

    /**
     * Replace all headers.
     *
     * @param headers new headers.
     */
    public void replaceHeaders(MultivaluedMap<String, Object> headers) {
        getHeaders().clear();
        if (headers != null) {
            getHeaders().putAll(headers);
        }
    }

    /**
     * Get a multi-valued map representing outbound message headers with their values converted
     * to strings.
     *
     * @return multi-valued map of outbound message header names to their string-converted values.
     */
    public MultivaluedMap<String, String> getStringHeaders() {
        return HeaderUtils.asStringHeaders(headers, runtimeDelegateDecorator);
    }

    /**
     * Get a message header as a single string value.
     * <p>
     * Each single header value is converted to String using a
     * {@link jakarta.ws.rs.ext.RuntimeDelegate.HeaderDelegate} if one is available
     * via {@link jakarta.ws.rs.ext.RuntimeDelegate#createHeaderDelegate(java.lang.Class)}
     * for the header value class or using its {@code toString} method  if a header
     * delegate is not available.
     *
     * @param name the message header.
     * @return the message header value. If the message header is not present then
     * {@code null} is returned. If the message header is present but has no
     * value then the empty string is returned. If the message header is present
     * more than once then the values of joined together and separated by a ','
     * character.
     */
    public String getHeaderString(String name) {
        return HeaderUtils.asHeaderString(headers.get(name), runtimeDelegateDecorator);
    }

    @Override
    public HeaderValueException.Context getHeaderValueExceptionContext() {
        return HeaderValueException.Context.OUTBOUND;
    }

    /**
     * Get the mutable message headers multivalued map.
     *
     * @return mutable multivalued map of message headers.
     */
    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }

    /**
     * Get the media type of the entity.
     *
     * @return the media type or {@code null} if not specified (e.g. there's no
     * message entity).
     */
    public MediaType getMediaType() {
        if (headers.isObservedAndReset(HttpHeaders.CONTENT_TYPE) && mediaTypeCache.isInitialized()) {
            mediaTypeCache = mediaTypeCache(); // headers changed -> drop cache
        }
        return mediaTypeCache.get();
    }

    private LazyValue<MediaType> mediaTypeCache() {
        return Values.lazy((Value<MediaType>) () ->
                singleHeader(HttpHeaders.CONTENT_TYPE, MediaType.class, RuntimeDelegateDecorator.configured(configuration)
                    .createHeaderDelegate(MediaType.class)::fromString, false)
        );
    }

    /**
     * Get a list of media types that are acceptable for the message.
     *
     * @return a read-only list of requested message media types sorted according
     * to their q-value, with highest preference first.
     */
    @SuppressWarnings("unchecked")
    public List<MediaType> getAcceptableMediaTypes() {
        final List<Object> values = headers.get(HttpHeaders.ACCEPT);

        if (values == null || values.isEmpty()) {
            return WILDCARD_ACCEPTABLE_TYPE_SINGLETON_LIST;
        }
        final List<MediaType> result = new ArrayList<>(values.size());
        boolean conversionApplied = false;
        for (final Object value : values) {
            try {
                if (value instanceof MediaType) {
                    final AcceptableMediaType _value = AcceptableMediaType.valueOf((MediaType) value);
                    conversionApplied = _value != value; // true if value was not an instance of AcceptableMediaType already
                    result.add(_value);
                } else {
                    conversionApplied = true;
                    result.addAll(HttpHeaderReader.readAcceptMediaType(HeaderUtils.asString(value, runtimeDelegateDecorator)));
                }
            } catch (java.text.ParseException e) {
                throw exception(HttpHeaders.ACCEPT, value, e);
            }
        }

        if (conversionApplied) {
            // cache converted
            headers.put(HttpHeaders.ACCEPT,
                        result.stream()
                              .map((Function<MediaType, Object>) mediaType -> mediaType)
                              .collect(Collectors.toList()));
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Get a list of languages that are acceptable for the message.
     *
     * @return a read-only list of acceptable languages sorted according
     * to their q-value, with highest preference first.
     */
    public List<Locale> getAcceptableLanguages() {
        final List<Object> values = headers.get(HttpHeaders.ACCEPT_LANGUAGE);

        if (values == null || values.isEmpty()) {
            return Collections.singletonList(new AcceptableLanguageTag("*", null).getAsLocale());
        }

        final List<Locale> result = new ArrayList<Locale>(values.size());
        boolean conversionApplied = false;
        for (final Object value : values) {
            if (value instanceof Locale) {
                result.add((Locale) value);
            } else {
                conversionApplied = true;
                try {
                    result.addAll(HttpHeaderReader.readAcceptLanguage(HeaderUtils.asString(value, runtimeDelegateDecorator))
                                                  .stream()
                                                  .map(LanguageTag::getAsLocale)
                                                  .collect(Collectors.toList()));
                } catch (java.text.ParseException e) {
                    throw exception(HttpHeaders.ACCEPT_LANGUAGE, value, e);
                }
            }
        }

        if (conversionApplied) {
            // cache converted
            headers.put(HttpHeaders.ACCEPT_LANGUAGE,
                        result.stream()
                              .map((Function<Locale, Object>) locale -> locale)
                              .collect(Collectors.toList()));
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Get the links attached to the message as header.
     *
     * @return links, may return empty {@link java.util.Set} if no links are present. Never
     * returns {@code null}.
     */
    public Set<Link> getLinks() {
        List<Object> values = headers.get(HttpHeaders.LINK);
        if (values == null || values.isEmpty()) {
            return Collections.emptySet();
        }

        final Set<Link> result = new HashSet<Link>(values.size());
        boolean conversionApplied = false;
        for (final Object value : values) {
            if (value instanceof Link) {
                result.add((Link) value);
            } else {
                conversionApplied = true;
                try {
                    result.add(Link.valueOf(HeaderUtils.asString(value, runtimeDelegateDecorator)));
                } catch (IllegalArgumentException e) {
                    throw exception(HttpHeaders.LINK, value, e);
                }
            }
        }

        if (conversionApplied) {
            // cache converted
            headers.put(HttpHeaders.LINK,
                        result.stream()
                              .map((Function<Link, Object>) link -> link)
                              .collect(Collectors.toList()));
        }

        return Collections.unmodifiableSet(result);
    }

    // Message entity

    /**
     * Check if there is an entity available in the message.
     * <p>
     * The method returns {@code true} if the entity is present, returns
     * {@code false} otherwise.
     *
     * @return {@code true} if there is an entity present in the message,
     * {@code false} otherwise.
     */
    public boolean hasEntity() {
        return entity != null;
    }

    /**
     * Get the message entity Java instance.
     * <p>
     * Returns {@code null} if the message does not contain an entity.
     *
     * @return the message entity or {@code null} if message does not contain an
     * entity body.
     */
    public Object getEntity() {
        return entity;
    }

    /**
     * Set a new message message entity.
     *
     * @param entity entity object.
     * @see jakarta.ws.rs.ext.MessageBodyWriter
     */
    public void setEntity(Object entity) {
        setEntity(entity, ReflectionHelper.genericTypeFor(entity));
    }

    /**
     * Set a new message message entity.
     *
     * @param entity      entity object.
     * @param annotations annotations attached to the entity.
     * @see jakarta.ws.rs.ext.MessageBodyWriter
     */
    public void setEntity(Object entity, Annotation[] annotations) {
        setEntity(entity, ReflectionHelper.genericTypeFor(entity));
        setEntityAnnotations(annotations);
    }

    /**
     * Set a new message message entity.
     *
     * @param entity entity object.
     * @param type   entity generic type information.
     * @see jakarta.ws.rs.ext.MessageBodyWriter
     */
    private void setEntity(Object entity, GenericType<?> type) {
        if (entity instanceof GenericEntity) {
            this.entity = ((GenericEntity) entity).getEntity();
        } else {
            this.entity = entity;
        }
        // ignoring overridden generic entity type information
        this.entityType = type;
    }

    /**
     * Set a new message message entity.
     *
     * @param entity      entity object.
     * @param type        declared entity class.
     * @param annotations annotations attached to the entity.
     * @see jakarta.ws.rs.ext.MessageBodyWriter
     */
    public void setEntity(Object entity, Type type, Annotation[] annotations) {
        setEntity(entity, new GenericType(type));
        setEntityAnnotations(annotations);
    }

    /**
     * Set a new message message entity.
     *
     * @param entity      entity object.
     * @param annotations annotations attached to the entity.
     * @param mediaType   entity media type.
     * @see jakarta.ws.rs.ext.MessageBodyWriter
     */
    public void setEntity(Object entity, Annotation[] annotations, MediaType mediaType) {
        setEntity(entity, annotations);
        setMediaType(mediaType);
    }

    /**
     * Set the message content media type.
     *
     * @param mediaType message content media type.
     */
    public void setMediaType(MediaType mediaType) {
        this.headers.putSingle(HttpHeaders.CONTENT_TYPE, mediaType);
    }

    /**
     * Get the raw message entity type information.
     *
     * @return raw message entity type information.
     */
    public Class<?> getEntityClass() {
        return entityType == null ? null : entityType.getRawType();
    }

    /**
     * Get the message entity type information.
     *
     * @return message entity type.
     */
    public Type getEntityType() {
        return entityType == null ? null : entityType.getType();
    }

    /**
     * Set the message entity type information.
     * <p>
     * This method overrides any computed or previously set entity type information.
     *
     * @param type overriding message entity type.
     */
    public void setEntityType(Type type) {
        this.entityType = new GenericType(type);
    }

    /**
     * Get the annotations attached to the entity.
     *
     * @return entity annotations.
     */
    public Annotation[] getEntityAnnotations() {
        return entityAnnotations.clone();
    }

    /**
     * Set the annotations attached to the entity.
     *
     * @param annotations entity annotations.
     */
    public void setEntityAnnotations(Annotation[] annotations) {
        this.entityAnnotations = (annotations == null) ? EMPTY_ANNOTATIONS : annotations;
    }

    /**
     * Get the entity output stream.
     *
     * @return entity output stream.
     */
    public OutputStream getEntityStream() {
        return entityStream;
    }

    /**
     * Set a new entity output stream.
     *
     * @param outputStream new entity output stream.
     */
    public void setEntityStream(OutputStream outputStream) {
        this.entityStream = outputStream;
    }

    /**
     * Enable a buffering of serialized entity. The buffering will be configured from configuration. The property
     * determining the size of the buffer is {@link CommonProperties#OUTBOUND_CONTENT_LENGTH_BUFFER}.
     * </p>
     * The buffering functionality is by default disabled and could be enabled by calling this method. In this case
     * this method must be called before first bytes are written to the {@link #getEntityStream() entity stream}.
     *
     * @param configuration runtime configuration.
     */
    public void enableBuffering(Configuration configuration) {
        final Integer bufferSize = CommonProperties.getValue(configuration.getProperties(),
                configuration.getRuntimeType(), CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, Integer.class);
        if (bufferSize != null) {
            committingOutputStream.enableBuffering(bufferSize);
        } else {
            committingOutputStream.enableBuffering();
        }
    }

    /**
     * Set a stream provider callback.
     * <p/>
     * This method must be called before first bytes are written to the {@link #getEntityStream() entity stream}.
     *
     * @param streamProvider non-{@code null} output stream provider.
     */
    public void setStreamProvider(StreamProvider streamProvider) {
        committingOutputStream.setStreamProvider(streamProvider);
    }


    /**
     * Commits the {@link #getEntityStream() entity stream} if it wasn't already committed.
     *
     * @throws IOException in case of the IO error.
     */
    public void commitStream() throws IOException {
        if (!committingOutputStream.isCommitted()) {
            entityStream.flush();
            if (!committingOutputStream.isCommitted()) {
                committingOutputStream.commit();
                committingOutputStream.flush();
            }
        }
    }

    /**
     * Returns {@code true} if the entity stream has been committed.
     *
     * @return {@code true} if the entity stream has been committed. Otherwise returns {@code false}.
     */
    public boolean isCommitted() {
        return committingOutputStream.isCommitted();
    }

    /**
     * Closes the context. Flushes and closes the entity stream.
     * @throws UncheckedIOException if IO errors
     */
    public void close() {
        if (hasEntity()) {
            try {
                final OutputStream es = getEntityStream();
                if (CommittingOutputStream.class.isInstance(es)) {
                    ((CommittingOutputStream) es).flushOnClose();
                } else if (!FlushedCloseable.flushOnClose(es)) {
                    es.flush();
                }
                es.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                // In case some of the output stream wrapper does not delegate close() call we
                // close the root stream manually to make sure it commits the data.
                if (!committingOutputStream.isClosed()) {
                    try {
                        committingOutputStream.close();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
        }
    }

    void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        this.runtimeDelegateDecorator = RuntimeDelegateDecorator.configured(configuration);
    }

    /**
     * The related client/server side {@link Configuration}. Can be {@code null}.
     * @return {@link Configuration} the configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }
}
