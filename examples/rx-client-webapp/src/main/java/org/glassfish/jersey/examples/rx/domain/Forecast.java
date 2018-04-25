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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Michal Gajdos
 */
@XmlRootElement
public class Forecast {

    private String forecast;
    private String destination;

    public Forecast() {
    }

    public Forecast(final String destination, final String forecast) {
        this.destination = destination;
        this.forecast = forecast;
    }

    public String getForecast() {
        return forecast;
    }

    public void setForecast(final String forecast) {
        this.forecast = forecast;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(final String destination) {
        this.destination = destination;
    }
}
