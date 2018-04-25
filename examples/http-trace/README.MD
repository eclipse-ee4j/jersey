[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Jersey HTTP PATCH
=================

### *Jersey HTTP PATCH Support Example*

This example demonstrates how to implement a support for **HTTP TRACE method** via **HttpMethod** annotation in Jersey.

Using HttpMethod annotation you can implement whatever name of an HTTP method you want.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                    | Resource class      | HTTP methods
--------------------------- | ------------------- | --------------
**_/tracing/annotated_**    | TracingResource     | TRACE
**_/tracing/programmatic_** | ---                 | TRACE

Sample Response
---------------

You can trace your annotated resource using this command:

>   curl -v -X TRACE http://localhost:9998/base/tracing/annotated

or programmatic resource:

>   curl -v -X TRACE http://localhost:9998/base/tracing/programmatic

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container.