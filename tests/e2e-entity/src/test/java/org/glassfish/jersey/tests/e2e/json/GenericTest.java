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

import org.glassfish.jersey.tests.e2e.json.entity.ColorHolder;
import org.glassfish.jersey.tests.e2e.json.entity.Jersey1199List;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Unignore when you need to run a specific test.
 *
 * @author Michal Gajdos
 */
@Ignore("Unignore only when you need to run a specific test.")
@RunWith(Parameterized.class)
public class GenericTest extends AbstractJsonTest {

    @Parameterized.Parameters()
    public static Collection<JsonTestSetup[]> generateTestCases() throws Exception {
        final List<JsonTestSetup[]> jsonTestSetups = new LinkedList<JsonTestSetup[]>();
        final Class<?>[] classes = {Jersey1199List.class, ColorHolder.class};

        for (JsonTestProvider jsonProvider : new JsonTestProvider[]{new JsonTestProvider.MoxyJsonTestProvider()}) {
            jsonTestSetups.add(new JsonTestSetup[]{new JsonTestSetup(classes, jsonProvider)});
        }

        return jsonTestSetups;
    }

    public GenericTest(final JsonTestSetup jsonTestSetup) throws Exception {
        super(jsonTestSetup);
    }

}
