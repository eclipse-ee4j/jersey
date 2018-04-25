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
 * Jersey module categories definition.
 * The categories are based on maven groupId, this enum assigns to each "known" groupId a human-readable caption
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public enum PredefinedCategories {
    CORE("org.glassfish.jersey.core", "Jersey Core"),
    CONTAINERS("org.glassfish.jersey.containers", "Jersey Containers"),
    CONNECTORS("org.glassfish.jersey.connectors", "Jersey Connectors"),
    MEDIA("org.glassfish.jersey.media", "Jersey Media"),
    EXTENSIONS("org.glassfish.jersey.ext", "Jersey Extensions"),
    TEST_FRAMEWORK("org.glassfish.jersey.test-framework", "Jersey Test Framework"),
    TEST_FRAMEWORK_PROVIDERS("org.glassfish.jersey.test-framework.providers", "Jersey Test Framework Providers"),
    GLASSFISH_BUNDLES("org.glassfish.jersey.containers.glassfish", "Jersey Glassfish Bundles"),
    SECURITY("org.glassfish.jersey.security", "Security"),
    EXAMPLES("org.glassfish.jersey.examples", "Jersey Examples");

    private String groupId;
    private String caption;

    private PredefinedCategories(String groupId, String caption) {
        this.groupId = groupId;
        this.caption = caption;
    }

    public String getCaption() {
        return this.caption;
    }

    public String getGroupId() {
        return this.groupId;
    }
}
