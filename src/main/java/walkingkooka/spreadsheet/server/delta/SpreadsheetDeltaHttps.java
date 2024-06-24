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
package walkingkooka.spreadsheet.server.delta;

import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetValueType;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.json.JsonNode;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * A collection of factory methods to create various {@link HateosResourceHandler}.
 */
public final class SpreadsheetDeltaHttps implements PublicStaticHelper {

    /**
     * {@see SpreadsheetDeltaHateosResourceHandlerClearColumns}
     */
    public static HateosResourceHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> clearColumns(final SpreadsheetEngine engine,
                                                                                                                     final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerClearColumns.with(engine, context);
    }

    /**
     * {@see SpreadsheetDeltaHateosResourceHandlerClearRows}
     */
    public static HateosResourceHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> clearRows(final SpreadsheetEngine engine,
                                                                                                               final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerClearRows.with(engine, context);
    }

    /**
     * {@see SpreadsheetDeltaHateosResourceHandlerDeleteColumns}
     */
    public static HateosResourceHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> deleteColumns(final SpreadsheetEngine engine,
                                                                                                                      final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerDeleteColumns.with(engine, context);
    }

    /**
     * {@see SpreadsheetDeltaHateosResourceHandlerDeleteRows}
     */
    public static HateosResourceHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> deleteRows(final SpreadsheetEngine engine,
                                                                                                                final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerDeleteRows.with(engine, context);
    }

    /**
     * {@see SpreadsheetDeltaHateosResourceHandlerFillCells}
     */
    public static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> fillCells(final SpreadsheetEngine engine,
                                                                                                                final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerFillCells.with(engine, context);
    }

    /**
     * {@see SpreadsheetDeltaHateosResourceHandlerFindCells}
     */
    public static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> findCells(final int defaultMax,
                                                                                                                final SpreadsheetEngine engine,
                                                                                                                final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerFindCells.with(
                defaultMax,
                engine,
                context
        );
    }

    /**
     * {@see SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandler}
     */
    public static HateosResourceHandler<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> findSimilarities(final SpreadsheetEngineContext context) {
        return SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandler.with(context);
    }

    /**
     * {@see SpreadsheetDeltaHateosResourceHandlerInsertAfterColumn}
     */
    public static HateosResourceHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertAfterColumns(final SpreadsheetEngine engine,
                                                                                                                           final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerInsertAfterColumn.with(engine, context);
    }

    /**
     * {@see SpreadsheetDeltaHateosResourceHandlerInsertAfterRow}
     */
    public static HateosResourceHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertAfterRows(final SpreadsheetEngine engine,
                                                                                                                     final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerInsertAfterRow.with(engine, context);
    }

    /**
     * {@see SpreadsheetDeltaHateosResourceHandlerInsertBeforeColumn}
     */
    public static HateosResourceHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertBeforeColumns(final SpreadsheetEngine engine,
                                                                                                                            final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerInsertBeforeColumn.with(engine, context);
    }

    /**
     * {@see SpreadsheetDeltaHateosResourceHandlerInsertBeforeRow}
     */
    public static HateosResourceHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertBeforeRows(final SpreadsheetEngine engine,
                                                                                                                      final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerInsertBeforeRow.with(engine, context);
    }

    /**
     * {@see SpreadsheetDeltaHateosResourceHandlerLoadCell}
     */
    public static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCell(final SpreadsheetEngineEvaluation evaluation,
                                                                                                               final SpreadsheetEngine engine,
                                                                                                               final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerLoadCell.with(evaluation,
                engine,
                context);
    }

    /**
     * {@see SpreadsheetDeltaHateosResourceHandlerSaveCell}
     */
    public static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> saveCell(final SpreadsheetEngine engine,
                                                                                                               final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerSaveCell.with(engine, context);
    }

    /**
     * {@see SpreadsheetDeltaHateosResourceHandlerDeleteCell}
     */
    public static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell(final SpreadsheetEngine engine,
                                                                                                                 final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerDeleteCell.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosResourceHandlerSpreadsheetDeltaSortCell}
     */
    public static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> sortCells(final SpreadsheetEngine engine,
                                                                                                                final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerSortCells.with(engine, context);
    }

    /**
     * {@see SpreadsheetDeltaPatchFunctionCell}
     */
    public static UnaryOperator<JsonNode> patchCell(final HttpRequest request,
                                                    final SpreadsheetEngine engine,
                                                    final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaPatchFunction.cell(
                request,
                engine,
                context
        );
    }

    /**
     * {@see SpreadsheetDeltaPatchFunctionColumn}
     */
    public static UnaryOperator<JsonNode> patchColumn(final HttpRequest request,
                                                      final SpreadsheetEngine engine,
                                                      final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaPatchFunction.column(
                request,
                engine,
                context
        );
    }

    /**
     * {@see SpreadsheetDeltaPatchFunctionRow}
     */
    public static UnaryOperator<JsonNode> patchRow(final HttpRequest request,
                                                   final SpreadsheetEngine engine,
                                                   final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaPatchFunction.row(
                request,
                engine,
                context
        );
    }

    /**
     * Prepares a {@link SpreadsheetDelta} response honouring any present query and window query parameters.
     */
    static SpreadsheetDelta prepareResponse(final Optional<SpreadsheetDelta> in,
                                            final Map<HttpRequestAttribute<?>, Object> parameters,
                                            final SpreadsheetDelta out,
                                            final SpreadsheetEngine engine,
                                            final SpreadsheetEngineContext context) {

        final Optional<Expression> maybeExpression = SpreadsheetDeltaUrlQueryParameters.query(
                parameters,
                context
        );

        final Optional<String> maybeValueType = SpreadsheetDeltaUrlQueryParameters.valueType(
                parameters,
                context
        );

        SpreadsheetDelta result = out;

        if (maybeExpression.isPresent()) {
            result = out.setMatchedCells(
                    engine.filterCells(
                                    out.cells(),
                                    maybeValueType.orElse(SpreadsheetValueType.ANY),
                                    maybeExpression.get(),
                                    context
                            ).stream()
                            .map(
                                    SpreadsheetCell::reference
                            ).collect(Collectors.toCollection(Sets::ordered))
            );
        }

        return result.setWindow(
                SpreadsheetDeltaUrlQueryParameters.window(
                        parameters,
                        in,
                        engine,
                        context
                )
        );
    }

    /**
     * Stop creation.
     */
    private SpreadsheetDeltaHttps() {
        throw new UnsupportedOperationException();
    }
}
