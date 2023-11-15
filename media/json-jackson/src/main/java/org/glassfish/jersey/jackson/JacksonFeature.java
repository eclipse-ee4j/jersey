/*
 * Copyright (c) 2012, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson;

import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

import com.fasterxml.jackson.core.StreamReadConstraints;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.jackson.internal.DefaultJacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.FilteringJacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.JacksonFilteringFeature;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.base.JsonMappingExceptionMapper;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.base.JsonParseExceptionMapper;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.message.MessageProperties;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;

/**
 * Feature used to register Jackson JSON providers.
 *
 * @author Stepan Kopriva
 * @author Michal Gajdos
 */
public class JacksonFeature implements Feature {

    /**
     * Define whether to use Jackson's exception mappers ore not
     * Using them can provide useful information to the user, but it can expose unnecessary information, too.
     */
    private final boolean registerExceptionMappers;

    /**
     * Overridable Jackon's {@link StreamReadConstraints#DEFAULT_MAX_STRING_LEN} value.
     */
    private int maxStringLength = StreamReadConstraints.DEFAULT_MAX_STRING_LEN;

    /**
     * Default constructor enables registering Jackson's exception mappers
     */
    public JacksonFeature() {
        this(true);
    }

    private JacksonFeature(boolean registerExceptionMappers) {
        this.registerExceptionMappers = registerExceptionMappers;
    }

    /**
     * Create JacksonFeature with working Jackson's exception mappers
     * @return JacksonFeature with working Jackson's exception mappers
     */
    public static JacksonFeature withExceptionMappers() {
        return new JacksonFeature();
    }

    /**
     * Create JacksonFeature without registered Jackson's exception mappers
     * @return JacksonFeature without registered Jackson's exception mappers
     */
    public static JacksonFeature withoutExceptionMappers() {
        return new JacksonFeature(false);
    }

    /**
     * <p>
     *     Sets the {@link MessageProperties#JSON_MAX_STRING_LENGTH} property to a provided value. The property value already
     *     {@link Configuration configured} takes priority.
     * </p>
     * <p>
     *     Both uses of {@link #maxStringLength(int)} and {@link MessageProperties#JSON_MAX_STRING_LENGTH} override
     *     StreamReadConstraints defined on Jackson's {@code ObjectMapper's JsonFactory} provided via
     *     {@link jakarta.ws.rs.ext.ContextResolver ContextResolver&lt;ObjectMapper&gt;}.
     * </p>
     * @param maxStringLength the integer value to override the default Jackson's
     * {@link StreamReadConstraints#DEFAULT_MAX_STRING_LEN}.
     * @return JacksonFeature that has the Jackson's {@link StreamReadConstraints#DEFAULT_MAX_STRING_LEN} set.
     */
    public JacksonFeature maxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;
        return this;
    }

    private static final String JSON_FEATURE = JacksonFeature.class.getSimpleName();

    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        final String jsonFeature = CommonProperties.getValue(config.getProperties(), config.getRuntimeType(),
                InternalProperties.JSON_FEATURE, JSON_FEATURE, String.class);
        // Other JSON providers registered.
        if (!JSON_FEATURE.equalsIgnoreCase(jsonFeature)) {
            return false;
        }

        // Disable other JSON providers.
        context.property(PropertiesHelper.getPropertyNameForRuntime(InternalProperties.JSON_FEATURE, config.getRuntimeType()),
                JSON_FEATURE);

        // Register Jackson.
        if (!config.isRegistered(JacksonJaxbJsonProvider.class)) {

            if (registerExceptionMappers) {
                // add the default Jackson exception mappers
                context.register(JsonParseExceptionMapper.class);
                context.register(JsonMappingExceptionMapper.class);
            }

            if (EntityFilteringFeature.enabled(config)) {
                context.register(JacksonFilteringFeature.class);
                context.register(FilteringJacksonJaxbJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
            } else {
                context.register(DefaultJacksonJaxbJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
            }
        }

        if (config.getProperty(MessageProperties.JSON_MAX_STRING_LENGTH) == null) {
            context.property(MessageProperties.JSON_MAX_STRING_LENGTH, maxStringLength);
        }

        return true;
    }
}
