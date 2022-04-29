[//]: # " Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. "
[//]: # "  "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Public License v. 2.0, which is available at "
[//]: # " http://www.eclipse.org/legal/epl-2.0. "
[//]: # "  "
[//]: # " This Source Code may also be made available under the following Secondary "
[//]: # " Licenses when the conditions for such availability set forth in the "
[//]: # " Eclipse Public License v. 2.0 are satisfied: GNU General Public License, "
[//]: # " version 2 with the GNU Classpath Exception, which is available at "
[//]: # " https://www.gnu.org/software/classpath/license.html. "
[//]: # "  "
[//]: # " SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 "


<h3>Jakarta RESTful WebServices 3.0.0 / Jersey {{ site.latest3xVersion }}</h3>                             

Jersey&nbsp;{{ site.latest3xVersion }}, that implements [Jakarta RESTful WebServices 3.0][jaxrs-3.0] API is the future release of Jersey. 
Note that Jersey 2.x releases will continue along with Jersey 3.x releases.             

For the convenience of non-maven developers the following links are provided:

*   [<var class="icon-cloud-download"></var> Jersey 3.x bundle][zip-3.x] bundle contains
    the Jakarta RESTful WebServices 3.0.0 API jar, all the core Jersey module jars as well as all the required 3rd-party
    dependencies.

All the Jersey 3 release binaries, including the source & apidocs jars, are available for
download under the Jersey 3 maven root group identifier `org.glassfish.jersey` from the 
[maven central repository][mvn-central] as well as from the [Sonatype maven repository][mvn-oss].

Chances are you are using Apache Maven as a build & dependency management tool for your project.
If you do, there is a very easy and convenient way to start playing with Jersey {{ site.latest3xVersion }} by generating
the skeleton application from one of the Jersey 3 maven archetypes that we provide.

For instance, to create a Jersey {{ site.latest3xVersion }} application using the Grizzly 3 HTTP server container, use

```bash
mvn archetype:generate -DarchetypeGroupId=org.glassfish.jersey.archetypes \
    -DarchetypeArtifactId=jersey-quickstart-grizzly2 -DarchetypeVersion={{ site.latest3xVersion }}
```

If you want to create a Servlet container deployable Jersey {{ site.latest3xVersion }} web application instead, use

```bash
mvn archetype:generate -DarchetypeGroupId=org.glassfish.jersey.archetypes \
    -DarchetypeArtifactId=jersey-quickstart-webapp -DarchetypeVersion={{ site.latest3xVersion }}
```

For the full list of updates for Jersey {{ site.latest3xVersion }}, details about all changes, bug fixed and updates, 
please check the [Jersey {{ site.latest3xVersion }} Release Notes][rn-3.x].

<h3>JAX-RS 2.1 / Jersey 2.26+</h3>

Jersey&nbsp;{{ site.latestVersion }}, that implements [JAX-RS 2.1 API][jaxrs-2.1] API is the most recent release of Jersey.
To see the details about all changes, bug fixed and updates, please check the [Jersey {{ site.latestVersion }} Release Notes][rn-2.x].

For the convenience of non-maven developers the following links are provided:

*   [<var class="icon-cloud-download"></var> Jersey JAX-RS 2.1 RI bundle][zip-2.x] bundle contains
    the JAX-RS 2.1 API jar, all the core Jersey module jars as well as all the required 3rd-party
    dependencies.
*   [<var class="icon-cloud-download"></var> Jersey {{ site.latestVersion }} Examples bundle][examples-2.x] provides
    convenient access to the Jersey 2 examples for off-line browsing.

All the Jersey 2 release binaries, including the source & apidocs jars, are available for
download under the Jersey 2 maven root group identifier `org.glassfish.jersey` from the 
[maven central repository][mvn-central] as well as from the [Sonatype maven repository][mvn-oss].

Chances are you are using Apache Maven as a build & dependency management tool for your project.
If you do, there is a very easy and convenient way to start playing with Jersey {{ site.latestVersion }} by generating
the skeleton application from one of the Jersey 2 maven archetypes that we provide.
For instance, to create a Jersey {{ site.latestVersion }} application using the Grizzly 2 HTTP server container, use

```bash
mvn archetype:generate -DarchetypeGroupId=org.glassfish.jersey.archetypes \
    -DarchetypeArtifactId=jersey-quickstart-grizzly2 -DarchetypeVersion={{ site.latestVersion }}
```

If you want to create a Servlet container deployable Jersey {{ site.latestVersion }} web application instead, use

```bash
mvn archetype:generate -DarchetypeGroupId=org.glassfish.jersey.archetypes \
    -DarchetypeArtifactId=jersey-quickstart-webapp -DarchetypeVersion={{ site.latestVersion }}
```

Maven users may also be interested in the list of all [Jersey 2 modules and dependencies][deps-2.x]

<h3>JAX-RS 2.0 / Jersey 2.25.x</h3>

Jersey&nbsp;2.25.1 is the most recent release of Jersey which implements [JAX-RS 2.0 API][jaxrs-2.0] API.
To see the details about all changes, bug fixed and updates, please check the [Jersey 2.25.1 Release Notes][rn-2.25.x].

For the convenience of non-maven developers the following links are provided:

*   [<var class="icon-cloud-download"></var> Jersey JAX-RS 2.0 RI bundle][zip-2.25.x] bundle contains
    the JAX-RS 2.0 API jar, all the core Jersey module jars as well as all the required 3rd-party
    dependencies.
*   [<var class="icon-cloud-download"></var> Jersey 2.25.1 Examples bundle][examples-2.25.x] provides
    convenient access to the Jersey 2 examples for off-line browsing.

All the Jersey 2 release binaries, including the source & apidocs jars, are available for
download under the Jersey 2 maven root group identifier `org.glassfish.jersey` from the 
[central maven repository][mvn-central] as well as from the [sonatype maven repository][mvn-jvn].

Chances are you are using Apache Maven as a build & dependency management tool for your project.
If you do, there is a very easy and convenient way to start playing with Jersey {{ site.latestVersion }} by generating
the skeleton application from one of the Jersey 2 maven archetypes that we provide.
For instance, to create a Jersey {{ site.latestVersion }} application using the Grizzly 2 HTTP server container, use

```bash
mvn archetype:generate -DarchetypeGroupId=org.glassfish.jersey.archetypes \
    -DarchetypeArtifactId=jersey-quickstart-grizzly2 -DarchetypeVersion={{ site.latestVersion }}
```

If you want to create a Servlet container deployable Jersey {{ site.latestVersion }} web application instead, use

```bash
mvn archetype:generate -DarchetypeGroupId=org.glassfish.jersey.archetypes \
    -DarchetypeArtifactId=jersey-quickstart-webapp -DarchetypeVersion={{ site.latestVersion }}
```

Maven users may also be interested in the list of all [Jersey 2 modules and dependencies][deps-2.25.x]

<h3>JAX-RS 1.1 / Jersey 1.x</h3>

Jersey 1.19.1 is the latest released version of Jersey 1.x. For the convenience of non-maven developers
the following links are provided:

*   [<var class="icon-cloud-download"></var> Jersey 1.19.1 ZIP bundle][zip-1.x] contains the Jersey
    jars, core dependencies (it does not provide dependencies for third party jars beyond those for JSON
    support) and JavaDoc.
*   [<var class="icon-cloud-download"></var> Jersey 1.19.1 JAR bundle][jar-1.x] is a single-JAR Jersey
    bundle to avoid the dependency management of multiple Jersey module JARs.

[mvn-central]: https://repo1.maven.org/maven2/org/glassfish/jersey/
[mvn-jvn]: https://oss.sonatype.org/content/groups/public/org/glassfish/jersey/
[mvn-oss]: https://jakarta.oss.sonatype.org/content/groups/public/org/glassfish/jersey/

[zip-1.x]: https://repo1.maven.org/maven2/com/sun/eclipse-ee4j/jersey-archive/1.19.1/jersey-archive-1.19.1.zip
[jar-1.x]: https://repo1.maven.org/maven2/com/sun/eclipse-ee4j/jersey-bundle/1.19.1/jersey-bundle-1.19.1.jar
[deps-1.x]: {{ site.links.newJerseyURL }}/documentation/1.19.1/chapter_deps.html

[jaxrs-3.0]: https://jakarta.ee/specifications/restful-ws/3.0/
[jaxrs-2.1]: https://jcp.org/en/jsr/detail?id=370
[jaxrs-2.0]: https://jcp.org/en/jsr/detail?id=339
[zip-3.x]: https://repo1.maven.org/maven2/org/glassfish/jersey/bundles/jaxrs-ri/{{ site.latest3xVersion }}/jaxrs-ri-{{ site.latest3xVersion }}.zip
[zip-2.x]: https://repo1.maven.org/maven2/org/glassfish/jersey/bundles/jaxrs-ri/{{ site.latestVersion }}/jaxrs-ri-{{ site.latestVersion }}.zip
[zip-2.25.x]: https://repo1.maven.org/maven2/org/glassfish/jersey/bundles/jaxrs-ri/2.25.1/jaxrs-ri-2.25.1.zip
[examples-2.x]: https://repo1.maven.org/maven2/org/glassfish/jersey/bundles/jersey-examples/{{ site.latestVersion }}/jersey-examples-{{ site.latestVersion }}-all.zip
[examples-2.25.x]: https://repo1.maven.org/maven2/org/glassfish/jersey/bundles/jersey-examples/2.25.1/jersey-examples-2.25.1-all.zip
[deps-2.x]: {{ site.links.newJerseyURL }}/documentation/latest/modules-and-dependencies.html
[deps-2.25.x]: {{ site.links.newJerseyURL }}/documentation/latest/modules-and-dependencies.html
[rn-2.x]:https://github.com/eclipse-ee4j/jersey/releases/tag/{{ site.latestVersion }}
[rn-2.25.x]: {{ site.links.newJerseyURL }}/release-notes/2.25.1.html
[tag-3.x]:https://github.com/eclipse-ee4j/jersey/releases/tag/{{ site.latest3xVersion }}
[rn-3.x]:https://github.com/eclipse-ee4j/jersey/releases/tag/{{ site.latest3xVersion }}
