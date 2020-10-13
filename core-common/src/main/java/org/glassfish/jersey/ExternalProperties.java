package org.glassfish.jersey;

import org.glassfish.jersey.internal.util.PropertiesClass;
import org.glassfish.jersey.internal.util.PropertiesHelper;

import javax.ws.rs.RuntimeType;
import java.util.Map;

@PropertiesClass
public final class ExternalProperties {

    // JDK Properties

    public static final String HTTP_PROXY_HOST = "http.proxyHost";

    public static final String HTTP_PROXY_PORT = "http.proxyPort";

    public static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    public static final String HTTP_AGENT = "http.agent";

    public static final String HTTP_KEEPALIVE = "http.keepalive";

    public static final String HTTP_MAX_CONNECTIONS = "http.maxConnections";

    public static final String HTTP_MAX_REDIRECTS = "http.maxRedirects";

    public static final String HTTP_AUTH_DIGEST_VALIDATE_SERVER = "http.auth.digest.validateServer";

    public static final String HTTP_AUTH_DIGEST_VALIDATE_PROXY = "http.auth.digest.validateProxy";

    public static final String HTTP_AUTH_DIGEST_CNONCE_REPEAT = "http.auth.digest.cnonceRepeat";

    public static final String HTTP_AUTH_NTLM_DOMAIN = "http.auth.ntlm.domain";

    public static final String HTTPS_PROXY_HOST = "https.ProxyHost";

    public static final String HTTPS_PROXY_PORT = "https.ProxyPort";

    public static final String FTP_PROXY_HOST = "ftp.proxyHost";

    public static final String FTP_PROXY_PORT = "ftp.proxyPort";

    public static final String FTP_NON_PROXY_HOSTS = "ftp.nonProxyHosts";

    public static final String SOCKS_PROXY_HOST = "socksProxyHost";

    public static final String SOCKS_PROXY_PORT = "socksProxyPort";

    public static final String SOCKS_PROXY_VERSION = "socksProxyVersion";

    public static final String JAVA_NET_SOCKS_USERNAME = "java.net.socks.username";

    public static final String JAVA_NET_SOCKS_PASSWORD = "java.net.socks.password";

    public static final String JAVA_NET_USE_SYSTEM_PROXIES = "java.net.useSystemProxies";

    public static final String NETWORK_ADDRESS_CACHE_TTL = "networkaddress.cache.ttl";

    public static final String NETWORK_ADDRESS_CACHE_NEGATIVE_TTL = "networkaddress.cache.negative.ttl";

    // JAX-B Properties

    public static final String JAXB_CONTEXT_FACTORY = "jakarta.xml.bind.JAXBContextFactory";

    public static final String JAXB_ENCODING = "jaxb.encoding";

    public static final String JAXB_FORMATTED_OUTPUT = "jaxb.formatted.output";

    public static final String JAXB_SCHEMA_LOCATION = "jaxb.schemaLocation";

    public static final String JAXB_NO_NAMESPACE_SCHEMA_LOCATION = "jaxb.noNamespaceSchemaLocation";

    public static final String JAXB_FRAGMENT = "jaxb.fragment";

    // JSON-B Properties

    public static final String JSONB_FORMATTING = "jsonb.formatting";

    public static final String JSONB_ENCODING = "jsonb.encoding";

    public static final String JSONB_PROPERTY_NAMING_STRATEGY = "jsonb.property-naming-strategy";

    public static final String JSONB_PROPERTY_ORDER_STRATEGY = "jsonb.property-order-strategy";

    public static final String JSONB_NULL_VALUES = "jsonb.null-values";

    public static final String JSONB_STRICT_IJSON = "jsonb.strict-ijson";

    public static final String JSONB_PROPERTY_VISIBILITY_STRATEGY = "jsonb.property-visibility-strategy";

    public static final String JSONB_ADAPTERS = "jsonb.adapters";

    public static final String JSONB_SERIALIZERS = "jsonb.serializers";

    public static final String JSONB_DESERIALIZERS = "jsonb.derializers";

    public static final String JSONB_BINARY_DATA_STRATEGY = "jsonb.binary-data-strategy";

    public static final String JSONB_DATE_FORMAT = "jsonb.date-format";

    public static final String JSONB_LOCALE = "jsonb.locale";

    /**
     * Prevent instantiation.
     */
    private ExternalProperties() {
    }

    /**
     * Get the value of the specified property.
     *
     * If the property is not set or the actual property value type is not compatible with the specified type, the method will
     * return {@code null}.
     *
     * @param properties    Map of properties to get the property value from.
     * @param propertyName  Name of the property.
     * @param type          Type to retrieve the value as.
     * @return              Value of the property or {@code null}.
     *
     * @since 2.8
     */
    public static Object getValue(final Map<String, ?> properties, final String propertyName, final Class<?> type) {
        return PropertiesHelper.getValue(properties, propertyName, type, null);
    }

    /**
     * Get the value of the specified property.
     *
     * If the property is not set or the real value type is not compatible with {@code defaultValue} type,
     * the specified {@code defaultValue} is returned. Calling this method is equivalent to calling
     * {@code CommonProperties.getValue(properties, key, defaultValue, (Class&lt;T&gt;) defaultValue.getClass())}
     *
     * @param properties    Map of properties to get the property value from.
     * @param propertyName  Name of the property.
     * @param defaultValue  Default value if property is not registered
     * @param <T>           Type of the property value.
     * @return              Value of the property or {@code null}.
     *
     * @since 2.8
     */
    public static <T> T getValue(final Map<String, ?> properties, final String propertyName, final T defaultValue) {
        return PropertiesHelper.getValue(properties, propertyName, defaultValue, null);
    }

    /**
     * Get the value of the specified property.
     *
     * If the property is not set or the real value type is not compatible with {@code defaultValue} type,
     * the specified {@code defaultValue} is returned. Calling this method is equivalent to calling
     * {@code CommonProperties.getValue(properties, runtimeType, key, defaultValue, (Class&lt;T&gt;) defaultValue.getClass())}
     *
     * @param properties    Map of properties to get the property value from.
     * @param runtime       Runtime type which is used to check whether there is a property with the same
     *                      {@code key} but post-fixed by runtime type (<tt>.server</tt>
     *                      or {@code .client}) which would override the {@code key} property.
     * @param propertyName  Name of the property.
     * @param defaultValue  Default value if property is not registered
     * @param <T>           Type of the property value.
     * @return              Value of the property or {@code null}.
     *
     * @since 2.8
     */
    public static <T> T getValue(final Map<String, ?> properties,
                                 final RuntimeType runtime,
                                 final String propertyName,
                                 final T defaultValue) {
        return PropertiesHelper.getValue(properties, runtime, propertyName, defaultValue, null);
    }

    /**
     * Get the value of the specified property.
     *
     * If the property is not set or the real value type is not compatible with the specified value type,
     * returns {@code defaultValue}.
     *
     * @param properties    Map of properties to get the property value from.
     * @param runtime       Runtime type which is used to check whether there is a property with the same
     *                      {@code key} but post-fixed by runtime type (<tt>.server</tt>
     *                      or {@code .client}) which would override the {@code key} property.
     * @param propertyName  Name of the property.
     * @param defaultValue  Default value if property is not registered
     * @param type          Type to retrieve the value as.
     * @param <T>           Type of the property value.
     * @return              Value of the property or {@code null}.
     *
     * @since 2.8
     */
    public static <T> T getValue(final Map<String, ?> properties,
                                 final RuntimeType runtime,
                                 final String propertyName,
                                 final T defaultValue,
                                 final Class<T> type) {
        return PropertiesHelper.getValue(properties, runtime, propertyName, defaultValue, type, null);
    }

    /**
     * Get the value of the specified property.
     *
     * If the property is not set or the actual property value type is not compatible with the specified type, the method will
     * return {@code null}.
     *
     * @param properties    Map of properties to get the property value from.
     * @param runtime       Runtime type which is used to check whether there is a property with the same
     *                      {@code key} but post-fixed by runtime type (<tt>.server</tt>
     *                      or {@code .client}) which would override the {@code key} property.
     * @param propertyName  Name of the property.
     * @param type          Type to retrieve the value as.
     * @param <T>           Type of the property value.
     * @return              Value of the property or {@code null}.
     *
     * @since 2.8
     */
    public static <T> T getValue(final Map<String, ?> properties,
                                 final RuntimeType runtime,
                                 final String propertyName,
                                 final Class<T> type) {
        return PropertiesHelper.getValue(properties, runtime, propertyName, type, null);
    }

}
