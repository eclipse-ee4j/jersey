/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.artifacts;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class LegalDocsIncludedTest extends AbstractMojoTestCase {
    private static final File localRepository = MavenUtil.getLocalMavenRepository();
    private static final Properties properties = MavenUtil.getMavenProperties();

    private static final String LICENSE_FILE = "LICENSE.md";
    private static final String NOTICE_FILE = "NOTICE.md";

    @Test
    public void testLegalFiles() throws IOException, XmlPullParserException {
        TestResult testResult = new TestResult();
        List<File> jars = MavenUtil.streamJerseyJars()
                .map(dependency -> MavenUtil.getArtifactJar(localRepository, dependency, properties))
                .collect(Collectors.toList());
        testLegalFiles(jars, testResult);

        jars = MavenUtil.streamJerseySources()
                .map(dependency -> MavenUtil.getArtifactJar(localRepository, dependency, properties))
                .collect(Collectors.toList());
        testLegalFiles(jars, testResult);
    }

    private void testLegalFiles(List<File> jars, TestResult testResult) throws IOException {
        for (File jar : jars) {
            for (String filename : new String[]{LICENSE_FILE, NOTICE_FILE}) {
                JarFile jarFile = new JarFile(jar);
                String value;
                try {
                    value = jarFile.getEntry("META-INF/" + filename).getName();
                } catch (NullPointerException npe) {
                    value = null;
                }
                TestResult.MessageBuilder builder = value != null ? testResult.ok() : testResult.exception();
                builder.append(jar.getName()).append(value == null ? " DOES NOT CONTAIN " : " CONTAINS ")
                        .append(filename).println(" file");
            }
        }

        //Assertions.assertTrue(testResult.result(), "Some error occurred, see previous messages");
        Assert.assertTrue("Some error occurred, see previous messages", testResult.result());
    }
}
