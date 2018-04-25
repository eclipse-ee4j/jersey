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

package org.glassfish.jersey.server.internal.process;

import java.util.function.Function;

import org.glassfish.jersey.process.internal.ChainableStage;
import org.glassfish.jersey.process.internal.Stage;
import org.glassfish.jersey.server.ContainerResponse;

/**
 * Context that can be used during the data processing for registering response
 * processing stages and/or functions that will be invoked during the response processing
 * to transform the response before it is written to the client.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface RespondingContext {

    /**
     * Push response transformation function that should be applied.
     *
     * @param responseTransformation response transformation function.
     */
    void push(Function<ContainerResponse, ContainerResponse> responseTransformation);

    /**
     * Push chainable response transformation stage that should be applied.
     *
     * @param stage response transformation chainable stage.
     */
    void push(ChainableStage<ContainerResponse> stage);

    /**
     * (Optionally) create a responder chain from all transformations
     * previously pushed into the context.
     *
     * @return created responder chain root or {@code null} in case of no
     *         registered transformations.
     */
    Stage<ContainerResponse> createRespondingRoot();
}
