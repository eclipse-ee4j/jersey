/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.sonar;

/**
 * The purpose of this class is to verify the reported test coverage shows correct results in various modes of test executions.
 * For further details, see javadoc bellow.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class SonarJerseyCommon {

    /**
     * This method is invoked indirectly from the tests.
     */
    public String common() {
        return "common";
    }

    /**
     * A method that is executed from a unit test by maven surefire plugin within the same Maven module.
     */
    public String unitTest() {
        return common() + " unit test";
    }

    /**
     * This method is executed from a unit test by maven surefire plugin from a dependant module.
     */
    public String e2e() {
        return common() + " e2e";
    }

    /**
     * A method that is executed in a JVM of maven failsafe plugin from a dependant maven module. The call is executed directly.
     */
    public String integrationTestJvm() {
        return common() + " test jvm";
    }

    /**
     * This method is executed from a server (Jetty for instance) during the integration test phase. This server is called by a
     * JUnit test that is executed by maven failsafe plugin.
     */
    public String integrationServerJvm() {
        return common() + " server jvm";
    }
}
