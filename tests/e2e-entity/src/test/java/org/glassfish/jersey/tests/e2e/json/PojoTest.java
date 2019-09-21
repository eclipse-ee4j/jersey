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

package org.glassfish.jersey.tests.e2e.json;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.glassfish.jersey.tests.e2e.json.entity.pojo.PojoAnimal;
import org.glassfish.jersey.tests.e2e.json.entity.pojo.PojoAnimalList;
import org.glassfish.jersey.tests.e2e.json.entity.pojo.PojoCat;
import org.glassfish.jersey.tests.e2e.json.entity.pojo.PojoDog;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class PojoTest extends AbstractJsonTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<JsonTestSetup[]> generateTestCases() throws Exception {
        final List<JsonTestSetup[]> jsonTestSetups = new LinkedList<>();
        final Class<?>[] classes = {PojoAnimalList.class, PojoAnimal.class, PojoDog.class, PojoCat.class};

        for (final JsonTestProvider jsonProvider : JsonTestProvider.POJO_PROVIDERS) {
            jsonTestSetups.add(new JsonTestSetup[]{
                    new JsonTestSetup(classes, jsonProvider)
            });
        }

        return jsonTestSetups;
    }

    public PojoTest(final JsonTestSetup jsonTestSetup) throws Exception {
        super(jsonTestSetup);
    }

}
