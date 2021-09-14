/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld.internal.managed;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.inject.weld.spi.BootstrapPreinitialization;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;

import java.util.function.Supplier;

/**
 * Jersey Client Runtime pre-initialization implementation.
 */
// TODO: put to a proper Jersey module
public class ClientBootstrapPreinitialization implements BootstrapPreinitialization {

    @Override
    public void register(RuntimeType runtimeType, AbstractBinder binder) {
        if (runtimeType == RuntimeType.SERVER) {
            return;
        }

        ClientConfig config = new ClientConfig();
        JerseyClient client = (JerseyClient) ClientBuilder.newClient(config);
        client.getConfiguration().getClientExecutor();
    }
}
