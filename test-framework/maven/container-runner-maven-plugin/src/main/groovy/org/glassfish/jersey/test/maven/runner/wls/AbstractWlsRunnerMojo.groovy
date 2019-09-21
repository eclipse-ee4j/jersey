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
import org.apache.maven.plugins.annotations.Parameter
import org.codehaus.gmavenplus.mojo.AbstractGroovyMojo
import org.glassfish.jersey.test.maven.runner.RunnerMojo

import java.nio.file.Paths

/**
 * Abstract class for all Weblogic related mojos.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
abstract class AbstractWlsRunnerMojo extends AbstractGroovyMojo implements RunnerMojo {

    /**
     * The value of {@code MW_HOME} directory
     */
    @Parameter(defaultValue = ".")
    String mwHome

    /**
     * The WLS server name. Passed to WLS JVM as {@code weblogic.Name}.
     */
    @Parameter(defaultValue = "MemoryLeakTestServer")
    String serverName

    /**
     * The location of Wls log file that is being inspected. If not specified, defaults to
     * <code>{@link #mwHome}/{@link #getDomain()}/wls.log</code> which is where the WLS outputs its messages by default.
     */
    @Parameter
    String logFile;

    @Override
    void execute() throws MojoExecutionException, MojoFailureException {
        mwHome = Paths.get(mwHome).isAbsolute() ? mwHome : Paths.get(distDir, distSubdir, mwHome)
        logFile = logFile ?: Paths.get(mwHome, domain, "wls.log").toString()
        executeRunner()
    }

    Map containerEnvironment() {
        return [
                "MW_HOME"        : mwHome,
                "WLS_SERVER_NAME": serverName
        ]
    }


}
