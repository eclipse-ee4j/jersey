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

package org.glassfish.jersey.server.model;

import java.util.List;

import javax.ws.rs.core.MediaType;

/**
 * Model component that is able to produce media types.
 *
 * A component implementing this interface provides additional information about
 * the supported produced {@link MediaType media types}.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 *
 * @see javax.ws.rs.Produces
 * @see Consuming
 */
public interface Producing {

    /**
     * Get the produced media types supported by the component.
     *
     * @return immutable collection of supported produced media types.
     */
    public List<MediaType> getProducedTypes();
}
