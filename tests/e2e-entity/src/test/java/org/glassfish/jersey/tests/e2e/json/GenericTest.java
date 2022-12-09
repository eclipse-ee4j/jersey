/*
 * Copyright (c) 2012, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.json;

import org.glassfish.jersey.test.spi.TestHelper;
import org.glassfish.jersey.tests.e2e.json.JsonTest.JsonTestSetup;
import org.glassfish.jersey.tests.e2e.json.entity.ColorHolder;
import org.glassfish.jersey.tests.e2e.json.entity.Jersey1199List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;

/**
 * Unignore when you need to run a specific test.
 *
 * @author Michal Gajdos
 */
@Disabled("Unignore only when you need to run a specific test.")
public class GenericTest {

    @TestFactory
    public DynamicContainer generateTests() throws Exception {
        final Class<?>[] classes = {Jersey1199List.class, ColorHolder.class};

        JsonTestProvider jsonProvider = new JsonTestProvider.MoxyJsonTestProvider();
        JsonTestSetup setupTest = new JsonTestSetup(classes, jsonProvider);
        JsonTest jsonTest = new JsonTest(setupTest) {};
        return TestHelper.toTestContainer(jsonTest, String.format("genTest (%s)", jsonProvider.getClass().getSimpleName()));
    }
}
