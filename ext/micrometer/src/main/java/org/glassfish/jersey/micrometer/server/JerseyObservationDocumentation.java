/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.micrometer.server;

import io.micrometer.common.docs.KeyName;
import io.micrometer.common.lang.NonNullApi;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * An {@link ObservationDocumentation} for Jersey.
 *
 * @author Marcin Grzejszczak
 * @since 2.41
 */
@NonNullApi
public enum JerseyObservationDocumentation implements ObservationDocumentation {

    /**
     * Default observation for Jersey.
     */
    DEFAULT {
        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return DefaultJerseyObservationConvention.class;
        }

        @Override
        public KeyName[] getLowCardinalityKeyNames() {
            return JerseyLegacyLowCardinalityTags.values();
        }
    };

    @NonNullApi
    enum JerseyLegacyLowCardinalityTags implements KeyName {

        OUTCOME {
            @Override
            public String asString() {
                return "outcome";
            }
        },

        METHOD {
            @Override
            public String asString() {
                return "method";
            }
        },

        URI {
            @Override
            public String asString() {
                return "uri";
            }
        },

        EXCEPTION {
            @Override
            public String asString() {
                return "exception";
            }
        },

        STATUS {
            @Override
            public String asString() {
                return "status";
            }
        }

    }

}
