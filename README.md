[![Build Status](https://github.com/mP1/walkingkooka-spreadsheet-server/actions/workflows/build.yaml/badge.svg)](https://github.com/mP1/walkingkooka-spreadsheet-server/actions/workflows/build.yaml/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/mP1/walkingkooka-spreadsheet-server/badge.svg?branch=master)](https://coveralls.io/repos/github/mP1/walkingkooka-spreadsheet-server?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/mP1/walkingkooka-spreadsheet-server.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-spreadsheet-server/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/mP1/walkingkooka-spreadsheet-server.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-spreadsheet-server/alerts/)
[![J2CL compatible](https://img.shields.io/badge/J2CL-compatible-brightgreen.svg)](https://github.com/mP1/j2cl-central)


The http server for [walkingkooka-spreadsheet](https://github.com/mP1/walkingkooka-spreadsheet). It includes a router
and some handlers for spreadsheet services.

## REST

GET methods require no BODY and always return a BODY POST, PUT methods require and return a BODY DELETE methods require
no body and return a BODY

A swagger-ui is available at:

> http://server/api-doc/index.html

- Simple payloads are present.
- The definitions of the available JSON types are not
  present [TODO](https://github.com/mP1/walkingkooka-spreadsheet-server/issues/480)
- To examine the JSON form examining tests will be required.

### Context

A collection of end points that return a `SpreadsheetMetadata`, in JSON form.

- GET /api/spreadsheet/$spreadsheet-id
- POST /api/spreadsheet/ expects no BODY, creates a Spreadsheet with `SpreadsheetMetadata` with defaults using any
  provided `Locale`.
- POST /api/spreadsheet/$spreadsheet-id requires a BODY to update existing `SpreadsheetMetadata.`
- PATCH /api/spreadsheet/$spreadsheet-id Patch an existing `SpreadsheetMetadata.`



### Engine

A collection of end points that support manipulating cells, columns and rows and similar functionality. All input and
output is always a `SpreadsheetDelta` in JSON form, where necessary.



#### cell

- GET /api/spreadsheet/$spreadsheet-id/cell/*
  /clear-value-error-skip-evaluate?home=A1&xOffset=0&yOffset=1&width=2&height=3&selection-type=cell-range&selection=A1:
  B2
- GET /api/spreadsheet/$spreadsheet-id/cell/A1/clear-value-error-skip-evaluate
- GET /api/spreadsheet/$spreadsheet-id/cell/A1/skip-evaluate
- GET /api/spreadsheet/$spreadsheet-id/cell/A1/force-recompute
- GET /api/spreadsheet/$spreadsheet-id/cell/A1/compute-if-necessary
- PATCH /api/spreadsheet/$spreadsheet-id/cell/A1
- POST /api/spreadsheet/$spreadsheet-id/cell/A1
- DELETE /api/spreadsheet/$spreadsheet-id/cell/A1
- DELETE /api/spreadsheet/$spreadsheet-id/cell/A1:B2
- POST /api/spreadsheet/$spreadsheet-id/cell/A1/clear
- POST /api/spreadsheet/$spreadsheet-id/cell/A1:B2/clear
- POST /api/spreadsheet/$spreadsheet-id/cell/A1-B2/fill **input includes region of cells to be the fill content**

#### column

- POST /api/spreadsheet/$spreadsheet-id/column/A/clear
- POST /api/spreadsheet/$spreadsheet-id/column/A:B/clear
- POST /api/spreadsheet/$spreadsheet-id/column/A/before?count=1
- POST /api/spreadsheet/$spreadsheet-id/column/A:B/before?count=1
- POST /api/spreadsheet/$spreadsheet-id/column/A/after?count=1
- POST /api/spreadsheet/$spreadsheet-id/column/A:B/after?count=1
- DELETE /api/spreadsheet/$spreadsheet-id/column/A
- DELETE /api/spreadsheet/$spreadsheet-id/column/A:B

#### format

- POST /api/spreadsheet/$spreadsheet-id/format

#### label

- DELETE /api/spreadsheet/$spreadsheet-id/label/$label-name
- GET /api/spreadsheet/$spreadsheet-id/label/$label-name
- POST /api/spreadsheet/$spreadsheet-id/label
- POST /api/spreadsheet/$spreadsheet-id/label/$label-name

#### parse

- GET /api/spreadsheet/$spreadsheet-id/parse

#### range

- GET /api/spreadsheet/$spreadsheet-id/range/A1:0:0:150:25

#### row

- POST /api/spreadsheet/$spreadsheet-id/row/1
- POST /api/spreadsheet/$spreadsheet-id/row/1/after?count=1
- POST /api/spreadsheet/$spreadsheet-id/row/1:2/after?count=1
- POST /api/spreadsheet/$spreadsheet-id/row/1/before?count=1
- POST /api/spreadsheet/$spreadsheet-id/row/1:2/before?count=1
- POST /api/spreadsheet/$spreadsheet-id/row/1/clear
- POST /api/spreadsheet/$spreadsheet-id/row/1:3/clear
- DELETE /api/spreadsheet/$spreadsheet-id/row/1
- DELETE /api/spreadsheet/$spreadsheet-id/row/1:2


- The selection-type and selection url parameters are optional, but must both be present together.
- Window may be passed to specify a window for the returned delta.

# Execution environment

Currently communication between the browser and the server follows a browser client and Http server paradigm.
A `HttpRequest` is constructed, headers describe various aspects of the JSON payload and the desired response. The
server hosts a router which examines the request which selects a handler which performs the action and prepares a
response.

## browser client / jetty server

A simple message from the browser to an API, looks something like this, where the react app uses the browser's fetch
object.

```
main UI thread -> Fetch HttpRequest -> Http Servlet container -> promise -> dispatch
```

## offline mode

The switch to offline mode means the React application simply replaces the Jetty servlet container, rather than using
the browser's fetch object to communicate via http to a Jetty server, the request is serialized and posted to a
webworker. The webworker hosts the same java server code translated to javascript.

`HttpRequests` and `HttpResponses` abstractions are still present but in webworker mode they are serialized as strings
within messages and the semantics are emulated
by [walkingkooka-spreadsheet-webworker](https://github.com/mP1/walkingkooka-spreadsheet-webworker).

```
main UI thread -> Fetch that really posts message (HttpRequest) -> window.postMessage(JSON.stringify(HttpRequest)) ->

web worker.onMessage -> message -> JSON.parse(HttpRequest) -> fake HttpServer
```

The url, headers and body of the request are still present and used to evaluate, route and select a handler which will
be responsible for producing some reply.

```
HttpResponse -> webworker.postMessage(JSON.stringify(HttpResponse) -> window.onMessage -> JSON.parse(HttpResponse) -> dispatch
```

Naturally there are limitations due to the offline nature and browser sand-boxing and those are currently unaddressed
and issues will be created.