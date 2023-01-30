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

package org.glassfish.jersey.uri;

import java.util.regex.MatchResult;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link PathTemplate}.
 *
 * @author Marek Potociar
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
public class PathPatternTest {

    @Test
    public void testTerminalPathPatterMatching() {
        final String[] rootPaths = new String[]{
                "/a/b/c",
                "/a/b/c/"
        };

        final String[] subPaths = new String[]{
                "d/e",
                "d/e/",
                "/d/e",
                "/d/e/",
        };

        final String path1 = "/a/b/c";
        final String path2 = "/a/b/c/";

        final PathPattern[] patterns = new PathPattern[]{
                new PathPattern("a"),
                new PathPattern("b"),
                new PathPattern("c"),
                new PathPattern("", PathPattern.RightHandPath.capturingZeroSegments)
        };

        String rhp = path1;
        MatchResult matchResult;

        for (PathPattern pattern : patterns) {
            matchResult = pattern.match(rhp);
            assertNotNull(matchResult, "No match of " + rhp + " for pattern " + pattern);
            rhp = matchResult.group(matchResult.groupCount());
            rhp = (rhp == null) ? "" : rhp;
        }

        assertEquals("", rhp);

        rhp = path2;

        for (PathPattern pattern : patterns) {
            matchResult = pattern.match(rhp);
            rhp = matchResult.group(matchResult.groupCount());
        }

        assertEquals("/", rhp);
    }

    @Test
    public void testSimplePattern() throws Exception {
        PathPattern pattern = new PathPattern("/test");
        assertNull(pattern.match("doesn't match"));
        assertNotNull(pattern.match("/test/me"));
    }

    @Test
    public void testSimplePatternWithRightHandSide() throws Exception {

        PathPattern pattern = new PathPattern(new PathTemplate("/test/{template: abc.*}"));
        assertNull(pattern.match("/test/me"), "Why matched?");
        assertNotNull(pattern.match("/test/abc-should_work"), "Why not matched?");
    }

    @Test
    public void testSetsAndGetsUriTemplate() throws Exception {
        PathTemplate tmpl = new PathTemplate("/test");
        PathPattern pattern = new PathPattern(tmpl);
        assertEquals(
                tmpl,
                pattern.getTemplate(),
                "We just injected the value, why it is different?"
        );
    }

    @Test
    public void testLastElementOfMatchIsRestOfPath() throws Exception {
        PathPattern path = new PathPattern("{a: (\\d)(\\d*)}-{b: (\\d)(\\d*)}-{c: (\\d)(\\d*)}");


        MatchResult m = path.match("/123-456-789/d");
        String value = m.group(m.groupCount());

        assertEquals(
                "/d",
                value,
                "Last value should match all of the trailing part"
        );
    }
}
