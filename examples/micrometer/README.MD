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
**_/micro/timed_**                            | MeasuredTimedResource            | GET
**_/micro/metrics_**                            | MetricsResource            | GET
**_/micro/summary_**                            | SummaryResource            | GET

Sample Response
---------------

```html
--- (micro/timed)
Requests to this method are measured. Use /init to see more
---- (micro/metrics)
Static meters are initialized, try summary. If you want more measurements just refresh this page several times.
---- (micro/summary)
Listing available meters
Many occurrences of the same name means that there are more meters which could be used with different tags, but this is actually a challenge to handle all available metrics :
http.timers;
http.shared.metrics;
Counts to the init page: 2, time spent on requests to the init page (millis): 2.759025
Requests to 'measure/timed' counts: 2, total time (millis): 40.110161
```


Running the Example
-------------------

Run the example using [Grizzly](https://javaee.github.io/grizzly/) container as follows:

>     mvn clean compile exec:java

- <http://localhost:8080/micro/metrics>
- after few request to the main page go to the url
- <http://localhost:8080/micro/timed>
- and see the responses from available resource pages 
- then go to the 
- <http://localhost:8080/micro/summary>
- and see statistics for the micro/meter page