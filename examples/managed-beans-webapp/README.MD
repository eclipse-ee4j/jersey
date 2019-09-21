[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Managed Beans Example
=====================

This example demonstrates managed bean support in Jersey. JAX-RS root
resource classes are annotated with @ManagedBean, which means EE-related
resources may be injected into instances of those classes.

Content
-------

The example application includes two root resource classes that are
treated as Java EE managed beans. One root resource class is managed in
the default JAX-RS life-cycle (one instance per request) and the other
is managed in the singleton life-cycle (one instance per web
application).

Two Java EE artifacts are injected into the singleton root resource. The
first is a resource constant defined in the web.xml. The second is an
entity manager factory to allow integration with JPA layer.

Running the Example
-------------------

This sample utilizes Java EE features in GlassFish application server.

The easiest way to get the application running is to build it and deploy
as follows:

    mvn clean package
    $AS_HOME/asadmin deploy target/managed-beans-webapp.war

From a web browser, visit:

-   <http://localhost:8080/managed-beans-webapp>
