/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.httppatch;

import java.util.ArrayList;
import java.util.List;

/**
 * A resource state modelled as Java bean (that can be patched).
 *
 * @author Gerard Davison (gerard.davison at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class State {

    private String title;
    private String message;
    private List<String> list;

    /**
     * Create new bean instance without any data.
     */
    public State() {
        this.title = "";
        this.message = "";
        this.list = new ArrayList<String>();
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public List<String> getList() {
        return list;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        State bean = (State) o;

        if (list != null ? !list.equals(bean.list) : bean.list != null) {
            return false;
        }
        if (message != null ? !message.equals(bean.message) : bean.message != null) {
            return false;
        }
        if (title != null ? !title.equals(bean.title) : bean.title != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (list != null ? list.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "State{"
                + "title='" + title + '\''
                + ", message='" + message + '\''
                + ", list=" + list
                + '}';
    }
}
