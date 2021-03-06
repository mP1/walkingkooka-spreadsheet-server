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
import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetCellBox;
import walkingkooka.spreadsheet.SpreadsheetCoordinates;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
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

/**
 * A collection of factory methods to create various {@link HateosHandler}.
 */
public final class SpreadsheetEngineHateosResourceMappings implements PublicStaticHelper {

    // cell................................................................................................................

    public static HateosResourceMapping<SpreadsheetCellReference,
            SpreadsheetDelta,
            SpreadsheetDelta,
            SpreadsheetCell> cell(final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> fillCells,
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellClearValueErrorSkipEvaluate,
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellSkipEvaluate,
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellForceRecompute,
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellComputeIfNecessary,
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> saveCell,
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell) {
        Objects.requireNonNull(fillCells, "fillCells");
        Objects.requireNonNull(loadCellClearValueErrorSkipEvaluate, "loadCellClearValueErrorSkipEvaluate");
        Objects.requireNonNull(loadCellSkipEvaluate, "loadCellSkipEvaluate");
        Objects.requireNonNull(loadCellForceRecompute, "loadCellForceRecompute");
        Objects.requireNonNull(loadCellComputeIfNecessary, "loadCellComputeIfNecessary");
        Objects.requireNonNull(saveCell, "saveCell");
        Objects.requireNonNull(deleteCell, "deleteCell");

        // cell GET, POST...............................................................................................

        HateosResourceMapping<SpreadsheetCellReference,
                SpreadsheetDelta,
                SpreadsheetDelta,
                SpreadsheetCell> cell = HateosResourceMapping.with(CELL,
                SpreadsheetEngineHateosResourceMappings::parseCellReference,
                SpreadsheetDelta.class,
                SpreadsheetDelta.class,
                SpreadsheetCell.class)
                .set(LinkRelation.SELF, HttpMethod.GET, loadCellComputeIfNecessary)
                .set(LinkRelation.SELF, HttpMethod.POST, saveCell)
                .set(LinkRelation.SELF, HttpMethod.DELETE, deleteCell);

        // cell/SpreadsheetEngineEvaluation GET.........................................................................

        for (
                SpreadsheetEngineEvaluation evaluation : SpreadsheetEngineEvaluation.values()) {
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
        return cell.set(FILL,
                HttpMethod.POST,
                fillCells);
    }

    /**
     * A {@link HateosResourceName} with <code>cell</code>.
     */
    private static final HateosResourceName CELL = HateosResourceName.with("cell");

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

    /**
     * A {@link LinkRelation} with <code>fill</code>.
     */
    private static final LinkRelation<?> FILL = LinkRelation.with("fill");

    // cellBox...........................................................................................................

    public static HateosResourceMapping<SpreadsheetCoordinates, SpreadsheetCellBox, SpreadsheetCellBox, HateosResource<SpreadsheetCoordinates>> cellBox(final HateosHandler<SpreadsheetCoordinates, SpreadsheetCellBox, SpreadsheetCellBox> handler) {
        return HateosResourceMapping.with(
                COORDS,
                SpreadsheetEngineHateosResourceMappings::parseCoordinates,
                SpreadsheetCellBox.class,
                SpreadsheetCellBox.class,
                COORDS_HATEOS_RESOURCE)
                .set(LinkRelation.SELF, HttpMethod.GET, handler);
    }

    /**
     * A {@link HateosResourceName} with <code>cellbox</code>.
     */
    private static final HateosResourceName COORDS = HateosResourceName.with("cellbox");

    private static HateosResourceSelection<SpreadsheetCoordinates> parseCoordinates(final String selection) {
        return HateosResourceSelection.one(SpreadsheetCoordinates.parse(selection));
    }

    private static final Class<HateosResource<SpreadsheetCoordinates>> COORDS_HATEOS_RESOURCE = Cast.to(HateosResource.class);

    // column...........................................................................................................

    public static HateosResourceMapping<SpreadsheetColumnReference,
            SpreadsheetDelta,
            SpreadsheetDelta,
            SpreadsheetColumn> column(final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> deleteColumns,
                                      final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertColumns) {
        return HateosResourceMapping.with(COLUMN,
                SpreadsheetEngineHateosResourceMappings::parseColumn,
                SpreadsheetDelta.class,
                SpreadsheetDelta.class,
                SpreadsheetColumn.class)
                .set(LinkRelation.SELF, HttpMethod.POST, insertColumns)
                .set(LinkRelation.SELF, HttpMethod.DELETE, deleteColumns);
    }

    /**
     * A {@link HateosResourceName} with <code>column</code>.
     */
    private static final HateosResourceName COLUMN = HateosResourceName.with("column");

    private static HateosResourceSelection<SpreadsheetColumnReference> parseColumn(final String selection) {
        HateosResourceSelection<SpreadsheetColumnReference> result;

        if (selection.contains(":")) {
            result = HateosResourceSelection.range(SpreadsheetColumnReference.parseColumnRange(selection));
        } else {
            result = HateosResourceSelection.one(SpreadsheetColumnReference.parseColumn(selection));
        }

        return result;
    }

    // row..............................................................................................................

    public static HateosResourceMapping<SpreadsheetRowReference,
            SpreadsheetDelta,
            SpreadsheetDelta,
            SpreadsheetRow> row(final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> deleteRows,
                                final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertRows) {
        return HateosResourceMapping.with(ROW,
                SpreadsheetEngineHateosResourceMappings::parseRow,
                SpreadsheetDelta.class,
                SpreadsheetDelta.class,
                SpreadsheetRow.class)
                .set(LinkRelation.SELF, HttpMethod.POST, insertRows)
                .set(LinkRelation.SELF, HttpMethod.DELETE, deleteRows);
    }

    /**
     * A {@link HateosResourceName} with <code>row</code>.
     */
    private static final HateosResourceName ROW = HateosResourceName.with("row");

    private static HateosResourceSelection<SpreadsheetRowReference> parseRow(final String selection) {
        HateosResourceSelection<SpreadsheetRowReference> result;

        if (selection.contains(":")) {
            result = HateosResourceSelection.range(SpreadsheetRowReference.parseRowRange(selection));
        } else {
            result = HateosResourceSelection.one(SpreadsheetRowReference.parseRow(selection));
        }

        return result;
    }

    // viewport .........................................................................................................

    public static HateosResourceMapping<SpreadsheetViewport, SpreadsheetRange, SpreadsheetRange, HateosResource<SpreadsheetViewport>> viewport(final HateosHandler<SpreadsheetViewport,
            SpreadsheetRange,
            SpreadsheetRange> handler) {
        return HateosResourceMapping.with(
                VIEWPORT,
                SpreadsheetEngineHateosResourceMappings::parseViewport,
                SpreadsheetRange.class,
                SpreadsheetRange.class,
                VIEWPORT_HATEOS_RESOURCE)
                .set(LinkRelation.SELF, HttpMethod.GET, handler);
    }

    /**
     * A {@link HateosResourceName} with <code>viewport</code>.
     */
    private static final HateosResourceName VIEWPORT = HateosResourceName.with("viewport");

    private static HateosResourceSelection<SpreadsheetViewport> parseViewport(final String selection) {
        return HateosResourceSelection.one(SpreadsheetViewport.parseViewport(selection));
    }

    private static final Class<HateosResource<SpreadsheetViewport>> VIEWPORT_HATEOS_RESOURCE = Cast.to(HateosResource.class);

    /**
     * Stop creation.
     */
    private SpreadsheetEngineHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
