/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test;

import javax.ws.rs.client.Client;

import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestNgStrategy;

/**
 * TestNG strategy that creates separate test container / client per test method.
 *
 * @author Michal Gajdos
 */
public class ContainerPerMethodTestNgStrategy implements TestNgStrategy {

    private final ThreadLocal<TestContainer> testContainerLocal = new ThreadLocal<>();
    private final ThreadLocal<Client> clientLocal = new ThreadLocal<>();

    @Override
    public TestContainer testContainer() {
        return testContainerLocal.get();
    }

    @Override
    public TestContainer testContainer(final TestContainer testContainer) {
        final TestContainer old = testContainerLocal.get();

        if (testContainer == null) {
            testContainerLocal.remove();
        } else {
            testContainerLocal.set(testContainer);
        }

        return old;
    }

    @Override
    public Client client() {
        return clientLocal.get();
    }

    @Override
    public Client client(final Client client) {
        final Client old = clientLocal.get();

        if (client == null) {
            clientLocal.remove();
        } else  {
            clientLocal.set(client);
        }

        return old;
    }
}
