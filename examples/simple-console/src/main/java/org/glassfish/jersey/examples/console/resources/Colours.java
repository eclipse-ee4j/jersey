/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.console.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.codehaus.jettison.json.JSONArray;

/**
 * A web resource for a list of colours.
 */
public class Colours {

    private static String colours[] = {"red", "orange", "yellow", "green", "blue", "indigo", "violet"};

    /**
     * Returns a list of colours as plain text, one colour per line.
     * @param filter If not empty, constrains the list of colours to only
     * those that contain this substring
     * @return the list of colours matching the filter
     */
    @GET
    @Produces("text/plain")
    public String getColourListAsText(@QueryParam("match") String filter) {
        StringBuilder buf = new StringBuilder();
        for (String colour : getMatchingColours(filter)) {
            buf.append(colour);
            buf.append('\n');
        }
        return buf.toString();
    }

    /**
     * Returns a list of colours as a JSON array.
     * @param filter If not empty, constrains the list of colours to only
     * those that contain this substring
     * @return the list of colours matching the filter
     */
    @GET
    @Produces("application/json")
    public JSONArray getColourListAsJSON(@QueryParam("match") String filter) {
        return new JSONArray(getMatchingColours(filter));
    }

    /**
     * Returns a list of colours.
     * @param filter If not empty, constrains the list of colours to only
     * those that contain this substring
     * @return the list of colours matching the filter
     */
    public static List<String> getMatchingColours(String filter) {
        List<String> matches = new ArrayList<>();

        for (String colour : colours) {
            if (filter == null || filter.length() == 0 || colour.contains(filter)) {
                matches.add(colour);
            }
        }

        return matches;
    }
}
