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
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugins.enforcer.AbstractNonCacheableEnforcerRule;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

/**
 * Maven enforcer rule to enforce that given file does not contain line matching given pattern. When matched, exception is
 * raised.
 * <p/>
 * This is equivalent to shell pseudo-script: {code grep PATTERN FILE && 'raise error'}
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class PatternNotMatchedInFileRule extends AbstractNonCacheableEnforcerRule {

    /**
     * The pattern to look for in the given file.
     */
    String pattern;

    /**
     * The file where to look for lines matching given pattern.
     */
    File file;

    /**
     * Maximum number of lines to be matched (exception is raised regardless of the number of found lines as long as the number is
     * greater than 0)
     */
    int maxMatchedLines = 0;

    public void execute(EnforcerRuleHelper helper)
            throws EnforcerRuleException {

        if (file == null || !file.exists()) {
            return;
        }

        final Pattern patternCompiled = Pattern.compile(pattern);
        try {

            final List<String> lines = Files.readLines(file, Charset.defaultCharset(), new LineProcessor<List<String>>() {
                private List<String> matchedLines = new LinkedList<>();

                @Override
                public boolean processLine(final String line) throws IOException {
                    if (patternCompiled.matcher(line).matches()) {
                        matchedLines.add(line);
                        if (maxMatchedLines != 0 && maxMatchedLines <= matchedLines.size()) {
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public List<String> getResult() {
                    return matchedLines;
                }
            });

            if (lines.size() > 0) {
                throw new EnforcerRuleException(
                        "Found lines matching pattern: '" + pattern + "'! Lines matched: " + Arrays.toString(lines.toArray())
                                + " in file: " + file.getAbsolutePath());
            }

        } catch (IOException e) {
            throw new EnforcerRuleException("I/O Error occurred during processing of file: " + file.getAbsolutePath(), e);
        }
    }
}
