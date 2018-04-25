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

package org.glassfish.jersey.tests.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Paul Sandoz
 */
public class ConsumeProduceTest {
    @Consumes({"*/*", "a/*", "b/*", "a/b", "c/d"})
    class ConsumesClass {
    }

    @Produces ({"*/*", "a/*", "b/*", "a/b", "c/d"})
    class ProducesClass {
    }

    @Test
    public void testConsumes() {
        final Consumes c = ConsumesClass.class.getAnnotation(Consumes.class);
        final List<MediaType> l = MediaTypes.createFrom(c);
        checkMediaTypes(l);
    }

    @Test
    public void testProduces() {
        final Produces p = ProducesClass.class.getAnnotation(Produces.class);
        final List<MediaType> l = MediaTypes.createFrom(p);
        checkMediaTypes(l);
    }

    @Consumes("*/*, a/*, b/*, a/b, c/d")
    class ConsumesStringClass {
    }

    @Produces("*/*, a/*, b/*, a/b, c/d")
    class ProducesStringClass {
    }

    @Test
    public void testConsumesString() {
        final Consumes c = ConsumesStringClass.class.getAnnotation(Consumes.class);
        final List<MediaType> l = MediaTypes.createFrom(c);
        checkMediaTypes(l);
    }

    @Test
    public void testProducesString() {
        final Produces p = ProducesStringClass.class.getAnnotation(Produces.class);
        final List<MediaType> l = MediaTypes.createFrom(p);
        checkMediaTypes(l);
    }

    @Consumes({"*/*, a/*", "b/*, a/b", "c/d"})
    class ConsumesStringsClass {
    }

    @Produces({"*/*, a/*", "b/*, a/b", "c/d"})
    class ProducesStringsClass {
    }

    @Test
    public void testConsumesStrings() {
        final Consumes c = ConsumesStringsClass.class.getAnnotation(Consumes.class);
        final List<MediaType> l = MediaTypes.createFrom(c);
        checkMediaTypes(l);
    }

    @Test
    public void testProducesStrings() {
        final Produces p = ProducesStringsClass.class.getAnnotation(Produces.class);
        final List<MediaType> l = MediaTypes.createFrom(p);
        checkMediaTypes(l);
    }


    void checkMediaTypes(final List<MediaType> l) {
        assertEquals(5, l.size());
        assertEquals("a", l.get(0).getType());
        assertEquals("b", l.get(0).getSubtype());
        assertEquals("c", l.get(1).getType());
        assertEquals("d", l.get(1).getSubtype());
        assertEquals("a", l.get(2).getType());
        assertEquals("*", l.get(2).getSubtype());
        assertEquals("b", l.get(3).getType());
        assertEquals("*", l.get(3).getSubtype());
        assertEquals("*", l.get(4).getType());
        assertEquals("*", l.get(4).getSubtype());
    }

}
