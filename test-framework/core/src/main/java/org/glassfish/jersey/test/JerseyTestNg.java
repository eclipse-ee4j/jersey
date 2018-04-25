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
import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.glassfish.jersey.test.spi.TestNgStrategy;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * Parent class for testing JAX-RS and Jersey-based applications using Jersey test framework and TestNG framework.
 *
 * @author Michal Gajdos
 *
 * @see org.glassfish.jersey.test.JerseyTest
 * @see org.glassfish.jersey.test.spi.TestNgStrategy
 */
public abstract class JerseyTestNg extends JerseyTest {

    private TestNgStrategy strategy;

    /**
     * Initialize JerseyTestNg instance.
     *
     * This constructor can be used from an extending subclass. When this constructor is used, the extending
     * concrete subclass must implement {@link #configureStrategy()} method.
     * <p>
     * When this constructor is used, the extending concrete subclass must implement one of the
     * {@link #configure()} or {@link #configureDeployment()} methods to provide the tested application
     * configuration and deployment context.
     * </p>
     */
    public JerseyTestNg() throws TestContainerException {
        super();

        strategy = configureStrategy();
    }

    /**
     * Initialize JerseyTestNg instance and specify the test container factory to be used by this test.
     *
     * This constructor can be used from an extending subclass. When this constructor is used, the extending
     * concrete subclass must implement {@link #configureStrategy()} method.
     * <p>
     * When this constructor is used, the extending concrete subclass must implement one of the
     * {@link #configure()} or {@link #configureDeployment()} methods to provide the tested application
     * configuration and deployment context.
     * </p>
     *
     * @param testContainerFactory the test container factory to use for testing.
     */
    public JerseyTestNg(final TestContainerFactory testContainerFactory) {
        super(testContainerFactory);

        strategy = configureStrategy();
    }

    /**
     * Initialize JerseyTestNg instance.
     *
     * This constructor can be used from an extending subclass.
     * <p>
     * When this constructor is used, the extending concrete subclass the {@link #configure()}
     * or {@link #configureDeployment()} method are ignored. However it must implement
     * {@link #configureStrategy()} method.
     * </p>
     * <p>
     * Please note that when this constructor is used, recording of startup logs as well as configuring
     * other {@code JerseyTestNg} properties and features may not work properly. While using this constructor
     * should generally be avoided, in certain scenarios it may be necessary to use this constructor.
     * </p>
     *
     * @param jaxrsApplication tested application.
     */
    public JerseyTestNg(final Application jaxrsApplication) throws TestContainerException {
        super(jaxrsApplication);

        strategy = configureStrategy();
    }

    @Override
    /* package */ final TestContainer getTestContainer() {
        return strategy.testContainer();
    }

    @Override
    /* package */ final TestContainer setTestContainer(final TestContainer testContainer) {
        return strategy.testContainer(testContainer);
    }

    @Override
    protected final Client getClient() {
        return strategy.client();
    }

    @Override
    protected final Client setClient(final Client client) {
        return strategy.client(client);
    }

    /**
     * Configure {@link org.glassfish.jersey.test.spi.TestNgStrategy strategy} for this TestNG JerseyTest. The strategy defines
     * how a test container / client is stored (per class, per thread) and is also responsible for disposing stored instances.
     *
     * @return TestNG strategy instance.
     */
    protected TestNgStrategy configureStrategy() {
        throw new UnsupportedOperationException("The configureStrategy method must be implemented by the extending class");
    }

    /**
     * Parent for TestNg tests that needs to create a test container only once per a test class.
     * <p/>
     * The creation and disposal of the test container (or client) are managed by {@link #setUp()} / {@link #tearDown()} methods
     * annotated by TestNG annotations {@link BeforeClass} / {@link AfterClass}.
     */
    @SuppressWarnings("UnusedDeclaration")
    public abstract static class ContainerPerClassTest extends JerseyTestNg {

        public ContainerPerClassTest() throws TestContainerException {
            super();
        }

        public ContainerPerClassTest(final TestContainerFactory testContainerFactory) {
            super(testContainerFactory);
        }

        public ContainerPerClassTest(final Application jaxrsApplication) throws TestContainerException {
            super(jaxrsApplication);
        }

        @BeforeClass
        @Override
        public void setUp() throws Exception {
            super.setUp();
        }

        @AfterClass
        @Override
        public void tearDown() throws Exception {
            super.tearDown();
        }

        @Override
        protected TestNgStrategy configureStrategy() {
            return new ContainerPerClassTestNgStrategy();
        }
    }

    /**
     * Parent for TestNg tests that needs to create a separate test container for each test in a test class.
     * <p/>
     * The creation and disposal of the test container (or client) are managed by {@link #setUp()} / {@link #tearDown()} methods
     * annotated by TestNG annotations {@link BeforeMethod} / {@link AfterMethod}.
     */
    @SuppressWarnings("UnusedDeclaration")
    public abstract static class ContainerPerMethodTest extends JerseyTestNg {

        public ContainerPerMethodTest() throws TestContainerException {
            super();
        }

        public ContainerPerMethodTest(final TestContainerFactory testContainerFactory) {
            super(testContainerFactory);
        }

        public ContainerPerMethodTest(final Application jaxrsApplication) throws TestContainerException {
            super(jaxrsApplication);
        }

        @BeforeMethod
        @Override
        public void setUp() throws Exception {
            super.setUp();
        }

        @AfterMethod
        @Override
        public void tearDown() throws Exception {
            super.tearDown();
        }

        @Override
        protected TestNgStrategy configureStrategy() {
            return new ContainerPerMethodTestNgStrategy();
        }
    }
}
