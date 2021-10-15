[![Build Status](https://github.com/mP1/walkingkooka-spreadsheet-server/actions/workflows/build.yaml/badge.svg)](https://github.com/mP1/walkingkooka-spreadsheet-server/actions/workflows/build.yaml/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/mP1/walkingkooka-spreadsheet-server/badge.svg?branch=master)](https://coveralls.io/repos/github/mP1/walkingkooka-spreadsheet-server?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/mP1/walkingkooka-spreadsheet-server.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-spreadsheet-server/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/mP1/walkingkooka-spreadsheet-server.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-spreadsheet-server/alerts/)
[![J2CL compatible](https://img.shields.io/badge/J2CL-compatible-brightgreen.svg)](https://github.com/mP1/j2cl-central)


The http server for [walkingkooka-spreadsheet](https://github.com/mP1/walkingkooka-spreadsheet). It includes a router
and some handlers for spreadsheet services. 



# J2cl-compatibility

An abstraction will exist for a fetch that rather than making a network call to a real Http Servlet such as Jetty,
the `HttpRequest` will be serialized into a String and `WebWorker.postMessage`. The web worker have a `HttpServer` like
abstraction that routes the received `HttpRequest`. Both the servlet container and the web worker fake `HttpServer` process
their `HttpRequests` through the same routers based on paths and other request attributes until a handler is matched
and processing completed.



Standard browser Fetch makes request to HttpServer Servlet container like Jetty

```
main UI thread -> Fetch HttpRequest -> Http Servlet container -> promise -> dispatch
```



Fetch that posts Message to Web-worker.

In this case the client that calls this version of fetch interface remains unchanged. The `HttpRequest` will be converted
into a String with json and posted across the boundary.

```
main UI thread -> Fetch that really posts message (HttpRequest) -> window.postMessage(JSON.stringify(HttpRequest)) ->

web worker.onMessage -> message -> JSON.parse(HttpRequest) -> fake HttpServer
```



Routing the `HttpRequest` based on URLs, and other attributes will perform the same identical logic and result in the same
handler being matched, assuming the same routing mappings. Eventually the handler will produce a `HttpResponse` with some JSON body.

```
HttpRequest -> router -> handler -> HttpResponse
```

The return path is different of the `HttpResponse` is however different. The standard browser Fetch promise will resolve
while the Web Worker will post a message back the main UI thread. In both cases the `HttpResponse` status can be tested, headers
consumed, ahd the body parsed back into JSON.


Web worker
```
HttpResponse -> webworker.postMessage(JSON.stringify(HttpResponse) -> window.onMessage -> JSON.parse(HttpResponse) -> dispatch
```

The Web worker http server emulation layer will live in [walkingkooka-spreadsheet-webworker](https://github.com/mP1/walkingkooka-spreadsheet-webworker).



## REST

GET methods require no BODY and always return a BODY
POST, PUT methods require and return a BODY
DELETE methods require no body and return a BODY



### Context

A collection of end points that return a `SpreadsheetMetadata`, in JSON form.

- GET    /api/spreadsheet/$spreadsheet-id
- POST   /api/spreadsheet/                expects no BODY, creates a Spreadsheet with `SpreadsheetMetadata` with defaults using any provided `Locale`.
- POST   /api/spreadsheet/$spreadsheet-id requires a BODY to update existing `SpreadsheetMetadata.`



### Engine

A collection of end points that support manipulating cells, columns and rows and similar functionality.
All input and output is always a `SpreadsheetDelta` in JSON form, where necessary.

- GET /api/spreadsheet/$spreadsheet-id/cell/*
  /clear-value-error-skip-evaluate?home=A1&xOffset=0&yOffset=1&width=2&height=3&selection-type=cell-range&selection=A1:
  B2
- GET /api/spreadsheet/$spreadsheet-id/cell/A1/clear-value-error-skip-evaluate
- GET /api/spreadsheet/$spreadsheet-id/cell/A1/skip-evaluate
- GET /api/spreadsheet/$spreadsheet-id/cell/A1/force-recompute
- GET /api/spreadsheet/$spreadsheet-id/cell/A1/compute-if-necessary
- POST /api/spreadsheet/$spreadsheet-id/cell/A1
- DELETE /api/spreadsheet/$spreadsheet-id/cell/A1
- DELETE /api/spreadsheet/$spreadsheet-id/cell/A1:B2
- POST /api/spreadsheet/$spreadsheet-id/cell/A1-B2/fill **input includes region of cells to be the fill content**
- PATCH /api/spreadsheet/$spreadsheet-id/cell/A1
- GET /api/spreadsheet/$spreadsheet-id/cell-reference/Label123
- GET /api/spreadsheet/$spreadsheet-id/column/A
- POST /api/spreadsheet/$spreadsheet-id/column/A/before?count=1
- POST /api/spreadsheet/$spreadsheet-id/column/A:B/before?count=1
- POST /api/spreadsheet/$spreadsheet-id/column/A/after?count=1
- POST /api/spreadsheet/$spreadsheet-id/column/A:B/after?count=1
- DELETE /api/spreadsheet/$spreadsheet-id/column/A DELETE /api/spreadsheet/$spreadsheet-id/column/A:B
- GET /api/spreadsheet/$spreadsheet-id/format
- DELETE /api/spreadsheet/$spreadsheet-id/label/$label-name
- GET    /api/spreadsheet/$spreadsheet-id/label/$label-name
- POST   /api/spreadsheet/$spreadsheet-id/label
- POST   /api/spreadsheet/$spreadsheet-id/label/$label-name
- GET    /api/spreadsheet/$spreadsheet-id/parse
- GET    /api/spreadsheet/$spreadsheet-id/range/A1:0:0:150:25
- GET    /api/spreadsheet/$spreadsheet-id/row/1
- POST   /api/spreadsheet/$spreadsheet-id/row/1
- POST   /api/spreadsheet/$spreadsheet-id/row/1/after?count=1
- POST   /api/spreadsheet/$spreadsheet-id/row/1:2/after?count=1
- POST   /api/spreadsheet/$spreadsheet-id/row/1/before?count=1
- POST   /api/spreadsheet/$spreadsheet-id/row/1:2/before?count=1
- DELETE /api/spreadsheet/$spreadsheet-id/row/1
- DELETE /api/spreadsheet/$spreadsheet-id/row/1:2

#### Engine url query parameters
- The selection-type and selection url parameters are optional, but must both be present together.
- Window may be passed to specify a window for the returned delta.
