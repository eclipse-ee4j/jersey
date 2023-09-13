/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.micrometer.server;

import io.micrometer.core.instrument.Tag;
import org.glassfish.jersey.server.monitoring.RequestEvent;

/**
 * Provides {@link Tag Tags} for Jersey request metrics.
 *
 * @author Michael Weirauch
 * @since 2.41
 */
public interface JerseyTagsProvider {

    /**
     * Provides tags to be associated with metrics for the given {@code event}.
     * @param event the request event
     * @return tags to associate with metrics recorded for the request
     */
    Iterable<Tag> httpRequestTags(RequestEvent event);

    /**
     * Provides tags to be associated with the
     * {@link io.micrometer.core.instrument.LongTaskTimer} which instruments the given
     * long-running {@code event}.
     * @param event the request event
     * @return tags to associate with metrics recorded for the request
     */
    Iterable<Tag> httpLongRequestTags(RequestEvent event);

}
