/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.multimodule.ejb.reload.web1;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test reload functionality for two web app test case.
 * Run with:
 * <pre>
 * mvn clean package
 * $AS_HOME/bin/asadmin deploy ../ear/target/ejb-multimodule-reload-ear-*.ear
 * mvn -DskipTests=false test</pre>
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class ReloadTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new FirstApp();
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(LoggingFeature.class);
    }

    @Test
    public void testReload() {

        final WebTarget nanosTarget = target().path("ejb-multimodule-reload-war1/last-init-nano-time");

        final long nanos1 = _readInitTimeNanos(nanosTarget);
        final long nanos2 = _readInitTimeNanos(nanosTarget);

        assertThat(nanos2, is(equalTo(nanos1)));

        // J-592 reproducer:

//        reload();
//
//        final long nanos3 = _readInitTimeNanos(nanosTarget);
//        final long nanos4 = _readInitTimeNanos(nanosTarget);
//
//        assertThat(nanos4, is(equalTo(nanos3)));
//        assertThat(nanos3, is(greaterThan(nanos2)));
//
//        reload();
//
//        final long nanos5 = _readInitTimeNanos(nanosTarget);
//        final long nanos6 = _readInitTimeNanos(nanosTarget);
//
//        assertThat(nanos6, is(equalTo(nanos5)));
//        assertThat(nanos5, is(greaterThan(nanos4)));

        // END: J-592 reproducer
    }

    private void reload() {
        final WebTarget reloadTarget = target().path("ejb-multimodule-reload-war2/reload");
        assertThat(reloadTarget.request().get().getStatus(), is(204));
    }

    private long _readInitTimeNanos(final WebTarget target) throws NumberFormatException {
        final Response response = target.request().get();
        assertThat(response.getStatus(), is(200));
        return Long.parseLong(response.readEntity(String.class));
    }
}
