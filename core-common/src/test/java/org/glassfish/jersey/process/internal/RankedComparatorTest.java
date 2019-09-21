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

package org.glassfish.jersey.process.internal;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Priority;

import org.glassfish.jersey.model.internal.RankedComparator;
import org.glassfish.jersey.model.internal.RankedProvider;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link org.glassfish.jersey.model.internal.RankedComparator}.
 * @author Miroslav Fuksa
 *
 */
public class RankedComparatorTest {

    @Test
    public void testPriorityComparator() {
        List<RankedProvider<Object>> list = new LinkedList<>();
        list.add(new RankedProvider<Object>(new F1000()));
        list.add(new RankedProvider<Object>(new FF200()));
        list.add(new RankedProvider<Object>(new F0()));
        list.add(new RankedProvider<Object>(new F0()));
        list.add(new RankedProvider<Object>(new F300()));
        list.add(new RankedProvider<Object>(new F1000()));
        list.add(new RankedProvider<Object>(new F100()));
        list.add(new RankedProvider<Object>(new F200()));
        list.add(new RankedProvider<Object>(new F200()));
        list.add(new RankedProvider<Object>(new F_INT_MIN()));
        list.add(new RankedProvider<Object>(new F_INT_MAX()));
        Collections.sort(list, new RankedComparator<Object>(RankedComparator.Order.ASCENDING));
        int max = Integer.MIN_VALUE;
        for (RankedProvider<Object> o : list) {
            int val = o.getRank();
            assertTrue(val >= max);
            max = val;
        }

        Collections.sort(list, new RankedComparator<Object>(RankedComparator.Order.DESCENDING));
        max = Integer.MAX_VALUE;
        for (RankedProvider<Object> o : list) {
            int val = o.getRank();
            assertTrue(val <= max);
            max = val;
        }
    }

    @Priority(0)
    private static class F0 {
    }

    @Priority(100)
    private static class F100 {
    }

    @Priority(200)
    private static class F200 {
    }

    @Priority(200)
    private static class FF200 {
    }

    @Priority(300)
    private static class F300 {
    }

    @Priority(1000)
    private static class F1000 {
    }

    @Priority(Integer.MIN_VALUE)
    private static class F_INT_MIN {
    }

    @Priority(Integer.MAX_VALUE)
    private static class F_INT_MAX {
    }
}
