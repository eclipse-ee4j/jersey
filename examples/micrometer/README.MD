[//]: # " Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

jersey-micrometer-webapp
==========================================================

This example demonstrates basics of Micrometer Jersey integration

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                                   | Resource class            | HTTP methods
------------------------------------------ | ------------------------- | --------------
**_/micro/measure/timed_**                            | JerseyResource            | GET
**_/micro/measure/counted_**                            | JerseyResource            | GET
**_/micro/metrics_**                            | JerseyResource            | GET

Sample Response
---------------

```html
--- (micro/measure/timed)
Requests to this method are measured. Use /metrics to see more
--- (micro/measure/counted)
Requests to this method are counted. Use /metrics to see more
---- (micro/metrics)   
Listing available meters: http.shared.metrics;
----)
Overall requests counts: 9, total time (millis): 35.799483
```


Running the Example
-------------------

Run the example using [Grizzly](https://javaee.github.io/grizzly/) container as follows:

>     mvn clean compile exec:java

- <http://localhost:8080/micro/metrics>
- after few request to the main page go to the url
- - <http://localhost:8080/micro/measure/timed>
- - <http://localhost:8080/micro/measure/counted>
- and see the responses from available resource pages 
- then go to the 
- - <http://localhost:8080/micro/metrics>
- and see statistics for the micro/meter page