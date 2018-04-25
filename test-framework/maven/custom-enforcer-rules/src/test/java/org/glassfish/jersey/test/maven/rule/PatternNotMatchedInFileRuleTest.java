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

package org.glassfish.jersey.test.maven.rule;

import java.io.File;
import java.net.URISyntaxException;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.junit.Test;

/**
 * Basic sanity test of {@link PatternNotMatchedInFileRule} enforcer rule.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class PatternNotMatchedInFileRuleTest {

    @Test(expected = EnforcerRuleException.class)
    public void testMatchedPath() throws URISyntaxException, EnforcerRuleException {
        testFilePatternLineMatcher("/out_of_memory.dat");
    }

    @Test
    public void testNotMatchedPath() throws URISyntaxException, EnforcerRuleException {
        testFilePatternLineMatcher("/ok.dat");
    }

    private void testFilePatternLineMatcher(String fileOnClasspath) throws URISyntaxException, EnforcerRuleException {
        final PatternNotMatchedInFileRule filePatternDoesNotContainLineMatching = new
                PatternNotMatchedInFileRule();

        filePatternDoesNotContainLineMatching.file = new File(getClass().getResource(fileOnClasspath).toURI());
        filePatternDoesNotContainLineMatching.pattern = ".*java\\.lang\\.OutOfMemoryError.*";

        filePatternDoesNotContainLineMatching.execute(null);
    }
}
