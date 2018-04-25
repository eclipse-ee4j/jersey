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
import org.apache.maven.plugins.annotations.Parameter
import org.codehaus.gmavenplus.mojo.AbstractGroovyMojo
import org.glassfish.jersey.test.maven.runner.RunnerMojo

import java.nio.file.Paths
/**
 * Abstract class for all Tomcat related mojos.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */

abstract class AbstractTomcatRunnerMojo extends AbstractGroovyMojo implements RunnerMojo {

    /**
     * {@code CATALINA_HOME} environmental variable. If relative path specified, then it is appended to
     * <code>{@link #getDistDir()}/{@link #getDistSubdir()}/</code>.
     */
    @Parameter(defaultValue = ".")
    String catalinaHome

    /**
     * The location of Tomcat log file that is being inspected. If not specified, defaults to
     * <code>{@link #catalinaHome}/logs/catalina.out</code>
     */
    @Parameter
    String logFile

    @Override
    void execute() throws MojoExecutionException, MojoFailureException {
        catalinaHome = Paths.get(catalinaHome).isAbsolute() ? catalinaHome : Paths.get(distDir, distSubdir, catalinaHome)
        logFile = logFile ?: Paths.get(catalinaHome, "logs", "catalina.out").toString()
        executeRunner()
    }

    @Override
    Map containerEnvironment() {
        return [
                "CATALINA_HOME": catalinaHome,
                "CATALINA_PID" : "$catalinaHome/bin/catalina.pid" as String
        ]
    }
}
