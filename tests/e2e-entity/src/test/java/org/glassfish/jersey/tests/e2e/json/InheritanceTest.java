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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.glassfish.jersey.test.spi.TestHelper;
import org.glassfish.jersey.tests.e2e.json.JsonTest.JsonTestSetup;
import org.glassfish.jersey.tests.e2e.json.entity.Animal;
import org.glassfish.jersey.tests.e2e.json.entity.AnimalList;
import org.glassfish.jersey.tests.e2e.json.entity.Cat;
import org.glassfish.jersey.tests.e2e.json.entity.Dog;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;

/**
 * @author Michal Gajdos
 */
public class InheritanceTest {

    @TestFactory
    public Collection<DynamicContainer> generateTests() throws Exception {
        List<DynamicContainer> tests = new ArrayList<>();
        final Class<?>[] classes = {AnimalList.class, Animal.class, Dog.class, Cat.class};

        for (final JsonTestProvider jsonProvider : JsonTestProvider.JAXB_PROVIDERS) {
            // TODO - remove the condition after jsonb polymorphic adapter is implemented
            if (!(jsonProvider instanceof JsonTestProvider.JsonbTestProvider)) {
                JsonTestSetup setupTest = new JsonTestSetup(classes, jsonProvider);
                JsonTest jsonTest = new JsonTest(setupTest) {};
                tests.add(TestHelper.toTestContainer(jsonTest,
                        String.format("inheritanceTest (%s)", jsonProvider.getClass().getSimpleName())));
            }
        }
        return tests;
    }
}
