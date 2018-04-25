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
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.junit.Test;

/**
 * Basic sanity test of {@link FilePatternDoesNotExistRule} enforcer rule.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class FilePatternDoesNotExistRuleTest {

    @Test(expected = EnforcerRuleException.class)
    public void testMatchedPath() throws URISyntaxException, EnforcerRuleException {
        final FilePatternDoesNotExistRule filePatternDoesNotExistRule = new FilePatternDoesNotExistRule();

        final URI uri = getClass()
                .getResource("/org/glassfish/jersey/test/maven/rule/FilePatternDoesNotExistRule.class").toURI();
        final File file = new File(uri);
        final String pattern = file.getAbsolutePath().replace("PatternDoes", "*");
        filePatternDoesNotExistRule.files = new File[] {new File(pattern)};

        filePatternDoesNotExistRule.execute(null);
    }

    @Test
    public void testNotMatchedPath() throws URISyntaxException, EnforcerRuleException {
        final FilePatternDoesNotExistRule filePatternDoesNotExistRule = new FilePatternDoesNotExistRule();

        final URI uri = getClass()
                .getResource("/org/glassfish/jersey/test/maven/rule/FilePatternDoesNotExistRule.class").toURI();
        final File file = new File(uri);
        final String pattern = file.getAbsolutePath().replace("PatternDoes", "*").replace("Exist", "");
        filePatternDoesNotExistRule.files = new File[] {new File(pattern)};

        filePatternDoesNotExistRule.execute(null);
    }
}
