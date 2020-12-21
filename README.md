<h3>About</h3>

Developing RESTful Web services that seamlessly support exposing your data in a
variety of representation media types and abstract away the low-level details
of the client-server communication is not an easy task without a good toolkit.
In order to simplify development of RESTful Web services and their clients in Java,
a standard and portable [JAX-RS API][jax-rs] has been designed.

Jersey RESTful Web Services 2.x framework is open source, production quality,
framework for developing RESTful Web Services in Java that provides support for
JAX-RS APIs and serves as a JAX-RS (JSR 311 & JSR 339 & JSR 370) Reference Implementation.

Jersey RESTful Web Services 3.x framework is open source, production quality,
framework for developing RESTful Web Services in Java that provides support for
Jakarta RESTful Web Services 3.0.

Jersey framework is more than the JAX-RS Reference Implementation. Jersey provides
it's own [API][jersey-api] that extend the JAX-RS toolkit with additional features
and utilities to further simplify RESTful service and client development. Jersey
also exposes numerous extension SPIs so that developers may extend Jersey to best
suit their needs.

Goals of Jersey project can be summarized in the following points:

*   Track the JAX-RS API and provide regular releases of production quality
    Reference Implementations that ships with GlassFish;
*   Provide APIs to extend Jersey & Build a community of users and developers;
    and finally
*   Make it easy to build RESTful Web services utilising Java and the
    Java Virtual Machine.

---

*   The latest stable release of Jersey is [{{ site.latestVersion }}][dwnld].
*   The latest published release of Jakartified (3.x) Jersey is [{{ site.latest3xVersion }}][dwnld].

---

<table style="border:none;">
<tr>
<td  style="width:30%;border:none;vertical-align: top;">
<h3><a class="headerlink" href="{{ site.links.newJerseyURL }}/documentation/latest/getting-started.html">
    <var class="icon-compass"></var> Get Started
</a></h3>

<a href ="{{ site.links.newJerseyURL }}/documentation/latest/getting-started.html">Learn</a> how to use Jersey in your projects.
</td><td style="width:40%;border:none;vertical-align: top;">

<h3><a class="headerlink" href="{{ site.links.newJerseyURL }}/documentation/latest/index.html">
    <var class="icon-book"></var> Documentation
</a></h3>

<h4>Jersey 3.x</h4>
	<ul>
		<li><a href="{{ site.links.newJerseyURL }}/documentation/3.0.0/index.html">latest Jakartified (3.0.0) Jersey User Guide</a></li>
		<li><a href="{{ site.links.newJerseyURL }}/apidocs/3.0.0/jersey/index.html">latest Jakartified (3.0.0) Jersey API</a></li>
        </ul>
<h4>Jersey 2.x</h4>
	<ul><li><a href="{{ site.links.newJerseyURL }}/documentation/latest/index.html">latest Jersey {{ site.latestVersion }} User Guide</a></li>
	<li><a href="{{ site.links.newJerseyURL }}/apidocs/latest/jersey/index.html">latest Jersey {{ site.latestVersion }} API</a></li></ul>
<h4>Jersey 1.x</h4>
	<ul><li><a href="{{ site.links.newJerseyURL }}/documentation/1.19.1/index.html">Jersey 1.19.1 User Guide</a></li>
	<li><a href="{{ site.links.newJerseyURL }}/apidocs/1.19.1/jersey/index.html">Jersey 1.19.1 API</a></li></ul>
</td><td style="border:none;vertical-align: top;">

<h3><a class="headerlink" href="download.html">
    <var class="icon-cloud-download"></var> Download
</a></h3>

Jersey is distributed mainly via Maven and it offers some extra modules.
Check the <a href="download.html">How to Download</a> page or see our list of <a href="{{ site.links.newJerseyURL }}/documentation/latest/modules-and-dependencies.html">dependencies</a> for details.
</td></tr>
<tr><td style="border:none;vertical-align: top;">
<h3><a class="headerlink" href="related.html">
    <var class="icon-tags"></var> Related Projects
</a></h3>

List of projects related to Jersey.

</td><td style="border:none;vertical-align: top;">
<h3><a class="headerlink" href="contribute.html">
    <var class="icon-group"></var> Contribute
</a></h3>

<a href="contribute.html">Learn</a> how you can contribute to the project by:
<ul class="icons-ul">
    <li><var class="icon-li icon-bug"></var> Reporting issues</li>
    <li><var class="icon-li icon-code-fork"></var> Submitting patches</li>
    <li><var class="icon-li icon-eye-open"></var> Reviewing code</li>
</ul>

</td><td style="border:none;vertical-align: top;">
<h3><a class="headerlink" href="bloggers.html">
    <var class="icon-rss"></var> Developer Blogs
</a></h3>

Find out what our developers <a href="bloggers.html">blog</a> about.
</td></tr>
</table>

---

[jersey-api]: {{ site.links.newJerseyURL }}/apidocs/latest/jersey/index.html
[dwnld]: download.html
[jax-rs]: https://jakarta.ee/specifications/restful-ws/

<table style="border:none">
<tr>
<td style="width: 30%;
               text-align: start;
               vertical-align: top;
               border:none;">
<h3> <a name="Links"></a>Links</h3>

- <a href="https://projects.eclipse.org/projects/ee4j.jersey">Jersey project page</a><br/>
- <a href="TCK-Results.html">TCK Results</a><br/>
- <a href="{{ site.links.newJerseyURL }}">jersey.github.io (obsolete)</a><br/>
- <a href="https://jcp.org/en/jsr/detail?id=370">JSR-370 page on JCP site</a><br/>
- <a href="https://stackoverflow.com/questions/tagged/jersey">Stack Overflow</a><br/>
</td>
 <td style="border:none;width:70%">
 <h3>Latest Articles</h3>

<table>
<tr> <td> <a class="article" href="{{ site.links.honzablog }}/?p=235" target="_blank">Happy Jakarta EE 9 with Jersey 3.0.0</a>               </td><td> Dec 03, 2020 </td><td> Jan Supol </td> </tr>
<tr> <td> <a class="article" href="{{ site.links.honzablog }}/?p=202" target="_blank">Understanding Jakarta EE 9</a>                         </td><td> Oct 12, 2020 </td><td> Jan Supol </td> </tr>
<tr> <td> <a class="article" href="{{ site.links.honzablog }}/?p=190" target="_blank">What is new in Jersey 2.32</a>                         </td><td> Oct 10, 2020 </td><td> Jan Supol </td> </tr>
<tr> <td> <a class="article" href="{{ site.links.honzablog }}/?p=190" target="_blank">Jersey 2.30.1 has been released</a>                         </td><td> March 1, 2020 </td><td> Jan Supol </td> </tr>
<tr> <td> <a class="article" href="{{ site.links.honzablog }}/?p=171" target="_blank">New Features in Jersey Client</a>                         </td><td> Jan 13, 2020 </td><td> Jan Supol </td> </tr>
<tr> <td> <a class="article" href="{{ site.links.honzablog }}/?p=164" target="_blank">Jersey Apache Connector Hangs …?</a>                      </td><td> Jan 7, 2020 </td><td> Jan Supol </td> </tr>
<tr> <td> <a class="article" href="{{ site.links.honzablog }}/?p=150" target="_blank">Configuring Jersey Application</a>                        </td><td> Oct 4, 2019 </td><td> Jan Supol </td> </tr>
</table>


<a href="older-articles.html">Older Articles &hellip;</a>

 </td></tr>
    </table>
