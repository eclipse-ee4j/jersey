/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.aggregator;

/**
 * Fake message aggregator used for testing purposes pointing to SSE event stream implemented using Jersey-specific API.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class TestAggregatorJersey extends AbstractTestAggregator {

    public TestAggregatorJersey(String rgbColor) {
        super(rgbColor);
    }

    @Override
    public String getPath() {
        return "message/stream/jersey";
    }

    @Override
    protected String getPrefix() {
        return "Jersey aggregator: ";
    }

}
