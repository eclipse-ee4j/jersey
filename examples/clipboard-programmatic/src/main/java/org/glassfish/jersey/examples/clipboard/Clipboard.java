/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.clipboard;

/**
 * Simple clipboard implementation.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class Clipboard {
    private final StringBuffer content = new StringBuffer();

    public String content() { // GET
        return content.toString();
    }

    public void setContent(String replacement) { // PUT
        content.delete(0, content.length()).append(replacement);
    }

    public String append(String append) { // POST
        return content.append(append).toString();
    }

    public void clear() { // DELETE
        content.delete(0, content.length());
    }

}
