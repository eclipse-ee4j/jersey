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

package org.glassfish.jersey.test.maven.runner

import org.apache.maven.plugins.annotations.Parameter

/**
 * Common functionality of Start and Deploy Mojos.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
trait CommonStartAndDeploy implements RunnerMojo {

    /**
     * The archive location of an application to be deployed.
     */
    @Parameter(required = true, name = "warPath")
    File warPath

    /**
     * The maximum heap size. Must conform to java {@code Xmx} standards.
     */
    @Parameter(defaultValue = "256m", name = "maxHeap")
    String maxHeap

    /**
     * Additional JVM arguments to pass to the container jvm.
     */
    @Parameter(defaultValue = "", name = "jvmArgs", property = "jersey.runner.jvmArgs")
    String jvmArgs

    /**
     * Whether to skip deployment.
     */
    @Parameter(defaultValue = "false", name = "skipDeploy", property = "jersey.runner.skipDeploy")
    boolean skipDeploy

    /**
     * Whether to skip check for running java processes with a magic identifier
     * {@code jersey.config.test.memleak.*.magicRunnerIdentifier} which provides a way to prevent multiple containers
     * to run in parallel. By default, when another container instance is running, the startup fails in order to prevent
     * an uncontrolled explosion of number of running java containers that weren't supposed to run possibly.
     */
    @Parameter(defaultValue = "false", name = "skipCheck", property = "jersey.runner.skipCheck")
    boolean skipCheck

    Map commonEnvironment() {
        return [
                "WAR_PATH"   : warPath.absolutePath,
                "MAX_HEAP"   : maxHeap,
                "PORT"       : port as String,
                "SKIP_DEPLOY": skipDeploy as String,
                "JVM_ARGS"   : jvmArgs ?: "",
                "SKIP_CHECK" : skipCheck as String
        ]
    }

    void startAndDeployStopOnFailure(String shell, String stopShell) {
        startAndDeployStopOnFailure(shell, stopShell, null)
    }

    void startAndDeployStopOnFailure(String shell, String stopShell, Map env) {
        try {
            executeShell(shell, env)
        } catch (ShellMojoExecutionException e) {
            // regardless of the state we need to be sure, the container wasn't left in a running state
            try {
                executeShell(stopShell)
            } catch (ShellMojoExecutionException se) {
                log.warn("Container stop ended with error.", se)
                // not re-trowing
            }

            throw e
        }
    }

    void setSkipDeploy(final boolean skipDeploy) {
        this.skipDeploy = skipDeploy
    }

    void setMaxHeap(final String maxHeap) {
        this.maxHeap = maxHeap
    }

    void setWarPath(final File warPath) {
        this.warPath = warPath
    }

    void setJvmArgs(final String jvmArgs) {
        this.jvmArgs = jvmArgs
    }

    void setSkipCheck(final boolean skipCheck) {
        this.skipCheck = skipCheck
    }
}
