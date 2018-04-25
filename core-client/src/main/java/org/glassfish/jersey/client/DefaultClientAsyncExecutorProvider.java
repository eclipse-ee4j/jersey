/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import org.glassfish.jersey.client.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.spi.ThreadPoolExecutorProvider;

/**
 * Default {@link org.glassfish.jersey.spi.ExecutorServiceProvider} used on the client side for asynchronous request processing.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@ClientAsyncExecutor
class DefaultClientAsyncExecutorProvider extends ThreadPoolExecutorProvider {

    private static final Logger LOGGER = Logger.getLogger(DefaultClientAsyncExecutorProvider.class.getName());

    private final LazyValue<Integer> asyncThreadPoolSize;

    /**
     * Creates a new instance.
     *
     * @param poolSize size of the default executor thread pool (if used). Zero or negative values are ignored.
     *                 See also {@link org.glassfish.jersey.client.ClientProperties#ASYNC_THREADPOOL_SIZE}.
     */
    @Inject
    public DefaultClientAsyncExecutorProvider(@Named("ClientAsyncThreadPoolSize") final int poolSize) {
        super("jersey-client-async-executor");

        this.asyncThreadPoolSize = Values.lazy(new Value<Integer>() {
            @Override
            public Integer get() {
                if (poolSize <= 0) {
                    LOGGER.config(LocalizationMessages.IGNORED_ASYNC_THREADPOOL_SIZE(poolSize));
                    // using default
                    return Integer.MAX_VALUE;
                } else {
                    LOGGER.config(LocalizationMessages.USING_FIXED_ASYNC_THREADPOOL(poolSize));
                    return poolSize;
                }
            }
        });
    }

    @Override
    protected int getMaximumPoolSize() {
        return asyncThreadPoolSize.get();
    }

    @Override
    protected int getCorePoolSize() {
        // Mimicking the Executors.newCachedThreadPool and newFixedThreadPool configuration values.
        final Integer maximumPoolSize = getMaximumPoolSize();
        if (maximumPoolSize != Integer.MAX_VALUE) {
            return maximumPoolSize;
        } else {
            return 0;
        }
    }
}
