/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.http;

import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class Expect100ContinueFeature implements Feature {

    private long thresholdSize;

    public Expect100ContinueFeature() {
        this(ClientProperties.DEFAULT_EXPECT_100_CONTINUE_THRESHOLD_SIZE);
    }

    private Expect100ContinueFeature(long thresholdSize) {
        this.thresholdSize = thresholdSize;
    }

    /**
     * Creates Expect100ContinueFeature with custom (not default) threshold size for content length.
     *
     * @param thresholdSize size of threshold
     * @return Expect100Continue Feature
     */
    public static Expect100ContinueFeature withCustomThreshold(long thresholdSize) {
        return new Expect100ContinueFeature(thresholdSize);
    }

    /**
     * Creates Expect100Continue Feature with default threshold size
     *
     * @return Expect100Continue Feature
     */
    public static Expect100ContinueFeature basic() {
        return new Expect100ContinueFeature();
    }

    @Override
    public boolean configure(FeatureContext configurableContext) {
        if (configurableContext.getConfiguration().getProperty(
                ClientProperties.EXPECT_100_CONTINUE) == null) {
            configurableContext.property(ClientProperties.EXPECT_100_CONTINUE, Boolean.TRUE);
        } else {
            return false; //Expect:100-Continue handling is already done via property config
        }
        if (configurableContext.getConfiguration().getProperty(
                ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE) == null) {
            configurableContext.property(ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE, thresholdSize);
        }
        return true;
    }

}