/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;

import org.glassfish.jersey.spi.Contract;

/**
 * Contract for a model processors that processes {@link ResourceModel resource models} during application initialization
 * and {@link Resource resource} returned by sub resource locators. Even though {@link ModelProcessor model processors} can
 * completely change the resource model, the standard use case it to enhance the current resource model by
 * additional methods and resources (like for example adding OPTIONS http methods for every URI endpoint).
 * <p/>
 * More model processors can be registered. These providers will be execute in the chain so that each model
 * processor will be executed with resource model processed by the previous model processor. The first model
 * processor in the chain will be invoked with the initial resource model from which the application was initiated.
 * <p/>
 * Model processors implementations can define {@link javax.annotation.Priority binding priority}
 * to define the order in which they are executed (processors with a lower priority is invoked
 * before processor with a higher priority). The highest possible priority (Integer.MAX_VALUE) is used for
 * model processor which enhance resource models by the default OPTIONS method defined by JAX-RS specification and therefore
 * this priority should not be used.
 * <p/>
 * Note that if model processor adds a resources that are intended to be supportive resources like
 * {@code OPTIONS} method providing information about the resource, it should properly define the
 * {@link org.glassfish.jersey.server.model.ResourceMethod#isExtended() extended} flag of such a new method.
 * See {@link org.glassfish.jersey.server.model.ExtendedResource} for more information.
 *
 * @author Miroslav Fuksa
 *
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface ModelProcessor {
    /**
     * Process {@code resourceModel} and return the processed model. Returning input {@code resourceModel} will cause
     * no effect on the final resource model.
     *
     * @param resourceModel Input resource model to be processed.
     * @param configuration Runtime configuration.
     * @return Processed resource model containing root resources. Non root resources will be ignored.
     */
    public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration);

    /**
     * Process {@code subResourceModel} which was returned a sub resource locator.
     * <p/>
     * The {@code subResourceModel} contains only one {@link Resource resource} representing model that should be processed
     * by further matching. The method must return also exactly one resource in the model. Returning input
     * {@code subResourceModel} instance will cause no effect on the final sub resource model.
     *
     * @param subResourceModel {@link Resource Sub resource} which is based on sub resource returned from sub resource locator.
     * @param configuration Runtime configuration.
     * @return Processed resource model with one {@link Resource resource} which should be used for handling sub resource.
     */
    public ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration);
}
