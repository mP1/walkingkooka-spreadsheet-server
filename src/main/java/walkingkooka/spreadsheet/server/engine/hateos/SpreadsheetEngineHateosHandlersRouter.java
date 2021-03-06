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

import walkingkooka.Cast;
import walkingkooka.NeverError;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.StaticHelper;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetCellBox;
import walkingkooka.spreadsheet.SpreadsheetCoordinates;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumn;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;
import walkingkooka.spreadsheet.reference.SpreadsheetRow;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A collection of factory methods to create various {@link HateosHandler}.
 */
final class SpreadsheetEngineHateosHandlersRouter implements StaticHelper {

    static {
        SpreadsheetCellBox.with(SpreadsheetCellReference.parseCellReference("A1"), 1, 1, 1, 1);
        SpreadsheetDelta.NO_CELLS.isEmpty(); // force static initializers
        SpreadsheetViewport.COMPARATOR.hashCode();
    }

    /**
     * A {@link HateosResourceName} with <code>cell</code>.
     */
    private static final HateosResourceName CELL = HateosResourceName.with("cell");

    /**
     * A {@link LinkRelation} with <code>fill</code>.
     */
    private static final LinkRelation<?> FILL = LinkRelation.with("fill");

    /**
     * A {@link HateosResourceName} with <code>column</code>.
     */
    private static final HateosResourceName COLUMN = HateosResourceName.with("column");

    /**
     * A {@link HateosResourceName} with <code>row</code>.
     */
    private static final HateosResourceName ROW = HateosResourceName.with("row");

    /**
     * A {@link HateosResourceName} with <code>cellbox</code>.
     */
    private static final HateosResourceName COORDS = HateosResourceName.with("cellbox");
    private static final Class<HateosResource<SpreadsheetCoordinates>> COORDS_HATEOS_RESOURCE = Cast.to(HateosResource.class);

    private static final Class<SpreadsheetCellBox> OPTIONAL_CELLBOX = Cast.to(SpreadsheetCellBox.class);
    private static final Class<SpreadsheetCellBox> RANGE_CELLBOX = Cast.to(SpreadsheetCellBox.class);
    
    /**
     * A {@link HateosResourceName} with <code>viewport</code>.
     */
    private static final HateosResourceName VIEWPORT = HateosResourceName.with("viewport");
    private static final Class<HateosResource<SpreadsheetViewport>> VIEWPORT_HATEOS_RESOURCE = Cast.to(HateosResource.class);
    
    private static final Class<SpreadsheetRange> OPTIONAL_RANGE = Cast.to(SpreadsheetRange.class);
    private static final Class<SpreadsheetRange> RANGE_RANGE = Cast.to(SpreadsheetRange.class);

    private static final Class<SpreadsheetDelta> OPTIONAL_CELL_REFERENCE = Cast.to(SpreadsheetDelta.class);
    private static final Class<SpreadsheetDelta> RANGE_CELL_REFERENCE = Cast.to(SpreadsheetDelta.class);
    private static final Class<SpreadsheetDelta> OPTIONAL_COLUMN_REFERENCE = Cast.to(SpreadsheetDelta.class);
    private static final Class<SpreadsheetDelta> RANGE_COLUMN_REFERENCE = Cast.to(SpreadsheetDelta.class);
    private static final Class<SpreadsheetDelta> OPTIONAL_ROW_REFERENCE = Cast.to(SpreadsheetDelta.class);
    private static final Class<SpreadsheetDelta> RANGE_ROW_REFERENCE = Cast.to(SpreadsheetDelta.class);

    /**
     * Builds a {@link Router} that handles all operations, using the given {@link SpreadsheetEngine} and {@link SpreadsheetEngineContext}.
     */
    static Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> router(final AbsoluteUrl baseUrl,
                                                                                         final HateosContentType contentType,
                                                                                         final HateosHandler<SpreadsheetCoordinates,
                                                                                                 SpreadsheetCellBox,
                                                                                                 SpreadsheetCellBox> cellBox,
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
        Objects.requireNonNull(baseUrl, "baseUrl");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(cellBox, "cellBox");
        Objects.requireNonNull(computeRange, "computeRange");
        Objects.requireNonNull(fillCells, "fillCells");
        Objects.requireNonNull(deleteColumns, "deleteColumns");
        Objects.requireNonNull(deleteRows, "deleteRows");
        Objects.requireNonNull(insertColumns, "insertColumns");
        Objects.requireNonNull(insertRows, "insertRows");
        Objects.requireNonNull(loadCellClearValueErrorSkipEvaluate, "loadCellClearValueErrorSkipEvaluate");
        Objects.requireNonNull(loadCellSkipEvaluate, "loadCellSkipEvaluate");
        Objects.requireNonNull(loadCellForceRecompute, "loadCellForceRecompute");
        Objects.requireNonNull(loadCellComputeIfNecessary, "loadCellComputeIfNecessary");
        Objects.requireNonNull(saveCell, "saveCell");
        Objects.requireNonNull(deleteCell, "deleteCell");

        // cellBox GET.................................................................................................
        final HateosResourceMapping<SpreadsheetCoordinates, SpreadsheetCellBox, SpreadsheetCellBox, HateosResource<SpreadsheetCoordinates>>
                cellBoxGet = HateosResourceMapping.with(
                COORDS,
                SpreadsheetEngineHateosHandlersRouter::parseCoordinates,
                OPTIONAL_CELLBOX,
                RANGE_CELLBOX,
                COORDS_HATEOS_RESOURCE)
                .set(LinkRelation.SELF, HttpMethod.GET, cellBox);

        // viewport GET.................................................................................................
        final HateosResourceMapping<SpreadsheetViewport, SpreadsheetRange, SpreadsheetRange, HateosResource<SpreadsheetViewport>>
                viewportGet = HateosResourceMapping.with(
                VIEWPORT,
                SpreadsheetEngineHateosHandlersRouter::parseViewport,
                OPTIONAL_RANGE,
                RANGE_RANGE,
                VIEWPORT_HATEOS_RESOURCE)
                .set(LinkRelation.SELF, HttpMethod.GET, computeRange);

        // cell GET, POST...............................................................................................

        HateosResourceMapping<SpreadsheetCellReference,
                SpreadsheetDelta,
                SpreadsheetDelta,
                SpreadsheetCell> cell = HateosResourceMapping.with(CELL,
                SpreadsheetEngineHateosHandlersRouter::parseCellReference,
                OPTIONAL_CELL_REFERENCE,
                RANGE_CELL_REFERENCE,
                SpreadsheetCell.class)
                .set(LinkRelation.SELF, HttpMethod.GET, loadCellComputeIfNecessary)
                .set(LinkRelation.SELF, HttpMethod.POST, saveCell)
                .set(LinkRelation.SELF, HttpMethod.DELETE, deleteCell);

        // cell/SpreadsheetEngineEvaluation GET.........................................................................

        for (SpreadsheetEngineEvaluation evaluation : SpreadsheetEngineEvaluation.values()) {
            final HateosHandler<SpreadsheetCellReference,
                    SpreadsheetDelta,
                    SpreadsheetDelta> loadCell;
            switch (evaluation) {
                case CLEAR_VALUE_ERROR_SKIP_EVALUATE:
                    loadCell = loadCellClearValueErrorSkipEvaluate;
                    break;
                case SKIP_EVALUATE:
                    loadCell = loadCellSkipEvaluate;
                    break;
                case FORCE_RECOMPUTE:
                    loadCell = loadCellForceRecompute;
                    break;
                case COMPUTE_IF_NECESSARY:
                    loadCell = loadCellComputeIfNecessary;
                    break;
                default:
                    NeverError.unhandledEnum(evaluation, SpreadsheetEngineEvaluation.values());
                    loadCell = null;
            }

            cell = cell.set(evaluation.toLinkRelation(),
                    HttpMethod.GET,
                    loadCell);
        }

        // cell/copy POST...............................................................................................
        cell = cell.set(FILL,
                HttpMethod.POST,
                fillCells);

        // columns POST DELETE..........................................................................................

        HateosResourceMapping<SpreadsheetColumnReference,
                SpreadsheetDelta,
                SpreadsheetDelta,
                SpreadsheetColumn> column = HateosResourceMapping.with(COLUMN,
                SpreadsheetEngineHateosHandlersRouter::parseColumn,
                OPTIONAL_COLUMN_REFERENCE,
                RANGE_COLUMN_REFERENCE,
                SpreadsheetColumn.class)
                .set(LinkRelation.SELF, HttpMethod.POST, insertColumns)
                .set(LinkRelation.SELF, HttpMethod.DELETE, deleteColumns);

        // rows POST DELETE.............................................................................................

        HateosResourceMapping<SpreadsheetRowReference,
                SpreadsheetDelta,
                SpreadsheetDelta,
                SpreadsheetRow> row = HateosResourceMapping.with(ROW,
                SpreadsheetEngineHateosHandlersRouter::parseRow,
                OPTIONAL_ROW_REFERENCE,
                RANGE_ROW_REFERENCE,
                SpreadsheetRow.class)
                .set(LinkRelation.SELF, HttpMethod.POST, insertRows)
                .set(LinkRelation.SELF, HttpMethod.DELETE, deleteRows);

        return HateosResourceMapping.router(baseUrl,
                contentType,
                Sets.of(cell, column, row, viewportGet, cellBoxGet));
    }

    private static HateosResourceSelection<SpreadsheetCoordinates> parseCoordinates(final String selection) {
        return HateosResourceSelection.one(SpreadsheetCoordinates.parse(selection));
    }

    private static HateosResourceSelection<SpreadsheetCellReference> parseCellReference(final String selection) {
        HateosResourceSelection<SpreadsheetCellReference> result;

        if (selection.isEmpty()) {
            result = HateosResourceSelection.none();
        } else {
            if (selection.contains(":")) {
                result = HateosResourceSelection.range(SpreadsheetExpressionReference.parseRange(selection).range());
            } else {
                result = HateosResourceSelection.one(SpreadsheetExpressionReference.parseCellReference(selection));
            }
        }

        return result;
    }

    private static HateosResourceSelection<SpreadsheetColumnReference> parseColumn(final String selection) {
        HateosResourceSelection<SpreadsheetColumnReference> result;

        if (selection.contains(":")) {
            result = HateosResourceSelection.range(SpreadsheetColumnReference.parseColumnRange(selection));
        } else {
            result = HateosResourceSelection.one(SpreadsheetColumnReference.parseColumn(selection));
        }

        return result;
    }

    private static HateosResourceSelection<SpreadsheetRowReference> parseRow(final String selection) {
        HateosResourceSelection<SpreadsheetRowReference> result;

        if (selection.contains(":")) {
            result = HateosResourceSelection.range(SpreadsheetRowReference.parseRowRange(selection));
        } else {
            result = HateosResourceSelection.one(SpreadsheetRowReference.parseRow(selection));
        }

        return result;
    }

    private static HateosResourceSelection<SpreadsheetViewport> parseViewport(final String selection) {
        return HateosResourceSelection.one(SpreadsheetViewport.parseViewport(selection));
    }


    /**
     * Stop creation.
     */
    private SpreadsheetEngineHateosHandlersRouter() {
        throw new UnsupportedOperationException();
    }
}
