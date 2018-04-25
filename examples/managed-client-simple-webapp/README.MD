[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Managed Client Simple Example
=============================

Description
-----------

This sample shows usage of injected client managed by jersey container
into a resource methods. The annotation org.glassfish.jersey.server.Uri
is used to inject client for specified path. The jax-rs application
contains standard resources and also resources which use injected client
to query these standard resources. In other words the injected client
invokes requests to the other resources from the same jax-rs
application. Also there is a example of client requesting resource from
outside of the application.

Front page shows the table with resources exposed in deployed
application with description. Simply just click the links to go to these
resources.

Key Features
------------

-   `@Uri`
-   `@Path`, `@PathParam`

Running the Example
-------------------

Run the example as follows:

>     mvn clean package jetty:run

This deploys current example on the local host. You can then access
frontpage at <http://localhost:8080/managed-client-simple-webapp/index.html>.
This page contians description of all resources available in the application.
