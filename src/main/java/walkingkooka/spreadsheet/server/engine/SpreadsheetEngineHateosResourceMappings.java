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
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.net.http.server.hateos.HateosHandlers;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfoSet;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
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
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;

import java.util.Objects;
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
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> findCells,
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellClearValueErrorSkipEvaluate,
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellSkipEvaluate,
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellForceRecompute,
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellComputeIfNecessary,
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> saveCell,
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell,
                                  final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> sortCells,
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
                .set(LinkRelation.SELF, HttpMethod.PATCH, HateosHandlers.fake());

        // cell/SpreadsheetEngineEvaluation GET.........................................................................

        for (final SpreadsheetEngineEvaluation evaluation : SpreadsheetEngineEvaluation.values()) {
            final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCell;

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
            SpreadsheetExpressionReferenceSimilarities> cellReference(final HateosHandler<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> cellReference) {

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
            SpreadsheetColumn> column(final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> clearColumns,
                                      final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> deleteColumns,
                                      final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertAfterColumns,
                                      final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertBeforeColumns) {
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

    // comparator.......................................................................................................

    public static HateosResourceMapping<SpreadsheetComparatorName,
            SpreadsheetComparatorInfo,
            SpreadsheetComparatorInfoSet,
            SpreadsheetComparatorInfo> comparator(final HateosHandler<SpreadsheetComparatorName, SpreadsheetComparatorInfo, SpreadsheetComparatorInfoSet> loadSpreadsheetComparators) {
        Objects.requireNonNull(loadSpreadsheetComparators, "loadSpreadsheetComparators");

        // comparator GET...............................................................................................

        HateosResourceMapping<SpreadsheetComparatorName, SpreadsheetComparatorInfo, SpreadsheetComparatorInfoSet,
                SpreadsheetComparatorInfo> comparator = HateosResourceMapping.with(
                        COMPARATOR,
                        SpreadsheetEngineHateosResourceMappings::parseComparatorSelection,
                        SpreadsheetComparatorInfo.class, // valueType
                        SpreadsheetComparatorInfoSet.class, // collectionType
                        SpreadsheetComparatorInfo.class// resourceType
                )
                .set(LinkRelation.SELF, HttpMethod.GET, loadSpreadsheetComparators);

        return comparator;
    }

    private static HateosResourceSelection<SpreadsheetComparatorName> parseComparatorSelection(final String text) {
        final HateosResourceSelection<SpreadsheetComparatorName> selection;

        switch (text.length()) {
            case 0:
                selection = HateosResourceSelection.all();
                break;
            default:
                selection = HateosResourceSelection.one(
                        SpreadsheetComparatorName.with(text)
                );
                break;
        }

        return selection;
    }

    /**
     * A {@link HateosResourceName} with <code>comparator</code>.
     */
    private static final HateosResourceName COMPARATOR = HateosResourceName.with("comparator");

    // formatter.......................................................................................................

    public static HateosResourceMapping<SpreadsheetFormatterName,
            SpreadsheetFormatterInfo,
            SpreadsheetFormatterInfoSet,
            SpreadsheetFormatterInfo> formatter(final HateosHandler<SpreadsheetFormatterName, SpreadsheetFormatterInfo, SpreadsheetFormatterInfoSet> loadSpreadsheetFormatters) {
        Objects.requireNonNull(loadSpreadsheetFormatters, "loadSpreadsheetFormatters");

        // formatter GET...............................................................................................

        HateosResourceMapping<SpreadsheetFormatterName, SpreadsheetFormatterInfo, SpreadsheetFormatterInfoSet,
                SpreadsheetFormatterInfo> formatter = HateosResourceMapping.with(
                        FORMATTER,
                        SpreadsheetEngineHateosResourceMappings::parseFormatterSelection,
                        SpreadsheetFormatterInfo.class, // valueType
                        SpreadsheetFormatterInfoSet.class, // collectionType
                        SpreadsheetFormatterInfo.class// resourceType
                )
                .set(LinkRelation.SELF, HttpMethod.GET, loadSpreadsheetFormatters);

        return formatter;
    }

    private static HateosResourceSelection<SpreadsheetFormatterName> parseFormatterSelection(final String text) {
        final HateosResourceSelection<SpreadsheetFormatterName> selection;

        switch (text.length()) {
            case 0:
                selection = HateosResourceSelection.all();
                break;
            default:
                throw new IllegalArgumentException("Invalid formatter selection " + CharSequences.quoteAndEscape(text));
        }

        return selection;
    }

    /**
     * A {@link HateosResourceName} with <code>formatter</code>.
     */
    private static final HateosResourceName FORMATTER = HateosResourceName.with("formatter");

    // expression-function..............................................................................................

    public static HateosResourceMapping<FunctionExpressionName,
            ExpressionFunctionInfo,
            ExpressionFunctionInfoSet,
            ExpressionFunctionInfo> expressionFunction(final HateosHandler<FunctionExpressionName, ExpressionFunctionInfo, ExpressionFunctionInfoSet> loadSpreadsheetExpressionFunctions) {
        Objects.requireNonNull(loadSpreadsheetExpressionFunctions, "loadSpreadsheetExpressionFunctions");

        // function GET...............................................................................................

        HateosResourceMapping<FunctionExpressionName, ExpressionFunctionInfo, ExpressionFunctionInfoSet,
                ExpressionFunctionInfo> function = HateosResourceMapping.with(
                        FUNCTION,
                        SpreadsheetEngineHateosResourceMappings::parseFunctionSelection,
                        ExpressionFunctionInfo.class, // valueType
                        ExpressionFunctionInfoSet.class, // collectionType
                        ExpressionFunctionInfo.class// resourceType
                )
                .set(LinkRelation.SELF, HttpMethod.GET, loadSpreadsheetExpressionFunctions);

        return function;
    }

    private static HateosResourceSelection<FunctionExpressionName> parseFunctionSelection(final String text) {
        final HateosResourceSelection<FunctionExpressionName> selection;

        switch (text.length()) {
            case 0:
                selection = HateosResourceSelection.all();
                break;
            default:
                throw new IllegalArgumentException("Invalid selection " + CharSequences.quoteAndEscape(text));
        }

        return selection;
    }

    /**
     * A {@link HateosResourceName} with <code>expression-function</code>.
     */
    private static final HateosResourceName FUNCTION = HateosResourceName.with("expression-function");

    // parser...........................................................................................................

    public static HateosResourceMapping<SpreadsheetParserName,
            SpreadsheetParserInfo,
            SpreadsheetParserInfoSet,
            SpreadsheetParserInfo> parser(final HateosHandler<SpreadsheetParserName, SpreadsheetParserInfo, SpreadsheetParserInfoSet> loadSpreadsheetParsers) {
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

        switch (text.length()) {
            case 0:
                selection = HateosResourceSelection.all();
                break;
            default:
                throw new IllegalArgumentException("Invalid parser selection " + CharSequences.quoteAndEscape(text));
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
            SpreadsheetRow> row(final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> clearRows,
                                final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> deleteRows,
                                final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertAfterRows,
                                final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertBeforeRows) {
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
