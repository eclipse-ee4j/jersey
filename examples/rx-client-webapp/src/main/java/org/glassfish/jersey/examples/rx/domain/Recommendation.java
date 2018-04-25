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

/**
 * @author Michal Gajdos
 */
public class Recommendation {

    private String destination;
    private String forecast;
    private int price;

    public Recommendation() {
    }

    public Recommendation(final String destination) {
        this.destination = destination;
    }

    public Recommendation(final String destination, final String forecast, final int price) {
        this.destination = destination;
        this.forecast = forecast;
        this.price = price;
    }

    public Recommendation(final Destination destination) {
        this.destination = destination.getDestination();
    }

    public Recommendation(final Destination destination, final Forecast forecast, final Calculation calculation) {
        this.destination = destination.getDestination();
        this.forecast = forecast.getForecast();
        this.price = calculation.getPrice();
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(final String destination) {
        this.destination = destination;
    }

    public String getForecast() {
        return forecast;
    }

    public void setForecast(final String forecast) {
        this.forecast = forecast;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(final int price) {
        this.price = price;
    }

    public Recommendation forecast(final Forecast forecast) {
        setForecast(forecast.getForecast());
        return this;
    }

    public Recommendation calculation(final Calculation calculation) {
        setPrice(calculation.getPrice());
        return this;
    }
}
