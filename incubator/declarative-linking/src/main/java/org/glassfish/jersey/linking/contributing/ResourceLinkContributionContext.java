/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.linking.contributing;

import java.util.List;

import org.glassfish.jersey.linking.ProvideLinkDescriptor;

/**
 * The ResourceLinkContributionContext provides access for link contributions from other sources to an entity.
 *
 * @author Leonard Brünings
 */
public interface ResourceLinkContributionContext {

    /**
     * Returns all link contributions for an entity class.
     *
     * It also includes contributions for every ancestor of entityClass.
     *
     * @param entityClass the entityClass
     * @return list of link contributions to add to the class
     */
    List<ProvideLinkDescriptor> getContributorsFor(Class<?> entityClass);
}
