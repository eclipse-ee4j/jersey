/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.helidon;

import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.core.GenericType;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.process.internal.RequestScoped;

class HelidonHttpContainerBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bindFactory(WebServerRequestReferencingFactory.class).to(ServerRequest.class)
                .proxy(true).proxyForSameScope(false)
                .in(RequestScoped.class);
        bindFactory(ReferencingFactory.<ServerRequest>referenceFactory()).to(new GenericType<Ref<ServerRequest>>() { })
                .in(RequestScoped.class);

        bindFactory(WebServerResponseReferencingFactory.class).to(ServerResponse.class)
                .proxy(true).proxyForSameScope(false)
                .in(RequestScoped.class);
        bindFactory(ReferencingFactory.<ServerResponse>referenceFactory()).to(new GenericType<Ref<ServerResponse>>() { })
                .in(RequestScoped.class);
    }

    private static class WebServerRequestReferencingFactory extends ReferencingFactory<ServerRequest> {

        @Inject
        WebServerRequestReferencingFactory(final Provider<Ref<ServerRequest>> referenceFactory) {
            super(referenceFactory);
        }
    }

    private static class WebServerResponseReferencingFactory extends ReferencingFactory<ServerResponse> {

        @Inject
        WebServerResponseReferencingFactory(final Provider<Ref<ServerResponse>> referenceFactory) {
            super(referenceFactory);
        }
    }
}

