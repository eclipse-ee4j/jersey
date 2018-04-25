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

package org.glassfish.jersey.tests.e2e.sonar;

import org.glassfish.jersey.internal.sonar.SonarJerseyCommon;
import org.glassfish.jersey.server.internal.sonar.SonarJerseyServer;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class SonarJerseyE2ETest {

    @Test
    public void testJerseyCommonE2E() {
        Assert.assertEquals("common e2e", new SonarJerseyCommon().e2e());
    }

    @Test
    public void testJerseyCoreE2E() {
        Assert.assertEquals("server e2e", new SonarJerseyServer().e2e());
    }
}
