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
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugins.enforcer.AbstractNonCacheableEnforcerRule;

/**
 * Maven enforcer rule to enforce that given set of files does not exist.<br/>
 * This rule is similar to apache {@code requireFilesDontExist} with a support for wildcards.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class FilePatternDoesNotExistRule extends AbstractNonCacheableEnforcerRule {

    File[] files;

    public void execute(EnforcerRuleHelper helper)
            throws EnforcerRuleException {

        if (files == null) {
            return;
        }

        for (File file : files) {
            final String filePath = file.getPath();
            final String prefix = filePath.substring(0, filePath.indexOf("*"));
            final String dirName = prefix.substring(0, prefix.lastIndexOf(File.separator));
            final String fileItselfPattern = filePath.substring(dirName.length() + 1);

            final File dir = new File(dirName);
            if (!dir.isDirectory()) {
                continue;
            }

            final Set<File> matchedFiles = new TreeSet<>();
            for (File matchedFile : dir.listFiles((FileFilter) new WildcardFileFilter(fileItselfPattern))) {
                matchedFiles.add(matchedFile);
            }

            if (!matchedFiles.isEmpty()) {
                throw new EnforcerRuleException("Files found! " + Arrays.toString(matchedFiles.toArray()));
            }
        }
    }
}
