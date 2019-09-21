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

package org.glassfish.jersey.test.maven.runner.tomcat

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.glassfish.jersey.test.maven.runner.CommonRedeploy

/**
 * This mojo repetitively redeploys given application to Tomcat and performs a http request to it.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
@Mojo(name = "redeployTomcat")
class RedeployTomcatMojo extends AbstractTomcatRunnerMojo implements CommonRedeploy {

    void executeRunner() throws MojoExecutionException, MojoFailureException {
        redeployAndSendRequest("/runner/tomcat/redeploy.sh", "/runner/tomcat/stop.sh")
    }

}
