/*
 * Copyright (c) 2022, 2025 Oracle and/or its affiliates. All rights reserved.
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

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.junit.Test;

import static org.glassfish.jersey.test.artifacts.MavenUtil.getModelFromFile;
import static org.junit.Assert.assertEquals;

// We test that org.ow2.asm:asm and org.eclipse.persistence:org.eclipse.persistence.asm are to some degree
// equal.
//
// It's not exactly clear what the tolerance is, and why we are checking this.
public class MoxyAsmTest {

    @Test
    public void testAsmInMoxy() throws Exception {
        Dependency moxyAsmDependency =
            getModelFromFile("../../media/moxy/pom.xml")
                .getDependencies()
                .stream()
                .filter(dependency -> dependency.getArtifactId().equals("org.eclipse.persistence.asm"))
                .findFirst().get();

        Model projectPom = MavenUtil.getModelFromFile("../../pom.xml");

        // org.ow2.asm:asm can be 9.9 or 9.9.1
        String asmVersion = projectPom.getProperties().getProperty("asm.version").substring(0, 4);

        // org.eclipse.persistence:org.eclipse.persistence.asm can be 9.7.1, 9.8.0, 9.9.1
        String moxyAsmVersion = findVersionInModel(moxyAsmDependency.getVersion(), projectPom).substring(0, 4);

        assertEquals(
            "org.eclipse.persistence.asm version " + moxyAsmVersion
                + " differs from asm version " + asmVersion + " in /media/moxy/pom.xml",
            asmVersion, moxyAsmVersion);

        System.out.println("Found expected Moxy ASM version " + moxyAsmVersion);
    }

    private static String findVersionInModel(String version, Model model) {
        if (version.startsWith("${")) {
            return
                model.getProperties()
                     .getProperty(version.substring(2, version.length() - 1));
        }

        return version;
    }
}
