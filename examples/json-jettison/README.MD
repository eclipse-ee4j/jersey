[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Jettison JAX-RS JSON Provider Example
=====================================

This example demonstrates how to use JSON representation of JAXB based
resources with Jettison JSON provider.

A simple web application is presented with two resources: a flight list
and a aircraft type list. A client can obtain the former list or update
it using XML or JSON representation. The second list is read-only and
shows how to provide JSON array using List&lt;JAXBBean&gt; objects.

Contents
--------

The flight list web resource is implemented by
`org.glassfish.jersey.examples.jettison.FlightList` class.

The aircraft type list web resource is implemented by
`org.glassfish.jersey.examples.jettison.AircraftTypeList` class.

The mapping of the URI path space is presented in the following table:

URI path           | Resource class     | HTTP method
------------------ | ------------------ | -------------
**_/flights_**     | FlightList         | GET, PUT
**_/aircrafts_**   | AircraftTypeList   | GET

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the JSON from JAXB example using [Grizzly](http://grizzly.java.net/)

A [WADL description](http://wadl.java.net/#spec) may be accessed at the URL:

-   <http://localhost:8080/jettison/application.wadl>

Following steps are using [cURL](http://curl.haxx.se/) command line tool:

Get the list of flights in JSON format:

>     curl -HAccept:application/json http://localhost:8080/jettison/flights

Get the list of flights in XML format:

>     curl -HAccept:application/xml http://localhost:8080/jettison/flights

Get the list of aircraft types in JSON format:

>     curl -HAccept:application/json http://localhost:8080/jettison/aircrafts

You can also see test classes at `src/main/test` subdirectory for detail
information how to consume the service using JAX-RS client API
