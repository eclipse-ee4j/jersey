/*
 * Copyright (c) 2019, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client;

import org.glassfish.jersey.client.internal.LocalizationMessages;
import org.glassfish.jersey.client.internal.routing.ClientResponseMediaTypeDeterminer;
import org.glassfish.jersey.client.spi.PostInvocationInterceptor;
import org.glassfish.jersey.client.spi.PreInvocationInterceptor;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.model.internal.RankedComparator;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for {@link PreInvocationInterceptor} and {@link PostInvocationInterceptor} execution.
 *
 * @since 2.30
 */
class InvocationInterceptorStages {

    private static final Logger LOGGER = Logger.getLogger(InvocationInterceptorStages.class.getName());
    private InvocationInterceptorStages() {
        // prevent instantiation
    }

    /**
     * Create a {@link PreInvocationInterceptorStage} executing all {@link PreInvocationInterceptor PreInvocationInterceptors}.
     *
     * @param injectionManager the injection manager providing the registered {@code PreInvocationInterceptors}.
     * @return {@code PreInvocationInterceptorStage} class to execute all the {@code PreInvocationInterceptors}.
     */
    static PreInvocationInterceptorStage createPreInvocationInterceptorStage(InjectionManager injectionManager) {
        return new PreInvocationInterceptorStage(injectionManager);
    }

    /**
     * Create a {@link PostInvocationInterceptorStage} executing all {@link PostInvocationInterceptor PostInvocationInterceptors}.
     *
     * @param injectionManager the injection manager providing the registered {@code PostInvocationInterceptors}.
     * @return {@code PostInvocationInterceptorStage} class to execute all the {@code PostInvocationInterceptors}.
     */
    static PostInvocationInterceptorStage createPostInvocationInterceptorStage(InjectionManager injectionManager) {
        return new PostInvocationInterceptorStage(injectionManager);
    }

    /**
     * The stage to execute all the {@link PreInvocationInterceptor PreInvocationInterceptors}.
     */
    static class PreInvocationInterceptorStage {
        private Iterable<PreInvocationInterceptor> preInvocationInterceptors;
        private PreInvocationInterceptorStage(InjectionManager injectionManager) {
            final RankedComparator<PreInvocationInterceptor> comparator =
                    new RankedComparator<>(RankedComparator.Order.DESCENDING);
            preInvocationInterceptors = Providers.getAllProviders(injectionManager, PreInvocationInterceptor.class, comparator);
        }

        /**
         * Returns {@code true} if there is a {@link PreInvocationInterceptor} registered not yet executed in the request.
         *
         * @return {@code true} if there is a {@link PreInvocationInterceptor} yet to be executed.
         */
        boolean hasPreInvocationInterceptors() {
            return preInvocationInterceptors.iterator().hasNext();
        }

        /**
         * Execute the  {@link PreInvocationInterceptor PreInvocationInterceptors}.
         *
         * @param request {@link javax.ws.rs.client.ClientRequestContext} to be passed to {@code PreInvocationInterceptor}.
         */
        void beforeRequest(ClientRequest request) {
            final LinkedList<Throwable> throwables = new LinkedList<>();
            final ClientRequestContext requestContext = new InvocationInterceptorRequestContext(request);
            final Iterator<PreInvocationInterceptor> preInvocationInterceptorIterator = preInvocationInterceptors.iterator();
            while (preInvocationInterceptorIterator.hasNext()) {
                try {
                    preInvocationInterceptorIterator.next().beforeRequest(requestContext);
                } catch (Throwable throwable) {
                    LOGGER.log(Level.FINE, LocalizationMessages.PREINVOCATION_INTERCEPTOR_EXCEPTION(), throwable);
                    throwables.add(throwable);
                }
            }
            if (!throwables.isEmpty()) {
                throw suppressExceptions(throwables);
            }
        }

        /**
         * Create an empty {@link ClientRequestFilter} to executed the first after all {@code PreInvocationInterceptors}
         * for the runtime to handle {@link ClientRequestContext#abortWith(Response)} utilization.
         *
         * @return an empty {@link ClientRequestFilter}.
         */
        ClientRequestFilter createPreInvocationInterceptorFilter() {
            return new ClientRequestFilter() {
                @Override
                public void filter(ClientRequestContext requestContext) throws IOException {
                    // do nothing, the filtering stage will handle requestContext#abortWith
                    // set in the PreInvocationInterceptor
                }
            };
        }
    }

    /**
     * The stage to execute all the {@link PostInvocationInterceptor PostInvocationInterceptors}.
     */
    static class PostInvocationInterceptorStage {
        private final Iterable<PostInvocationInterceptor> postInvocationInterceptors;

        private PostInvocationInterceptorStage(InjectionManager injectionManager) {
            final RankedComparator<PostInvocationInterceptor> comparator =
                    new RankedComparator<>(RankedComparator.Order.ASCENDING);
            postInvocationInterceptors
                    = Providers.getAllProviders(injectionManager, PostInvocationInterceptor.class, comparator);
        }

        /**
         * Returns {@code true} if there is a {@link PostInvocationInterceptor} registered not yet executed in the request.
         *
         * @return {@code true} if there is a {@link PostInvocationInterceptor} yet to be executed.
         */
        boolean hasPostInvocationInterceptor() {
            return postInvocationInterceptors.iterator().hasNext();
        }

        private ClientResponse afterRequestWithoutException(Iterator<PostInvocationInterceptor> postInvocationInterceptors,
                                                            InvocationInterceptorRequestContext requestContext,
                                                            PostInvocationExceptionContext exceptionContext) {
            boolean withoutException = true;
            if (postInvocationInterceptors.hasNext()) {
                final PostInvocationInterceptor postInvocationInterceptor = postInvocationInterceptors.next();
                try {
                    postInvocationInterceptor.afterRequest(requestContext, exceptionContext.getResponseContext().get());
                } catch (Throwable throwable) {
                    LOGGER.log(Level.FINE, LocalizationMessages.POSTINVOCATION_INTERCEPTOR_EXCEPTION(), throwable);
                    withoutException = false;
                    exceptionContext.throwables.add(throwable);
                } finally {
                    return withoutException
                            ? afterRequestWithoutException(postInvocationInterceptors, requestContext, exceptionContext)
                            : afterRequestWithException(postInvocationInterceptors, requestContext, exceptionContext);
                }
            } else {
                return exceptionContext.responseContext;
            }
        }

        private ClientResponse afterRequestWithException(Iterator<PostInvocationInterceptor> postInvocationInterceptors,
                                                         InvocationInterceptorRequestContext requestContext,
                                                         PostInvocationExceptionContext exceptionContext) {
            Throwable caught = null;
            if (postInvocationInterceptors.hasNext()) {
                final PostInvocationInterceptor postInvocationInterceptor = postInvocationInterceptors.next();
                try {
                    postInvocationInterceptor.onException(requestContext, exceptionContext);
                } catch (Throwable throwable) {
                    LOGGER.log(Level.FINE, LocalizationMessages.POSTINVOCATION_INTERCEPTOR_EXCEPTION(), throwable);
                    caught = throwable; // keep this if handleResponse clears the Throwables
                }

                try {
                    resolveResponse(requestContext, exceptionContext);
                } catch (Throwable throwable) {
                    LOGGER.log(Level.FINE, LocalizationMessages.POSTINVOCATION_INTERCEPTOR_EXCEPTION(), throwable);
                    exceptionContext.throwables.add(throwable);
                } finally {
                    if (caught != null) {
                        exceptionContext.throwables.add(caught);
                    }
                }
                return exceptionContext.throwables.isEmpty() && exceptionContext.responseContext != null
                        ? afterRequestWithoutException(postInvocationInterceptors, requestContext, exceptionContext)
                        : afterRequestWithException(postInvocationInterceptors, requestContext, exceptionContext);

            } else {
                throw suppressExceptions(exceptionContext.throwables);
            }
        }

        /**
         * Execute all {@link PostInvocationInterceptor PostInvocationInterceptors}.
         *
         * @param request {@link ClientRequestContext}
         * @param response {@link ClientResponseContext}
         * @param previousException Any possible {@code Throwable} caught from executing the previous parts of the Client Request
         *                          chain.
         * @return the actual {@link ClientResponseContext} provided by the previous parts of the Client Request chain or by
         *         {@link PostInvocationInterceptor.ExceptionContext#resolve(Response)}.
         * @throws {@code RuntimeException} if {@code previousException} or any new {@code Exception} was not
         *         {@link PostInvocationInterceptor.ExceptionContext#resolve(Response) resolved}.
         */
        ClientResponse afterRequest(ClientRequest request, ClientResponse response, Throwable previousException) {
            final PostInvocationExceptionContext exceptionContext
                    = new PostInvocationExceptionContext(response, previousException);
            final InvocationInterceptorRequestContext requestContext = new InvocationInterceptorRequestContext(request);
            return previousException != null
                    ? afterRequestWithException(postInvocationInterceptors.iterator(), requestContext, exceptionContext)
                    : afterRequestWithoutException(postInvocationInterceptors.iterator(), requestContext, exceptionContext);
        }

        private static boolean resolveResponse(InvocationInterceptorRequestContext requestContext,
                                               PostInvocationExceptionContext exceptionContext) {
            if (exceptionContext.response != null) {
                exceptionContext.throwables.clear();
                final ClientResponseMediaTypeDeterminer determiner = new ClientResponseMediaTypeDeterminer(
                        requestContext.clientRequest.getWorkers());
                determiner.setResponseMediaTypeIfNotSet(exceptionContext.response, requestContext.getConfiguration());

                final ClientResponse response = new ClientResponse(requestContext.clientRequest, exceptionContext.response);
                exceptionContext.responseContext = response;
                exceptionContext.response = null;
                return true;
            } else {
                return false;
            }
        }
    }

    private static class InvocationInterceptorRequestContext implements ClientRequestContext {

        private final ClientRequest clientRequest;

        private InvocationInterceptorRequestContext(ClientRequest clientRequestContext) {
            this.clientRequest = clientRequestContext;
        }

        @Override
        public Object getProperty(String name) {
            return clientRequest.getProperty(name);
        }

        @Override
        public Collection<String> getPropertyNames() {
            return clientRequest.getPropertyNames();
        }

        @Override
        public void setProperty(String name, Object object) {
            clientRequest.setProperty(name, object);
        }

        @Override
        public void removeProperty(String name) {
            clientRequest.removeProperty(name);
        }

        @Override
        public URI getUri() {
            return clientRequest.getUri();
        }

        @Override
        public void setUri(URI uri) {
            clientRequest.setUri(uri);
        }

        @Override
        public String getMethod() {
            return clientRequest.getMethod();
        }

        @Override
        public void setMethod(String method) {
            clientRequest.setMethod(method);
        }

        @Override
        public MultivaluedMap<String, Object> getHeaders() {
            return clientRequest.getHeaders();
        }

        @Override
        public MultivaluedMap<String, String> getStringHeaders() {
            return clientRequest.getStringHeaders();
        }

        @Override
        public String getHeaderString(String name) {
            return clientRequest.getHeaderString(name);
        }

        @Override
        public Date getDate() {
            return clientRequest.getDate();
        }

        @Override
        public Locale getLanguage() {
            return clientRequest.getLanguage();
        }

        @Override
        public MediaType getMediaType() {
            return clientRequest.getMediaType();
        }

        @Override
        public List<MediaType> getAcceptableMediaTypes() {
            return clientRequest.getAcceptableMediaTypes();
        }

        @Override
        public List<Locale> getAcceptableLanguages() {
            return clientRequest.getAcceptableLanguages();
        }

        @Override
        public Map<String, Cookie> getCookies() {
            return clientRequest.getCookies();
        }

        @Override
        public boolean hasEntity() {
            return clientRequest.hasEntity();
        }

        @Override
        public Object getEntity() {
            return clientRequest.getEntity();
        }

        @Override
        public Class<?> getEntityClass() {
            return clientRequest.getEntityClass();
        }

        @Override
        public Type getEntityType() {
            return clientRequest.getEntityType();
        }

        @Override
        public void setEntity(Object entity) {
            clientRequest.setEntity(entity);
        }

        @Override
        public void setEntity(Object entity, Annotation[] annotations, MediaType mediaType) {
            clientRequest.setEntity(entity, annotations, mediaType);
        }

        @Override
        public Annotation[] getEntityAnnotations() {
            return clientRequest.getEntityAnnotations();
        }

        @Override
        public OutputStream getEntityStream() {
            return clientRequest.getEntityStream();
        }

        @Override
        public void setEntityStream(OutputStream outputStream) {
            clientRequest.setEntityStream(outputStream);
        }

        @Override
        public Client getClient() {
            return clientRequest.getClient();
        }

        @Override
        public Configuration getConfiguration() {
            return clientRequest.getConfiguration();
        }

        @Override
        public void abortWith(Response response) {
            if (clientRequest.getAbortResponse() != null) {
                LOGGER.warning(LocalizationMessages.PREINVOCATION_INTERCEPTOR_MULTIPLE_ABORTIONS());
                throw new IllegalStateException(LocalizationMessages.PREINVOCATION_INTERCEPTOR_MULTIPLE_ABORTIONS());
            }
            LOGGER.finer(LocalizationMessages.PREINVOCATION_INTERCEPTOR_ABORT_WITH());
            clientRequest.abortWith(response);
        }
    }

    private static class PostInvocationExceptionContext implements PostInvocationInterceptor.ExceptionContext {
        private ClientResponse responseContext; // responseContext instance can be changed by PostInvocationInterceptor
        private LinkedList<Throwable> throwables;
        private Response response = null;

        private PostInvocationExceptionContext(ClientResponse responseContext, Throwable throwable) {
            this.responseContext = responseContext;
            this.throwables = new LinkedList<>();
            if (throwable != null) {
                if (InvocationInterceptorException.class.isInstance(throwable)) { // from PreInvocationInterceptor
                    for (Throwable t : throwable.getSuppressed()) {
                        throwables.add(t);
                    }
                } else {
                    throwables.add(throwable);
                }
            }
        }

        @Override
        public Optional<ClientResponseContext> getResponseContext() {
            return responseContext == null
                    ? Optional.empty()
                    : Optional.of(new InvocationInterceptorResponseContext(responseContext));
        }

        @Override
        public Deque<Throwable> getThrowables() {
            return throwables;
        }

        @Override
        public void resolve(Response response) {
            if (this.response != null) {
                LOGGER.warning(LocalizationMessages.POSTINVOCATION_INTERCEPTOR_MULTIPLE_RESOLVES());
                throw new IllegalStateException(LocalizationMessages.POSTINVOCATION_INTERCEPTOR_MULTIPLE_RESOLVES());
            }
            LOGGER.finer(LocalizationMessages.POSTINVOCATION_INTERCEPTOR_RESOLVE());
            this.response = response;
        }
    }

    private static class InvocationInterceptorResponseContext implements ClientResponseContext {
        private final ClientResponse clientResponse;

        private InvocationInterceptorResponseContext(ClientResponse clientResponse) {
            this.clientResponse = clientResponse;
        }

        @Override
        public int getStatus() {
            return clientResponse.getStatus();
        }

        @Override
        public void setStatus(int code) {
            clientResponse.setStatus(code);
        }

        @Override
        public Response.StatusType getStatusInfo() {
            return clientResponse.getStatusInfo();
        }

        @Override
        public void setStatusInfo(Response.StatusType statusInfo) {
            clientResponse.setStatusInfo(statusInfo);
        }

        @Override
        public MultivaluedMap<String, String> getHeaders() {
            return clientResponse.getHeaders();
        }

        @Override
        public String getHeaderString(String name) {
            return clientResponse.getHeaderString(name);
        }

        @Override
        public Set<String> getAllowedMethods() {
            return clientResponse.getAllowedMethods();
        }

        @Override
        public Date getDate() {
            return clientResponse.getDate();
        }

        @Override
        public Locale getLanguage() {
            return clientResponse.getLanguage();
        }

        @Override
        public int getLength() {
            return clientResponse.getLength();
        }

        @Override
        public MediaType getMediaType() {
            return clientResponse.getMediaType();
        }

        @Override
        public Map<String, NewCookie> getCookies() {
            return clientResponse.getCookies();
        }

        @Override
        public EntityTag getEntityTag() {
            return clientResponse.getEntityTag();
        }

        @Override
        public Date getLastModified() {
            return clientResponse.getLastModified();
        }

        @Override
        public URI getLocation() {
            return clientResponse.getLocation();
        }

        @Override
        public Set<Link> getLinks() {
            return clientResponse.getLinks();
        }

        @Override
        public boolean hasLink(String relation) {
            return clientResponse.hasLink(relation);
        }

        @Override
        public Link getLink(String relation) {
            return clientResponse.getLink(relation);
        }

        @Override
        public Link.Builder getLinkBuilder(String relation) {
            return clientResponse.getLinkBuilder(relation);
        }

        @Override
        public boolean hasEntity() {
            return clientResponse.hasEntity();
        }

        @Override
        public InputStream getEntityStream() {
            return clientResponse.getEntityStream();
        }

        @Override
        public void setEntityStream(InputStream input) {
            clientResponse.setEntityStream(input);
        }
    }

    private static ProcessingException createProcessingException(Throwable t) {
        final ProcessingException processingException = createProcessingException(LocalizationMessages.EXCEPTION_SUPPRESSED());
        processingException.addSuppressed(t);
        return processingException;
    }

    private static ProcessingException createProcessingException(String message) {
        return new InvocationInterceptorException(message);
    }

    private static class InvocationInterceptorException extends ProcessingException {
        private InvocationInterceptorException(String message) {
            super(message);
        }
    }

    private static RuntimeException suppressExceptions(Deque<Throwable> throwables) {
        if (throwables.size() == 1 && RuntimeException.class.isInstance(throwables.getFirst())) {
            throw (RuntimeException) throwables.getFirst();
        }
        final ProcessingException processingException = createProcessingException(LocalizationMessages.EXCEPTION_SUPPRESSED());
        for (Throwable throwable : throwables) {
            // The first throwable is also marked as the cause for visibility in logs
            if (processingException.getCause() == null) {
                processingException.initCause(throwable);
            }
            processingException.addSuppressed(throwable);
        }
        return processingException;
    }
}
