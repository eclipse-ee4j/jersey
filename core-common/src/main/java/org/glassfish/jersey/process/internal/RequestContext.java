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

package org.glassfish.jersey.process.internal;

/**
 * Wrapper for externally provided request context data.
  *
 * @author Petr Bouda
 */
public interface RequestContext {

    /**
     * Get a "new" reference of the scope instance. {@link RequestContext} implementation is able to track referenced instances
     * and then provide any additional logic during the releasing.
     *
     * @return referenced scope instance.
     */
    RequestContext getReference();

    /**
     * Release a single reference to the current request scope instance. Once all instance references are released, the instance
     * will be recycled.
     */
    void release();

}
