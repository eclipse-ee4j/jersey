/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

"use strict";

function receiveMessages() {
    if (typeof(EventSource) !== "undefined") {
        // Yes! Server-sent events support!
        var sourceJersey = new EventSource('/aggregator-api/message/stream/jersey');
        var sourceJaxRs = new EventSource('/aggregator-api/message/stream/jaxrs');
        var eventHandler =function (event) {
            var data = JSON.parse(event.data);
            console.log(data);

            var newEntry =
                '<div class="message">'
                    + '<img src="' + data.user.profile_image_url + '" />'
                    + '<span style="color: #' + data.rgbColor + '">'
                    + data.text + '</span></div>';

            document.body.innerHTML = newEntry + document.body.innerHTML;
        };
        sourceJersey.onmessage = eventHandler;
        sourceJaxRs.onmessage = eventHandler;

        sourceJersey.onopen = function (event) {
            // Connection was opened.
            console.log('Jresey stream opened.')
        };

        sourceJaxRs.onopen = function (event) {
            console.log('JAX-RS stream opened.')
        }

        sourceJersey.onclose = function (event) {
            // Connection was closed.
            console.log('Jersey connection closed')
        };

        sourceJaxRs.onclose = function (event) {
            // Connection was closed.
            console.log('JAX-RS connection closed')
        };
    } else {
        // Sorry! No server-sent events support..
        console.log('SSE not supported by browser.')
    }
}

window.onload = receiveMessages ;
