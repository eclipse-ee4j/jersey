/*
 * Copyright (c) 2014, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.resources;

import java.net.URI;
import java.util.stream.Stream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Part of GF-21033 reproducer. Make sure form data processed by the Jersey multi-part
 * feature make it forth and back all right.
 *
 * <p/>Run with:
 * <pre>
 * mvn clean package
 * $AS_HOME/bin/asadmin deploy target/cdi-test-webapp
 * mvn -DskipTests=false test</pre>
 *
 * @author Jakub Podlesak
 */
public class MultipartFeatureTest extends JerseyTest {

    public static Stream<String> testData() {
        return Stream.of("No matter what", "You should never", "ever", "just give up");
    }

    @Override
    protected Application configure() {
        return new MyApplication();
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path("cdi-multipart-webapp").build();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(MultiPartFeature.class);
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testPostFormData(String TestFormDATA) {

        final WebTarget target = target().path("echo");

        FormDataMultiPart formData = new FormDataMultiPart().field("input", TestFormDATA);

        final Response response = target.request().post(Entity.entity(formData, MediaType.MULTIPART_FORM_DATA_TYPE));
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is(TestFormDATA));
    }
}

