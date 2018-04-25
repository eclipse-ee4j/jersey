/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.clipboard;

import java.util.Objects;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class ClipboardData {

    private String content;

    public ClipboardData(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content;
    }

    boolean isEmpty() {
        return "".equals(content);
    }

    ClipboardData append(ClipboardData addition) {
        content = content + addition.content;
        return this;
    }

    void clear() {
        content = "";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClipboardData)) {
            return false;
        }
        final ClipboardData that = (ClipboardData) o;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
}
