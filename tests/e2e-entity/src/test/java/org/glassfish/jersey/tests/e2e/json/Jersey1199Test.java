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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.json.bind.adapter.JsonbAdapter;

import org.glassfish.jersey.test.spi.TestHelper;
import org.glassfish.jersey.tests.e2e.json.JsonTest.JsonTestSetup;
import org.glassfish.jersey.tests.e2e.json.entity.ColorHolder;
import org.glassfish.jersey.tests.e2e.json.entity.Jersey1199List;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;

/**
 * @author Michal Gajdos
 */
public class Jersey1199Test {

    @TestFactory
    public Collection<DynamicContainer> generateTests() throws Exception {
        List<DynamicContainer> tests = new ArrayList<>();
        final Class<?>[] classes = {Jersey1199List.class, ColorHolder.class};

        for (final JsonTestProvider jsonProvider : JsonTestProvider.JAXB_PROVIDERS) {
            JsonTestSetup setupTest = new JsonTestSetup(classes, jsonProvider);
            JsonTest jsonTest = new JsonTest(setupTest) {};
            tests.add(TestHelper.toTestContainer(jsonTest,
                    String.format("jersey1199Test (%s)", jsonProvider.getClass().getSimpleName())));
        }

        return tests;
    }

    /**
     * Custom {@link JsonbAdapter} to provide JSONB with the type hidden behind the generic Object array returned by
     * {@link Jersey1199List#getObjects()}
     */
    public static class JsonbObjectToColorHolderAdapter implements JsonbAdapter<Object[], ColorHolder[]> {

        @Override
        public ColorHolder[] adaptToJson(Object[] o) throws Exception {
            return Arrays.copyOf(o, o.length, ColorHolder[].class);
        }

        @Override
        public Object[] adaptFromJson(ColorHolder[] colorHolder) throws Exception {
            return colorHolder;
        }
    }

}
