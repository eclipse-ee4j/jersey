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

package org.glassfish.jersey.server.internal.scanning;

import java.io.InputStream;
import java.util.NoSuchElementException;

import org.glassfish.jersey.server.ResourceFinder;
import org.glassfish.jersey.server.internal.AbstractResourceFinderAdapter;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class CompositeResourceFinderTest {

    public static class MyIterator extends AbstractResourceFinderAdapter {

        boolean iterated = false;

        @Override
        public boolean hasNext() {
            return !iterated;
        }

        @Override
        public String next() {
            if (!iterated) {
                iterated = true;
                return "value";
            }

            throw new NoSuchElementException();
        }

        @Override
        public void reset() {
        }

        @Override
        public InputStream open() {
            return null;
        }
    }

    @Test
    public void test() {
        final ResourceFinder i = new MyIterator();
        final ResourceFinder j = new MyIterator();

        final CompositeResourceFinder iteratorStack = new CompositeResourceFinder();
        iteratorStack.push(i);
        iteratorStack.push(j);

        assertEquals(iteratorStack.next(), "value");
        assertEquals(iteratorStack.next(), "value");

        try {
            iteratorStack.next();
            assertTrue(false);
        } catch (final NoSuchElementException nsee) {
            assertTrue(true);
        }
    }
}
