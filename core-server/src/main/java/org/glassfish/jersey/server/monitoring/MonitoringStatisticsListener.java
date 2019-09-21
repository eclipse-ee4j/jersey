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

package org.glassfish.jersey.server.monitoring;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.spi.Contract;

/**
 * A Jersey specific provider that listens to monitoring statistics. Each time when new statistics are available,
 * the implementation of {@code MonitoringStatisticsListener} will be called and new statistics will be passed.
 * Statistics are calculated in irregular undefined intervals.
 * <p/>
 * The provider must not throw any exception.
 * <p/>
 * The implementation of this interface can be registered as a standard Jersey/JAX-RS provider
 * by annotating with {@link javax.ws.rs.ext.Provider @Provider} annotation in the case of
 * class path scanning, by registering as a provider using {@link org.glassfish.jersey.server.ResourceConfig}
 * or by returning from {@link javax.ws.rs.core.Application#getClasses()}
 * or {@link javax.ws.rs.core.Application#getSingletons()}}. The provider can be registered only on the server
 * side.
 * <p/>
 *
 * @author Miroslav Fuksa
 * @see DestroyListener
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface MonitoringStatisticsListener {

    /**
     * The method is called when new statistics are available and statistics are passed as an argument.
     *
     * @param statistics Newly calculated monitoring statistics.
     */
    public void onStatistics(MonitoringStatistics statistics);

}
