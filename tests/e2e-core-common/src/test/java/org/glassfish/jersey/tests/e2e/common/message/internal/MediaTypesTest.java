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

package org.glassfish.jersey.tests.e2e.common.message.internal;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.message.internal.HttpHeaderReader;
import org.glassfish.jersey.message.internal.MediaTypes;

import org.junit.Assert;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * MediaTypes utility method tests.
 *
 * @author Miroslav Fuksa
 * @author Marek Potociar (marek.potociar at oralce.com)
 */
public class MediaTypesTest {

    @Test
    public void testConvertToString() {
        final List<MediaType> emptyList = Collections.emptyList();
        Assert.assertEquals("", MediaTypes.convertToString(emptyList));


        Assert.assertEquals("\"text/plain\"", MediaTypes.convertToString(Collections.singleton(MediaType.TEXT_PLAIN_TYPE)));

        Assert.assertEquals("\"text/plain\", \"application/json\"",
                MediaTypes.convertToString(Arrays.asList(MediaType.TEXT_PLAIN_TYPE,
                                                         MediaType.APPLICATION_JSON_TYPE)));

        Assert.assertEquals("\"text/plain\", \"application/json\", \"text/html\"",
                            MediaTypes.convertToString(Arrays.asList(MediaType.TEXT_PLAIN_TYPE,
                                                                     MediaType.APPLICATION_JSON_TYPE,
                                                                     MediaType.TEXT_HTML_TYPE)));
    }

    @Test
    public void testMostSpecific() {

        MediaType m1;
        MediaType m2;

        /* wildcard type */
        m1 = MediaType.WILDCARD_TYPE;

        // wildcard type #1 - concrete type wins
        m2 = new MediaType("foo", "bar");
        _testMostSpecific(m1, m2, m2);
        _testMostSpecific(m2, m1, m2);

        // wildcard type #2 - wildcard subtype wins
        m2 = new MediaType("foo", "*");
        _testMostSpecific(m1, m2, m2);
        _testMostSpecific(m2, m1, m2);

        // wildcard type #3 - first parameter wins
        m2 = new MediaType("*", "*");
        _testMostSpecific(m1, m2, m1);
        _testMostSpecific(m2, m1, m2);

        /* wildcard subtype */
        m1 = new MediaType("moo", "*");

        // wildcard subtype #1 - concrete type wins
        m2 = new MediaType("foo", "bar");
        _testMostSpecific(m1, m2, m2);
        _testMostSpecific(m2, m1, m2);

        // wildcard subtype #2 - first parameter in method wins
        m2 = new MediaType("foo", "*");
        _testMostSpecific(m1, m2, m1);
        _testMostSpecific(m2, m1, m2);

        /* concrete types */
        // concrete types - first parameter in method wins
        m1 = new MediaType("moo", "boo");
        m2 = new MediaType("foo", "bar");
        _testMostSpecific(m1, m2, m1);
        _testMostSpecific(m2, m1, m2);

        /* concrete type with parameters */
        m1 = new MediaType("foo", "bar", asMap("p1=v1;p2=v2"));

        // concrete type with parameters #1 - wildcard type looses
        m2 = MediaType.WILDCARD_TYPE;
        _testMostSpecific(m1, m2, m1);
        _testMostSpecific(m2, m1, m1);

        // concrete type with parameters #2 - wildcard subtype looses
        m2 = new MediaType("foo", "*");
        _testMostSpecific(m1, m2, m1);
        _testMostSpecific(m2, m1, m1);

        // concrete type with parameters #3 - concrete parameter-less type looses
        m2 = new MediaType("foo", "baz");
        _testMostSpecific(m1, m2, m1);
        _testMostSpecific(m2, m1, m1);

        // concrete type with parameters #4 - type with less parameters type looses
        m2 = new MediaType("foo", "baz", asMap("a1=b1"));
        _testMostSpecific(m1, m2, m1);
        _testMostSpecific(m2, m1, m1);

        // both concrete types with parameters #5 - first parameter in method wins
        m2 = new MediaType("foo", "baz", asMap("a1=b1;a2=b2"));
        _testMostSpecific(m1, m2, m1);
        _testMostSpecific(m2, m1, m2);
    }

    private static void _testMostSpecific(MediaType m1, MediaType m2, MediaType result) {
        assertThat("Unexpected media type selected to be most specific.",
                MediaTypes.mostSpecific(m1, m2), is(result));
    }

    /**
     * Creates a map from HTTP header parameter strings.
     *
     * @param parameters HTTP header parameters string.
     * @return HTTP header parameters map.
     */
    public static Map<String, String> asMap(String parameters) {
        HttpHeaderReader reader = HttpHeaderReader.newInstance(";" + parameters);

        if (reader.hasNext()) {
            try {
                return HttpHeaderReader.readParameters(reader);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        return Collections.emptyMap();
    }
}
