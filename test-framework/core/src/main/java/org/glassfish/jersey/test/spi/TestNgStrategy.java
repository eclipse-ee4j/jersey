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

package org.glassfish.jersey.test.spi;

import javax.ws.rs.client.Client;

/**
 * Strategy defining how test containers and clients are stored and passed to TestNG tests.
 * <p/>
 * {@link org.glassfish.jersey.test.JerseyTestNg Jersey Test} calls {@link #testContainer(TestContainer)} /
 * {@link #client(javax.ws.rs.client.Client)} methods before {@link #testContainer()} / {@link #client()}. Strategy is not
 * supposed to create instances of test container / client. It's purpose is to appropriately store given instances for different
 * TestNG approaches defined by {@code @BeforeXXX} and {@code @AfterXXX} annotations.
 *
 * @author Michal Gajdos
 */
public interface TestNgStrategy {

    /**
     * Return a test container to run the tests in. This method is called after {@link #testContainer(TestContainer)}.
     *
     * @return a test container instance or {@code null} if the test container is not set.
     */
    public TestContainer testContainer();

    /**
     * Set a new test container instance to run the tests in and return the old, previously stored, instance.
     *
     * @param testContainer new container instance.
     * @return an old container instance or {@code null} if the container is not set.
     */
    public TestContainer testContainer(final TestContainer testContainer);

    /**
     * Return a JAX-RS client. This method is called after {@link #client(javax.ws.rs.client.Client)}.
     *
     * @return a client instance or {@code null} if the client is not set.
     */
    public Client client();

    /**
     * Set a new JAX-RS client instance and return the old, previously stored, instance.
     *
     * @param client new client.
     * @return an old client instance or {@code null} if the client is not set.
     */
    public Client client(final Client client);
}
