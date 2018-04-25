/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.tests.e2e.json.entity.SingleItemListWrapperBean;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Reproducible test case for JERSEY-1835.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RunWith(Parameterized.class)
public class Jersey1835Test extends AbstractJsonTest {

    @Parameterized.Parameters()
    public static Collection<JsonTestSetup[]> generateTestCases() throws Exception {
        final List<JsonTestSetup[]> result = new LinkedList<JsonTestSetup[]>();
        result.add(new JsonTestSetup[] {new JsonTestSetup(new Class<?>[] {SingleItemListWrapperBean.class},
                new JsonTestProvider.JettisonMappedJsonTestProvider())});
        return result;
    }

    public Jersey1835Test(final JsonTestSetup jsonTestSetup) throws Exception {
        super(jsonTestSetup);
    }
}
