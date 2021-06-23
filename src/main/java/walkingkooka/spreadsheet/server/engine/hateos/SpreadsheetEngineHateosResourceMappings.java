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
import walkingkooka.spreadsheet.SpreadsheetViewport;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReferenceOrLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetColumn;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;
import walkingkooka.spreadsheet.reference.SpreadsheetRow;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.text.CharSequences;
import walkingkooka.tree.expression.ExpressionReference;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

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
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell,
                                  final Function<SpreadsheetLabelName, Optional<SpreadsheetCellReference>> labelToCellReference) {
        Objects.requireNonNull(fillCells, "fillCells");
        Objects.requireNonNull(loadCellClearValueErrorSkipEvaluate, "loadCellClearValueErrorSkipEvaluate");
        Objects.requireNonNull(loadCellSkipEvaluate, "loadCellSkipEvaluate");
        Objects.requireNonNull(loadCellForceRecompute, "loadCellForceRecompute");
        Objects.requireNonNull(loadCellComputeIfNecessary, "loadCellComputeIfNecessary");
        Objects.requireNonNull(saveCell, "saveCell");
        Objects.requireNonNull(deleteCell, "deleteCell");
        Objects.requireNonNull(labelToCellReference, "labelToCellReference");

        // cell GET, POST...............................................................................................

        HateosResourceMapping<SpreadsheetCellReference,
                SpreadsheetDelta,
                SpreadsheetDelta,
                SpreadsheetCell> cell = HateosResourceMapping.with(CELL,
                (t) -> SpreadsheetEngineHateosResourceMappings.parseCellReferenceOrLabel(t, labelToCellReference),
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

    /**
     * Handles parsing just a cell or label or a range with either, always resolving labels to cells. Any returned
     * {@link walkingkooka.collect.Range} will only have {@link SpreadsheetCellReference}.
     */
    private static HateosResourceSelection<SpreadsheetCellReference> parseCellReferenceOrLabel(final String selection,
                                                                                               final Function<SpreadsheetLabelName, Optional<SpreadsheetCellReference>> labelToCellReference) {
        final HateosResourceSelection<SpreadsheetCellReference> result;

        if (selection.isEmpty()) {
            result = HateosResourceSelection.none();
        } else {
            if("*".equals(selection)) {
                result = HateosResourceSelection.all();
            } else {
                final int colon = selection.indexOf(':');
                switch (colon) {
                    case -1:
                        result = HateosResourceSelection.one(parseCellReferenceOrLabel0(selection, labelToCellReference));
                        break;
                    case 0:
                        throw new IllegalArgumentException("Missing begin");
                    default:
                        final SpreadsheetCellReference begin = parseCellReferenceOrLabel0(selection.substring(0, colon), labelToCellReference);

                        if (colon + 1 == selection.length()) {
                            throw new IllegalArgumentException("Missing end");
                        }
                        final SpreadsheetCellReference end = parseCellReferenceOrLabel0(selection.substring(colon + 1), labelToCellReference);
                        result = HateosResourceSelection.range(begin.range(end));
                        break;
                }
            }
        }

        return result;
    }

    /**
     * Parses the given text as either a cell reference or label name, if the later it is resolved to a {@link SpreadsheetCellReference}.
     */
    private static SpreadsheetCellReference parseCellReferenceOrLabel0(final String cellOrLabelText,
                                                                       final Function<SpreadsheetLabelName, Optional<SpreadsheetCellReference>> labelToCellReference) {
        final SpreadsheetCellReferenceOrLabelName<?> cellOrLabel = SpreadsheetExpressionReference.parseCellReferenceOrLabelName(cellOrLabelText);
        return cellOrLabel instanceof SpreadsheetLabelName ?
                labelToCellReference.apply((SpreadsheetLabelName) cellOrLabel).orElseThrow(() -> new IllegalArgumentException("Unknown label " + CharSequences.quote(cellOrLabelText))) :
                (SpreadsheetCellReference) cellOrLabel;
    }

    /**
     * A {@link LinkRelation} with <code>fill</code>.
     */
    private static final LinkRelation<?> FILL = LinkRelation.with("fill");
    
    // cellReference....................................................................................................

    public static HateosResourceMapping<String,
            SpreadsheetExpressionReferenceSimilarities,
            SpreadsheetExpressionReferenceSimilarities,
            SpreadsheetExpressionReferenceSimilarities> cellReference(final HateosHandler<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> cellReference) {
        return HateosResourceMapping.with(CELL_REFERENCE,
                SpreadsheetEngineHateosResourceMappings::parseCellReferenceText,
                SpreadsheetExpressionReferenceSimilarities.class,
                SpreadsheetExpressionReferenceSimilarities.class,
                SpreadsheetExpressionReferenceSimilarities.class
        ).set(
                LinkRelation.SELF,
                HttpMethod.GET,
                cellReference
        );
    }

    /**
     * A {@link HateosResourceName} with <code>cell-reference</code>.
     */
    private static final HateosResourceName CELL_REFERENCE = HateosResourceName.with("cell-reference");

    private static HateosResourceSelection<String> parseCellReferenceText(final String text) {
        return HateosResourceSelection.one(text);
    }

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

    // range .........................................................................................................

    public static HateosResourceMapping<SpreadsheetViewport, SpreadsheetRange, SpreadsheetRange, HateosResource<SpreadsheetViewport>> range(final HateosHandler<SpreadsheetViewport,
            SpreadsheetRange,
            SpreadsheetRange> handler) {
        return HateosResourceMapping.with(
                RANGE,
                SpreadsheetEngineHateosResourceMappings::parseViewport,
                SpreadsheetRange.class,
                SpreadsheetRange.class,
                RANGE_HATEOS_RESOURCE)
                .set(LinkRelation.SELF, HttpMethod.GET, handler);
    }

    /**
     * A {@link HateosResourceName} with <code>viewport</code>.
     */
    private static final HateosResourceName RANGE = HateosResourceName.with("range");

    private static HateosResourceSelection<SpreadsheetViewport> parseViewport(final String selection) {
        return HateosResourceSelection.one(SpreadsheetViewport.parse(selection));
    }

    private static final Class<HateosResource<SpreadsheetViewport>> RANGE_HATEOS_RESOURCE = Cast.to(HateosResource.class);

    /**
     * {@see SpreadsheetEngineHateosResourceMappingsSpreadsheetExpressionReferenceVisitor}
     */
    public static Optional<SpreadsheetCellReference> reference(final ExpressionReference reference,
                                                               final SpreadsheetLabelStore store) {
        return SpreadsheetEngineHateosResourceMappingsSpreadsheetExpressionReferenceVisitor.reference(reference, store);
    }

    /**
     * Stop creation.
     */
    private SpreadsheetEngineHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
