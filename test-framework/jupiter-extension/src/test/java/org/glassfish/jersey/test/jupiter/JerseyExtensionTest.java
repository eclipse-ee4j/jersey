/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.jupiter;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JerseyExtension should")
class JerseyExtensionTest {

    @Nested
    @DisplayName("when registered programmatically")
    class RegisteredProgrammatically implements BasicTestCases {

        @RegisterExtension
        JerseyExtension jerseyExtension = new JerseyExtension(this::application);

        @SuppressWarnings("unused")
        private WebTarget target;
        @SuppressWarnings("unused")
        private Client client;
        @SuppressWarnings("unused")
        private URI uri;

        @Override
        public WebTarget injectedTarget() {
            return target;
        }

        @Override
        public Client injectedClient() {
            return client;
        }

        @Override
        public URI injectedURI() {
            return uri;
        }

        private Application application() {
            return new ResourceConfig(SimpleResource.class);
        }
    }

    @Nested
    @DisplayName("when registered by @JerseyTest")
    @JerseyTest
    class RegisteredByJerseyTest implements BasicTestCases {

        @SuppressWarnings("unused")
        private WebTarget target;
        @SuppressWarnings("unused")
        private Client client;
        @SuppressWarnings("unused")
        private URI uri;

        @Override
        public WebTarget injectedTarget() {
            return target;
        }

        @Override
        public Client injectedClient() {
            return client;
        }

        @Override
        public URI injectedURI() {
            return uri;
        }

        @SuppressWarnings("unused")
        private Application application() {
            return new ResourceConfig().register(new SimpleResource());
        }
    }

    interface BasicTestCases {

        WebTarget injectedTarget();

        Client injectedClient();

        URI injectedURI();

        @Test
        @DisplayName("inject a WebTarget parameter")
        default void injectsWebTargetParameter(WebTarget target) {
            assertThat(target).isNotNull();
        }

        @Test
        @DisplayName("access the resource through WebTarget parameter")
        default void accessesResourceThroughWebTargetParameter(WebTarget target) {
            assertThat(target.path("simple").request().get(String.class)).isEqualTo(SimpleResource.RESULT);
        }

        @Test
        @DisplayName("inject a Client parameter")
        default void injectsClientParameter(Client client) {
            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("inject a URI parameter")
        default void injectsURIParameter(URI uri) {
            assertThat(uri).isNotNull();
        }

        @Test
        @DisplayName("access the resource through Client and URI parameters")
        default void accessesResourceThroughClientAndURIParameters(Client client, URI baseUri) {
            assertThat(client.target(baseUri).path("simple").request().get(String.class)).isEqualTo(SimpleResource.RESULT);
        }

        @Test
        @DisplayName("inject a WebTarget field")
        default void injectsWebTargetField() {
            assertThat(injectedTarget()).isNotNull();
        }

        @Test
        @DisplayName("access the resource through WebTarget field")
        default void accessesResourceThroughWebTargetField() {
            assertThat(injectedTarget().path("simple").request().get(String.class)).isEqualTo(SimpleResource.RESULT);
        }

        @Test
        @DisplayName("inject a Client field")
        default void injectsClientParameter() {
            assertThat(injectedClient()).isNotNull();
        }

        @Test
        @DisplayName("inject a URI field")
        default void injectsURIParameter() {
            assertThat(injectedURI()).isNotNull();
        }

        @Test
        @DisplayName("access the resource through Client and URI fields")
        default void accessesResourceThroughClientAndURIParameters() {
            assertThat(injectedClient().target(injectedURI()).path("simple").request().get(String.class))
                    .isEqualTo(SimpleResource.RESULT);
        }
    }
}