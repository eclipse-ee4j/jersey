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
import org.apache.maven.plugins.annotations.Parameter
import org.glassfish.jersey.test.maven.runner.CommonStartAndDeploy

/**
 * This mojo starts Tomcat and deploys provided application to it.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
@Mojo(name = "startAndDeployTomcat")
class StartAndDeployTomcatMojo extends AbstractTomcatRunnerMojo implements CommonStartAndDeploy {

    /**
     * For how long (multiplied by 5) to wait for Tomcat to start up.
     */
    @Parameter(defaultValue = "20", name = "triesCount")
    int triesCount

    /**
     * Tomcat specific memory leak prevention listeners flag. Disabled by default so that memory leaks can be detected.
     * When enabled, memory leak prevention is turned on; in particular, listeners
     * {@code org.apache.catalina.core.ThreadLocalLeakPreventionListener},
     * {@code org.apache.catalina.core.JreMemoryLeakPreventionListener} and
     * {@code org.apache.catalina.mbeans.GlobalResourcesLifecycleListener} are enabled.
     *
     */
    @Parameter(defaultValue = "false", property = "jersey.runner.memoryLeakPrevention")
    boolean memoryLeakPrevention

    @Override
    void executeRunner() throws MojoExecutionException, MojoFailureException {
        startAndDeployStopOnFailure("/runner/tomcat/start.sh", "/runner/tomcat/stop.sh", [
                "TRIES_COUNT": triesCount as String,
                "MEMORY_LEAK_PREVENTION": memoryLeakPrevention as String
        ])
    }

}
