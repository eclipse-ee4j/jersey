/*
 * Copyright (c) 2020, 2025, 2026 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson3.internal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Providers;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.jackson3.LocalizationMessages;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.json.JacksonXmlBindJsonProvider;
import org.glassfish.jersey.message.MessageProperties;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;

import java.util.logging.Logger;

/**
 * Entity Data provider based on Jackson JSON provider.
 */
@Singleton
public class DefaultJacksonXmlBindJsonProvider extends JacksonXmlBindJsonProvider {

    private static final Logger LOGGER = Logger.getLogger(DefaultJacksonXmlBindJsonProvider.class.getName());

    private final Configuration commonConfig;

    @Inject
    public DefaultJacksonXmlBindJsonProvider(@Context Providers providers, @Context Configuration config) {
        super(new JacksonMapperConfigurator(null,
                AnnotationIntrospector.pair(new JacksonAnnotationIntrospector(), JaxbHolder.get()),
                config));

        this.commonConfig = config;
        _providers = providers;
        try {
            Object jakartaRSFeatureBagObject = config.getProperty(JakartaRSFeatureBag.JAKARTA_RS_FEATURE);
            if ((jakartaRSFeatureBagObject instanceof JakartaRSFeatureBag jakartaRSFeatureBag)) {
                jakartaRSFeatureBag.configureJakartaRSFeatures(this);
            }
            updateDefaultStreamReadConstraints();
        } catch (RuntimeException e) {
            // ignore - not configured
            LOGGER.fine(LocalizationMessages.ERROR_CONFIGURING(e.getMessage()));
        }
    }

    @Override
    protected JsonMapper _locateMapperViaProvider(Class<?> type, MediaType mediaType) {
        JsonMapper mapper = super._locateMapperViaProvider(type, mediaType);
        if (mapper instanceof AbstractJsonMapper abstractJsonMapper) {
            abstractJsonMapper.jakartaRSFeatureBag.configureJakartaRSFeatures(this);
        }
        return mapper;
    }

    // Is this still necessary with Jackson 3? Seems like an attempt to handle misc Jackson 2.x versions
    private void updateDefaultStreamReadConstraints() {
        // Priorities 1. property, 2.JacksonFeature#maxStringLength, 3.defaultValue
        final Object maxStringLengthObject = commonConfig.getProperty(MessageProperties.JSON_MAX_STRING_LENGTH);
        final Integer maxStringLength = PropertiesHelper.convertValue(maxStringLengthObject, Integer.class);

        if (maxStringLength != null && maxStringLength != StreamReadConstraints.DEFAULT_MAX_STRING_LEN) {
            StreamReadConstraints.Builder builder = StreamReadConstraints.builder()
                .maxStringLength(maxStringLength);
            StreamReadConstraints.overrideDefaultStreamReadConstraints(builder.build());
        }
    }

    // Simple encapsulation to defer loading of JAXB introspector
    private static class JaxbHolder {
        public static AnnotationIntrospector get() {
            return new JakartaXmlBindAnnotationIntrospector();
        }
    }
}
