[![Build Status](https://travis-ci.com/mP1/walkingkooka-spreadsheet-server.svg?branch=master)](https://travis-ci.com/mP1/walkingkooka-spreadsheet-server.svg?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/mP1/walkingkooka-spreadsheet-server/badge.svg?branch=master)](https://coveralls.io/repos/github/mP1/walkingkooka-spreadsheet-server?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/mP1/walkingkooka-spreadsheet-server.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-spreadsheet-server/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/mP1/walkingkooka-spreadsheet-server.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-spreadsheet-server/alerts/)
[![J2CL compatible](https://img.shields.io/badge/J2CL-compatible-brightgreen.svg)](https://github.com/mP1/j2cl-central)


The http server for [walkingkooka-spreadsheet](https://github.com/mP1/walkingkooka-spreadsheet). It includes a router
and some handlers for several spreadsheet services. 



# J2cl-compatibility

An abstraction will exist for a fetch that rather than making a network call to a real Http Servlet such as Jetty,
the `HttpRequest` will be serialized into a String and `WebWorker.postMessage`. The web worker have a `HttpServer` like
abstraction that routes the received `HttpRequest`. Both the servlet container and the web worker fake `HttpServer` process
their `HttpRequests` through the same routers based on paths and other request attributes until a handler is matched
and processing completed.



Standard browser Fetch makes request to HttpServer Servlet container like Jetty

```
main UI thread -> Fetch HttpRequest -> Http Servlet container -> ...
```



Fetch that posts Message to Web-worker. In this case the client that calls this version of fetch remains unchanged.
Requests have URLS, they have headers and a body. These will be serialized and posted to the web-worker.

```
main UI thread -> Fetch that really posts message (HttpRequest) -> window.postMessage(JSON.stringify(HttpRequest)) ->

web worker.onMessage -> message -> JSON.parse(HttpRequest) -> fake HttpServer
```



Routing the Request based on URLs, and other attributes will perform the same identical logic and result in the same
handler being matched. Eventually the handler will produce a `HttpResponse` with some JSON body.

```
HttpRequest -> router -> handler -> service -> HttpResponse
```

The return path is different of the `HttpResponse` is different, the standard browser Fetch promise will resolve while
the Web Worker will post a message back the main UI thread. In both cases the `HttpResponse` status can be tested, headers
read, ahd the body parsed back into JSON.


Web worker
```
HttpResponse -> Json -> webworker.postMessage() -> window.onMessage -> JSON.parse(HttpResponse) -> React handles type
```



## Getting the source

You can either download the source using the "ZIP" button at the top
of the github page, or you can make a clone using git:

```
git clone git://github.com/mP1/walkingkooka-spreadsheet-server.git
```
 