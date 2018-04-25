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

package org.glassfish.jersey.jdk.connector.internal;

import java.net.CookieManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class HttpConnectionPool {

    // TODO better solution, containers won't like this
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });

    private final ConnectorConfiguration connectorConfiguration;
    private final CookieManager cookieManager;
    private final Map<DestinationConnectionPool.DestinationKey, DestinationConnectionPool> destinationPools = new
            ConcurrentHashMap<>();

    HttpConnectionPool(ConnectorConfiguration connectorConfiguration, CookieManager cookieManager) {
        this.connectorConfiguration = connectorConfiguration;
        this.cookieManager = cookieManager;
    }

    void send(HttpRequest httpRequest, CompletionHandler<HttpResponse> completionHandler) {
        final DestinationConnectionPool.DestinationKey destinationKey = new DestinationConnectionPool.DestinationKey(
                httpRequest.getUri());
        DestinationConnectionPool destinationConnectionPool = destinationPools.get(destinationKey);

        if (destinationConnectionPool == null) {
            synchronized (this) {
                // check again while holding the lock
                destinationConnectionPool = destinationPools.get(destinationKey);

                if (destinationConnectionPool == null) {
                    final DestinationConnectionPool pool = new DestinationConnectionPool(connectorConfiguration, cookieManager,
                            scheduler);
                    pool.setConnectionCloseListener(() -> {
                        /* There is a potential race when there is a request just about to be submitted to the pool
                        we are just removing. Such request will be executed on the removed pool without any problems.
                        The only issue is that this listener will be called for the second time in such a case, so we
                        have to make sure we don't remove a new pool that might have been created in the meantime. */
                        destinationPools.remove(destinationKey, pool);
                    });

                    destinationConnectionPool = pool;
                    destinationPools.put(destinationKey, destinationConnectionPool);
                }
            }
        }

        destinationConnectionPool.send(httpRequest, completionHandler);
    }

    synchronized void close() {
        destinationPools.values().forEach(DestinationConnectionPool::close);
    }
}
