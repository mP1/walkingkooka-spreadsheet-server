[![Build Status](https://github.com/mP1/walkingkooka-spreadsheet-server/actions/workflows/build.yaml/badge.svg)](https://github.com/mP1/walkingkooka-spreadsheet-server/actions/workflows/build.yaml/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/mP1/walkingkooka-spreadsheet-server/badge.svg)](https://coveralls.io/github/mP1/walkingkooka-spreadsheet-server)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/mP1/walkingkooka-spreadsheet-server.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-spreadsheet-server/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/mP1/walkingkooka-spreadsheet-server.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-spreadsheet-server/alerts/)
![](https://tokei.rs/b1/github/mP1/walkingkooka-spreadsheet-server)
[![J2CL compatible](https://img.shields.io/badge/J2CL-compatible-brightgreen.svg)](https://github.com/mP1/j2cl-central)

This module registers service end points that call [SpreadsheetEngine](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/engine/SpreadsheetEngine.java) services belonging to [walkingkooka-spreadsheet](https://github.com/mP1/walkingkooka-spreadsheet). 
Each module contains minimal logic and is mostly concerned with unmarshalling/marshalling parameters and invoking the service.

## REST

- **GET** methods require no BODY and always return a BODY
- **POST**, **PUT** methods require and return a BODY 
- **DELETE** methods require no body and return a BODY

A swagger-ui is available at:

> http://server/api-doc/index.html

- Simple payloads are present.
- The following tests contain many examples [SpreadsheetHttpServerTest](https://github.com/mP1/walkingkooka-spreadsheet-server/blob/master/src/test/java/walkingkooka/spreadsheet/server/SpreadsheetHttpServerTest.java)

### Plugin

A collection of end points to help manage plugins.

- **GET** /api/plugin/*
- **GET** /api/plugin/[PluginName](https://github.com/mP1/walkingkooka-plugin/blob/master/src/main/java/walkingkooka/plugin/PluginName.java)
- **GET** /api/plugin/[from PluginName](https://github.com/mP1/walkingkooka-plugin/blob/master/src/main/java/walkingkooka/plugin/PluginName.java):[to PluginName](https://github.com/mP1/walkingkooka-plugin/blob/master/src/main/java/walkingkooka/plugin/PluginName.java)
- **POST** /api/plugin/[PluginName](https://github.com/mP1/walkingkooka-plugin/blob/master/src/main/java/walkingkooka/plugin/PluginName.java)
 Body contains Plugin as JSON.
- **DELETE** /api/plugin/*
- **DELETE** /api/plugin/[PluginName](https://github.com/mP1/walkingkooka-plugin/blob/master/src/main/java/walkingkooka/plugin/PluginName.java)
- **DELETE** /api/plugin/[from PluginName](https://github.com/mP1/walkingkooka-plugin/blob/master/src/main/java/walkingkooka/plugin/PluginName.java):[to PluginName](https://github.com/mP1/walkingkooka-plugin/blob/master/src/main/java/walkingkooka/plugin/PluginName.java)
- **POST** /api/plugin/*/upload 
 Multipart file upload Binary file upload
- **GET** /api/plugin/[pluginName](https://github.com/mP1/walkingkooka-plugin/blob/master/src/main/java/walkingkooka/plugin/pluginName.java)/download
- **GET** /api/plugin/[pluginName](https://github.com/mP1/walkingkooka-plugin/blob/master/src/main/java/walkingkooka/plugin/pluginName.java)/list
- **GET** /api/plugin/[pluginName](https://github.com/mP1/walkingkooka-plugin/blob/master/src/main/java/walkingkooka/plugin/pluginName.java)/filter?query=$query&offset=0&count=1

### [SpreadsheetMetadata](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/metadata/SpreadsheetMetadata.java)

A collection of end points that support CRUD operations on spreadsheets. Note all payloads are in `JSON` form.

- **GET** /api/spreadsheet/*?offset=0&count=10
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)
- **POST** /api/spreadsheet/ expects no BODY, creates a Spreadsheet with [SpreadsheetMetadata](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/meta/SpreadsheetMetadata.java) with defaults using any provided `Locale`.
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java) requires a BODY to update existing `SpreadsheetMetadata.`
- **PATCH** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java) Used to update an existing [SpreadsheetMetadata](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/meta/SpreadsheetMetadata.java), name or individual global settings for a spreadsheet.
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/metadata/[SpreadsheetMetadataPropertyNameConverterSelector](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/meta/SpreadsheetMetadataPropertyNameConverterSelector.java)/verify
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/metadata/[SpreadsheetMetadataPropertyNameFormatterSelector](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/meta/SpreadsheetMetadataPropertyNameFormatterSelector.java)/edit/[SpreadsheetFormatterSelector](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/format/SpreadsheetFormatterSelector.java)
- **DELETE** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)

### [Engine](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/engine/SpreadsheetEngine.java)

A collection of end points that support manipulating cells, columns and rows and similar functionality. All input and
output is always  a [SpreadsheetDelta](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/engine/SpreadsheetDelta.java)
in JSON form, where necessary.

#### [Cell](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetCell.java)

Most of these end points are used to fill the viewport or from actions such as selecting a cell or cell-range and then
clicking a context menu command.

- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/&ast;/clear-value-error-skip-evaluate?home=A1&&width=2&height=3&includeFrozenColumnsRows=true&selectionType=cell-range&selection=A1:B2&selectionNavigation=extend-left&properties="*"  
  Used by the UI to load just enough cells to fill the viewport.
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1/clear-value-error-skip-evaluate
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1/skip-evaluate
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1/force-recompute
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1/compute-if-necessary
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1:B2/sort?comparators=A=day-of-month;B=month-of-year
- **PATCH** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1 
  The UI uses this to update individual properties of a cell, such as updating the formula text
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1
- **DELETE** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1
- **DELETE** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1:B2
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1-B2/fill 
  Input includes region of cells to be the fill content This has many purposes including functionality such as filling a range, pasting previously copied cells etc.
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1/form/[FormName](https://github.com/mP1/walkingkooka-validation/blob/master/src/main/java/walkingkooka/validation/form/FormName.java)
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1/form/[FormName](https://github.com/mP1/walkingkooka-validation/blob/master/src/main/java/walkingkooka/validation/form/FormName.java)
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1/formatter-edit/[SpreadsheetFormatterSelector](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/format/SpreadsheetFormatterSelector.java)
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1/formatter-menu
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1/labels?offset=0&count=1
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/A1-B2/labels?offset=0&count=1
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/*/labels?offset=0&count=1
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/cell/*/references?offset=0&count=1

#### [Column](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetColumn.java)

Many of these are very closely mapped to the context menu that appears when column/columns are selected.

- **PATCH** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/column/A
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/column/A/clear  
  Used by the UI to clear or erase all cells within the selected column/columns.
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/column/A:B/clear
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/column/A/before?count=1 
  Used by the UI to insert one or more columns before the given column
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/column/A:B/before?count=1
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/column/A/after?count=1
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/column/A:B/after?count=1
- **DELETE** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/column/A
- **DELETE** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/column/A:B

#### [Comparator](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/compare/SpreadsheetComparator.java)

These end points may be used to work with available [SpreadsheetComparatorInfoSet](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/compare/SpreadsheetComparatorInfoSet.java)

- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/comparator
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/comparator/[SpreadsheetComparatorName](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/compare/SpreadsheetComparatorName.java)

#### [Converter](https://github.com/mP1/walkingkooka-convert/blob/master/src/main/java/walkingkooka/convert/Converter.java)

These end points may be used to work with available [ConverterInfoSet](https://github.com/mP1/walkingkooka-convert-provider/blob/master/src/main/java/walkingkooka/convert/provider/ConverterInfoSet.java)

- **GET** /api/converter
- **GET** /api/converter/[ConverterName](https://github.com/mP1/walkingkooka-convert-provider/blob/master/src/main/java/walkingkooka/convert/provider/ConverterName.java)

#### [Currency](https://github.com/mP1/walkingkooka-spreadsheet-server/blob/master/src/main/java/walkingkooka/spreadsheet/server/currency/CurrencyCode.java)

End points to query available Currencies.

- **GET** /api/currency/[CurrencyCode](https://github.com/mP1/walkingkooka-spreadsheet-server/blob/master/src/main/java/walkingkooka/spreadsheet/server/currency/CurrencyCide.java)
- **GET** /api/currency/*?offset=0&count=1

#### [DateTimeSymbols](https://github.com/mP1/walkingkooka-datetime/blob/master/src/main/java/walkingkooka/datetime/DateTimeSymbols.java)

End points to fetch `DateTimeSymbols` for a given `Locale`.

- **GET** /api/dateTimeSymbols/[LocaleTag](https://github.com/mP1/walkingkooka-spreadsheet-server/blob/master/src/main/java/walkingkooka/spreadsheet/server/locale/LocaleTag.java)
- **GET** /api/dateTimeSymbols/*/localeStartsWith/LOCALE

#### [DecimalNumberSymbols](https://github.com/mP1/walkingkooka-math/blob/master/src/main/java/walkingkooka/math/DecimalNumberSymbols.java)

End points to fetch `DecimalNumberSymbols` for a given `Locale`.

- **GET** /api/decimalNumberSymbols/*/localeStartsWith/LOCALE
- **GET** /api/decimalNumberSymbols/[LocaleTag](https://github.com/mP1/walkingkooka-spreadsheet-server/blob/master/src/main/java/walkingkooka/spreadsheet/server/locale/LocaleTag.java)

#### [Exporter](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/export/SpreadsheetExporter.java)

These end points may be used to work with available [SpreadsheetExporterInfoSet](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/export/SpreadsheetExporterInfoSet.java)

- **GET** /api/exporter
- **GET** /api/exporter/[SpreadsheetExporterName](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/export/SpreadsheetExporterName.java)

#### [Form](https://github.com/mP1/walkingkooka-validation/blob/master/src/main/java/walkingkooka/validation/form/Form.java)

- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/form/[FormName](https://github.com/mP1/walkingkooka-validation/blob/master/src/main/java/walkingkooka/validation/form/FormName.java)
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/form/*?offset=0&count=1
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/form/[FormName](https://github.com/mP1/walkingkooka-validation/blob/master/src/main/java/walkingkooka/validation/form/FormName.java)
- **DELETE** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/form/[FormName](https://github.com/mP1/walkingkooka-validation/blob/master/src/main/java/walkingkooka/validation/form/FormName.java)

#### [Formatter](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/format/SpreadsheetFormatter.java)

These end points may be used to work with available [SpreadsheetFormatterInfoSet](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/format/SpreadsheetFormatterInfoSet.java)

- **GET** /api/formatter
- **GET** /api/formatter/[SpreadsheetFormatterName](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/format/SpreadsheetFormatterName.java)

#### [FormHandler](https://github.com/mP1/walkingkooka-validation/blob/master/src/main/java/walkingkooka/validation/form/FormHandler.java)

These end points may be used to work with available [SpreadsheetFormHandlerInfoSet](https://github.com/mP1/walkingkooka-validation/blob/master/src/main/java/walkingkooka/validation/form/provider/FormHandlerInfoSet.java)

- **GET** /api/formHandler
- **GET** /api/formHandler/[FormHandlerName](https://github.com/mP1/walkingkooka-validation/blob/master/src/main/java/walkingkooka/validation/form/provider/FormHandlerName.java)

#### [Expression function](https://github.com/mP1/walkingkooka-tree/blob/master/src/main/java/walkingkooka/tree/expression/function/ExpressionFunction.java)

These end points may be used to work with available [ExpressionFunctionInfoSet](https://github.com/mP1/walkingkooka-tree-expression-function-provider/blob/master/src/main/java/walkingkooka/tree/expression/function/provider/ExpressionFunctionInfoSet.java)

- **GET** /api/function
- **GET** /api/function/[ExpressionFunctionName](https://github.com/mP1/walkingkooka-tree/blob/master/src/main/java/walkingkooka/tree/expression/ExpressionFunctionName.java)

#### [Importer](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/importer/SpreadsheetImporter.java)

These end points may be used to work with available [SpreadsheetImporterInfoSet](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/importer/SpreadsheetImporterInfoSet.java)

- **GET** /api/importer
- **GET** /api/importer/[SpreadsheetImporterName](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/importer/SpreadsheetImporterName.java)

#### [Label](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/reference/SpreadsheetLabelName.java)

These end points are mostly used by the label management dialog.

- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/label/*?offset=0&count=1
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/label/[spreadsheetLabelName](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/reference/spreadsheetLabelName.java)
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/label/*/findByName/QUERY
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/label 
  Used by the UI to create a new label to cell or cell-range
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/label/[spreadsheetLabelName](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/reference/spreadsheetLabelName.java)
- **DELETE** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/label/[spreadsheetLabelName](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/reference/spreadsheetLabelName.java)

#### [Locale](https://github.com/mP1/walkingkooka-spreadsheet-server/blob/master/src/main/java/walkingkooka/spreadsheet/server/locale/LocaleTag.java)

End points to query available Locales. The locale query parameter is optional

- **GET** /api/locale/[LocaleTag](https://github.com/mP1/walkingkooka-spreadsheet-server/blob/master/src/main/java/walkingkooka/spreadsheet/server/locale/LocaleTag.java)?locale=en-AU
- **GET** /api/locale/*?offset=0&count=1&locale=en-AU

#### [Parser](https://github.com/mP1/walkingkooka-text-cursor-parser/blob/master/src/main/java/walkingkooka/text/cursor/parser/Parser.java)

These end points may be used to work with available [SpreadsheetParserInfoSet](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/format/SpreadsheetParserInfoSet.java)

- **GET** /api/parser
- **GET** /api/parser/[SpreadsheetParserName](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/format/SpreadsheetParserName.java)
- **GET** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/parser/*/edit/[SpreadsheetParserSelector](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/parser/SpreadsheetParserSelector.java)

#### [Row](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetRow.java)

Many of these are very closely mapped to the context menu that appears when row/rows are selected.

- **PATCH** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/row/1
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/row/1
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/row/1/after?count=1
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/row/1:2/after?count=1
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/row/1/before?count=1
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/row/1:2/before?count=1
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/row/1/clear
- **POST** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/row/1:3/clear
- **DELETE** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/row/1
- **DELETE** /api/spreadsheet/[SpreadsheetId](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/SpreadsheetId.java)/row/1:2

#### [Validator](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/validation/Validator.java)

These end points may be used to work with
available [ValidatorInfoSet](https://github.com/mP1/walkingkooka-spreadsheet/blob/master/src/main/java/walkingkooka/spreadsheet/validator/ValidatorInfoSet.java)

- **GET** /api/validator
- **GET** /api/validator/[ValidatorName](https://github.com/mP1/walkingkooka-validation/blob/master/src/main/java/walkingkooka/validation/provider/ValidatorName.java)

#### url / query string parameters

- `selection-type` & `selection`  
  These parameters which must be present together are used to include the current selection and may affect the results,
  such as loading cells.
- `window`  
  This parameter is used to limit the range of cells, columns and rows returned. For example updating a cell may
  result in many cells being updated but only those visible in the viewport should be returned.

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

The switch to offline mode means the [walkingkooka-spreadsheet-dominokit](https://github.com/mP1/walkingkooka-spreadsheet-dominokit) simply  replaces the Jetty servlet 
container, rather than using the browser's fetch object to communicate via http to a Jetty server, the request is
serialized and posted to a webworker. The webworker hosts the same java server code translated to javascript.

`HttpRequests` and `HttpResponses` abstractions are still present but in webworker mode they are serialized as strings
within messages and the semantics are emulated by [walkingkooka-spreadsheet-webworker](https://github.com/mP1/walkingkooka-spreadsheet-webworker).

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