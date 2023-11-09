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
import io.micrometer.core.instrument.Tags;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.monitoring.RequestEvent;

/**
 * Default implementation for {@link JerseyTagsProvider}.
 *
 * @author Michael Weirauch
 * @author Johnny Lim
 * @since 2.41
 */
public final class DefaultJerseyTagsProvider implements JerseyTagsProvider {

    @Override
    public Iterable<Tag> httpRequestTags(RequestEvent event) {
        ContainerResponse response = event.getContainerResponse();
        return Tags.of(JerseyTags.method(event.getContainerRequest()), JerseyTags.uri(event),
                JerseyTags.exception(event), JerseyTags.status(response), JerseyTags.outcome(response));
    }

    @Override
    public Iterable<Tag> httpLongRequestTags(RequestEvent event) {
        return Tags.of(JerseyTags.method(event.getContainerRequest()), JerseyTags.uri(event));
    }

}
