/*
 * Copyright (c) 2013, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey;

import java.util.HashMap;
import java.util.Map;

import jakarta.ws.rs.RuntimeType;

import org.glassfish.jersey.internal.util.PropertiesClass;
import org.glassfish.jersey.internal.util.PropertiesHelper;

/**
 * Common (server/client) Jersey configuration properties.
 *
 * @author Michal Gajdos
 * @author Libor Kramolis
 */
@PropertiesClass
public final class CommonProperties {

    private static final Map<String, String> LEGACY_FALLBACK_MAP = new HashMap<String, String>();

    static {
        LEGACY_FALLBACK_MAP.put(CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER_CLIENT,
                "jersey.config.contentLength.buffer.client");
        LEGACY_FALLBACK_MAP.put(CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER_SERVER,
                "jersey.config.contentLength.buffer.server");
        LEGACY_FALLBACK_MAP.put(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE_CLIENT,
                "jersey.config.disableAutoDiscovery.client");
        LEGACY_FALLBACK_MAP.put(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE_SERVER,
                "jersey.config.disableAutoDiscovery.server");
        LEGACY_FALLBACK_MAP.put(CommonProperties.JSON_PROCESSING_FEATURE_DISABLE_CLIENT,
                "jersey.config.disableJsonProcessing.client");
        LEGACY_FALLBACK_MAP.put(CommonProperties.JSON_PROCESSING_FEATURE_DISABLE_SERVER,
                "jersey.config.disableJsonProcessing.server");
        LEGACY_FALLBACK_MAP.put(CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE_CLIENT,
                "jersey.config.disableMetainfServicesLookup.client");
        LEGACY_FALLBACK_MAP.put(CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE_SERVER,
                "jersey.config.disableMetainfServicesLookup.server");
        LEGACY_FALLBACK_MAP.put(CommonProperties.MOXY_JSON_FEATURE_DISABLE_CLIENT,
                "jersey.config.disableMoxyJson.client");
        LEGACY_FALLBACK_MAP.put(CommonProperties.MOXY_JSON_FEATURE_DISABLE_SERVER,
                "jersey.config.disableMoxyJson.server");
    }

    /**
     * Property which allows (if true) default System properties configuration provider.
     *
     * If an external properties provider is used, the system properties are not used.
     *
     * Shall be set to turn on the ability to propagate system properties to Jersey configuration.
     * @since 2.29
     */
    public static final String ALLOW_SYSTEM_PROPERTIES_PROVIDER = "jersey.config.allowSystemPropertiesProvider";

    /**
     * If {@code true} then disable feature auto discovery globally on client/server.
     * <p>
     * By default, auto discovery is automatically enabled. The value of this property may be overridden by the client/server
     * variant of this property.
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String FEATURE_AUTO_DISCOVERY_DISABLE = "jersey.config.disableAutoDiscovery";

    /**
     * Client-specific version of {@link CommonProperties#FEATURE_AUTO_DISCOVERY_DISABLE}.
     *
     * If present, it overrides the generic one for the client environment.
     * @since 2.8
     */
    public static final String FEATURE_AUTO_DISCOVERY_DISABLE_CLIENT = "jersey.config.client.disableAutoDiscovery";

    /**
     * Server-specific version of {@link CommonProperties#FEATURE_AUTO_DISCOVERY_DISABLE}.
     *
     * If present, it overrides the generic one for the server environment.
     * @since 2.8
     */
    public static final String FEATURE_AUTO_DISCOVERY_DISABLE_SERVER = "jersey.config.server.disableAutoDiscovery";


    /**
     * If {@code true} then disable configuration of Json Binding (JSR-367) feature.
     * <p>
     * By default, Json Binding is automatically enabled. The value of this property may be overridden by the client/server
     * variant of this property.
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     * @since 2.45
     */
    public static final String JSON_BINDING_FEATURE_DISABLE = "jersey.config.disableJsonBinding";

    /**
     * Client-specific version of {@link CommonProperties#JSON_BINDING_FEATURE_DISABLE}.
     *
     * If present, it overrides the generic one for the client environment.
     * @since 2.45
     */
    public static final String JSON_BINDING_FEATURE_DISABLE_CLIENT = "jersey.config.client.disableJsonBinding";

    /**
     * Server-specific version of {@link CommonProperties#JSON_BINDING_FEATURE_DISABLE}.
     *
     * If present, it overrides the generic one for the server environment.
     * @since 2.45
     */
    public static final String JSON_BINDING_FEATURE_DISABLE_SERVER = "jersey.config.server.disableJsonBinding";

    /**
     * Disables configuration of Json Binding (JSR-367) feature for {@link jakarta.ws.rs.core.Application} subclasses whose
     * package names are specified as a value. The value is comma-separated string defining prefixes of the application
     * package names.
     * <p>
     * By default, Json Binding is automatically enabled.
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     * @since 2.45
     */
    public static final String JSON_BINDING_FEATURE_DISABLE_APPLICATION = "jersey.config.application.disableJsonBinding";

    /**
     * If {@code true} then disable configuration of Json Processing (JSR-353) feature.
     * <p>
     * By default, Json Processing is automatically enabled. The value of this property may be overridden by the client/server
     * variant of this property.
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String JSON_PROCESSING_FEATURE_DISABLE = "jersey.config.disableJsonProcessing";

    /**
     * Client-specific version of {@link CommonProperties#JSON_PROCESSING_FEATURE_DISABLE}.
     *
     * If present, it overrides the generic one for the client environment.
     * @since 2.8
     */
    public static final String JSON_PROCESSING_FEATURE_DISABLE_CLIENT = "jersey.config.client.disableJsonProcessing";

    /**
     * Server-specific version of {@link CommonProperties#JSON_PROCESSING_FEATURE_DISABLE}.
     *
     * If present, it overrides the generic one for the server environment.
     * @since 2.8
     */
    public static final String JSON_PROCESSING_FEATURE_DISABLE_SERVER = "jersey.config.server.disableJsonProcessing";

    /**
     * If {@code true} then disable META-INF/services lookup globally on client/server.
     * <p>
     * By default, Jersey looks up SPI implementations described by META-INF/services/* files.
     * Then you can register appropriate provider classes by {@link jakarta.ws.rs.core.Application}.
     * </p>
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @since 2.1
     */
    public static final String METAINF_SERVICES_LOOKUP_DISABLE = "jersey.config.disableMetainfServicesLookup";

    /**
     * Client-specific version of {@link CommonProperties#METAINF_SERVICES_LOOKUP_DISABLE}.
     *
     * If present, it overrides the generic one for the client environment.
     * @since 2.8
     */
    public static final String METAINF_SERVICES_LOOKUP_DISABLE_CLIENT = "jersey.config.client.disableMetainfServicesLookup";

    /**
     * Server-specific version of {@link CommonProperties#METAINF_SERVICES_LOOKUP_DISABLE}.
     *
     * If present, it overrides the generic one for the server environment.
     * @since 2.8
     */
    public static final String METAINF_SERVICES_LOOKUP_DISABLE_SERVER = "jersey.config.server.disableMetainfServicesLookup";

    /**
     * If {@code true} then disable configuration of MOXy Json feature.
     * <p>
     * By default, MOXy Json is automatically enabled. The value of this property may be overridden by the client/server
     * variant of this property.
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String MOXY_JSON_FEATURE_DISABLE = "jersey.config.disableMoxyJson";

    /**
     * Client-specific version of {@link CommonProperties#MOXY_JSON_FEATURE_DISABLE}.
     *
     * If present, it overrides the generic one for the client environment.
     * @since 2.8
     */
    public static final String MOXY_JSON_FEATURE_DISABLE_CLIENT = "jersey.config.client.disableMoxyJson";

    /**
     * Server-specific version of {@link CommonProperties#MOXY_JSON_FEATURE_DISABLE}.
     *
     * If present, it overrides the generic one for the server environment.
     * @since 2.8
     */
    public static final String MOXY_JSON_FEATURE_DISABLE_SERVER = "jersey.config.server.disableMoxyJson";

    /**
     * An integer value that defines the buffer size used to buffer the outbound message entity in order to
     * determine its size and set the value of HTTP <tt>{@value jakarta.ws.rs.core.HttpHeaders#CONTENT_LENGTH}</tt> header.
     * <p>
     * If the entity size exceeds the configured buffer size, the buffering would be cancelled and the entity size
     * would not be determined. Value less or equal to zero disable the buffering of the entity at all.
     * </p>
     * The value of this property may be overridden by the client/server variant of this property by defining the suffix
     * to this property "<tt>.server</tt>" or "<tt>.client</tt>"
     * (<tt>{@value}.server</tt> or  <tt>{@value}.client</tt>).
     * <p>
     * The default value is <tt>8192</tt>.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String OUTBOUND_CONTENT_LENGTH_BUFFER = "jersey.config.contentLength.buffer";

    /**
     * Client-specific version of {@link CommonProperties#OUTBOUND_CONTENT_LENGTH_BUFFER}.
     *
     * If present, it overrides the generic one for the client environment.
     * @since 2.8
     */
    public static final String OUTBOUND_CONTENT_LENGTH_BUFFER_CLIENT = "jersey.config.client.contentLength.buffer";

    /**
     * Server-specific version of {@link CommonProperties#OUTBOUND_CONTENT_LENGTH_BUFFER}.
     *
     * If present, it overrides the generic one for the server environment.
     * @since 2.8
     */
    public static final String OUTBOUND_CONTENT_LENGTH_BUFFER_SERVER = "jersey.config.server.contentLength.buffer";

    /**
     * Disable some of the default providers from being loaded. The following providers extend application footprint
     * by XML dependencies, which is too heavy for native image, or by AWT which may possibly be not available by JDK 11 desktop:
     * <ul>
     *     <li>jakarta.activation.DataSource</li>
     *     <li>java.awt.image.RenderedImage</li>
     *     <li>javax.xml.transform.Source</li>
     *     <li>javax.xml.transform.dom.DOMSource</li>
     *     <li>javax.xml.transform.sax.SAXSource</li>
     *     <li>javax.xml.transform.stream.StreamSource</li>
     * </ul>
     * The following are the options to disable the provides:
     * {@code DATASOURCE, DOMSOURCE, RENDEREDIMAGE, SAXSOURCE, SOURCE, STREAMSOURCE},
     * or to disable all: {@code ALL}. Multiple options can be disabled by adding multiple comma separated values.
     *
     * @since 2.30
     */
    public static final String PROVIDER_DEFAULT_DISABLE = "jersey.config.disableDefaultProvider";


    /**
     * Allows API services loading. If absent or true JAXRS services loading is allowed.
     * Services shall implement Feature or DynamicFeature interface and be listed as SPI
     * in user's application.
     *
     * @since 2.35
     */
    public static final String JAXRS_SERVICE_LOADING_ENABLE = "jakarta.ws.rs.loadServices";

    /**
     * Comma separated list of jackson modules which are only enabled (only those modules will be used)
     * for json-jackson processing
     *
     * @since 2.36
     */
    public static final String JSON_JACKSON_ENABLED_MODULES = "jersey.config.json.jackson.enabled.modules";

    /**
     * Client-specific version of {@link CommonProperties#JSON_JACKSON_ENABLED_MODULES}.
     *
     * If present, it overrides the generic one for the client environment.
     * @since 2.36
     */
    public static final String JSON_JACKSON_ENABLED_MODULES_CLIENT = "jersey.config.client.json.jackson.enabled.modules";

    /**
     * Server-specific version of {@link CommonProperties#JSON_JACKSON_ENABLED_MODULES}.
     *
     * If present, it overrides the generic one for the server environment.
     * @since 2.36
     */
    public static final String JSON_JACKSON_ENABLED_MODULES_SERVER = "jersey.config.server.json.jackson.enabled.modules";

    /**
     * Comma separated list of jackson modules which shall be excluded from json-jackson processing.
     * the JaxbAnnotationModule is always excluded (cannot be configured).
     *
     * @since 2.36
     */

    public static final String JSON_JACKSON_DISABLED_MODULES = "jersey.config.json.jackson.disabled.modules";

    /**
     * Client-specific version of {@link CommonProperties#JSON_JACKSON_DISABLED_MODULES}.
     *
     * If present, it overrides the generic one for the client environment.
     * @since 2.36
     */
    public static final String JSON_JACKSON_DISABLED_MODULES_CLIENT = "jersey.config.client.json.jackson.disabled.modules";

    /**
     * Server-specific version of {@link CommonProperties#JSON_JACKSON_DISABLED_MODULES}.
     *
     * If present, it overrides the generic one for the client environment.
     * @since 2.36
     */
    public static final String JSON_JACKSON_DISABLED_MODULES_SERVER = "jersey.config.server.json.jackson.disabled.modules";

    /**
     * <p>
     *  Force the {@link jakarta.ws.rs.ext.ParamConverter} to throw {@link IllegalArgumentException} as mandated in javadoc.
     *  Must be convertible to {@link Boolean} value.
     * </p>
     * <p>
     *  Internally the {@code Exception} is caught by Jersey and usually converted to {@code null}.
     *  Therefore, the default value is set to {@code false} to speed-up the conversion.
     * </p>
     * <p>
     *  The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     * @since 2.40
     */
    public static final String PARAM_CONVERTERS_THROW_IAE = "jersey.config.paramconverters.throw.iae";

    /**
     * <p>
     *     Defines the {@link java.util.concurrent.ThreadFactory} to be used by internal default Executor Services.
     * </p>
     * <p>
     *     The default is {@link  java.util.concurrent.Executors#defaultThreadFactory()} on platform threads and
     *     {@code Thread.ofVirtual().factory()} on virtual threads.
     * </p>
     * @since 3.1.7
     */
    public static String THREAD_FACTORY = "jersey.config.threads.factory";

    /**
     * <p>
     *     Defines whether the virtual threads should be used by Jersey on JDK 21+ when not using an exact number
     *     of threads by {@code FixedThreadPool}.
     * </p>
     * <p>
     *     The default is {@code false} for this version of Jersey.
     * </p>
     * @since 3.1.7
     */
    public static String USE_VIRTUAL_THREADS = "jersey.config.threads.use.virtual";

    /**
     * Prevent instantiation.
     */
    private CommonProperties() {
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
    public static <T> T getValue(final Map<String, ?> properties, final String propertyName, final Class<T> type) {
        return PropertiesHelper.getValue(properties, propertyName, type, CommonProperties.LEGACY_FALLBACK_MAP);
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
        return PropertiesHelper.getValue(properties, propertyName, defaultValue, CommonProperties.LEGACY_FALLBACK_MAP);
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
        return PropertiesHelper.getValue(properties, runtime, propertyName, defaultValue, CommonProperties.LEGACY_FALLBACK_MAP);
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
        return PropertiesHelper.getValue(properties, runtime, propertyName, defaultValue, type,
                CommonProperties.LEGACY_FALLBACK_MAP);
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
        return PropertiesHelper.getValue(properties, runtime, propertyName, type, CommonProperties.LEGACY_FALLBACK_MAP);
    }
}
