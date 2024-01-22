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

package org.glassfish.jersey.server;

import org.glassfish.jersey.innate.BootstrapPreinitialization;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.wadl.WadlFeature;
import org.glassfish.jersey.server.wadl.processor.WadlModelProcessor;

import jakarta.ws.rs.RuntimeType;

public class ServerBootstrapPreinitialization implements BootstrapPreinitialization {

    @Override
    public void preregister(RuntimeType runtimeType, InjectionManager injectionManager) {
        if (runtimeType == RuntimeType.SERVER) {
            new ApplicationHandler(injectionManager);
            if (new WadlFeature().configure(BootstrapPreinitialization.featureContextInstance())) {
                injectionManager.register(
                        Bindings.serviceAsContract(WadlModelProcessor.OptionsHandler.class).in(RequestScoped.class)
                );
            }
        }
    }
}
