/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld.binder.client;

import org.glassfish.jersey.innate.BootstrapPreinitialization;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;

import jakarta.ws.rs.RuntimeType;

public class ClientOnlyPreinitialization implements BootstrapPreinitialization {
    public static interface ClientOnlyInterface {

    }
    public static class ClientOnlyClassA implements ClientOnlyInterface {

    }
    public static class ClientOnlyClassB implements ClientOnlyInterface {

    }
    @Override
    public void preregister(RuntimeType runtimeType, InjectionManager injectionManager) {
        injectionManager.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(ClientOnlyClassA.class).to(ClientOnlyInterface.class);
                bind(ClientOnlyClassB.class).to(ClientOnlyInterface.class);
            }
        });
    }
}
