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

import org.glassfish.jersey.linking.mapping.ResourceMappingContext;

/**
 * Utility for working with @Ref annotations
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
interface InjectLinkDescriptor {

    /**
     * Get the style
     * @return the style
     */
    InjectLink.Style getLinkStyle();

    /**
     * Get the link template, either directly from the value() or from the
     * @Path of the class referenced in resource()
     * @return the link template
     */
    String getLinkTemplate(ResourceMappingContext rmc);

    /**
     * Get the binding as an EL expression for a particular URI template parameter
     * @param name
     * @return the EL binding
     */
    String getBinding(String name);

    /**
     * Get the condition.
     * @return the condition
     */
    String getCondition();
}
