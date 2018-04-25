/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

"use strict";

function addItem() {
    var itemInput = document.getElementById("name");

    var req = new XMLHttpRequest();
    req.open("POST", "resources/items", true);
    req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    req.onreadystatechange = function () {
        if (req.readyState == 4 && req.status == 204) {
            //Call a function when the state changes.
            itemInput.value = "";
            getItems();
        }
    };
    req.send("name=" + itemInput.value);
}

function getItems() {
    var req = new XMLHttpRequest();
    req.open("GET", "resources/items", true);
    req.setRequestHeader("Accept", "text/plain");
    req.onreadystatechange = function () {
        //Call a function when the state changes.
        if (req.readyState == 4 && req.status == 200) {
            document.getElementById("items").innerHTML = req.responseText;
        }
    };
    req.send();
}

function display(data, rgb) {
    var msgSpan = document.createElement("span");
    msgSpan.style.color = rgb;
    msgSpan.innerHTML = data;
    var msgDiv = document.createElement("div");
    msgDiv.className = "message";
    msgDiv.appendChild(msgSpan);

    var messages = document.getElementById("messages");
    messages.insertBefore(msgDiv, messages.firstChild);
}

function receiveMessages() {
    if (typeof(EventSource) !== "undefined") {
        // Yes! Server-sent events support!
        var source = new EventSource("resources/items/events");
        source.onmessage = function (event) {
            console.log('Received unnamed event: ' + event.data);
            display("Added new item: " + event.data, "#444444");
        };

        source.addEventListener("size", function(e) {
            console.log('Received event ' + event.name + ': ' + event.data);
            display("New items size: " + event.data, "#0000FF");
        }, false);

        source.onopen = function (event) {
            console.log("event source opened");
        };

        source.onerror = function (event) {
            console.log('Received error event: ' + event.data);
            display(event.data, "#FF0000");
        };
    } else {
        // Sorry! No server-sent events support..
        display('SSE not supported by browser.', "#FF0000");
    }
}

window.onload = receiveMessages;
