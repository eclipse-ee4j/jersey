[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Jersey Weld Integration Jersey Weld on Grizzly Example
======================================================

This example demonstrates how to develop RESTful web service with CDI
managed beans and Grizzly HTTP container.

We suggest you inspect the example source code to get better
understanding on various CDI features utilized there. Following table
contains some of the published resources that you might want to check.

Contents
--------

The mapping of the URI path space is presented in the following table:

URL                                            | Description                                                          | Expected Results
---------------------------------------------- | -------------------------------------------------------------------- | ------------------------------------------------------
**_/helloworld?name=Bill_**                    | A hello-world managed bean                                           | Hello Bill
**_/app/count_**                               | Application scoped resource                                          | request count (incremented with every other request)
**_/req/app/counter_**                         | Request scoped resource                                              | links to above request counter
**_/req/parameterized-async?q=echo%20this_**   | Asynchronous echo resource method that is being JAX-RS intercepted   | echo this

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container. You can access the application at:

- <http://localhost:8080/weld>