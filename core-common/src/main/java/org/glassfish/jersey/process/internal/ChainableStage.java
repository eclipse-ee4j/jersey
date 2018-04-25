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

import java.util.function.Function;

/**
 * Linear acceptor that can be composed into a chain.
 *
 * The acceptor exposes a method for setting a value of the
 * {@link #setDefaultNext(Stage) next acceptor} in the chain that
 * should be returned from the chain by default.
 * <p>
 * The typical use case for implementing the acceptor is a logic that usually
 * needs to perform some logic, but unlike an {@link Stage.Builder#to(Function)}
 * acceptor created from a function} it also needs to be able to decide to override
 * the default next acceptor and return a different acceptor, effectively branching
 * away from the original linear acceptor chain. This technique can be e.g. used
 * to break the accepting chain by returning a custom {@link Inflecting inflecting}
 * acceptor, etc.
 * </p>
 *
 * @param <DATA> processed data type.
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface ChainableStage<DATA> extends Stage<DATA> {

    /**
     * Set the default next stage that should be returned from this
     * stage after it has been invoked by default.
     *
     * @param next the next default stage in the chain.
     */
    public void setDefaultNext(Stage<DATA> next);
}
