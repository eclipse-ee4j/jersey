/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.spi.ScheduledThreadPoolExecutorProvider;

/**
 * Default {@link org.glassfish.jersey.spi.ScheduledExecutorServiceProvider} used on the client side for providing the scheduled
 * executor service that runs background tasks.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 * @since 2.26
 */
@ClientBackgroundScheduler
class DefaultClientBackgroundSchedulerProvider extends ScheduledThreadPoolExecutorProvider {

    /**
     * Creates a new instance.
     */
    DefaultClientBackgroundSchedulerProvider() {
        super("jersey-client-background-scheduler");
    }

    @Override
    protected int getCorePoolSize() {
        return 1;
    }
}
