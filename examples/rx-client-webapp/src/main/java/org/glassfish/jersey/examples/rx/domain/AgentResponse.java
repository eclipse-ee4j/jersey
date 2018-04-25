/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.rx.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Gajdos
 */
public class AgentResponse {

    private List<Destination> visited = new ArrayList<>();
    private List<Recommendation> recommended;
    private long processingTime;

    public AgentResponse() {
    }

    public List<Destination> getVisited() {
        return visited;
    }

    public void setVisited(final List<Destination> visited) {
        this.visited = visited;
    }

    public void setRecommended(final List<Recommendation> recommended) {
        this.recommended = recommended;
    }

    public List<Recommendation> getRecommended() {
        return recommended;
    }

    public void setProcessingTime(final long processingTime) {
        this.processingTime = processingTime;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public AgentResponse visited(final List<Destination> visited) {
        setVisited(visited);
        return this;
    }

    public AgentResponse recommended(final List<Recommendation> recommended) {
        setRecommended(recommended);
        return this;
    }
}
