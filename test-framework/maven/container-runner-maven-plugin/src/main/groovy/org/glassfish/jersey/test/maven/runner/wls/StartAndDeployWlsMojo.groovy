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

package org.glassfish.jersey.test.maven.runner.wls

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.glassfish.jersey.test.maven.runner.CommonStartAndDeploy
import org.glassfish.jersey.test.maven.runner.ShellMojoExecutionException

/**
 * This mojo starts Weblogic and deploys provided application to it.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
@Mojo(name = "startAndDeployWls")
class StartAndDeployWlsMojo extends AbstractWlsRunnerMojo implements CommonStartAndDeploy {

    /**
     * For how long (multiplied by 5) to wait for Weblogic to start up.
     */
    @Parameter(defaultValue = "20", name = "triesCount")
    int triesCount

    @Override
    void executeRunner() throws MojoExecutionException, MojoFailureException {
        startAndDeployStopOnFailure("/runner/wls/start.sh", "/runner/wls/stop.sh", [
                "TRIES_COUNT": triesCount as String,
        ])
    }

}
