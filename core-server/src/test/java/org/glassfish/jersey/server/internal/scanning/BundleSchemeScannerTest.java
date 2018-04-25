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

package org.glassfish.jersey.server.internal.scanning;

import java.net.URI;
import java.util.NoSuchElementException;

import org.glassfish.jersey.server.ResourceFinder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class BundleSchemeScannerTest {

    private ResourceFinder bundleSchemeScanner;

    @Before
    public void setUpBundleSchemeScanner() throws Exception {
        String canonicalName = getClass().getCanonicalName();
        URI uri = getClass().getClassLoader().getResource(canonicalName.replace('.', '/') + ".class").toURI();
        bundleSchemeScanner = new BundleSchemeResourceFinderFactory().create(uri, true);
    }

    @Test
    public void hasNextReturnsTrue() throws Exception {
        Assert.assertTrue(bundleSchemeScanner.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void multipleNextInvocationFails() throws Exception {
        bundleSchemeScanner.next();

        Assert.assertFalse(bundleSchemeScanner.hasNext());
        bundleSchemeScanner.next(); // throw NoSuchElementException
    }

    /**
     * Iterator class {@link org.glassfish.jersey.server.internal.scanning.BundleSchemeResourceFinderFactory.BundleSchemeScanner}
     * breaks the {@link java.util.Iterator} contract as {@link org.glassfish.jersey.server.ResourceFinder#open()} causes it to
     * advance.
     *
     * @throws Exception
     */
    @Test(expected = NoSuchElementException.class)
    public void openFinishesTheIteration() throws Exception {
        Assert.assertNotNull(bundleSchemeScanner.open());
        Assert.assertFalse(bundleSchemeScanner.hasNext());

        bundleSchemeScanner.next(); // throw NoSuchElementException
    }

}
