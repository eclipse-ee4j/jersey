[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Hello World and Counter Example
===============================

This example demonstrates Hello World and Counter examples with CDI 2 SE implemented by Weld.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                    | Resource class      | HTTP methods | Notes
--------------------------- | ------------------- | ------------ | --------------------------------------------------------
**_/helloworld/{name}_**    | HelloWorldResource  |  GET         |  Returns `Hello {name}`
**_/counter/request_**      | CounterResource     |  GET         |  Returns always `1` (injected always a new instance of counter)
**_/counter/application_**  | CounterResource     |  GET         |  Returns an incremented number (injected always the same counter).

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container.

-   <http://localhost:8080/helloworld>
