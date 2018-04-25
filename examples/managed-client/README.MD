[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Managed Client Example
======================

This example demonstrates a simple usage of Jersey Managed Client
feature.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path            | Resource class     | HTTP methods
------------------- | ------------------ | --------------
**_/public/a_**     | PublicResource     | GET
**_/public/b_**     | PublicResource     | GET
**_/internal/a_**   | InternalResource   | GET
**_/internal/b_**   | InternalResource   | GET

In the example, the requests to a *public resource* deployed on
`/public/` path are forwarded to an *internal resource* (deployed on
`/internal/` path) using injected
[javax.ws.rs.client.WebTarget](https://jax-rs-spec.java.net/nonav/2.0/apidocs/javax/ws/rs/client/WebTarget.html)
instances produced using 2 separate managed clients each of the managed
clients using it's own custom configuration.

An access to internal resource methods is guarded by a container request
filter (`CustomHeaderFilter`) which rejects any request that does not
contain expected custom header set to an expected value. In the example,
the 2 managed clients (used by a public resource to access the methods
on the internal resource) are configured with custom configurations,
each containing a registration of a custom client request filter that is
instructed to append a required custom header and value to every
outgoing request. Only with managed client support working properly, the
public resource is able to successfully retrieve data from the internal
resource.

Running the Example
-------------------

Run the example as follows:

>     mvn clean package exec:java

This deploys current example on the local host. You can then access WADL
description of the deployed application at
<http://localhost:8080/managed-client-webapp/application.wadl>.

You can access public resource of this application using curl:

>     curl -v -H "Accept: text/plain" http://localhost:8080/managed-client-webapp/public/a

>     curl -v -H "Accept: text/plain" http://localhost:8080/managed-client-webapp/public/b

In this example you should see the returned response message body
contains "a" or "b" respectively upon successful invocation.

You may also verify that access to internal resource is not possible
without including a proper header in the request. First try to access
the internal resource without any custom header:

>     curl -v -H "Accept: text/plain" http://localhost:8080/managed-client-webapp/internal/a

>     curl -v -H "Accept: text/plain" http://localhost:8080/managed-client-webapp/internal/b

In both cases a `HTTP 403 Forbidden.` response is returned. Now lets try
to access the resource once again, but this time we'll include also the
expected custom headers:

>     curl -v -H "Accept: text/plain" -H "custom-header:a" http://localhost:8080/managed-client-webapp/internal/a

>     curl -v -H "Accept: text/plain" -H "custom-header:b" http://localhost:8080/managed-client-webapp/internal/b

Finally, you should see the invocation succeeded and the returned response message body contains "a" or "b" respectively.
