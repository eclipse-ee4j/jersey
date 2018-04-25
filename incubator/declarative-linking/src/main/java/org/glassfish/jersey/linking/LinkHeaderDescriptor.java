/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.linking;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.linking.InjectLink.Style;
import org.glassfish.jersey.linking.mapping.ResourceMappingContext;

/**
 * Utility class for working with {@link org.glassfish.jersey.linking.InjectLink} annotations.
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
class LinkHeaderDescriptor implements InjectLinkDescriptor {

    private InjectLink linkHeader;
    private Map<String, String> bindings;

    LinkHeaderDescriptor(InjectLink linkHeader) {
        this.linkHeader = linkHeader;
        bindings = new HashMap<>();
        for (Binding binding : linkHeader.bindings()) {
            bindings.put(binding.name(), binding.value());
        }
    }

    InjectLink getLinkHeader() {
        return linkHeader;
    }

    public String getLinkTemplate(ResourceMappingContext rmc) {
        return InjectLinkFieldDescriptor.getLinkTemplate(rmc, linkHeader);
    }

    public Style getLinkStyle() {
        return linkHeader.style();
    }

    public String getBinding(String name) {
        return bindings.get(name);
    }

    public String getCondition() {
        return linkHeader.condition();
    }

}
