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

- **GET** /api/spreadsheet/*
- **GET** /api/spreadsheet/$spreadsheet-id1,spreadsheet-id2?from=0&count=10
- **GET** /api/spreadsheet/$spreadsheet-id
- **POST** /api/spreadsheet/  
  expects no BODY, creates a Spreadsheet with `SpreadsheetMetadata` with defaults using any provided `Locale`.
- **POST**
  /api/spreadsheet/$spreadsheet-id [TODO PUT](https://github.com/mP1/walkingkooka-spreadsheet-server/issues/504)  
  requires a BODY to update existing `SpreadsheetMetadata.`
- **PATCH** /api/spreadsheet/$spreadsheet-id  
  Used to update an existing `SpreadsheetMetadata`, name or individual global settings for a spreadsheet.

### Engine

A collection of end points that support manipulating cells, columns and rows and similar functionality. All input and
output is always a `SpreadsheetDelta` in JSON form, where necessary.



#### cell

Most of these end points are used to fill the viewport or from actions such as selecting a cell or cell-range and then
clicking a context menu command.

- **GET** /api/spreadsheet/$spreadsheet-id/cell/*
  /clear-value-error-skip-evaluate?home=A1&&width=2&height=3&includeFrozenColumnsRows=true&selectionType=cell-range&selection=A1:
  B2&selectionNavigation=extend-left&properties="*"  
  Used by the UI to load just enough cells to fill the viewport.
- **GET** /api/spreadsheet/$spreadsheet-id/cell/A1/clear-value-error-skip-evaluate
- **GET** /api/spreadsheet/$spreadsheet-id/cell/A1/skip-evaluate
- **GET** /api/spreadsheet/$spreadsheet-id/cell/A1/force-recompute
- **GET** /api/spreadsheet/$spreadsheet-id/cell/A1/compute-if-necessary
- **GET** /api/spreadsheet/$spreadsheet-id/cell/A1:B2/sort?comparators=A=day-of-month;B=month-of-year
- **PATCH** /api/spreadsheet/$spreadsheet-id/cell/A1  
  The UI uses this to update individual properties of a cell, such as updating the formula text
- **POST** /api/spreadsheet/$spreadsheet-id/cell/A1
- **DELETE** /api/spreadsheet/$spreadsheet-id/cell/A1
- **DELETE** /api/spreadsheet/$spreadsheet-id/cell/A1:B2
- **POST** /api/spreadsheet/$spreadsheet-id/cell/A1-B2/fill  
  input includes region of cells to be the fill content  
  This has many purposes including functionality such as filling a range, pasting previously copied cells etc.

#### column

Many of these are very closed mapped to the context menu that appears when column/columns are selected.

- **PATCH** /api/spreadsheet/$spreadsheet-id/column/A
- **POST** /api/spreadsheet/$spreadsheet-id/column/A/clear  
  Used by the UI to clear or erase all cells within the selected column/columns.
- **POST** /api/spreadsheet/$spreadsheet-id/column/A:B/clear
- **POST** /api/spreadsheet/$spreadsheet-id/column/A/before?count=1  
  Used by the UI to insert one or more columns before the given column
- **POST** /api/spreadsheet/$spreadsheet-id/column/A:B/before?count=1
- **POST** /api/spreadsheet/$spreadsheet-id/column/A/after?count=1
- **POST** /api/spreadsheet/$spreadsheet-id/column/A:B/after?count=1
- **DELETE** /api/spreadsheet/$spreadsheet-id/column/A
- **DELETE** /api/spreadsheet/$spreadsheet-id/column/A:B

#### comparators

These end points may be used to work with available SpreadsheetComparator(s)

- **GET** /api/spreadsheet/$spreadsheet-id/comparator

#### expression function(s)

These end points may be used to work with available ExpressionFuntion(s)

- **GET** /api/spreadsheet/$spreadsheet-id/expression-function

#### label

These end points are mostly used by the label management dialog.

- **DELETE** /api/spreadsheet/$spreadsheet-id/label/$label-name
- **GET** /api/spreadsheet/$spreadsheet-id/label/$label-name
- **POST** /api/spreadsheet/$spreadsheet-id/label  
  Used by the UI to create a new label to cell or cell-range
- **POST** /api/spreadsheet/$spreadsheet-id/label/$label-name

#### row

Many of these are very closed mapped to the context menu that appears when row/rows are selected.

- **PATCH** /api/spreadsheet/$spreadsheet-id/row/1
- **POST** /api/spreadsheet/$spreadsheet-id/row/1
- **POST** /api/spreadsheet/$spreadsheet-id/row/1/after?count=1
- **POST** /api/spreadsheet/$spreadsheet-id/row/1:2/after?count=1
- **POST** /api/spreadsheet/$spreadsheet-id/row/1/before?count=1
- **POST** /api/spreadsheet/$spreadsheet-id/row/1:2/before?count=1
- **POST** /api/spreadsheet/$spreadsheet-id/row/1/clear
- **POST** /api/spreadsheet/$spreadsheet-id/row/1:3/clear
- **DELETE** /api/spreadsheet/$spreadsheet-id/row/1
- **DELETE** /api/spreadsheet/$spreadsheet-id/row/1:2

#### url / query string parameters

- `selection-type` & `selection`  
  These parameters which must be present together are used to include the current selection and may affect the results,
  such as loading cells.
- `window`  
  This parameter is used to limit the range of cells, columns and rows returned. For example updating a cell may
  result  
  in many cells being updated but only those visible in the viewport should be returned.

# Execution environment

Currently communication between the browser and the server follows a browser client and Http server paradigm.
A `HttpRequest` is constructed, headers describe various aspects of the JSON payload and the desired response. The
server hosts a router which examines the request which selects a handler which performs the action and prepares a
response.

## browser client / jetty server

A simple message from the browser to an API, looks something like this, where the react app uses the browser's fetch
object.

```
main UI thread
-> Fetch HttpRequest
-> Http Servlet container
-> promise
-> dispatch
```

## offline mode

The switch to offline mode means the React application simply replaces the Jetty servlet container, rather than using
the browser's fetch object to communicate via http to a Jetty server, the request is serialized and posted to a
webworker. The webworker hosts the same java server code translated to javascript.

`HttpRequests` and `HttpResponses` abstractions are still present but in webworker mode they are serialized as strings
within messages and the semantics are emulated
by [walkingkooka-spreadsheet-webworker](https://github.com/mP1/walkingkooka-spreadsheet-webworker).

```
main UI thread 
-> Fetch that really posts message (HttpRequest)
-> window.postMessage(JSON.stringify(HttpRequest))

web worker.onMessage
-> message
-> JSON.parse(HttpRequest)
-> fake HttpServer
```

The url, headers and body of the request are still present and used to evaluate, route and select a handler which will
be responsible for producing some reply.

```
webworker HttpResponse
-> webworker.postMessage(JSON.stringify(HttpResponse)

main UI thread
-> window.onMessage
-> JSON.parse(HttpResponse)
-> dispatch
```

Naturally there are limitations due to the offline nature and browser sand-boxing and those are currently unaddressed
and issues will be created.