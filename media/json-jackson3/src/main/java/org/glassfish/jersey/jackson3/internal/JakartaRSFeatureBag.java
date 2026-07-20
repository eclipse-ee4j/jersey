/*
 * Copyright (c) 2026 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson3.internal;

import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.base.ProviderBase;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.JakartaRSFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Internal holder class for {@link JakartaRSFeature} settings and their values.
 */
public class JakartaRSFeatureBag<T extends JakartaRSFeatureBag> {
    protected static final String JAKARTA_RS_FEATURE = "jersey.config.jackson.jakarta.rs.feature";

    private static class JakartaRSFeatureState {
        /* package */ final JakartaRSFeature feature;
        /* package */ final boolean state;
        public JakartaRSFeatureState(JakartaRSFeature feature, boolean state) {
            this.feature = feature;
            this.state = state;
        }
    }

    private Optional<List<JakartaRSFeatureState>> jakartaRSFeature = Optional.empty();

    public T jakartaRSFeature(JakartaRSFeature feature, boolean state) {
        if (!jakartaRSFeature.isPresent()) {
            jakartaRSFeature = Optional.of(new ArrayList<>());
        }
        jakartaRSFeature.ifPresent(list -> list.add(new JakartaRSFeatureState(feature, state)));
        return (T) this;
    }

    protected boolean hasJakartaRSFeature() {
        return jakartaRSFeature.isPresent();
    }

    /* package */ void configureJakartaRSFeatures(ProviderBase providerBase) {
        jakartaRSFeature.ifPresent(list -> list.stream().forEach(state -> providerBase.configure(state.feature, state.state)));
    }
}
