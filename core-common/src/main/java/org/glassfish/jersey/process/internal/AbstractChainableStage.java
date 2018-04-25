/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * Abstract chainable linear acceptor.
 *
 * Implements support for managing the default next stage value.
 *
 * @param <DATA> processed data type.
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public abstract class AbstractChainableStage<DATA> implements ChainableStage<DATA> {

    private Stage<DATA> nextStage;

    /**
     * Create a new chainable acceptor with no next stage set.
     */
    protected AbstractChainableStage() {
        this(null);
    }

    /**
     * Create a new chainable acceptor with an initialized default
     * next stage value.
     *
     * @param nextStage default next stage.
     */
    protected AbstractChainableStage(Stage<DATA> nextStage) {
        this.nextStage = nextStage;
    }

    @Override
    public final void setDefaultNext(Stage<DATA> next) {
        this.nextStage = next;
    }

    /**
     * Get the default next stage currently configured on the acceptor.
     *
     * @return default next stage currently configured on the acceptor.
     */
    public final Stage<DATA> getDefaultNext() {
        return nextStage;
    }
}
