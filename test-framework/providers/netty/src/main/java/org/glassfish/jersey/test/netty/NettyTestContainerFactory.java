/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.netty;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import io.netty.channel.Channel;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerFactory;

/**
 * Netty test container factory.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class NettyTestContainerFactory implements TestContainerFactory {

    @Override
    public TestContainer create(URI baseUri, DeploymentContext deploymentContext) {
        return new NettyTestContainer(baseUri, deploymentContext);
    }

    /**
     * Netty Test Container.
     * <p>
     * All functionality is deferred to {@link NettyHttpContainerProvider}.
     */
    private static class NettyTestContainer implements TestContainer {

        private final URI baseUri;
        private final DeploymentContext deploymentContext;

        private volatile Channel server;

        NettyTestContainer(URI baseUri, DeploymentContext deploymentContext) {
            this.baseUri = UriBuilder.fromUri(baseUri).path(deploymentContext.getContextPath()).build();
            this.deploymentContext = deploymentContext;
        }

        @Override
        public ClientConfig getClientConfig() {
            return null;
        }

        @Override
        public URI getBaseUri() {
            return baseUri;
        }

        @Override
        public void start() {
            server = NettyHttpContainerProvider.createServer(getBaseUri(), deploymentContext.getResourceConfig(), false);
        }

        @Override
        public void stop() {
            try {
                server.close().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
