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

package org.glassfish.jersey.tests.integration.servlet_3_async;

import java.util.concurrent.Future;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Asynchronous servlet-deployed resource for testing {@link javax.ws.rs.container.AsyncResponse async response} timeouts.
 *
 * @author Michal Gajdos
 */
public class AsyncResumeTimeoutResourceTest extends JerseyTest {

    public AsyncResumeTimeoutResourceTest() {
        enable(TestProperties.LOG_TRAFFIC);
    }

    @Override
    protected DeploymentContext configureDeployment() {
        return ServletDeploymentContext.builder(new Application())
                .contextPath("servlet-3-gf-async")
                .build();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testTimeout() throws Exception {
        final WebTarget resourceTarget = target("resume-timeout");
        final Future<Response> responseFuture = resourceTarget.path("suspend").request().async().get();

        // Set timeout.
        assertThat(resourceTarget.path("timeout").request().post(Entity.text("500")).getStatus(), equalTo(204));

        // Wait for it.
        assertThat(responseFuture.get().readEntity(String.class), equalTo("timeout1=true_timeout2=true_handled"));
    }
}
