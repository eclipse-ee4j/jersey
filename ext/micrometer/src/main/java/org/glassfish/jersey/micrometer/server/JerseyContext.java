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

import java.util.List;

import io.micrometer.observation.transport.ReceiverContext;
import io.micrometer.observation.transport.RequestReplyReceiverContext;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.monitoring.RequestEvent;

/**
 * A {@link ReceiverContext} for Jersey.
 *
 * @author Marcin Grzejszczak
 * @since 2.41
 */
public class JerseyContext extends RequestReplyReceiverContext<ContainerRequest, ContainerResponse> {

    private RequestEvent requestEvent;

    public JerseyContext(RequestEvent requestEvent) {
        super((carrier, key) -> {
            List<String> requestHeader = carrier.getRequestHeader(key);
            if (requestHeader == null || requestHeader.isEmpty()) {
                return null;
            }
            return requestHeader.get(0);
        });
        this.requestEvent = requestEvent;
        setCarrier(requestEvent.getContainerRequest());
    }

    public void setRequestEvent(RequestEvent requestEvent) {
        this.requestEvent = requestEvent;
    }

    public RequestEvent getRequestEvent() {
        return requestEvent;
    }

}
