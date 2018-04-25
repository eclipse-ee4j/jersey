/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.gae;

import java.util.concurrent.ThreadFactory;

import org.glassfish.jersey.server.BackgroundScheduler;
import org.glassfish.jersey.spi.ScheduledThreadPoolExecutorProvider;

/**
 * This class implements Jersey's SPI {@link org.glassfish.jersey.spi.ScheduledExecutorServiceProvider} to provide a
 * {@link java.util.concurrent.ScheduledExecutorService} instances with a GAE specific {@link ThreadFactory} provider
 * - {@link com.google.appengine.api.ThreadManager}.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@BackgroundScheduler
class GaeBackgroundExecutorProvider extends ScheduledThreadPoolExecutorProvider {

    /**
     * Create new instance of GAE-specific background scheduled executor service provider.
     */
    public GaeBackgroundExecutorProvider() {
        super("gae-jersey-background-task-scheduler");
    }

    @Override
    public ThreadFactory getBackingThreadFactory() {
        return com.google.appengine.api.ThreadManager.backgroundThreadFactory();
    }

    @Override
    protected int getCorePoolSize() {
        return 1;
    }
}
