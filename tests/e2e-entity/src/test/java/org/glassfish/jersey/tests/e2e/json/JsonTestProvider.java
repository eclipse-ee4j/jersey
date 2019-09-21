/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jackson1.Jackson1Feature;
import org.glassfish.jersey.jettison.JettisonConfig;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.jsonb.JsonBindingFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;

/**
 * Common class for JSON providers that should be used for testing JSON capabilities.
 *
 * @author Michal Gajdos
 */
public abstract class JsonTestProvider {

    public static final Collection<JsonTestProvider> JAXB_PROVIDERS = new LinkedHashSet<JsonTestProvider>() {{
        add(new JacksonJsonTestProvider());
        add(new Jackson1JsonTestProvider());
        add(new JettisonMappedJsonTestProvider());
        add(new JettisonBadgerfishJsonTestProvider());
        add(new MoxyJsonTestProvider());
        add(new JsonbTestProvider());
    }};

    //  TODO add MoxyJsonTestProvider once MOXy supports POJO
    public static final Collection<JsonTestProvider> POJO_PROVIDERS = new LinkedHashSet<JsonTestProvider>() {{
        add(new JacksonJsonTestProvider());
        add(new Jackson1JsonTestProvider());
    }};

    private Feature feature;
    private JettisonConfig configuration;
    private Set<Object> providers = new LinkedHashSet<>();

    public static class JettisonMappedJsonTestProvider extends JsonTestProvider {

        public JettisonMappedJsonTestProvider() {
            final JettisonConfig jsonConfiguration =
                    JettisonConfig.mappedJettison().xml2JsonNs(new HashMap<String,
                            String>() {{
                        put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
                        put("http://example.com", "example");
                        put("http://test.jaxb.com", "jaxb");
                    }}).serializeAsArray("singleItemList").build();

            setFeature(new JettisonFeature());
            setConfiguration(jsonConfiguration);
        }

    }

    public static class JettisonBadgerfishJsonTestProvider extends JsonTestProvider {

        public JettisonBadgerfishJsonTestProvider() {
            setFeature(new JettisonFeature());

            setConfiguration(JettisonConfig.badgerFish().build());
        }

    }

    public static class MoxyJsonTestProvider extends JsonTestProvider {

        public MoxyJsonTestProvider() {
            setFeature(new MoxyJsonFeature());
            getProviders().add(new MoxyJsonConfigurationContextResolver());
        }

    }

    @Provider
    protected static final class MoxyJsonConfigurationContextResolver implements ContextResolver<MoxyJsonConfig> {

        @Override
        public MoxyJsonConfig getContext(final Class<?> objectType) {
            final MoxyJsonConfig configuration = new MoxyJsonConfig();

            final Map<String, String> namespacePrefixMapper = new HashMap<>(1);
            namespacePrefixMapper.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
            namespacePrefixMapper.put("http://example.com", "example");
            namespacePrefixMapper.put("http://test.jaxb.com", "jaxb");

            configuration.setNamespacePrefixMapper(namespacePrefixMapper);
            configuration.setNamespaceSeparator(':');

            return configuration;
        }
    }

    @Provider
    protected static final class JsonbContextResolver implements ContextResolver<Jsonb> {

        @Override
        public Jsonb getContext(Class<?> type) {
            JsonbConfig config = new JsonbConfig();
            return JsonbBuilder.create(config);
        }
    }

    public static class JacksonJsonTestProvider extends JsonTestProvider {

        public JacksonJsonTestProvider() {
            setFeature(new JacksonFeature());
        }

    }

    public static class Jackson1JsonTestProvider extends JsonTestProvider {
        public Jackson1JsonTestProvider() {
            setFeature(new Jackson1Feature());
        }
    }

    public static class JsonbTestProvider extends JsonTestProvider {
        public JsonbTestProvider() {
            setFeature(new JsonBindingFeature());
            getProviders().add(new JsonbContextResolver());
        }
    }

    public JettisonConfig getConfiguration() {
        return configuration;
    }

    protected void setConfiguration(final JettisonConfig configuration) {
        this.configuration = configuration;
    }

    public Feature getFeature() {
        return feature;
    }

    protected void setFeature(final Feature feature) {
        this.feature = feature;
    }

    public Set<Object> getProviders() {
        return providers;
    }

}
