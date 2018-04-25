/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.ext.cdi1x.hk2ban;

import java.lang.reflect.Type;

import org.glassfish.jersey.ext.cdi1x.spi.Hk2CustomBoundTypesProvider;
import org.glassfish.jersey.internal.ServiceFinder;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Test for {@link EmptyHk2CustomInjectionTypeProvider}.
 * Make sure that the empty provider could be loaded and provides an empty type set.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class EmptyHk2CustomInjectionTypeProviderTest {

    /**
     * Test sub-resource detection.
     */
    @Test
    public void testEmptyProviderLookup() {

        final Hk2CustomBoundTypesProvider[] providers = ServiceFinder.find(Hk2CustomBoundTypesProvider.class).toArray();
        assertThat(providers, is(notNullValue()));
        assertThat(providers.length, is(1));

        final Hk2CustomBoundTypesProvider theOnlyProvider = providers[0];
        assertThat(theOnlyProvider, is(instanceOf(EmptyHk2CustomInjectionTypeProvider.class)));
        assertThat(theOnlyProvider.getHk2Types(), is(emptyCollectionOf(Type.class)));
    }
}
