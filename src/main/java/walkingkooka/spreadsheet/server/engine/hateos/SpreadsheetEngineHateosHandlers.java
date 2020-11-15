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
package walkingkooka.spreadsheet.server.engine.hateos;

import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.tree.Node;

import java.util.function.BiConsumer;

/**
 * A collection of factory methods to create various {@link HateosHandler}.
 */
public final class SpreadsheetEngineHateosHandlers implements PublicStaticHelper {

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetViewportComputeRange}
     */
    public static HateosHandler<SpreadsheetViewport, SpreadsheetRange, SpreadsheetRange> computeRange(final SpreadsheetEngine engine,
                                                                                                      final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetViewportComputeRange.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertDeleteColumns}
     */
    public static HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> deleteColumns(final SpreadsheetEngine engine,
                                                                                                              final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertDeleteColumns.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertDeleteRows}
     */
    public static HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> deleteRows(final SpreadsheetEngine engine,
                                                                                                        final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertDeleteRows.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaFillCells}
     */
    public static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> fillCells(final SpreadsheetEngine engine,
                                                                                                        final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaFillCells.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertColumns}
     */
    public static HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertColumns(final SpreadsheetEngine engine,
                                                                                                              final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertColumns.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertRows}
     */
    public static HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertRows(final SpreadsheetEngine engine,
                                                                                                        final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertRows.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell}
     */
    public static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCell(final SpreadsheetEngineEvaluation evaluation,
                                                                                                       final SpreadsheetEngine engine,
                                                                                                       final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.with(evaluation,
                engine,
                context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaSaveCell}
     */
    public static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> saveCell(final SpreadsheetEngine engine,
                                                                                                       final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaSaveCell.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteCell}
     */
    public static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell(final SpreadsheetEngine engine,
                                                                                                         final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteCell.with(engine, context);
    }

    /**
     * Returns a {@link Router} that handles all operations, using the given {@link SpreadsheetEngine} and {@link SpreadsheetEngineContext}.
     */
    public static <N extends Node<N, ?, ?, ?>> Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> engineRouter(final AbsoluteUrl base,
                                                                                                                                   final HateosContentType contentType,
                                                                                                                                   final HateosHandler<SpreadsheetViewport,
                                                                                                                                           SpreadsheetRange,
                                                                                                                                           SpreadsheetRange> computeRange,
                                                                                                                                   final HateosHandler<SpreadsheetColumnReference,
                                                                                                                                           SpreadsheetDelta,
                                                                                                                                           SpreadsheetDelta> deleteColumns,
                                                                                                                                   final HateosHandler<SpreadsheetRowReference,
                                                                                                                                           SpreadsheetDelta,
                                                                                                                                           SpreadsheetDelta> deleteRows,
                                                                                                                                   final HateosHandler<SpreadsheetCellReference,
                                                                                                                                           SpreadsheetDelta,
                                                                                                                                           SpreadsheetDelta> fillCells,
                                                                                                                                   final HateosHandler<SpreadsheetColumnReference,
                                                                                                                                           SpreadsheetDelta,
                                                                                                                                           SpreadsheetDelta> insertColumns,
                                                                                                                                   final HateosHandler<SpreadsheetRowReference,
                                                                                                                                           SpreadsheetDelta,
                                                                                                                                           SpreadsheetDelta> insertRows,
                                                                                                                                   final HateosHandler<SpreadsheetCellReference,
                                                                                                                                           SpreadsheetDelta,
                                                                                                                                           SpreadsheetDelta> loadCellClearValueErrorSkipEvaluate,
                                                                                                                                   final HateosHandler<SpreadsheetCellReference,
                                                                                                                                           SpreadsheetDelta,
                                                                                                                                           SpreadsheetDelta> loadCellSkipEvaluate,
                                                                                                                                   final HateosHandler<SpreadsheetCellReference,
                                                                                                                                           SpreadsheetDelta,
                                                                                                                                           SpreadsheetDelta> loadCellForceRecompute,
                                                                                                                                   final HateosHandler<SpreadsheetCellReference,
                                                                                                                                           SpreadsheetDelta,
                                                                                                                                           SpreadsheetDelta> loadCellComputeIfNecessary,
                                                                                                                                   final HateosHandler<SpreadsheetCellReference,
                                                                                                                                           SpreadsheetDelta,
                                                                                                                                           SpreadsheetDelta> saveCell,
                                                                                                                                   final HateosHandler<SpreadsheetCellReference,
                                                                                                                                           SpreadsheetDelta,
                                                                                                                                           SpreadsheetDelta> deleteCell) {
        return SpreadsheetEngineHateosHandlersRouter.router(base,
                contentType,
                computeRange,
                deleteColumns,
                deleteRows,
                fillCells,
                insertColumns,
                insertRows,
                loadCellClearValueErrorSkipEvaluate,
                loadCellSkipEvaluate,
                loadCellForceRecompute,
                loadCellComputeIfNecessary,
                saveCell,
                deleteCell);
    }

    /**
     * Stop creation.
     */
    private SpreadsheetEngineHateosHandlers() {
        throw new UnsupportedOperationException();
    }
}
