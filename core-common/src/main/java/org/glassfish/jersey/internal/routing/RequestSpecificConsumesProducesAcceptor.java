/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.routing;

/**
 * A {@link Comparable} concrete request content-type, accept header, and a methodRouting triplet
 *
 * @see CombinedMediaType
 */
@SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
public final class RequestSpecificConsumesProducesAcceptor<MethodRouting> implements Comparable {
    private final CombinedMediaType consumes;
    private final CombinedMediaType produces;
    private final MethodRouting methodRouting;

    private final boolean producesFromProviders;

    public RequestSpecificConsumesProducesAcceptor(final CombinedMediaType consumes,
                                            final CombinedMediaType produces,
                                            final boolean producesFromProviders,
                                            final MethodRouting methodRouting) {

        this.methodRouting = methodRouting;
        this.consumes = consumes;
        this.produces = produces;
        this.producesFromProviders = producesFromProviders;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        }
        if (!(o instanceof RequestSpecificConsumesProducesAcceptor)) {
            return -1;
        }
        RequestSpecificConsumesProducesAcceptor other = (RequestSpecificConsumesProducesAcceptor) o;
        final int consumedComparison = CombinedMediaType.COMPARATOR.compare(consumes, other.consumes);
        return (consumedComparison != 0)
                ? consumedComparison : CombinedMediaType.COMPARATOR.compare(produces, other.produces);
    }

    /**
     * Get request content type
     * @return request content type
     */
    public CombinedMediaType getConsumes() {
        return consumes;
    }

    /**
     * Get specified method routing
     * @return method routing
     */
    public MethodRouting getMethodRouting() {
        return methodRouting;
    }

    /**
     * Get Accept header media type
     * @return request accept header
     */
    public CombinedMediaType getProduces() {
        return produces;
    }

    /**
     * Information whether {@link #getProduces()} was set by providers}. If not, resource method was annotated by
     * {@code @Produces}
     * @return {@code true} if the produces media type was set by providers and not by {@code @Produces} annotation.
     */
    public boolean producesFromProviders() {
        return producesFromProviders;
    }

    @Override
    public String toString() {
        return String.format("%s->%s:%s", consumes, produces, methodRouting);
    }

}