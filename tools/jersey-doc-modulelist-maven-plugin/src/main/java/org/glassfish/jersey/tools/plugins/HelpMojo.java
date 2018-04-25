/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tools.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Displays the plugin help message.
 *
 * @goal help
 * @phase process-sources
 * @aggregator
 */
public class HelpMojo extends AbstractMojo {

    private Log log;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        log.info("jersey-doc-modulelist-maven-plugin help");
        log.info("=======================================");
        log.info("Walks through the dependency tree and creates a list of maven modules in the docbook format.");
        log.info("The plugin contains a predefined list of known categories (based on groupId), "
                + "each one is represented by the separate table in the docbook output.");
        log.info("");
        log.info("Plugin needs some external files to be able to correctly format the output.");
        log.info("");
        log.info("Configuration: ");
        log.info("  <outputFileName>        specifies where the final output (docbook section) should be written");
        log.info("  <templateFileName>      the main template - contains the entire section and a placeholder "
                + GenerateJerseyModuleListMojo.CONTENT_PLACEHOLDER + " to be replaced by the generated table");
        log.info("  <tableHeaderFileName>   the header of a category table - used once per category; contains two placeholders:");
        log.info("              " + GenerateJerseyModuleListMojo.CATEGORY_CAPTION_PLACEHOLDER + " - used for the caption of "
                                                                                                    + "each category table");
        log.info("              " + GenerateJerseyModuleListMojo.CATEGORY_GROUP_ID_PLACEHOLDER + " - used to create a unique "
                                                                                                    + "id of a table");
        log.info("  <tableFooterFileName>   the footer of a category table, used once per category");
        log.info("  <tableRowFileName>      a template for a table row, used once per module. The template should contain two "
                                            + "placeholders:");
        log.info("              " + GenerateJerseyModuleListMojo.MODULE_NAME_PLACEHOLDER + " - replaced by the name of the "
                                                                                            + "module");
        log.info("              " + GenerateJerseyModuleListMojo.MODULE_DESCRIPTION_PLACEHOLDER + " - replaced by the "
                                                            + "description of the module from the module's pom.xml file");
        log.info("              " + GenerateJerseyModuleListMojo.MODULE_LINK_PATH_PLACEHOLDER + " - replaced by the relative "
                + "path of the project-description page on java.net");
        log.info("  <outputUnmatched>       specifies, if the modules not matching any predefined category should be also a "
                + "part of the output. If set to true, plugin generates a table for category called 'Other' at the end.");
        log.info("");
        log.info("");
        log.info("TARGETS:");
        log.info("  generate - main target, generates the docbook output file");
        log.info("  help     - displays this help message");
    }

    @Override
    public void setLog(org.apache.maven.plugin.logging.Log log) {
        this.log = log;
    }

}
