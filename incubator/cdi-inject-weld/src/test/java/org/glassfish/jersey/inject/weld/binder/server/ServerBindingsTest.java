/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld.binder.server;

import org.glassfish.jersey.inject.weld.TestParent;
import org.glassfish.jersey.inject.weld.binder.client.ClientOnlyPreinitialization;
import org.glassfish.jersey.internal.inject.ServiceHolder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ServerBindingsTest extends TestParent {
    @Test
    void clientOnlyInstancesTest() {
        injectionManager.completeRegistration();
        List<ClientOnlyPreinitialization.ClientOnlyInterface> instances =
                injectionManager.getAllInstances(ClientOnlyPreinitialization.ClientOnlyInterface.class);
        Assertions.assertEquals(2, instances.size());
        Assertions.assertTrue(instances.stream()
                .anyMatch(p -> p.getClass().equals(ClientOnlyPreinitialization.ClientOnlyClassA.class)));
        Assertions.assertTrue(instances.stream()
                .anyMatch(p -> p.getClass().equals(ClientOnlyPreinitialization.ClientOnlyClassB.class)));
    }

    @Test
    void clientOnlyServiceHoldersTest() {
        injectionManager.completeRegistration();
        List<ServiceHolder<ClientOnlyPreinitialization.ClientOnlyInterface>> instances =
                injectionManager.getAllServiceHolders(ClientOnlyPreinitialization.ClientOnlyInterface.class);
        Assertions.assertEquals(2, instances.size());
        Assertions.assertTrue(instances.stream()
                .anyMatch(p -> p.getInstance().getClass().equals(ClientOnlyPreinitialization.ClientOnlyClassA.class)));
        Assertions.assertTrue(instances.stream()
                .anyMatch(p -> p.getInstance().getClass().equals(ClientOnlyPreinitialization.ClientOnlyClassB.class)));
    }
}