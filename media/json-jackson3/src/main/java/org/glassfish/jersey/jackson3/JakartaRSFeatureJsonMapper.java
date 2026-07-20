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

package org.glassfish.jersey.jackson3;

import tools.jackson.databind.json.JsonMapper;
import org.glassfish.jersey.jackson3.internal.AbstractJsonMapper;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.JakartaRSFeature;


/**
 * The Jackson {@link JsonMapper} supporting {@link JakartaRSFeature}s.
 */
public class JakartaRSFeatureJsonMapper extends AbstractJsonMapper {

    public JakartaRSFeatureJsonMapper() {
        super();
    }

    /**
     * Method for changing state of an on/off {@link JakartaRSFeature}
     * features.
     */
    public JsonMapper configure(JakartaRSFeature f, boolean state) {
        jakartaRSFeatureBag.jakartaRSFeature(f, state);
        return this;
    }

    /**
     * Method for enabling specified {@link JakartaRSFeature}s
     * for parser instances this object mapper creates.
     */
    public JsonMapper enable(JakartaRSFeature... features) {
        if (features != null) {
            for (JakartaRSFeature f : features) {
                jakartaRSFeatureBag.jakartaRSFeature(f, true);
            }
        }
        return this;
    }

    /**
     * Method for disabling specified {@link JakartaRSFeature}s
     * for parser instances this object mapper creates.
     */
    public JsonMapper disable(JakartaRSFeature... features) {
        if (features != null) {
            for (JakartaRSFeature f : features) {
                jakartaRSFeatureBag.jakartaRSFeature(f, false);
            }
        }
        return this;
    }
}
