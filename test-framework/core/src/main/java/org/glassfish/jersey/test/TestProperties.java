/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.internal.util.PropertiesClass;
import org.glassfish.jersey.test.spi.TestContainerFactory;

/**
 * Jersey test framework configuration properties.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@PropertiesClass
public final class TestProperties {

    /**
     * If set to {@code true} the property enables basic logging of the request and
     * response flow on both - client and server. Note that traffic logging does not
     * dump message entities by default. Please see {@link #DUMP_ENTITY} documentation
     * for instructions how to enable entity content dumping.
     * <p/>
     * The default value is {@code false}.
     * <p/>
     * The name of the configuration property is <tt>{@value}</tt>.
     */
    public static final String LOG_TRAFFIC = "jersey.config.test.logging.enable";

    /**
     * If set to {@code true} the property instructs the test traffic logger to
     * dump message entities as part of the test traffic logging. Message entity
     * dumping is turned off by default for performance reasons. Note that the
     * value of the property will be ignored unless {@link #LOG_TRAFFIC traffic
     * logging} is enabled too.
     * <p/>
     * The default value is {@code false}.
     * <p/>
     * The name of the configuration property is <tt>{@value}</tt>.
     */
    public static final String DUMP_ENTITY = "jersey.config.test.logging.dumpEntity";

    /**
     * Specifies the {@link TestContainerFactory test container factory} implementation
     * class to be used to create a test container instance for the test. The value
     * of the property must be a {@code String} identifying a valid, fully qualified
     * name of a test container factory implementation class, otherwise it will
     * be ignored.
     * <p/>
     * The default value is <tt>{@value #DEFAULT_CONTAINER_FACTORY}</tt>.
     * <p/>
     * The name of the configuration property is <tt>{@value}</tt>.
     *
     * @see #CONTAINER_PORT
     */
    public static final String CONTAINER_FACTORY = "jersey.config.test.container.factory";

    /**
     * Specifies the default {@link TestContainerFactory test container factory}
     * implementation class to be used to create a test container instance for the
     * test.
     *
     * @see #CONTAINER_FACTORY
     */
    public static final String DEFAULT_CONTAINER_FACTORY = "org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory";

    /**
     * Specifies the network connection port to be used by an active test container
     * for test application deployment. The value of the property must be a valid
     * positive integer, otherwise it will be ignored.
     * <p/>
     * If the value of the property is {@code 0} then first available port is used.
     * <p/>
     * The default value is <tt>{@value #DEFAULT_CONTAINER_PORT}</tt>.
     * <p/>
     * The name of the configuration property is <tt>{@value}</tt>.
     *
     * @see #CONTAINER_FACTORY
     */
    public static final String CONTAINER_PORT = "jersey.config.test.container.port";

    /**
     * Specifies the default network connection port to be used by an active test
     * container for test application deployment.
     *
     * @see #CONTAINER_PORT
     */
    public static final int DEFAULT_CONTAINER_PORT = 9998;

    /**
     * If set to a numeric value then this property enables to store log records at {@link java.util.logging.Level log level}
     * value (or higher) defined by the value of this property.
     * Log records can be retrieved in tests using {@link org.glassfish.jersey.test.JerseyTest#getLoggedRecords()}.
     * <p/>
     * This property is not supported for parallel tests.
     * <p/>
     * The name of the configuration property is <tt>{@value}</tt>.
     */
    public static final String RECORD_LOG_LEVEL = "jersey.config.test.logging.record.level";

    /**
     * Specifies the multiplier which will be applied to timeouts for asynchronous tests. This property is useful to be
     * defined if there are problems with environment in which tests run and tests fail for timeout due to slow
     * processing.
     * <p>
     * For example if the timeout for asynchronous test is 5 seconds and this property is defined to 3
     * then the timeout final will be 15.
     * </p>
     *
     * <p/>
     * The property must be an integer value greater than 1.
     * <p/>
     * The default value is <tt>1</tt>.
     * <p/>
     * The name of the configuration property is <tt>{@value}</tt>.
     */
    public static final String ASYNC_TIMEOUT_MULTIPLIER = "jersey.config.test.async.timeout.multiplier";

    private TestProperties() {
        // prevents instantiation
    }
}
