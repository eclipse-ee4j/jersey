/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.innate.spi;

import jakarta.ws.rs.core.EntityPart;

/**
 * Jersey extension of provider of EntityPart.Builder.
 * A service meant to be implemented solely by Jersey.
 *
 * @since 3.1.0
 */
public interface EntityPartBuilderProvider {

    /**
     * @param partName name of the part to create within the multipart entity.
     * @return {@link EntityPart.Builder} for building new {@link EntityPart} instances.
     */
    public EntityPart.Builder withName(String partName);
}
