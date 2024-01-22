/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld.internal.inject;

import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.InstanceBinding;
import org.glassfish.jersey.internal.inject.ServiceHolder;
import org.glassfish.jersey.internal.inject.ServiceHolderImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InstanceListBinding<T> extends MatchableBinding<List<T>, InitializableInstanceBinding<List<T>>>
        implements Cloneable {

    private final Class<T> serviceType;
    private final List<InstanceBinding<T>> services = new ArrayList<>();

    /**
     * Creates a service as an instance.
     *
     * @param serviceType service's type.
     */
    public InstanceListBinding(Class<T> serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    protected MatchLevel bestMatchLevel() {
        return MatchLevel.NEVER;
    }

    @Override
    public Matching<MatchableBinding> matching(Binding other) {
        return Matching.noneMatching();
    }

    public void init(InstanceBinding<?> service) {
        services.add((InstanceBinding<T>) service);
    }

    public List<T> getServices() {
        return services.stream().map(binding -> binding.getService()).collect(Collectors.toList());
    }

    public List<ServiceHolder<T>> getServiceHolders() {
        return services.stream().map(binding ->
                new ServiceHolderImpl<T>(binding.getService(), serviceType, binding.getContracts(),
                        binding.getRank() == null ? 0 : binding.getRank()))
                .collect(Collectors.toList());
    }
}
