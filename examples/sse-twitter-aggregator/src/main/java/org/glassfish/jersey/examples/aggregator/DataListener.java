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
 * Incoming data listener.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface DataListener {
    /**
     * Invoked when the connection to the data stream has been established.
     */
    public void onStart();

    /**
     * Invoked when the data stream has dried out (or the connection has been closed).
     */
    public void onComplete();

    /**
     * Invoked when there was an error while receiving streamed data.
     */
    public void onError();

    /**
     * Invoked when a new message data are available.
     *
     * @param message new message data.
     */
    public void onMessage(Message message);
}
