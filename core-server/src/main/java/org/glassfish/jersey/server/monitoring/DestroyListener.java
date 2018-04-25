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
 * A listener contract that allows any registered implementation class to receive application destroy events.
 * <p>
 * The {@link #onDestroy()} method is called when application is being destroyed and after all the pending
 * {@link MonitoringStatisticsListener#onStatistics(MonitoringStatistics) monitoring statistics events} have been
 * dispatched and processed.
 * </p>
 * <p>
 * The advantage of using {@code DestroyListener} over using {@link ApplicationEventListener} directly to check for the
 * {@link ApplicationEvent.Type#DESTROY_FINISHED} event is, that the {@link #onDestroy()}
 * method is guaranteed to be called only AFTER all the {@code MonitoringStatisticsListener#onStatistics()} events have been
 * dispatched and processed, as opposed to using the {@code ApplicationEventListener} directly, in which case some monitoring
 * statistics events may still be concurrently fired after the {@code DESTROY_FINISHED} event has been dispatched
 * (due to potential race conditions).
 * </p>
 *
 * @author Miroslav Fuksa
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 * @see MonitoringStatisticsListener
 * @since 2.12
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface DestroyListener {
    /**
     * The method is called when application is destroyed. Use this method release resources of
     * the listener. This method will be called in the thread safe way (synchronously and by a single thread)
     * according to other methods from the related {@link org.glassfish.jersey.server.monitoring.MonitoringStatisticsListener}
     * interface.
     */
    public void onDestroy();
}
