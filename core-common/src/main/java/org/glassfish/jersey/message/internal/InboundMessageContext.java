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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Function;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.ReaderInterceptor;

import javax.xml.transform.Source;

import org.glassfish.jersey.innate.io.SafelyClosable;
import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.internal.util.collection.GuardianStringKeyMultivaluedMap;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.message.MessageBodyWorkers;

/**
 * Base inbound message context implementation.
 *
 * @author Marek Potociar
 */
public abstract class InboundMessageContext extends MessageHeaderMethods implements SafelyClosable {

    private static final InputStream EMPTY = new InputStream() {

        @Override
        public int read() throws IOException {
            return -1;
        }

        @Override
        public void mark(int readlimit) {
            // no-op
        }

        @Override
        public void reset() throws IOException {
            // no-op
        }

        @Override
        public boolean markSupported() {
            return true;
        }
    };
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    private static final List<AcceptableMediaType> WILDCARD_ACCEPTABLE_TYPE_SINGLETON_LIST =
            Collections.singletonList(MediaTypes.WILDCARD_ACCEPTABLE_TYPE);

    private final GuardianStringKeyMultivaluedMap<String> headers;
    private final EntityContent entityContent;
    private final boolean translateNce;
    private MessageBodyWorkers workers;
    private final Configuration configuration;
    private LazyValue<MediaType> contentTypeCache;
    private LazyValue<List<AcceptableMediaType>> acceptTypeCache;

    /**
     * Input stream and its state. State is represented by the {@link Type Type enum} and
     * is used to control the execution of interceptors.
     */
    private static class EntityContent extends EntityInputStream {

        private boolean buffered;

        EntityContent() {
            super(EMPTY);
        }

        void setContent(InputStream content, boolean buffered) {
            this.buffered = buffered;
            setWrappedStream(content);
        }

        boolean hasContent() {
            return getWrappedStream() != EMPTY;
        }

        boolean isBuffered() {
            return buffered;
        }

        @Override
        public void close() {
            close(false);
        }

        void close(boolean force) {
            if (buffered && !force) {
                return;
            }
            try {
                super.close();
            } finally {
                buffered = false;
                setWrappedStream(null);
            }
        }
    }

    /**
     * Create new inbound message context.
     *
     * @param configuration the related client/server side {@link Configuration}
     */
    public InboundMessageContext(Configuration configuration) {
        this(configuration, false);
    }

    /**
     * Create new inbound message context.
     *
     * @param configuration the related client/server side {@link Configuration}. If {@code null},
     *                      the default behaviour is expected.
     * @param translateNce  if {@code true}, the {@link jakarta.ws.rs.core.NoContentException} thrown by a
     *                      selected message body reader will be translated into a {@link jakarta.ws.rs.BadRequestException}
     *                      as required by JAX-RS specification on the server side.
     */
    public InboundMessageContext(Configuration configuration, boolean translateNce) {
        this(configuration, HeaderUtils.createInbound(), translateNce);
    }

    /**
     * Create new inbound message context.
     *
     * @param configuration the related client/server side {@link Configuration}. If {@code null},
     *                      the default behaviour is expected.
     * @param httpHeaders   the http headers map.
     * @param translateNce  if {@code true}, the {@link jakarta.ws.rs.core.NoContentException} thrown by a
     *                      selected message body reader will be translated into a {@link jakarta.ws.rs.BadRequestException}
     *                      as required by JAX-RS specification on the server side.
     */
    public InboundMessageContext(Configuration configuration, MultivaluedMap<String, String> httpHeaders, boolean translateNce) {
        super(configuration);
        this.headers = new GuardianStringKeyMultivaluedMap<>(httpHeaders);
        this.entityContent = new EntityContent();
        this.translateNce = translateNce;
        this.configuration = configuration;

        contentTypeCache = contentTypeCache();
        acceptTypeCache = acceptTypeCache();
        headers.setGuard(HttpHeaders.CONTENT_TYPE);
        headers.setGuard(HttpHeaders.ACCEPT);
    }

    /**
     * Create new inbound message context.
     * @see #InboundMessageContext(Configuration)
     */
    @Deprecated
    public InboundMessageContext() {
        this((Configuration) null);
    }

    /**
     * Create new inbound message context.
     *
     * @param translateNce  if {@code true}, the {@link jakarta.ws.rs.core.NoContentException} thrown by a
     *                      selected message body reader will be translated into a {@link jakarta.ws.rs.BadRequestException}
     *                      as required by JAX-RS specification on the server side.     *
     * @see #InboundMessageContext(Configuration)
     */
    @Deprecated
    public InboundMessageContext(boolean translateNce) {
        this((Configuration) null, translateNce);
    }

    // Message headers

    /**
     * Add a new header value.
     *
     * @param name  header name.
     * @param value header value.
     * @return updated context.
     */
    public InboundMessageContext header(String name, Object value) {
        getHeaders().add(name, HeaderUtils.asString(value, runtimeDelegateDecorator));
        return this;
    }

    /**
     * Add new header values.
     *
     * @param name   header name.
     * @param values header values.
     * @return updated context.
     */
    public InboundMessageContext headers(String name, Object... values) {
        this.getHeaders().addAll(name, HeaderUtils.asStringList(Arrays.asList(values), runtimeDelegateDecorator));
        return this;
    }

    /**
     * Add new header values.
     *
     * @param name   header name.
     * @param values header values.
     * @return updated context.
     */
    public InboundMessageContext headers(String name, Iterable<?> values) {
        this.getHeaders().addAll(name, iterableToList(values));
        return this;
    }

    /**
     * Add new headers.
     *
     * @param newHeaders new headers.
     * @return updated context.
     */
    public InboundMessageContext headers(MultivaluedMap<String, String> newHeaders) {
        for (Map.Entry<String, List<String>> header : newHeaders.entrySet()) {
            headers.addAll(header.getKey(), header.getValue());
        }
        return this;
    }

    /**
     * Add new headers.
     *
     * @param newHeaders new headers.
     * @return updated context.
     */
    public InboundMessageContext headers(Map<String, List<String>> newHeaders) {
        for (Map.Entry<String, List<String>> header : newHeaders.entrySet()) {
            headers.addAll(header.getKey(), header.getValue());
        }
        return this;
    }

    /**
     * Remove a header.
     *
     * @param name header name.
     * @return updated context.
     */
    public InboundMessageContext remove(String name) {
        this.getHeaders().remove(name);
        return this;
    }

    private List<String> iterableToList(final Iterable<?> values) {
        final LinkedList<String> linkedList = new LinkedList<String>();

        for (Object element : values) {
            linkedList.add(HeaderUtils.asString(element, runtimeDelegateDecorator));
        }

        return linkedList;
    }

    /**
     * Get a message header as a single string value.
     * <p/>
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
        List<String> values = this.headers.get(name);
        if (values == null) {
            return null;
        }
        if (values.isEmpty()) {
            return "";
        }

        final Iterator<String> valuesIterator = values.iterator();
        String next = valuesIterator.next();
        if (next == null) {
            next = "";
        }
        StringBuilder buffer = new StringBuilder(next);
        while (valuesIterator.hasNext()) {
            buffer.append(',').append(valuesIterator.next());
        }

        return buffer.toString();
    }

    @Override
    public HeaderValueException.Context getHeaderValueExceptionContext() {
        return HeaderValueException.Context.INBOUND;
    }

    /**
     * Get the mutable message headers multivalued map.
     *
     * @return mutable multivalued map of message headers.
     */
    public MultivaluedMap<String, String> getHeaders() {
        return this.headers;
    }

    /**
     * Get If-Match header.
     *
     * @return the If-Match header value, otherwise {@code null} if not present.
     */
    public Set<MatchingEntityTag> getIfMatch() {
        final String ifMatch = getHeaderString(HttpHeaders.IF_MATCH);
        if (ifMatch == null || ifMatch.isEmpty()) {
            return null;
        }
        try {
            return HttpHeaderReader.readMatchingEntityTag(ifMatch);
        } catch (java.text.ParseException e) {
            throw exception(HttpHeaders.IF_MATCH, ifMatch, e);
        }
    }

    /**
     * Get If-None-Match header.
     *
     * @return the If-None-Match header value, otherwise {@code null} if not present.
     */
    public Set<MatchingEntityTag> getIfNoneMatch() {
        final String ifNoneMatch = getHeaderString(HttpHeaders.IF_NONE_MATCH);
        if (ifNoneMatch == null || ifNoneMatch.isEmpty()) {
            return null;
        }
        try {
            return HttpHeaderReader.readMatchingEntityTag(ifNoneMatch);
        } catch (java.text.ParseException e) {
            throw exception(HttpHeaders.IF_NONE_MATCH, ifNoneMatch, e);
        }
    }

    /**
     * Get the media type of the entity.
     *
     * @return the media type or {@code null} if not specified (e.g. there's no
     * message entity).
     */
    public MediaType getMediaType() {
        if (headers.isObservedAndReset(HttpHeaders.CONTENT_TYPE) && contentTypeCache.isInitialized()) {
            contentTypeCache = contentTypeCache(); // headers changed -> drop cache
        }
        return contentTypeCache.get();
    }

    private LazyValue<MediaType> contentTypeCache() {
        return Values.lazy((Value<MediaType>) () -> singleHeader(
                HttpHeaders.CONTENT_TYPE, new Function<String, MediaType>() {
                    @Override
                    public MediaType apply(String input) {
                        try {
                            return runtimeDelegateDecorator
                                    .createHeaderDelegate(MediaType.class)
                                    .fromString(input);
                        } catch (IllegalArgumentException iae) {
                            throw new ProcessingException(iae);
                        }
                    }
                }, false));
    }

    /**
     * Get a list of media types that are acceptable for a request.
     *
     * @return a read-only list of requested response media types sorted according
     * to their q-value, with highest preference first.
     */
    public List<AcceptableMediaType> getQualifiedAcceptableMediaTypes() {
        if (headers.isObservedAndReset(HttpHeaders.ACCEPT) && acceptTypeCache.isInitialized()) {
            acceptTypeCache = acceptTypeCache();
        }
        return acceptTypeCache.get();
    }

    private LazyValue<List<AcceptableMediaType>> acceptTypeCache() {
        return Values.lazy((Value<List<AcceptableMediaType>>) () -> {
            final String value = getHeaderString(HttpHeaders.ACCEPT);

            if (value == null || value.isEmpty()) {
                return WILDCARD_ACCEPTABLE_TYPE_SINGLETON_LIST;
            }

            try {
                return Collections.unmodifiableList(HttpHeaderReader.readAcceptMediaType(value));
            } catch (ParseException e) {
                throw exception(HttpHeaders.ACCEPT, value, e);
            }
        });
    }

    /**
     * Get a list of languages that are acceptable for the message.
     *
     * @return a read-only list of acceptable languages sorted according
     * to their q-value, with highest preference first.
     */
    public List<AcceptableLanguageTag> getQualifiedAcceptableLanguages() {
        final String value = getHeaderString(HttpHeaders.ACCEPT_LANGUAGE);

        if (value == null || value.isEmpty()) {
            return Collections.singletonList(new AcceptableLanguageTag("*", null));
        }

        try {
            return Collections.unmodifiableList(HttpHeaderReader.readAcceptLanguage(value));
        } catch (ParseException e) {
            throw exception(HttpHeaders.ACCEPT_LANGUAGE, value, e);
        }
    }

    /**
     * Get the list of language tag from the "Accept-Charset" of an HTTP request.
     *
     * @return The list of AcceptableToken. This list
     * is ordered with the highest quality acceptable charset occurring first.
     */
    public List<AcceptableToken> getQualifiedAcceptCharset() {
        final String acceptCharset = getHeaderString(HttpHeaders.ACCEPT_CHARSET);
        try {
            if (acceptCharset == null || acceptCharset.isEmpty()) {
                return Collections.singletonList(new AcceptableToken("*"));
            }
            return HttpHeaderReader.readAcceptToken(acceptCharset);
        } catch (java.text.ParseException e) {
            throw exception(HttpHeaders.ACCEPT_CHARSET, acceptCharset, e);
        }
    }

    /**
     * Get the list of language tag from the "Accept-Charset" of an HTTP request.
     *
     * @return The list of AcceptableToken. This list
     * is ordered with the highest quality acceptable charset occurring first.
     */
    public List<AcceptableToken> getQualifiedAcceptEncoding() {
        final String acceptEncoding = getHeaderString(HttpHeaders.ACCEPT_ENCODING);
        try {
            if (acceptEncoding == null || acceptEncoding.isEmpty()) {
                return Collections.singletonList(new AcceptableToken("*"));
            }
            return HttpHeaderReader.readAcceptToken(acceptEncoding);
        } catch (java.text.ParseException e) {
            throw exception("Accept-Encoding", acceptEncoding, e);
        }
    }

    /**
     * Get the links attached to the message as header.
     *
     * @return links, may return empty {@link java.util.Set} if no links are present. Never
     * returns {@code null}.
     */
    public Set<Link> getLinks() {
        List<String> links = this.headers.get(HttpHeaders.LINK);
        if (links == null || links.isEmpty()) {
            return Collections.emptySet();
        }

        try {
            Set<Link> result = new HashSet<Link>(links.size());
            StringBuilder linkString;
            for (String link : links) {
                linkString = new StringBuilder();
                StringTokenizer st = new StringTokenizer(link, "<>,", true);
                boolean linkOpen = false;
                while (st.hasMoreTokens()) {
                    String n = st.nextToken();
                    if (n.equals("<")) {
                        linkOpen = true;
                    } else if (n.equals(">")) {
                        linkOpen = false;
                    } else if (!linkOpen && n.equals(",")) {
                        result.add(Link.valueOf(linkString.toString().trim()));
                        linkString = new StringBuilder();
                        continue; // don't add the ","
                    }

                    linkString.append(n);
                }

                if (linkString.length() > 0) {
                    result.add(Link.valueOf(linkString.toString().trim()));
                }
            }
            return result;
        } catch (IllegalArgumentException e) {
            throw exception(HttpHeaders.LINK, links, e);
        }
    }

    // Message entity

    /**
     * Get context message body workers.
     *
     * @return context message body workers.
     */
    public MessageBodyWorkers getWorkers() {
        if (workers == null) {
            throw new ProcessingException(LocalizationMessages.RESPONSE_CLOSED());
        }
        return workers;
    }

    /**
     * Set context message body workers.
     *
     * @param workers context message body workers.
     */
    public void setWorkers(MessageBodyWorkers workers) {
        this.workers = workers;
    }

    /**
     * Check if there is a non-empty entity input stream is available in the
     * message.
     * <p/>
     * The method returns {@code true} if the entity is present, returns
     * {@code false} otherwise.
     *
     * @return {@code true} if there is an entity present in the message,
     * {@code false} otherwise.
     */
    public boolean hasEntity() {
        entityContent.ensureNotClosed();

        try {
            return entityContent.isBuffered() || !entityContent.isEmpty();
        } catch (IllegalStateException ex) {
            // input stream has been closed.
            return false;
        }
    }

    /**
     * Get the entity input stream.
     *
     * @return entity input stream.
     */
    public InputStream getEntityStream() {
        entityContent.ensureNotClosed();

        return entityContent.getWrappedStream();
    }

    /**
     * Set a new entity input stream.
     *
     * @param input new entity input stream.
     */
    public void setEntityStream(InputStream input) {
        this.entityContent.setContent(input, false);
    }

    /**
     * Read entity from a context entity input stream.
     *
     * @param <T>                entity Java object type.
     * @param rawType            raw Java entity type.
     * @param propertiesDelegate request-scoped properties delegate.
     * @return entity read from a context entity input stream.
     */
    public <T> T readEntity(Class<T> rawType, PropertiesDelegate propertiesDelegate) {
        return readEntity(rawType, rawType, EMPTY_ANNOTATIONS, propertiesDelegate);
    }

    /**
     * Read entity from a context entity input stream.
     *
     * @param <T>                entity Java object type.
     * @param rawType            raw Java entity type.
     * @param annotations        entity annotations.
     * @param propertiesDelegate request-scoped properties delegate.
     * @return entity read from a context entity input stream.
     */
    public <T> T readEntity(Class<T> rawType, Annotation[] annotations, PropertiesDelegate propertiesDelegate) {
        return readEntity(rawType, rawType, annotations, propertiesDelegate);
    }

    /**
     * Read entity from a context entity input stream.
     *
     * @param <T>                entity Java object type.
     * @param rawType            raw Java entity type.
     * @param type               generic Java entity type.
     * @param propertiesDelegate request-scoped properties delegate.
     * @return entity read from a context entity input stream.
     */
    public <T> T readEntity(Class<T> rawType, Type type, PropertiesDelegate propertiesDelegate) {
        return readEntity(rawType, type, EMPTY_ANNOTATIONS, propertiesDelegate);
    }

    /**
     * Read entity from a context entity input stream.
     *
     * @param <T>                entity Java object type.
     * @param rawType            raw Java entity type.
     * @param type               generic Java entity type.
     * @param annotations        entity annotations.
     * @param propertiesDelegate request-scoped properties delegate.
     * @return entity read from a context entity input stream.
     */
    @SuppressWarnings("unchecked")
    public <T> T readEntity(Class<T> rawType, Type type, Annotation[] annotations, PropertiesDelegate propertiesDelegate) {
        final boolean buffered = entityContent.isBuffered();
        if (buffered) {
            entityContent.reset();
        }

        entityContent.ensureNotClosed();

        // TODO: revise if we need to re-introduce the check for performance reasons or once non-blocking I/O is supported.
        // The code has been commended out because in case of streaming input (e.g. SSE) the call might block until a first
        // byte is available, which would make e.g. the SSE EventSource construction or EventSource.open() method to block
        // until a first event is received, which is undesirable.
        //
        //        if (entityContent.isEmpty()) {
        //            return null;
        //        }

        if (workers == null) {
            return null;
        }

        MediaType mediaType = getMediaType();
        mediaType = mediaType == null ? MediaType.APPLICATION_OCTET_STREAM_TYPE : mediaType;

        boolean shouldClose = !buffered;
        try {
            T t = (T) workers.readFrom(
                    rawType,
                    type,
                    annotations,
                    mediaType,
                    headers,
                    propertiesDelegate,
                    entityContent.getWrappedStream(),
                    entityContent.hasContent() ? getReaderInterceptors() : Collections.<ReaderInterceptor>emptyList(),
                    translateNce);

            shouldClose = shouldClose && !(t instanceof Closeable) && !(t instanceof Source);

            return t;
        } catch (IOException ex) {
            throw new ProcessingException(LocalizationMessages.ERROR_READING_ENTITY_FROM_INPUT_STREAM(), ex);
        } finally {
            if (shouldClose) {
                // Workaround for JRFCAF-1344: the underlying stream close() implementation may be thread-unsafe
                // and as such the close() may result in an IOException at the socket input stream level,
                // if the close() gets called at once from multiple threads somehow.
                // We want to ignore these exceptions in the readEntity/bufferEntity operations though.
                ReaderWriter.safelyClose(entityContent);
            }
        }
    }

    /**
     * Buffer the entity stream (if not empty).
     *
     * @return {@code true} if the entity input stream was successfully buffered.
     * @throws jakarta.ws.rs.ProcessingException in case of an IO error.
     */
    public boolean bufferEntity() throws ProcessingException {
        entityContent.ensureNotClosed();

        try {
            if (entityContent.isBuffered() || !entityContent.hasContent()) {
                return true;
            }

            final InputStream entityStream = entityContent.getWrappedStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ReaderWriter.writeTo(entityStream, baos);
            } finally {
                // Workaround for JRFCAF-1344: the underlying stream close() implementation may be thread-unsafe
                // and as such the close() may result in an IOException at the socket input stream level,
                // if the close() gets called at once from multiple threads somehow.
                // We want to ignore these exceptions in the readEntity/bufferEntity operations though.
                ReaderWriter.safelyClose(entityStream);
            }

            entityContent.setContent(new ByteArrayInputStream(baos.toByteArray()), true);

            return true;
        } catch (IOException ex) {
            throw new ProcessingException(LocalizationMessages.MESSAGE_CONTENT_BUFFERING_FAILED(), ex);
        }
    }

    /**
     * Closes the underlying content stream.
     */
    public void close() {
        entityContent.close(true);
        if (workers != null) {
            workers.close();
        }
        setWorkers(null);
    }

    /**
     * Get reader interceptors bound to this context.
     * <p>
     * Interceptors will be used when one of the {@code readEntity} methods is invoked.
     * </p>
     *
     * @return reader interceptors bound to this context.
     */
    protected abstract Iterable<ReaderInterceptor> getReaderInterceptors();

    /**
     * The related client/server side {@link Configuration}. Can be {@code null}.
     * @return {@link Configuration} the configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }
}
