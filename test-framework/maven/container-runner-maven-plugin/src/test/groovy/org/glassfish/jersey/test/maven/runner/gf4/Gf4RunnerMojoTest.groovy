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

package org.glassfish.jersey.test.maven.runner.gf4

import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.testing.AbstractMojoTestCase
import org.junit.Assert
import org.junit.Assume
import org.junit.Test

/**
 * Glassfish4 runner tests.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
class Gf4RunnerMojoTest extends AbstractMojoTestCase {

    private Mojo lookupMojoTestPom(String goal, String pomFile) {
        def resource = getClass().getResource(pomFile)
        if (resource == null) {
            throw new IllegalStateException("Pom file: $pomFile was not located on classpath!")
        }
        def file = new File(resource.toURI())
        if (!file.exists()) {
            throw new IllegalStateException("Cannot locate test pom xml file!")
        }
        return lookupMojo(goal, file)
    }

    @Test
    void testDownloadGf4Mojo() {
        def mojo = lookupMojoTestPom("downloadGf4", "/pom-download-mojo.xml")
        mojo.execute()

        Assert.assertTrue("As admin in glassfish4/glassfish/bin/asadmin wasn't created",
                new File(getClass().getResource("/gf4/glassfish4/glassfish/bin/asadmin").toURI()).exists())
    }

    @Test
    void testStartAndDeployGf4Mojo() {
        def mojo = lookupMojoTestPom("startAndDeployGf4", "/pom-start-deploy-mojo.xml")
        mojo.execute()
    }

    @Test
    void testStopGf4Mojo() {
        def mojo = lookupMojoTestPom("stopGf4", "/pom-undeploy-stop-mojo.xml")
        mojo.execute()
    }
}
