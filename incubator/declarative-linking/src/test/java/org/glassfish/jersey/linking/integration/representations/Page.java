/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.linking.integration.representations;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.InjectLinks;

public class Page {

    /**
     * The number of the current page. Is always non-negative and less that {@code Page#getTotalPages()}.
     */
    private int number;

    /**
     * The size of the page.
     */
    private int size;

    /**
     * The number of total pages.
     */
    private int totalPages;

    /**
     * The number of elements currently on this page.
     */
    private int numberOfElements;

    /**
     * The total amount of elements.
     */
    private long totalElements;

    /**
     * If there is a previous page.
     */
    private boolean isPreviousPageAvailable;

    /**
     * Whether the current page is the first one.
     */
    private boolean isFirstPage;

    /**
     * If there is a next page.
     */
    private boolean isNextPageAvailable;

    /**
     * Whether the current page is the last one.
     */
    private boolean isLastPage;

    @InjectLinks
    private List<Link> links = new ArrayList<>();

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public boolean isPreviousPageAvailable() {
        return isPreviousPageAvailable;
    }

    public void setPreviousPageAvailable(boolean previousPageAvailable) {
        isPreviousPageAvailable = previousPageAvailable;
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public void setFirstPage(boolean firstPage) {
        isFirstPage = firstPage;
    }

    public boolean isNextPageAvailable() {
        return isNextPageAvailable;
    }

    public void setNextPageAvailable(boolean nextPageAvailable) {
        isNextPageAvailable = nextPageAvailable;
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    public void setLastPage(boolean lastPage) {
        isLastPage = lastPage;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }
}
