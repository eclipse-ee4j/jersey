/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.multimodule.ejb.web1;

import java.net.URI;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Test for EJB web application resources. The JAX-RS resources come from bundled EJB library jar.
 * Run with:
 * <pre>
 * mvn clean package
 * $AS_HOME/bin/asadmin deploy ../ear/target/ejb-multimodule-ear-*.ear
 * mvn -DskipTests=false test</pre>
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class JaxRsFromEjbLibraryTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new JaxRsConfiguration();
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path("ejb-multimodule-war").path("resources").build();
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(LoggingFeature.class);
    }

    @Test
    public void testRequestCountGetsIncremented() {
        final int requestCount1 = _nextCount(target().path("counter"));

        final int requestCount2 = _nextCount(target().path("counter"));
        assertThat(requestCount2, is(greaterThan(requestCount1)));

        final int requestCount3 = _nextCount(target().path("stateless"));
        assertThat(requestCount3, is(greaterThan(requestCount2)));

        final int requestCount4 = _nextCount(target().path("stateless"));
        assertThat(requestCount4, is(greaterThan(requestCount3)));

        final int requestCount5 = _nextCount(target().path("stateful").path("count"));
        assertThat(requestCount5, is(greaterThan(requestCount4)));

        final int requestCount6 = _nextCount(target().path("stateful").path("count"));
        assertThat(requestCount6, is(greaterThan(requestCount5)));

        final int requestCount7 = _nextCount(target().path("war-stateless"));
        assertThat(requestCount7, is(greaterThan(requestCount6)));

        final int requestCount8 = _nextCount(target().path("war-stateless"));
        assertThat(requestCount8, is(greaterThan(requestCount7)));
    }

    private int _nextCount(final WebTarget target) throws NumberFormatException {
        final Response response = target.request().get();
        assertThat(response.getStatus(), is(200));
        return Integer.parseInt(response.readEntity(String.class));
    }

    @Test
    public void testUriInfoInjection() {
        _testPath(target().path("counter").path("one"), "counter/one");
        _testPath(target().path("counter").path("two"), "counter/two");
        _testPath(target().path("stateless").path("three"), "stateless/three");
        _testPath(target().path("stateless").path("four"), "stateless/four");
        _testPath(target().path("war-stateless").path("five"), "war-stateless/five");
        _testPath(target().path("war-stateless").path("six"), "war-stateless/six");
    }

    private void _testPath(final WebTarget target, final String expectedResult) {
        final Response response = target.request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), equalTo(expectedResult));
    }
}
