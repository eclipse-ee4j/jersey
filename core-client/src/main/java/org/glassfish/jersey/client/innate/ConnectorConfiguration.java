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

package org.glassfish.jersey.client.innate;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.innate.http.SSLParamConfigurator;
import org.glassfish.jersey.internal.PropertiesResolver;

import javax.net.ssl.SSLContext;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import java.net.Proxy;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Configuration object to use for configuring the client connectors and HTTP request processing.
 * This configuration provides settings to be handled by the connectors, mainly declared by {@link ClientProperties}.
 *
 * @param <E> the connector configuration subtype.
 */
public class ConnectorConfiguration<E extends ConnectorConfiguration<E>> {
    protected final NullableRef<Integer> connectTimeout = NullableRef.empty();
    protected final NullableRef<Boolean> expect100Continue = NullableRef.empty();
    protected final NullableRef<Long> expect100continueThreshold = NullableRef.empty();
    protected final NullableRef<Boolean> followRedirects = NullableRef.empty();
    protected final NullableRef<String> prefix = NullableRef.empty();
    protected final NullableRef<Object> proxyUri = NullableRef.empty();
    protected final NullableRef<String> proxyUserName = NullableRef.empty();
    protected final NullableRef<String> proxyPassword = NullableRef.empty();
    protected final NullableRef<Integer> readTimeout = NullableRef.empty();
    protected final NullableRef<RequestEntityProcessing> requestEntityProcessing = NullableRef.empty();
    protected final NullableRef<String> sniHostname = NullableRef.empty();
    protected final NullableRef<Supplier<SSLContext>> sslContextSupplier = NullableRef.empty();
    protected final NullableRef<Integer> threadPoolSize = NullableRef.empty();

    /**
     * Use factory methods provided by each connector supporting this configuration object and its subclass instead.
     */
    protected ConnectorConfiguration() {
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
        expect100continueThreshold.set(size);
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
     * <p>
     * Set the prefix for the configuration properties used by Client/Request to configure and override the settings.
     * For instance, if the prefix would be {@code com.example.MyProject.}, the property {@link #connectTimeout(int)}
     * is overridden only by properties with key starting by the prefix,
     * i.e. for {@link ClientProperties#CONNECT_TIMEOUT},
     * the property key {@code com.example.MyProject.jersey.config.client.connectTimeout} would override the setting.
     * </p>
     * <p>
     *     The prefix can be used to override the settings by the System property set specifically for the
     *     prefixed connector. See {@link org.glassfish.jersey.CommonProperties#ALLOW_SYSTEM_PROPERTIES_PROVIDER}
     *     for enabling System properties usage.
     * </p>
     * <p>
     * The default configuration prefix is empty.
     * </p>
     *
     * @param prefix the non-null prefix.
     * @throws NullPointerException if the prefix is null.
     * @return updated configuration.
     */
    public E prefix(String prefix) {
        this.prefix.set(Objects.requireNonNull(prefix));
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

    public E sniHostName(String sniHostname) {
        this.sniHostname.set(sniHostname);
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
         * Return the value if present, the {@code other} otherwise.
         *
         * @param other the value if not present
         * @return inner value if present or the other otherwise.
         */
        public T ifPresentOrElse(T other) {
            return empty ? other : ref;
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

    protected interface Read<CC extends ConnectorConfiguration<CC> & Read<CC>>
            extends SSLParamConfigurator.SSLParamConfiguratorConfiguration {

        /**
         * Set and replace the values of current configuration by values of other configuration
         * if and only if the values of other configuration are set.
         *
         * @param other another configuration instance.
         */
        public default <X extends ConnectorConfiguration<?>> void setNonEmpty(X other) {
            me().connectTimeout.setNonEmpty(other.connectTimeout);
            me().expect100Continue.setNonEmpty(other.expect100Continue);
            me().expect100continueThreshold.setNonEmpty(other.expect100continueThreshold);
            me().followRedirects.setNonEmpty(other.followRedirects);
            me().prefix.setNonEmpty(other.prefix);
            me().proxyUri.setNonEmpty(other.proxyUri);
            me().proxyUserName.setNonEmpty(other.proxyUserName);
            me().proxyPassword.setNonEmpty(other.proxyPassword);
            me().readTimeout.setNonEmpty(other.readTimeout);
            me().requestEntityProcessing.setNonEmpty(other.requestEntityProcessing);
            me().sniHostname.setNonEmpty(other.sniHostname);
            me().sslContextSupplier.setNonEmpty(other.sslContextSupplier);
            me().threadPoolSize.setNonEmpty(other.threadPoolSize);
        }

        /**
         * Return the thread-pool size setting.
         *
         * @return the thread pool size setting.
         */
        public default Integer asyncThreadPoolSize() {
            return me().threadPoolSize.get();
        }

        /**
         * Update connect timeout value based on request properties settings.
         *
         * @param request the current HTTP client request.
         * @return the updated configuration.
         */
        public default int connectTimeout(ClientRequest request) {
            me().connectTimeout.set(
                    request.resolveProperty(prefixed(ClientProperties.CONNECT_TIMEOUT), me().connectTimeout.get())
            );
            return me().connectTimeout.get();
        }

        /**
         * Get the value of connect timeout setting.
         *
         * @return connect timeout value.
         */
        public default int connectTimeout() {
            return me().connectTimeout.get();
        }

        /**
         * Sets the default value. The default methods cannot be set on instances passed by the customers using
         * {@link ClientProperties#CONNECTOR_CONFIGURATION} since they would override the values previously set
         * by the connector configuration object.
         * @return the initialized configuration object.
         */
        public default CC init() {
            me().connectTimeout(0)
                    .expect100ContinueThreshold(ClientProperties.DEFAULT_EXPECT_100_CONTINUE_THRESHOLD_SIZE)
                    .followRedirects(Boolean.TRUE)
                    .prefix("")
                    .readTimeout(0);
            return me();
        }

        /**
         * Utility method to create a new instance of configuration to preserve the settings of previous configuration.
         *
         * @return a new instance of the configuration.
         */
        public default CC copy() {
            CC config = instance();
            config.init();
            config.setNonEmpty(me());
            return config;
        }

        public default CC copyFromClient(Configuration configuration) {
            CC clientConfiguration = copy();
            final Map<String, Object> properties = configuration.getProperties();
            Object configProp = properties.get(clientConfiguration.prefixed(ClientProperties.CONNECTOR_CONFIGURATION));
            if (configProp != null) {
                ConnectorConfiguration<?> clientCfg = (ConnectorConfiguration<?>) configProp;
                if (me().prefix.equals(clientCfg.prefix) || clientCfg.prefix.get() == null) {
                    clientConfiguration.setNonEmpty(clientCfg);
                }
            } else {
                configProp = properties.get(ClientProperties.CONNECTOR_CONFIGURATION);
                if (configProp != null && me().prefix.equals(((ConnectorConfiguration<?>) configProp).prefix)) {
                    clientConfiguration.setNonEmpty((ConnectorConfiguration<?>) configProp);
                }
            }
            return clientConfiguration;
        }

        public default CC copyFromRequest(ClientRequest request) {
            CC requestConfiguration = copy();
            Object configProp = request.getProperty(prefixed(ClientProperties.CONNECTOR_CONFIGURATION));
            if (configProp != null) {
                ConnectorConfiguration<?> requestCfg = (ConnectorConfiguration<?>) configProp;
                if (me().prefix.equals(requestCfg.prefix) || requestCfg.prefix.get() == null) {
                    requestConfiguration.setNonEmpty(requestCfg);
                }
            } else {
                configProp = request.getProperty(ClientProperties.CONNECTOR_CONFIGURATION);
                if (configProp != null && me().prefix.equals(((ConnectorConfiguration<?>) configProp).prefix)) {
                    requestConfiguration.setNonEmpty((ConnectorConfiguration<?>) configProp);
                }
            }
            return requestConfiguration;
        }

        @Override
        default String getSniHostNameProperty(Configuration configuration) {
            Object property = configuration.getProperty(prefixed(ClientProperties.SNI_HOST_NAME));
            if (property == null) {
                property = configuration.getProperty(prefixed(ClientProperties.SNI_HOST_NAME.toLowerCase(Locale.ROOT)));
            }
            return property == null ? me().sniHostname.get() : (String) property;
        }

        /**
         * Update the {@link #expect100Continue(boolean)} from the HTTP client request.
         *
         * @param request the HTTP client request.
         * @return the Expect: 100-Continue support value.
         */
        public default Boolean expect100Continue(ClientRequest request) {
            final Boolean expectContinueActivated =
                    request.resolveProperty(prefixed(ClientProperties.EXPECT_100_CONTINUE), Boolean.class);
            if (expectContinueActivated != null) {
                me().expect100Continue.set(expectContinueActivated);
            }
            return me().expect100Continue.get();
        }

        /**
         * Update the {@link #expect100ContinueThreshold(long)} from the HTTP client request.
         *
         * @param request the HTTP client request.
         * @return the content length threshold size.
         */
        public default long expect100ContinueThreshold(ClientRequest request) {
            me().expect100continueThreshold.set(
                    request.resolveProperty(prefixed(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE),
                            me().expect100continueThreshold.get())
            );
            return me().expect100continueThreshold.get();
        }

        /**
         * Update the {@link #followRedirects(boolean)} setting from the HTTP client request. The default is {@code true}.
         *
         * @param request the HTTP client request.
         * @return follow redirects setting.
         */
        public default boolean followRedirects(ClientRequest request) {
            me().followRedirects.set(
                    request.resolveProperty(prefixed(ClientProperties.FOLLOW_REDIRECTS), me().followRedirects.get())
            );
            return me().followRedirects.get();
        }

        /**
         * Get the value of the follow redirects setting. The default is {@code true}.
         *
         * @return whether to follow redirects or not.
         */
        public default boolean followRedirects() {
            return me().followRedirects.get();
        }

        public default Configuration prefixedConfiguration(Configuration configuration) {
            return me().prefix.get().isEmpty() ? configuration : new PrefixedConfiguration(me().prefix.get(), configuration);
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
            Optional<ClientProxy> proxy = ClientProxy.proxyFromRequest(
                    me().prefix.get().isEmpty()
                        ? request
                        : new PrefixedPropertiesResolver(me().prefix.get(), request)
            );
            if (!proxy.isPresent() && me().proxyUri.isPresent()) {
                final Map<String, Object> properties = me().prefix.get().isEmpty()
                        ? new HashMap<>()
                        : new PrefixedMap<>(me().prefix.get(), new HashMap<>());
                properties.put(me().prefix.get() + ClientProperties.PROXY_URI, me().proxyUri.get());
                properties.put(me().prefix.get() + ClientProperties.PROXY_USERNAME, me().proxyUserName.get());
                properties.put(me().prefix.get() + ClientProperties.PROXY_PASSWORD, me().proxyPassword.get());
                request.getPropertyNames().forEach(k -> properties.put(k, request.getProperty(k)));
                proxy = ClientProxy.proxyFromProperties(properties);
            }
            if (!proxy.isPresent()) {
                proxy = ClientProxy.proxyFromUri(requestUri);
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
            me().readTimeout.set(request.resolveProperty(prefixed(ClientProperties.READ_TIMEOUT), me().readTimeout.get()));
            return me();
        }

        /**
         * Get the value of preset {@link #readTimeout(int)}.
         *
         * @return the read timeout milliseconds.
         */
        public default int readTimeout() {
            return me().readTimeout.get();
        }

        /**
         * Get the {@link RequestEntityProcessing} updated by the HTTP client request.
         *
         * @param request the HTTP client request.
         * @return the RequestEntityProcessing type.
         */
        public default RequestEntityProcessing requestEntityProcessing(ClientRequest request) {
            RequestEntityProcessing entityProcessing =
                    request.resolveProperty(prefixed(ClientProperties.REQUEST_ENTITY_PROCESSING), RequestEntityProcessing.class);
            if (entityProcessing == null) {
                entityProcessing = me().requestEntityProcessing.get();
            }
            return entityProcessing;
        }

        @Override
        default String resolveSniHostNameProperty(PropertiesResolver resolver) {
            String property = resolver.resolveProperty(prefixed(ClientProperties.SNI_HOST_NAME), String.class);
            if (property == null) {
                property = resolver.resolveProperty(
                        prefixed(ClientProperties.SNI_HOST_NAME.toLowerCase(Locale.ROOT)), String.class);
            }
            return property == null ? me().sniHostname.get() : property;
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
            @SuppressWarnings("unchecked")
            Supplier<SSLContext> supplier =
                    request.resolveProperty(prefixed(ClientProperties.SSL_CONTEXT_SUPPLIER), Supplier.class);
            if (supplier == null) {
                supplier = me().sslContextSupplier.get();
            }
            return supplier == null ? client.getSslContext() : supplier.get();
        }

        public default String prefixed(String propertyName) {
            return me().prefix.get() + propertyName;
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
        public CC me();
    }


    /**
     * A properties map that works with prefixed properties.
     *
     * @param <V> Object type.
     */
    private static class PrefixedMap<V> implements Map<String, V> {
        private final Map<String, V> inner;
        private final String prefix;

        private PrefixedMap(String prefix, Map<String, V> inner) {
            this.inner = inner;
            this.prefix = prefix;
        }

        @Override
        public int size() {
            return inner.size();
        }

        @Override
        public boolean isEmpty() {
            return inner.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return inner.containsKey(prefix + key);
        }

        @Override
        public boolean containsValue(Object value) {
            return inner.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return inner.get(prefix + key);
        }

        @Override
        public V put(String key, V value) {
            return inner.put(key, value);
        }

        @Override
        public V remove(Object key) {
            return inner.remove(prefix + key);
        }

        @Override
        public void putAll(Map<? extends String, ? extends V> m) {
            inner.putAll(m);
        }

        @Override
        public void clear() {
            inner.clear();
        }

        @Override
        public Set<String> keySet() {
            return inner.keySet();
        }

        @Override
        public Collection<V> values() {
            return inner.values();
        }

        @Override
        public Set<Entry<String, V>> entrySet() {
            return inner.entrySet();
        }
    }

    /**
     * Properties resolver that resolves prefixed properties.
     */
    private static class PrefixedPropertiesResolver implements PropertiesResolver {
        private final String prefix;
        private final PropertiesResolver resolver;

        private PrefixedPropertiesResolver(String prefix, PropertiesResolver resolver) {
            this.prefix = prefix;
            this.resolver = resolver;
        }

        @Override
        public <T> T resolveProperty(String name, Class<T> type) {
            return resolver.resolveProperty(prefix + name, type);
        }

        @Override
        public <T> T resolveProperty(String name, T defaultValue) {
            return resolver.resolveProperty(prefix + name, defaultValue);
        }
    }

    protected static class PrefixedConfiguration implements Configuration {
        private final String prefix;
        private final Configuration inner;

        private PrefixedConfiguration(String prefix, Configuration inner) {
            this.prefix = prefix;
            this.inner = inner;
        }

        @Override
        public RuntimeType getRuntimeType() {
            return inner.getRuntimeType();
        }

        @Override
        public Map<String, Object> getProperties() {
            return new PrefixedMap<>(prefix, inner.getProperties());
        }

        @Override
        public Object getProperty(String name) {
            return inner.getProperty(prefix + name);
        }

        @Override
        public Collection<String> getPropertyNames() {
            return inner.getPropertyNames();
        }

        @Override
        public boolean isEnabled(Feature feature) {
            return inner.isEnabled(feature);
        }

        @Override
        public boolean isEnabled(Class<? extends Feature> featureClass) {
            return inner.isEnabled(featureClass);
        }

        @Override
        public boolean isRegistered(Object component) {
            return inner.isRegistered(component);
        }

        @Override
        public boolean isRegistered(Class<?> componentClass) {
            return inner.isRegistered(componentClass);
        }

        @Override
        public Map<Class<?>, Integer> getContracts(Class<?> componentClass) {
            return inner.getContracts(componentClass);
        }

        @Override
        public Set<Class<?>> getClasses() {
            return inner.getClasses();
        }

        @Override
        public Set<Object> getInstances() {
            return inner.getInstances();
        }
    }
}
