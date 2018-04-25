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

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Parameter
import org.codehaus.gmavenplus.mojo.AbstractGroovyMojo
import org.glassfish.jersey.test.maven.runner.RunnerMojo

import java.nio.file.Paths
/**
 * Abstract class for all Glassfish4 related mojos.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
abstract class AbstractGlassfishRunnerMojo extends AbstractGroovyMojo implements RunnerMojo {

    /**
     * The {@code AS_HOME} environment variable value. If relative directory is specified (which is the default), it is derived
     * from {@link #getDistDir()} and {@link #getDistSubdir()} together with the value of this property.
     */
    @Parameter(defaultValue = "glassfish")
    String asHome

    /**
     * The Glassfish admin port
     */
    @Parameter(defaultValue = "24848")
    int adminPort

    /**
     * The timeout value in milliseconds of {@code AS_ADMIN_READTIMEOUT} environmental variable.
     */
    @Parameter(defaultValue = "60000")
    int asAdminReadTimeout

    /**
     * The location of log file. If relative path specified, it is derived from <code>{@link #asHome}/domains/
     * {@link #getDomain()}/logs/&lt;the value of this property&gt;</code>
     */
    @Parameter(defaultValue = "server.log")
    String logFile

    @Override
    void execute() throws MojoExecutionException, MojoFailureException {
        asHome = Paths.get(asHome).isAbsolute() ? asHome : Paths.get(distDir, distSubdir, asHome)
        logFile = Paths.get(logFile).isAbsolute()? logFile : Paths.get(asHome, "domains", domain, "logs", logFile)
        executeRunner()
    }

    @Override
    Map containerEnvironment() {
        return [
                "AS_HOME"             : asHome,
                "ADMINPORT"           : adminPort as String,
                "AS_ADMIN_READTIMEOUT": asAdminReadTimeout as String
        ]
    }
}
