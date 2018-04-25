[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Resource Configuration Reload Example
=====================================

This example demonstrates how to create a simple Jersey application with
resource reloading capability using
[Grizzly HTTP Server container](http://grizzly.java.net).

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                    | Resource class                                            | Content                                          | HTTP methods
--------------------------- | --------------------------------------------------------- | ------------------------------------------------ | --------------
**_/flights/arrivals_**     | org.glassfish.jersey.examples.reload.ArrivalsResource     | Dummy Arrivals Information                       | GET
**_/flights/departures_**   | org.glassfish.jersey.examples.reload.DeparturesResource   | Dummy Departures Information                     | GET
**_/flights/stats_**        | org.glassfish.jersey.examples.reload.StatsResource        | Statistics on application resource utilization   | GET

The application gets configured via a plain text file named `resources`.
The file contains a list of resource classes to be published by the
application. After the application gets started, it watches the file system
and reloads the application as file updates are being detected.
Changes in JAX-RS resource source files and updates of the above mentioned resources file
are being detected. Java code re-compilation is done as required.

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys current example using[Grizzly
container](http://grizzly.java.net/). StatsResource should be available
at <http://localhost:8080/flights/stats>

You may want to check the other resources as well:

-   <http://localhost:8080/flights/departures>
-   <http://localhost:8080/flights/arrivals>

Updated information on number of hits should be available at
<http://localhost:8080/flights/stats>.
Now you can try to edit the `resources` file and comment the stats
resource out by using the `#` prefix. The point is you leave the
application running while editing the file. The changes wil get picked
up automatically. No need to manually restart the application. The
`resource` file content should now look like follows:

    org.glassfish.jersey.examples.reload.DeparturesResource
    org.glassfish.jersey.examples.reload.ArrivalsResource
    #org.glassfish.jersey.examples.reload.StatsResource

Once you save the change, the stats resource should not be available,
but the other, departures/arrivals, resources should remain functional.

Now you can revert the change back and check the stats resource is back
providing actual statistics information.

You can also try to change the JAX-RS resources source code. These changes
will be also picked up automatically.
