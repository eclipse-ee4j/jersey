/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.connector.internal;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.innate.ClientProxy;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

// TODO move to client

/**
 * Configuration object to use for configuring the client connectors and HTTP request processing.
 * This configuration provides settings to be handled by the connectors, mainly declared by {@link ClientProperties}.
 *
 * @param <E> the connector configuration subtype.
 */
public class ConnectorConfiguration<E extends ConnectorConfiguration<E>> {
    protected NullableRef<Integer> connectTimeout = NullableRef.of(0);
    protected NullableRef<Boolean> expect100Continue = NullableRef.empty();
    protected NullableRef<Long> expect100continueThreshold = NullableRef.of(
                                                ClientProperties.DEFAULT_EXPECT_100_CONTINUE_THRESHOLD_SIZE);
    protected NullableRef<Boolean> followRedirects = NullableRef.of(Boolean.TRUE);
    protected NullableRef<Object> proxyUri = NullableRef.empty();
    protected NullableRef<String> proxyUserName = NullableRef.empty();
    protected NullableRef<String> proxyPassword = NullableRef.empty();
    protected NullableRef<Integer> readTimeout = NullableRef.of(0);
    protected NullableRef<RequestEntityProcessing> requestEntityProcessing = NullableRef.empty();
    protected NullableRef<Supplier<SSLContext>> sslContextSupplier = NullableRef.empty();
    protected NullableRef<Integer> threadPoolSize = NullableRef.empty();

    /**
     * Use factory methods provided by each connector supporting this configuration object and its subclass instead.
     */
    protected ConnectorConfiguration() {

    }

    /**
     * Set and replace the values of current configuration by values of other configuration
     * if and only if the values of other configuration are set.
     *
     * @param other another configuration instance.
     */
    protected <X extends ConnectorConfiguration<?>> void setNonEmpty(X other) {
        this.connectTimeout.setNonEmpty(other.connectTimeout);
        this.expect100Continue.setNonEmpty(other.expect100Continue);
        this.expect100continueThreshold.setNonEmpty(other.expect100continueThreshold);
        this.followRedirects.setNonEmpty(other.followRedirects);
        this.proxyUri.setNonEmpty(other.proxyUri);
        this.proxyUserName.setNonEmpty(other.proxyUserName);
        this.proxyPassword.setNonEmpty(other.proxyPassword);
        this.readTimeout.setNonEmpty(other.readTimeout);
        this.requestEntityProcessing.setNonEmpty(other.requestEntityProcessing);
        this.sslContextSupplier.setNonEmpty(other.sslContextSupplier);
        this.threadPoolSize.setNonEmpty(other.threadPoolSize);
    }

    /**
     * Set the asynchronous thread-pool size. The property {@link ClientProperties#ASYNC_THREADPOOL_SIZE}
     * has precedence over this setting.
     *
     * @param threadPoolSize the size of the asynchronous thread-pool.
     * @return updated configuration.
     */
    public E asyncThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize.set(threadPoolSize);
        return self();
    }

    /**
     * Set connect timeout. The property {@link ClientProperties#CONNECT_TIMEOUT}
     * has precedence over this setting.
     *
     * @param millis timeout in milliseconds.
     * @return updated configuration.
     */
    public E connectTimeout(int millis) {
        connectTimeout.set(millis);
        return self();
    }

    /**
     * Allows for HTTP Expect:100-Continue.
     * The property {@link ClientProperties#EXPECT_100_CONTINUE} has precedence over this setting.
     *
     * @param enable allows for HTTP Expect:100-Continue or not.
     * @return updated configuration.
     */
    public E expect100Continue(boolean enable) {
        expect100Continue.set(enable);
        return self();
    }

    /**
     * Set the Expect:100-Continue content-length threshold size.
     * The {@link ClientProperties#EXPECT_100_CONTINUE_THRESHOLD_SIZE} property has precedence over this setting.
     *
     * @param size the content-length threshold.
     * @return updated configuration.
     */
    public E expect100ContinueThreshold(long size) {
        expect100ContinueThreshold(size);
        return self();
    }

    /**
     * Set to follow redirects. The property {@link ClientProperties#FOLLOW_REDIRECTS} has precedence over this setting.
     *
     * @param follow to follow or not to follow.
     * @return updated configuration.
     */
    public E followRedirects(boolean follow) {
        followRedirects.set(follow);
        return self();
    }

    /**
     * Set proxy password. The property {@link ClientProperties#PROXY_PASSWORD}
     * has precedence over this setting.
     *
     * @param proxyPassword the proxy password.
     * @return updated configuration.
     */
    public E proxyPassword(String proxyPassword) {
        this.proxyPassword.set(proxyPassword);
        return self();
    }

    /**
     * Set proxy username. The property {@link ClientProperties#PROXY_USERNAME}
     * has precedence over this setting.
     *
     * @param userName the proxy username.
     * @return updated configuration.
     */
    public E proxyUserName(String userName) {
        proxyUserName.set(userName);
        return self();
    }

    /**
     * Set proxy URI. The property {@link ClientProperties#PROXY_URI}
     * has precedence over this setting.
     *
     * @param proxyUri the proxy URI.
     * @return updated configuration.
     */
    public E proxyUri(String proxyUri) {
        this.proxyUri.set(proxyUri);
        return self();
    }

    /**
     * Set proxy URI. The property {@link ClientProperties#PROXY_URI}
     * has precedence over this setting.
     *
     * @param proxyUri the proxy URI.
     * @return updated configuration.
     */
    public E proxyUri(URI proxyUri) {
        this.proxyUri.set(proxyUri);
        return self();
    }

    /**
     * Set HTTP proxy. The property {@link ClientProperties#PROXY_URI}
     * has precedence over this setting.
     *
     * @param proxy the HTTP proxy.
     * @return updated configuration.
     */
    public E proxy(Proxy proxy) {
        this.proxyUri.set(proxy);
        return self();
    }

    /**
     * Set read timeout. The property {@link ClientProperties#READ_TIMEOUT}
     * has precedence over this setting.
     *
     * @param millis timeout in milliseconds.
     * @return updated configuration.
     */
    public E readTimeout(int millis) {
        readTimeout.set(millis);
        return self();
    }

    /**
     * Set the request entity processing type.
     *
     * @param requestEntityProcessing the request entity processing type.
     * @return the updated configuration.
     */
    public E requestEntityProcessing(RequestEntityProcessing requestEntityProcessing) {
        this.requestEntityProcessing.set(requestEntityProcessing);
        return self();
    }

    /**
     * Set the {@link SSLContext} supplier. The property {@link ClientProperties#SSL_CONTEXT_SUPPLIER} has precedence over
     * this setting.
     *
     * @param sslContextSupplier the {@link SSLContext} supplier.
     * @return the updated configuration.
     */
    public E sslContextSupplier(Supplier<SSLContext> sslContextSupplier) {
        this.sslContextSupplier.set(sslContextSupplier);
        return self();
    }

    /**
     * Return type-cast self.
     * @return self.
     */
    @SuppressWarnings("unchecked")
    protected E self() {
        return (E) this;
    }

    /**
     * <p>
     * A reference to a value. The reference can be empty, but unlike the {@code Optional}, once a value is set,
     * it never can be empty again. The {@code null} value is treated as a non-empty value of null.
     * </p><p>
     * This {@code null}
     * can be used to override some previous configuration value, to distinguish the intentional {@code null} override
     * from an empty (non-set) configuration value.
     * </p>
     * @param <T> type of the value.
     */
    protected static class NullableRef<T> implements org.glassfish.jersey.internal.util.collection.Ref<T> {

        private NullableRef() {
            // use factory methods;
        }

        /**
         * Return a new empty reference.
         *
         * @return an empty reference.
         * @param <T> The type of the empty value.
         */
        public static <T> NullableRef<T> empty() {
            return new NullableRef<>();
        }

        /**
         * Return a reference of a given value.
         *
         * @param value the value this reference refers to.*
         * @return a new reference to a given value.
         * @param <T> type of the value.
         */
        public static <T> NullableRef<T> of(T value) {
            NullableRef<T> ref = new NullableRef<>();
            ref.set(value);
            return ref;
        }

        private boolean empty = true;
        private T ref = null;

        @Override
        public void set(T value) {
            empty = false;
            ref = value;
        }

        /**
         * Set or replace the value if other value is set.
         * @param other a reference to another value.
         */
        public void setNonEmpty(NullableRef<T> other) {
            other.ifPresent(this::set);
        }

        @Override
        public T get() {
            return ref;
        }

        /**
         * Run action if and only if the condition applies.
         *
         * @param predicate the condition to be met.
         * @param action the action to run if condition is met.
         */
        public void iff(Predicate<T> predicate, Runnable action) {
            if (predicate.test(ref)) {
                action.run();
            }
        }

        /**
         * If it is empty, sets the {@code value} value. Keeps the original value, otherwise.
         *
         * @param value the value to be set if empty.
         */
        public void ifEmptySet(T value) {
            if (empty) {
                set(value);
            }
        }

        /**
         * If a value is present, performs the given action with the value,
         * otherwise does nothing.
         *
         * @param action the action to be performed, if a value is present
         * @throws NullPointerException if value is present and the given action is
         *         {@code null}
         */
        public void ifPresent(Consumer<? super T> action) {
            if (!empty) {
                action.accept(ref);
            }
        }

        /**
         * If a value is present, performs the given action with the value,
         * otherwise performs the given empty-based action.
         *
         * @param action the action to be performed, if a value is present
         * @param emptyAction the empty-based action to be performed, if no value is
         *        present
         * @throws NullPointerException if a value is present and the given action
         *         is {@code null}, or no value is present and the given empty-based
         *         action is {@code null}.
         */
        public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
            if (!empty) {
                action.accept(ref);
            } else {
                emptyAction.run();
            }
        }

        /**
         * If a value is  not present, returns {@code true}, otherwise
         * {@code false}.
         *
         * @return  {@code true} if a value is not present, otherwise {@code false}
         */
        public boolean isEmpty() {
            return empty;
        }

        /**
         * If a value is present, returns {@code true}, otherwise {@code false}.
         *
         * @return {@code true} if a value is present, otherwise {@code false}
         */
        public boolean isPresent() {
            return !empty;
        }


        @Override
        public int hashCode() {
            return Objects.hash(ref, empty);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof NullableRef)) {
                return false;
            }

            NullableRef<?> that = (NullableRef<?>) o;
            return Objects.equals(empty, that.empty) && Objects.equals(ref, that.ref);
        }

        @Override
        public String toString() {
            return empty ? "<empty>" : ref == null ? "<null>" : ref.toString();
        }
    }

    protected interface ReadWrite<CC extends ConnectorConfiguration<CC>> {
        /**
         * Return the thread-pool size setting.
         *
         * @return the thread pool size setting.
         */
        public default Integer asyncThreadPoolSize() {
            return self().threadPoolSize.get();
        }

        /**
         * Update connect timeout value based on request properties settings.
         *
         * @param request the current HTTP client request.
         * @return the updated configuration.
         */
        public default CC connectTimeout(ClientRequest request) {
            self().connectTimeout.set(request.resolveProperty(ClientProperties.CONNECT_TIMEOUT, self().connectTimeout.get()));
            return self();
        }

        /**
         * Get the value of connect timeout setting.
         *
         * @return connect timeout value.
         */
        public default int connectTimeout() {
            return self().connectTimeout.get();
        }

        /**
         * Utility method to create a new instance of configuration to preserve the settings of previous configuration.
         *
         * @return a new instance of the configuration.
         */
        public default CC copy() {
            CC config = instance();
            config.setNonEmpty(self());
            return config;
        }

        /**
         * Update the {@link #expect100Continue(boolean)} from the HTTP client request.
         *
         * @param request the HTTP client request.
         * @return the Expect: 100-Continue support value.
         */
        public default Boolean expect100Continue(ClientRequest request) {
            final Boolean expectContinueActivated = request.resolveProperty(ClientProperties.EXPECT_100_CONTINUE, Boolean.class);
            if (expectContinueActivated != null) {
                self().expect100Continue.set(expectContinueActivated);
            }
            return self().expect100Continue.get();
        }

        /**
         * Update the {@link #expect100ContinueThreshold(long)} from the HTTP client request.
         *
         * @param request the HTTP client request.
         * @return the content length threshold size.
         */
        public default long expect100ContinueThreshold(ClientRequest request) {
            self().expect100continueThreshold.set(request.resolveProperty(
                    ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE,
                    self().expect100continueThreshold.get()));
            return self().expect100continueThreshold.get();
        }

        /**
         * Update the {@link #followRedirects(boolean)} setting from the HTTP client request. The default is {@code true}.
         *
         * @param request the HTTP client request.
         * @return follow redirects setting.
         */
        public default boolean followRedirects(ClientRequest request) {
            self().followRedirects.set(request.resolveProperty(ClientProperties.FOLLOW_REDIRECTS, self().followRedirects.get()));
            return self().followRedirects.get();
        }

        /**
         * Get the value of the follow redirects setting.
         *
         * @return whether to follow redirects or not.
         */
        public default boolean followRedirects() {
            return self().followRedirects.get();
        }

        /**
         * Create optional client proxy information based on the proxy information set in the configuration
         * or the HTTP client request. The used settings are {@link #proxy(Proxy)},
         * {@link #proxyUri(URI)}, {@link #proxyUri(String)}, {@link #proxyUserName(String)},
         * and {@link #proxyPassword(String)}.
         *
         * @param request the HTTP client request,
         * @param requestUri the HTTP request URI. It can differ from the URI used in the request, based on other
         *                   information set by the HTTP client request.
         * @return the optional client proxy.
         */
        public default Optional<ClientProxy> proxy(ClientRequest request, URI requestUri) {
            Optional<ClientProxy> proxy = ClientProxy.proxyFromRequest(request);
            if (!proxy.isPresent() && self().proxyUri.isPresent()) {
                // TODO support in ClientProxy
                Map<String, Object> properties = new HashMap<>();
                properties.put(ClientProperties.PROXY_URI, self().proxyUri.get());
                properties.put(ClientProperties.PROXY_USERNAME, self().proxyUserName.get());
                properties.put(ClientProperties.PROXY_PASSWORD, self().proxyPassword.get());
                Configuration configuration = (Configuration) java.lang.reflect.Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        new Class[]{Configuration.class}, new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                switch (method.getName()) {
                                    case "getProperties":
                                        return properties;
                                }
                                return null;
                            }
                        });
                proxy = ClientProxy.proxyFromConfiguration(configuration);
            }
            if (!proxy.isPresent()) {
                proxy = ClientProxy.proxyFromProperties(requestUri);
            }
            return proxy;
        }

        /**
         * Update {@link #readTimeout(int) read timeout} based on the HTTP request properties.
         *
         * @param request the current HTTP client request.
         * @return updated configuration.
         */
        public default CC readTimeout(ClientRequest request) {
            self().readTimeout.set(request.resolveProperty(ClientProperties.READ_TIMEOUT, self().readTimeout.get()));
            return self();
        }

        /**
         * Get the value of preset {@link #readTimeout(int)}.
         *
         * @return the read timeout milliseconds.
         */
        public default int readTimeout() {
            return self().readTimeout.get();
        }

        /**
         * Get the {@link RequestEntityProcessing} updated by the HTTP client request.
         *
         * @param request the HTTP client request.
         * @return the RequestEntityProcessing type.
         */
        public default RequestEntityProcessing requestEntityProcessing(ClientRequest request) {
            RequestEntityProcessing entityProcessing = request.resolveProperty(
                    ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.class);
            if (entityProcessing == null) {
                entityProcessing = self().requestEntityProcessing.get();
            }
            return entityProcessing;
        }

        /**
         * Get {@link SSLContext} either from the {@link ClientProperties#SSL_CONTEXT_SUPPLIER}, or from this configuration,
         * or from the {@link Client#getSslContext()} in this order.
         *
         * @param client the client used to get the {@link SSLContext}.
         * @param request the request used to get the {@link SSLContext}.
         * @return the {@link SSLContext}.
         */
        public default SSLContext sslContext(Client client, ClientRequest request) {
            Supplier<SSLContext> supplier = request.resolveProperty(ClientProperties.SSL_CONTEXT_SUPPLIER, Supplier.class);
            if (supplier == null) {
                supplier = self().sslContextSupplier.get();
            }
            return supplier == null ? client.getSslContext() : supplier.get();
        }

        /**
         * Return a new instance of configuration.
         * @return a new instance of configuration.
         */
        public CC instance();

        /**
         * Return typed-cast self.
         * @return self.
         */
        public CC self();
    }
}
