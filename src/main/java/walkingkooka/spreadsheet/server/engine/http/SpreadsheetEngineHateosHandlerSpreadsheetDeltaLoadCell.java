/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package walkingkooka.spreadsheet.server.engine.http;

import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetViewport;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReferenceRange;
import walkingkooka.spreadsheet.reference.SpreadsheetReferenceKind;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReferenceRange;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.text.CharSequences;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A {@link HateosHandler} that calls {@link SpreadsheetEngine#loadCell(SpreadsheetCellReference, SpreadsheetEngineEvaluation, SpreadsheetEngineContext)}.
 */
final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell extends SpreadsheetEngineHateosHandlerSpreadsheetDelta<SpreadsheetCellReference> {

    static SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell with(final SpreadsheetEngineEvaluation evaluation,
                                                                       final SpreadsheetEngine engine,
                                                                       final SpreadsheetEngineContext context) {
        Objects.requireNonNull(evaluation, "evaluation");

        check(engine, context);
        return new SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell(evaluation,
                engine,
                context);
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell(final SpreadsheetEngineEvaluation evaluation,
                                                                   final SpreadsheetEngine engine,
                                                                   final SpreadsheetEngineContext context) {
        super(engine, context);
        this.evaluation = evaluation;
    }

    @Override
    public Optional<SpreadsheetDelta> handleAll(final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkResource(resource);
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

        final Set<SpreadsheetCellRange> window =
                this.ranges(
                        this.home(parameters),
                        this.width(parameters),
                        this.height(parameters),
                        this.selection(
                                resource,
                                parameters
                        ),
                        this.includeFrozenColumnsRows(parameters)
                );

        final Map<HttpRequestAttribute<?>, Object> parametersAndWindow = Maps.ordered();
        parametersAndWindow.putAll(parameters);
        parametersAndWindow.put(
                SpreadsheetEngineHttps.WINDOW,
                Lists.of(
                        window.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(","))
                )
        );

        return this.handleRange0(
                window,
                resource,
                parametersAndWindow
        );
    }

    private SpreadsheetCellReference home(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return firstParameterValueAndConvert(
                HOME,
                parameters,
                SpreadsheetCellReference::parseCell
        );
    }

    // @VisibleForTesting
    final static UrlParameterName HOME = UrlParameterName.with("home");

    private double width(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return firstDoubleParameterValue(WIDTH, parameters);
    }

    final static UrlParameterName WIDTH = UrlParameterName.with("width");

    private double height(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return firstDoubleParameterValue(HEIGHT, parameters);
    }

    final static UrlParameterName HEIGHT = UrlParameterName.with("height");

    private static double firstDoubleParameterValue(final UrlParameterName parameter,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters) {
        return firstParameterValueAndConvert(
                parameter,
                parameters,
                Double::parseDouble
        );
    }

    private boolean includeFrozenColumnsRows(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return firstParameterValueAndConvert(
                INCLUDE_FROZEN_COLUMNS_ROWS,
                parameters,
                Boolean::parseBoolean
        );
    }

    final static UrlParameterName INCLUDE_FROZEN_COLUMNS_ROWS = UrlParameterName.with("includeFrozenColumnsRows");

    private static <T> T firstParameterValueAndConvert(final UrlParameterName parameter,
                                                       final Map<HttpRequestAttribute<?>, Object> parameters,
                                                       final Function<String, T> converter) {
        final String value = parameter.firstParameterValueOrFail(parameters);
        try {
            return converter.apply(value);
        } catch (final Exception convertFailed) {
            throw new IllegalArgumentException("Invalid query parameter " + parameter + "=" + CharSequences.quoteIfChars(value));
        }
    }

    private Set<SpreadsheetCellRange> ranges(final SpreadsheetCellReference home,
                                             final double width,
                                             final double height,
                                             final Optional<SpreadsheetSelection> selection,
                                             final boolean includeFrozenColumnsRows) {
        final SpreadsheetEngineContext context = this.context;
        final SpreadsheetEngine engine = this.engine;
        final SpreadsheetMetadata metadata = context.metadata();

        double widthSum = 0;
        SpreadsheetColumnReference column = null;

        double heightSum = 0;
        SpreadsheetRowReference row = null;

        if (includeFrozenColumnsRows) {
            final Optional<SpreadsheetColumnReferenceRange> maybeFrozenColumns = metadata.get(SpreadsheetMetadataPropertyName.FROZEN_COLUMNS);
            if (maybeFrozenColumns.isPresent()) {
                final SpreadsheetColumnReference lastFrozen = maybeFrozenColumns.get().end();
                column = SpreadsheetReferenceKind.RELATIVE.firstColumn();

                while (!column.isLast()) {
                    widthSum += engine.columnWidth(column, context);
                    if (widthSum >= width) {
                        break;
                    }

                    if (column.equalsIgnoreReferenceKind(lastFrozen)) {
                        break;
                    }

                    column = column.addSaturated(1);
                }
            }

            final Optional<SpreadsheetRowReferenceRange> maybeFrozenRows = metadata.get(SpreadsheetMetadataPropertyName.FROZEN_ROWS);
            if (maybeFrozenRows.isPresent()) {
                final SpreadsheetRowReference lastFrozen = maybeFrozenRows.get().end();
                row = SpreadsheetReferenceKind.RELATIVE.firstRow();

                while (!row.isLast()) {
                    heightSum += engine.rowHeight(row, context);
                    if (heightSum >= height) {
                        break;
                    }

                    if (row.equalsIgnoreReferenceKind(lastFrozen)) {
                        break;
                    }

                    row = row.addSaturated(1);
                }
            }
        }

        // there might not be any viewport because frozen columns or frozen rows occupy the requested width/height......
        final double widthLeft = width - widthSum;
        final double heightLeft = height - heightSum;
        SpreadsheetCellRange viewport = null;
        if (widthLeft > 0 && heightLeft > 0) {
            viewport = engine.range(
                    SpreadsheetViewport.with(home, widthLeft, heightLeft),
                    selection,
                    context
            );
        }

        // A1 B1 C1   E1 F1   frozenColumnRows  frozenRows
        // A2 B2 C2   E2 F2
        //
        // A4 B4 C4   E4 F4   frozenColumns     viewport
        // A5 B5 C5   E5 F5

        // combos = fcr + fr + fc + v
        //                fr +      v
        //                     fc + v
        //          fcr
        //                fr
        //                     fc
        //                          v

        // compute the frozen ranges ...................................................................................
        SpreadsheetCellRange frozenColumnAndRow = null;
        SpreadsheetCellRange frozenColumns = null;
        SpreadsheetCellRange frozenRows = null;

        if (null != viewport) {
            if (null != column || null != row) {
                // A1 B1 C1   E1 F1   frozenColumnRows  frozenRows
                // A2 B2 C2   E2 F2
                //
                // A4 B4 C4   E4 F4   frozenColumns     viewport
                // A5 B5 C5   E5 F5

                final SpreadsheetCellReference viewportBegin = viewport.begin();
                final SpreadsheetColumnReference viewportBeginColumn = viewportBegin.column();
                final SpreadsheetRowReference viewportBeginRow = viewportBegin.row();

                final SpreadsheetCellReference viewportEnd = viewport.end();
                final SpreadsheetColumnReference viewportEndColumn = viewportEnd.column();
                final SpreadsheetRowReference viewportEndRow = viewportEnd.row();

                if (null != column && null != row) {
                    frozenColumnAndRow = this.rangeBottomRight(column, row);
                }

                if (null != row) {
                    frozenRows = viewportBeginColumn.setRow(SpreadsheetReferenceKind.RELATIVE.firstRow())
                            .cellRange(viewportEndColumn.setRow(row));
                }

                if (null != column) {
                    frozenColumns = SpreadsheetReferenceKind.RELATIVE.firstColumn().setRow(viewportBeginRow)
                            .cellRange(column.setRow(viewportEndRow));
                }
            }
        } else {
            if (null != column && null != row) {
                frozenColumnAndRow = this.rangeBottomRight(column, row);
            } else {
                if (null != column) {
                    // need to measure rows
                    row = SpreadsheetReferenceKind.RELATIVE.firstRow();

                    while (!row.isLast()) {
                        heightSum += engine.rowHeight(row, context);
                        if (heightSum >= height) {
                            break;
                        }
                        row = row.addSaturated(1);
                    }
                } else {
                    // need to measure columns
                    column = SpreadsheetReferenceKind.RELATIVE.firstColumn();

                    while (!column.isLast()) {
                        widthSum += engine.columnWidth(column, context);
                        if (widthSum >= width) {
                            break;
                        }
                        column = column.addSaturated(1);
                    }
                }
            }
        }

        // collect actual ranges.........................................................................................
        final Set<SpreadsheetCellRange> window = Sets.ordered();
        if (null != frozenColumnAndRow) {
            window.add(frozenColumnAndRow);
        }
        if (null != frozenColumns) {
            window.add(frozenColumns);
        }
        if (null != frozenRows) {
            window.add(frozenRows);
        }
        if (null != viewport) {
            window.add(viewport);
        }
        return Sets.readOnly(window);
    }

    private SpreadsheetCellRange rangeBottomRight(final SpreadsheetColumnReference column,
                                                  final SpreadsheetRowReference row) {
        return SpreadsheetCellReference.A1.cellRange(column.setRow(row));
    }

    // handleOne........................................................................................................

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetCellReference cell,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkCell(cell);
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

        return Optional.of(
                this.prepareResponse(
                        resource,
                        parameters,
                        this.loadCell(cell)
                )
        );
    }

    SpreadsheetDelta loadCell(final SpreadsheetCellReference reference) {
        return this.engine.loadCell(
                reference,
                this.evaluation,
                this.context
        );
    }

    private final SpreadsheetEngineEvaluation evaluation;

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetCellReference> cells,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkRange(cells);
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

        return this.handleRange0(
                Sets.of(SpreadsheetSelection.cellRange(cells)),
                resource,
                parameters
        );
    }

    private Optional<SpreadsheetDelta> handleRange0(final Set<SpreadsheetCellRange> window,
                                                    final Optional<SpreadsheetDelta> resource,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters) {
        return Optional.ofNullable(
                this.prepareResponse(
                        resource,
                        parameters,
                        this.engine.loadCells(
                                window,
                                this.evaluation,
                                this.context
                        )
                )
        );
    }

    @Override
    String operation() {
        return "loadCell " + this.evaluation;
    }
}
