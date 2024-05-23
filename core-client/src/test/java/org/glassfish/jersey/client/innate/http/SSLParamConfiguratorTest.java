/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.innate.http;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.http.HttpHeaders;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MultivaluedHashMap;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SSLParamConfiguratorTest {
    @Test
    public void testNoHost() {
        final URI uri = URI.create("http://xxx.com:8080");
        final JerseyClient client = (JerseyClient) ClientBuilder.newClient();
        final ClientConfig config = client.getConfiguration();
        final PropertiesDelegate delegate = new MapPropertiesDelegate();
        ClientRequest request = new ClientRequest(uri, config, delegate) {};
        SSLParamConfigurator configurator = SSLParamConfigurator.builder().request(request).build();
        MatcherAssert.assertThat(configurator.isSNIRequired(), Matchers.is(false));
    }

    @Test
    public void testHostHeaderHasPrecedence() {
        final URI uri = URI.create("http://xxx.com:8080");
        final JerseyClient client = (JerseyClient) ClientBuilder.newClient();
        final ClientConfig config = client.getConfiguration();
        final PropertiesDelegate delegate = new MapPropertiesDelegate();
        ClientRequest request = new ClientRequest(uri, config, delegate) {};
        request.getHeaders().add(HttpHeaders.HOST, "yyy.com");
        SSLParamConfigurator configurator = SSLParamConfigurator.builder().request(request).build();
        MatcherAssert.assertThat(configurator.isSNIRequired(), Matchers.is(true));
        MatcherAssert.assertThat(configurator.getSNIHostName(), Matchers.is("yyy.com"));
    }

    @Test
    public void testPropertyOnClientHasPrecedence() {
        final URI uri = URI.create("http://xxx.com:8080");
        final JerseyClient client = (JerseyClient) ClientBuilder.newClient();
        final ClientConfig config = client.getConfiguration();
        final PropertiesDelegate delegate = new MapPropertiesDelegate();
        client.property(ClientProperties.SNI_HOST_NAME, "yyy.com");
        ClientRequest request = new ClientRequest(uri, config, delegate) {};
        SSLParamConfigurator configurator = SSLParamConfigurator.builder().request(request).build();
        MatcherAssert.assertThat(configurator.isSNIRequired(), Matchers.is(true));
        MatcherAssert.assertThat(configurator.getSNIHostName(), Matchers.is("yyy.com"));
    }

    @Test
    public void testPropertyOnDelegateHasPrecedence() {
        final URI uri = URI.create("http://xxx.com:8080");
        final JerseyClient client = (JerseyClient) ClientBuilder.newClient();
        final ClientConfig config = client.getConfiguration();
        final PropertiesDelegate delegate = new MapPropertiesDelegate();
        client.property(ClientProperties.SNI_HOST_NAME, "yyy.com");
        delegate.setProperty(ClientProperties.SNI_HOST_NAME, "zzz.com");
        ClientRequest request = new ClientRequest(uri, config, delegate) {};
        SSLParamConfigurator configurator = SSLParamConfigurator.builder().request(request).build();
        MatcherAssert.assertThat(configurator.isSNIRequired(), Matchers.is(true));
        MatcherAssert.assertThat(configurator.getSNIHostName(), Matchers.is("zzz.com"));
    }

    @Test
    public void testPropertyOnDelegateHasPrecedenceOverHost() {
        final URI uri = URI.create("http://xxx.com:8080");
        final JerseyClient client = (JerseyClient) ClientBuilder.newClient();
        final ClientConfig config = client.getConfiguration();
        final PropertiesDelegate delegate = new MapPropertiesDelegate();
        client.property(ClientProperties.SNI_HOST_NAME, "yyy.com");
        delegate.setProperty(ClientProperties.SNI_HOST_NAME, "zzz.com");
        ClientRequest request = new ClientRequest(uri, config, delegate) {};
        request.getHeaders().add(HttpHeaders.HOST, "www.com");
        SSLParamConfigurator configurator = SSLParamConfigurator.builder().request(request).build();
        MatcherAssert.assertThat(configurator.isSNIRequired(), Matchers.is(true));
        MatcherAssert.assertThat(configurator.getSNIHostName(), Matchers.is("zzz.com"));
    }

    @Test
    public void testDisableSni() {
        final URI uri = URI.create("http://xxx.com:8080");
        final JerseyClient client = (JerseyClient) ClientBuilder.newClient();
        final ClientConfig config = client.getConfiguration();
        final PropertiesDelegate delegate = new MapPropertiesDelegate();
        client.property(ClientProperties.SNI_HOST_NAME, "yyy.com");
        delegate.setProperty(ClientProperties.SNI_HOST_NAME, "xxx.com");
        ClientRequest request = new ClientRequest(uri, config, delegate) {};
        request.getHeaders().add(HttpHeaders.HOST, "www.com");
        SSLParamConfigurator configurator = SSLParamConfigurator.builder().request(request).build();
        MatcherAssert.assertThat(configurator.isSNIRequired(), Matchers.is(false));
        MatcherAssert.assertThat(configurator.getSNIHostName(), Matchers.is("xxx.com"));
    }

    @Test
    public void testLowerCasePropertyOnClientHasPrecedence() {
        final URI uri = URI.create("http://xxx.com:8080");
        final JerseyClient client = (JerseyClient) ClientBuilder.newClient();
        final ClientConfig config = client.getConfiguration();
        final PropertiesDelegate delegate = new MapPropertiesDelegate();
        client.property(ClientProperties.SNI_HOST_NAME.toLowerCase(Locale.ROOT), "yyy.com");
        ClientRequest request = new ClientRequest(uri, config, delegate) {};
        request.getHeaders().add(HttpHeaders.HOST, "www.com");
        SSLParamConfigurator configurator = SSLParamConfigurator.builder().request(request).build();
        MatcherAssert.assertThat(configurator.isSNIRequired(), Matchers.is(true));
        MatcherAssert.assertThat(configurator.getSNIHostName(), Matchers.is("yyy.com"));
    }

    @Test
    public void testUriAndHeadersAndConfig() {
        final URI uri = URI.create("http://xxx.com:8080");
        final JerseyClient client = (JerseyClient) ClientBuilder.newClient();
        Map<String, List<Object>> httpHeaders = new MultivaluedHashMap<>();
        httpHeaders.put(HttpHeaders.HOST, Collections.singletonList("www.com"));
        SSLParamConfigurator configurator = SSLParamConfigurator.builder()
                .uri(uri)
                .headers(httpHeaders)
                .configuration(client.getConfiguration())
                .build();
        MatcherAssert.assertThat(configurator.isSNIRequired(), Matchers.is(true));
        MatcherAssert.assertThat(configurator.getSNIHostName(), Matchers.is("www.com"));

        client.property(ClientProperties.SNI_HOST_NAME, "yyy.com");
        configurator = SSLParamConfigurator.builder()
                .uri(uri)
                .headers(httpHeaders)
                .configuration(client.getConfiguration())
                .build();
        MatcherAssert.assertThat(configurator.isSNIRequired(), Matchers.is(true));
        MatcherAssert.assertThat(configurator.getSNIHostName(), Matchers.is("yyy.com"));
    }
}
