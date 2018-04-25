/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.jersey.process.internal.Stages;
import org.glassfish.jersey.server.ContainerResponse;

/**
 * Default implementation of the request-scoped
 * {@link org.glassfish.jersey.server.internal.process.RespondingContext responding context}.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
class DefaultRespondingContext implements RespondingContext {

    private Stage<ContainerResponse> rootStage;

    @Override
    public void push(Function<ContainerResponse, ContainerResponse> responseTransformation) {
        rootStage = (rootStage == null)
                ? new Stages.LinkedStage<>(responseTransformation)
                : new Stages.LinkedStage<>(responseTransformation, rootStage);
    }

    @Override
    public void push(final ChainableStage<ContainerResponse> stage) {
        if (rootStage != null) {
            stage.setDefaultNext(rootStage);
        }

        rootStage = stage;
    }

    @Override
    public Stage<ContainerResponse> createRespondingRoot() {
        return rootStage;
    }
}
