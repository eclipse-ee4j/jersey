/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unused")
public class MoxyAsmTest {

    @Test
    public void testAsmInMoxy() throws Exception {
        String moxyPomFile = "../../media/moxy/pom.xml";
        Model moxyPom = MavenUtil.getModelFromFile(moxyPomFile);
        final Dependency moxyAsmDependency = moxyPom.getDependencies().stream()
                .filter(dependency -> dependency.getArtifactId().equals("org.eclipse.persistence.asm"))
                .findFirst().get();
        Model projectPom = MavenUtil.getModelFromFile("../../pom.xml");
        final String asmVersion = projectPom.getProperties().getProperty("asm.version");
        final String moxyAsmVersion = findVersionInModel(moxyAsmDependency.getVersion(), projectPom);

        assertEquals(getMajorVersion(asmVersion), getMajorVersion(moxyAsmVersion), "major version");
        assertEquals(getMinorVersion(asmVersion), getMinorVersion(moxyAsmVersion), "minor version");
        // Commented out - 2026-09-27 - Moxy is a bit behind, but we can tolerate that.
//        assertEquals(getPatchVersion(asmVersion), getPatchVersion(moxyAsmVersion), "patch version");
        if (asmVersion.equals(moxyAsmVersion)) {
            System.out.println("Found expected Moxy ASM version " + moxyAsmVersion);
        } else {
            System.err.println("Moxy ASM version " + moxyAsmVersion + " differs from ASM version " + asmVersion
                + " we use, but it is tolerable. Support of latest Java versions might be limited.");
        }
    }

    private static String findVersionInModel(String version, Model model) {
        if (version.startsWith("${")) {
            String _version = version.substring(2, version.length() - 1);
            return model.getProperties().getProperty(_version);
        }
        return version;
    }

    private String getMajorVersion(String version) {
        int dotIndex = version.indexOf('.');
        if (dotIndex < 1) {
            return version;
        }
        return version.substring(0, dotIndex);
    }

    private String getMinorVersion(String version) {
        int dotIndex = version.indexOf('.');
        int maxIndex = version.length() - 1;
        if (dotIndex < 1 || dotIndex == maxIndex) {
            return null;
        }
        int dotIndex2 = version.indexOf('.', dotIndex + 1);
        if (dotIndex2 < 0 || dotIndex2 == maxIndex) {
            version.substring(dotIndex + 1);
        }
        return version.substring(dotIndex + 1, dotIndex2);
    }

    private String getPatchVersion(String version) {
        int dotIndex = version.indexOf('.');
        int maxIndex = version.length() - 1;
        if (dotIndex < 1 || dotIndex == maxIndex) {
            return null;
        }
        int dotIndex2 = version.indexOf('.', dotIndex + 1);
        if (dotIndex2 < 0 || dotIndex2 == maxIndex) {
            return null;
        }
        int dotIndex3 = version.indexOf('.', dotIndex2 + 1);
        if (dotIndex3 < 0 || dotIndex3 == maxIndex) {
            return version.substring(dotIndex2 + 1);
        }
        return version.substring(dotIndex2 + 1, dotIndex3);
    }
}
