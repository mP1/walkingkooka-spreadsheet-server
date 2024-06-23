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
package walkingkooka.spreadsheet.server.engine;

import walkingkooka.NeverError;
import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceHandlers;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.format.SpreadsheetParserInfo;
import walkingkooka.spreadsheet.format.SpreadsheetParserInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetParserName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRowRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.text.CharSequences;

import java.util.Objects;
import java.util.function.Function;

/**
 * A collection of factory methods to create various {@link HateosResourceHandler}.
 */
public final class SpreadsheetEngineHateosResourceMappings implements PublicStaticHelper {

    // cell................................................................................................................

    public static HateosResourceMapping<SpreadsheetCellReference,
            SpreadsheetDelta,
            SpreadsheetDelta,
            SpreadsheetCell> cell(final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> fillCells,
                                  final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> findCells,
                                  final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellClearValueErrorSkipEvaluate,
                                  final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellSkipEvaluate,
                                  final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellForceRecompute,
                                  final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellComputeIfNecessary,
                                  final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> saveCell,
                                  final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell,
                                  final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> sortCells,
                                  final Function<SpreadsheetLabelName, SpreadsheetCellReference> labelToCellReference) {
        Objects.requireNonNull(fillCells, "fillCells");
        Objects.requireNonNull(findCells, "findCells");
        Objects.requireNonNull(loadCellClearValueErrorSkipEvaluate, "loadCellClearValueErrorSkipEvaluate");
        Objects.requireNonNull(loadCellSkipEvaluate, "loadCellSkipEvaluate");
        Objects.requireNonNull(loadCellForceRecompute, "loadCellForceRecompute");
        Objects.requireNonNull(loadCellComputeIfNecessary, "loadCellComputeIfNecessary");
        Objects.requireNonNull(saveCell, "saveCell");
        Objects.requireNonNull(deleteCell, "deleteCell");
        Objects.requireNonNull(sortCells, "sortCells");
        Objects.requireNonNull(labelToCellReference, "labelToCellReference");

        // cell GET, POST...............................................................................................

        HateosResourceMapping<SpreadsheetCellReference,
                SpreadsheetDelta,
                SpreadsheetDelta,
                SpreadsheetCell> cell = HateosResourceMapping.with(
                        CELL,
                        (t) -> parseSelectionAndResolveLabels(t, labelToCellReference),
                        SpreadsheetDelta.class,
                        SpreadsheetDelta.class,
                        SpreadsheetCell.class
                )
                .set(LinkRelation.SELF, HttpMethod.GET, loadCellComputeIfNecessary)
                .set(LinkRelation.SELF, HttpMethod.POST, saveCell)
                .set(LinkRelation.SELF, HttpMethod.DELETE, deleteCell)
                .set(LinkRelation.SELF, HttpMethod.PATCH, HateosResourceHandlers.fake());

        // cell/SpreadsheetEngineEvaluation GET.........................................................................

        for (final SpreadsheetEngineEvaluation evaluation : SpreadsheetEngineEvaluation.values()) {
            final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCell;

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
                    loadCell = NeverError.unhandledEnum(evaluation, SpreadsheetEngineEvaluation.values());
                    break;
            }

            cell = cell.set(evaluation.toLinkRelation(),
                    HttpMethod.GET,
                    loadCell);
        }

        // cell/fill/find/sort POST...............................................................................................
        cell = cell.set(
                FILL,
                HttpMethod.POST,
                fillCells
        );

        cell = cell.set(
                FIND,
                HttpMethod.GET,
                findCells
        );

        cell = cell.set(
                SORT,
                HttpMethod.GET,
                sortCells
        );

        return cell;
    }

    /**
     * A {@link HateosResourceName} with <code>cell</code>.
     */
    private static final HateosResourceName CELL = HateosResourceName.with("cell");

    /**
     * Handles parsing just a cell or label or a range with either, always resolving labels to cells. Any returned
     * {@link walkingkooka.collect.Range} will only have {@link SpreadsheetCellReference}.
     */
    private static HateosResourceSelection<SpreadsheetCellReference> parseSelectionAndResolveLabels(final String selection,
                                                                                                    final Function<SpreadsheetLabelName, SpreadsheetCellReference> labelToCellReference) {
        final HateosResourceSelection<SpreadsheetCellReference> result;

        if (selection.isEmpty()) {
            result = HateosResourceSelection.none();
        } else {
            if ("*".equals(selection)) {
                result = HateosResourceSelection.all();
            } else {
                final int separator = selection.indexOf(SpreadsheetSelection.SEPARATOR.character());
                switch (separator) {
                    case -1:
                        result = HateosResourceSelection.one(
                                parseSelectionAndResolveLabels0(
                                        selection,
                                        labelToCellReference
                                )
                        );
                        break;
                    case 0:
                        throw new IllegalArgumentException("Missing begin");
                    default:
                        final SpreadsheetCellReference begin = parseSelectionAndResolveLabels0(
                                selection.substring(0, separator),
                                labelToCellReference
                        );

                        if (separator + 1 == selection.length()) {
                            throw new IllegalArgumentException("Missing end");
                        }
                        final SpreadsheetCellReference end = parseSelectionAndResolveLabels0(
                                selection.substring(separator + 1),
                                labelToCellReference
                        );
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
    private static SpreadsheetCellReference parseSelectionAndResolveLabels0(final String cellOrLabelText,
                                                                            final Function<SpreadsheetLabelName, SpreadsheetCellReference> labelToCellReference) {
        return ParseCellOrLabelAndResolveLabelsFunction.with(labelToCellReference)
                .apply(cellOrLabelText);
    }

    /**
     * A {@link LinkRelation} with <code>clear</code>.
     */
    private static final LinkRelation<?> CLEAR = LinkRelation.with("clear");

    /**
     * A {@link LinkRelation} with <code>fill</code>.
     */
    private static final LinkRelation<?> FILL = LinkRelation.with("fill");

    /**
     * A {@link LinkRelation} with <code>find</code>.
     */
    private static final LinkRelation<?> FIND = LinkRelation.with("find");

    /**
     * A {@link LinkRelation} with <code>sort</code>.
     */
    private static final LinkRelation<?> SORT = LinkRelation.with("sort");

    // cellReference....................................................................................................

    public static HateosResourceMapping<String,
            SpreadsheetExpressionReferenceSimilarities,
            SpreadsheetExpressionReferenceSimilarities,
            SpreadsheetExpressionReferenceSimilarities> cellReference(final HateosResourceHandler<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> cellReference) {

        return HateosResourceMapping.with(
                CELL_REFERENCE,
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
            SpreadsheetColumn> column(final HateosResourceHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> clearColumns,
                                      final HateosResourceHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> deleteColumns,
                                      final HateosResourceHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertAfterColumns,
                                      final HateosResourceHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertBeforeColumns) {
        return HateosResourceMapping.with(
                        COLUMN,
                        SpreadsheetEngineHateosResourceMappings::parseColumn,
                        SpreadsheetDelta.class,
                        SpreadsheetDelta.class,
                        SpreadsheetColumn.class
                )
                .set(CLEAR, HttpMethod.POST, clearColumns)
                .set(AFTER, HttpMethod.POST, insertAfterColumns)
                .set(BEFORE, HttpMethod.POST, insertBeforeColumns)
                .set(LinkRelation.SELF, HttpMethod.DELETE, deleteColumns);
    }

    /**
     * A {@link HateosResourceName} with <code>column</code>.
     */
    private static final HateosResourceName COLUMN = HateosResourceName.with("column");

    private static HateosResourceSelection<SpreadsheetColumnReference> parseColumn(final String selection) {
        final SpreadsheetColumnRangeReference parsed = SpreadsheetSelection.parseColumnRange(selection);

        return parsed.isSingle() ?
                HateosResourceSelection.one(parsed.begin()) :
                HateosResourceSelection.range(parsed.range());
    }

    // parser...........................................................................................................

    public static HateosResourceMapping<SpreadsheetParserName,
            SpreadsheetParserInfo,
            SpreadsheetParserInfoSet,
            SpreadsheetParserInfo> parser(final HateosResourceHandler<SpreadsheetParserName, SpreadsheetParserInfo, SpreadsheetParserInfoSet> loadSpreadsheetParsers) {
        Objects.requireNonNull(loadSpreadsheetParsers, "loadSpreadsheetParsers");

        // GET /parser..................................................................................................

        HateosResourceMapping<SpreadsheetParserName, SpreadsheetParserInfo, SpreadsheetParserInfoSet,
                SpreadsheetParserInfo> parser = HateosResourceMapping.with(
                        PARSER,
                        SpreadsheetEngineHateosResourceMappings::parseParserSelection,
                        SpreadsheetParserInfo.class, // valueType
                        SpreadsheetParserInfoSet.class, // collectionType
                        SpreadsheetParserInfo.class// resourceType
                )
                .set(LinkRelation.SELF, HttpMethod.GET, loadSpreadsheetParsers);

        return parser;
    }

    private static HateosResourceSelection<SpreadsheetParserName> parseParserSelection(final String text) {
        final HateosResourceSelection<SpreadsheetParserName> selection;

        switch (text) {
            case "":
                selection = HateosResourceSelection.all();
                break;
            case "*":
                throw new IllegalArgumentException("Invalid parser selection " + CharSequences.quoteAndEscape(text));
            default:
                selection = HateosResourceSelection.one(
                        SpreadsheetParserName.with(text)
                );
                break;
        }

        return selection;
    }

    /**
     * A {@link HateosResourceName} with <code>parser</code>.
     */
    private static final HateosResourceName PARSER = HateosResourceName.with("parser");
    
    // row..............................................................................................................

    public static HateosResourceMapping<SpreadsheetRowReference,
            SpreadsheetDelta,
            SpreadsheetDelta,
            SpreadsheetRow> row(final HateosResourceHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> clearRows,
                                final HateosResourceHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> deleteRows,
                                final HateosResourceHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertAfterRows,
                                final HateosResourceHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertBeforeRows) {
        return HateosResourceMapping.with(
                        ROW,
                        SpreadsheetEngineHateosResourceMappings::parseRow,
                        SpreadsheetDelta.class,
                        SpreadsheetDelta.class,
                        SpreadsheetRow.class
                )
                .set(CLEAR, HttpMethod.POST, clearRows)
                .set(AFTER, HttpMethod.POST, insertAfterRows)
                .set(BEFORE, HttpMethod.POST, insertBeforeRows)
                .set(LinkRelation.SELF, HttpMethod.DELETE, deleteRows);
    }

    /**
     * A {@link HateosResourceName} with <code>row</code>.
     */
    private static final HateosResourceName ROW = HateosResourceName.with("row");

    private static HateosResourceSelection<SpreadsheetRowReference> parseRow(final String selection) {
        final SpreadsheetRowRangeReference parsed = SpreadsheetSelection.parseRowRange(selection);

        return parsed.isSingle() ?
                HateosResourceSelection.one(parsed.begin()) :
                HateosResourceSelection.range(parsed.range());
    }

    private final static LinkRelation<?> AFTER = LinkRelation.with("after");
    private final static LinkRelation<?> BEFORE = LinkRelation.with("before");

    /**
     * Stop creation.
     */
    private SpreadsheetEngineHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
