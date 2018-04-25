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

/**
 * Container class for plugin configuration.
 * Holds the content of the loaded template files.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
class Configuration {
    private String sectionTemplate;
    private String tableHeader;
    private String tableRow;
    private String tableFooter;

    public String getTableFooter() {
        return tableFooter;
    }

    public void setTableFooter(String tableFooter) {
        this.tableFooter = tableFooter;
    }

    public String getTableRow() {
        return tableRow;
    }

    public void setTableRow(String tableRow) {
        this.tableRow = tableRow;
    }

    public String getTableHeader() {
        return tableHeader;
    }

    public void setTableHeader(String tableHeader) {
        this.tableHeader = tableHeader;
    }

    public String getSectionTemplate() {
        return sectionTemplate;
    }

    public void setSectionTemplate(String sectionTemplate) {
        this.sectionTemplate = sectionTemplate;
    }

}
