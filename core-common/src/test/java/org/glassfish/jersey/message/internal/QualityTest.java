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

package org.glassfish.jersey.message.internal;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Quality unit tests.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@RunWith(Parameterized.class)
public class QualityTest {

    private static final Locale ORIGINAL_LOCALE = Locale.getDefault();

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{{Locale.US}, {Locale.GERMANY}});
    }

    @Parameterized.Parameter(0)
    public Locale locale;

    @Before
    public void setUp() throws Exception {
        Locale.setDefault(locale);
    }

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(ORIGINAL_LOCALE);
    }

    /**
     * Test enhancing HTT header parameter map with a quality parameter.
     */
    @Test
    public void testEnhanceWithQualityParameter() {
        Map<String, String> result;

        result = Quality.enhanceWithQualityParameter(null, "q", 1000);
        assertThat(result, equalTo(null));

        result = Quality.enhanceWithQualityParameter(null, "q", 200);
        assertThat(result, equalTo(asMap("q=0.2")));

        result = Quality.enhanceWithQualityParameter(null, "q", 220);
        assertThat(result, equalTo(asMap("q=0.22")));

        result = Quality.enhanceWithQualityParameter(null, "q", 222);
        assertThat(result, equalTo(asMap("q=0.222")));

        Map<String, String> parameters;

        parameters = asMap("a=b");
        result = Quality.enhanceWithQualityParameter(parameters, "q", 1000);
        assertThat(result, equalTo(parameters));

        result = Quality.enhanceWithQualityParameter(parameters, "q", 200);
        assertThat(result, equalTo(asMap("a=b;q=0.2")));

        result = Quality.enhanceWithQualityParameter(parameters, "q", 220);
        assertThat(result, equalTo(asMap("a=b;q=0.22")));

        result = Quality.enhanceWithQualityParameter(parameters, "q", 222);
        assertThat(result, equalTo(asMap("a=b;q=0.222")));

        // test quality parameter override
        parameters = asMap("a=b;q=0.3");
        result = Quality.enhanceWithQualityParameter(parameters, "q", 1000);
        assertThat(result, equalTo(asMap("a=b;q=1.0")));

        result = Quality.enhanceWithQualityParameter(parameters, "q", 200);
        assertThat(result, equalTo(asMap("a=b;q=0.2")));

        result = Quality.enhanceWithQualityParameter(parameters, "q", 220);
        assertThat(result, equalTo(asMap("a=b;q=0.22")));

        result = Quality.enhanceWithQualityParameter(parameters, "q", 222);
        assertThat(result, equalTo(asMap("a=b;q=0.222")));
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
