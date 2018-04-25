/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.rx;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.examples.rx.domain.AgentResponse;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Invoke clients in Agent part of the application.
 *
 * @author Michal Gajdos
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class RxClientsTest extends JerseyTest {

    @Override
    protected DeploymentContext configureDeployment() {
        return ServletDeploymentContext.builder(RxApplication.class)
                .contextPath("rx-client-webapp/rx").build();
    }

    @Test
    public void testSyncClient() throws Exception {
        // warmup
        target("agent").path("sync").request().get();

        final Response response = target("agent").path("sync").request().get();
        response.bufferEntity();

        final AgentResponse agentResponse = response.readEntity(AgentResponse.class);

        assertThat(agentResponse.getVisited().size(), is(5));
        assertThat(agentResponse.getRecommended().size(), is(5));

        assertThat(agentResponse.getProcessingTime() > 4500, is(true));

        System.out.println(response.readEntity(String.class));
        System.out.println("Processing Time: " + agentResponse.getProcessingTime());
    }

    @Test
    public void testAsyncClient() throws Exception {
        // warmup
        target("agent").path("async").request().get();

        final Response response = target("agent").path("async").request().get();
        response.bufferEntity();

        final AgentResponse agentResponse = response.readEntity(AgentResponse.class);

        assertThat(agentResponse.getVisited().size(), is(5));
        assertThat(agentResponse.getRecommended().size(), is(5));

        assertThat(agentResponse.getProcessingTime() > 850, is(true));
        assertThat(agentResponse.getProcessingTime() < 4500, is(true));

        System.out.println(response.readEntity(String.class));
        System.out.println("Processing Time: " + agentResponse.getProcessingTime());
    }

    @Test
    public void testRxObservableClient() throws Exception {
        // warmup
        target("agent").path("observable").request().get();

        final Response response = target("agent").path("observable").request().get();
        response.bufferEntity();

        final AgentResponse agentResponse = response.readEntity(AgentResponse.class);

        assertThat(agentResponse.getVisited().size(), is(5));
        assertThat(agentResponse.getRecommended().size(), is(5));

        assertThat(agentResponse.getProcessingTime() > 850, is(true));
        assertThat(agentResponse.getProcessingTime() < 4500, is(true));

        System.out.println(response.readEntity(String.class));
        System.out.println("Processing Time: " + agentResponse.getProcessingTime());
    }

    @Test
    public void testRxFlowableClient() throws Exception {
        // warmup
        target("agent").path("flowable").request().get();

        final Response response = target("agent").path("flowable").request().get();
        response.bufferEntity();

        final AgentResponse agentResponse = response.readEntity(AgentResponse.class);

        assertThat(agentResponse.getVisited().size(), is(5));
        assertThat(agentResponse.getRecommended().size(), is(5));

        assertThat(agentResponse.getProcessingTime() > 850, is(true));
        assertThat(agentResponse.getProcessingTime() < 4500, is(true));

        System.out.println(response.readEntity(String.class));
        System.out.println("Processing Time: " + agentResponse.getProcessingTime());
    }

    @Test
    public void testRxCompletionStageClient() throws Exception {
        // warmup
        target("agent").path("completion").request().get();

        final Response response = target("agent").path("completion").request().get();
        response.bufferEntity();

        final AgentResponse agentResponse = response.readEntity(AgentResponse.class);

        assertThat(agentResponse.getVisited().size(), is(5));
        assertThat(agentResponse.getRecommended().size(), is(5));

        assertThat(agentResponse.getProcessingTime() > 850, is(true));
        assertThat(agentResponse.getProcessingTime() < 4500, is(true));

        System.out.println(response.readEntity(String.class));
        System.out.println("Processing Time: " + agentResponse.getProcessingTime());
    }
}
