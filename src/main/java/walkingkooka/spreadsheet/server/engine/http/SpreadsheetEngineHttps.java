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

import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetViewport;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelectionAnchor;
import walkingkooka.text.CharSequences;
import walkingkooka.tree.json.JsonNode;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * A collection of factory methods to create various {@link HateosHandler}.
 */
public final class SpreadsheetEngineHttps implements PublicStaticHelper {

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearCells}
     */
    public static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> clearCells(final SpreadsheetEngine engine,
                                                                                                         final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearCells.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns}
     */
    public static HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> clearColumns(final SpreadsheetEngine engine,
                                                                                                             final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows}
     */
    public static HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> clearRows(final SpreadsheetEngine engine,
                                                                                                       final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteColumns}
     */
    public static HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> deleteColumns(final SpreadsheetEngine engine,
                                                                                                              final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteColumns.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteRows}
     */
    public static HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> deleteRows(final SpreadsheetEngine engine,
                                                                                                        final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteRows.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaFillCells}
     */
    public static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> fillCells(final SpreadsheetEngine engine,
                                                                                                        final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaFillCells.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities}
     */
    public static HateosHandler<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> findSimilarities(final SpreadsheetEngine engine,
                                                                                                                                                 final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertAfterColumn}
     */
    public static HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertAfterColumns(final SpreadsheetEngine engine,
                                                                                                                   final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertAfterColumn.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertAfterRow}
     */
    public static HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertAfterRows(final SpreadsheetEngine engine,
                                                                                                             final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertAfterRow.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn}
     */
    public static HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertBeforeColumns(final SpreadsheetEngine engine,
                                                                                                                    final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow}
     */
    public static HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertBeforeRows(final SpreadsheetEngine engine,
                                                                                                              final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow.with(engine, context);
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
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetCellRangeRange}
     */
    public static HateosHandler<SpreadsheetViewport, SpreadsheetCellRange, SpreadsheetCellRange> range(final SpreadsheetEngine engine,
                                                                                                       final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetCellRangeRange.with(engine, context);
    }

    /**
     * {@see SpreadsheetEngineSpreadsheetCellPatchFunction}
     */
    public static UnaryOperator<JsonNode> patchCell(final HttpRequest request,
                                                    final SpreadsheetEngine engine,
                                                    final SpreadsheetEngineContext context) {
        return SpreadsheetEngineSpreadsheetCellPatchFunction.with(
                request,
                engine,
                context
        );
    }

    /**
     * Checks the given {@link SpreadsheetDelta} and if selection is absent then checks the selection query parameter.
     */
    static Optional<SpreadsheetViewportSelection> viewportSelection(final Optional<SpreadsheetDelta> input,
                                                                    final Map<HttpRequestAttribute<?>, Object> parameters) {
        Optional<SpreadsheetViewportSelection> viewportSelection = input.isPresent() ?
                input.get().selection() :
                Optional.empty();
        if (!viewportSelection.isPresent()) {
            viewportSelection = viewportSelection(parameters);
        }

        return viewportSelection;
    }

    static Optional<SpreadsheetViewportSelection> viewportSelection(final Map<HttpRequestAttribute<?>, Object> parameters) {
        final SpreadsheetSelection selection = selectionOrNull(parameters);
        final Optional<SpreadsheetViewportSelectionAnchor> anchor = anchor(parameters);
        return Optional.ofNullable(
                null != selection ?
                        selection.setAnchor(anchor.isPresent() ? anchor : selection.defaultAnchor()) :
                        null
        );
    }

    /**
     * Returns the selection from the request parameters if one was present.
     */
    static Optional<SpreadsheetSelection> selection(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return Optional.ofNullable(
                selectionOrNull(parameters)
        );
    }

    static SpreadsheetSelection selectionOrNull(final Map<HttpRequestAttribute<?>, Object> parameters) {
        final SpreadsheetSelection selection;

        final Optional<String> maybeSelectionType = SELECTION_TYPE.firstParameterValue(parameters);
        if (maybeSelectionType.isPresent()) {
            final String selectionType = maybeSelectionType.get();
            final String selectionText = SELECTION.firstParameterValueOrFail(parameters);

            switch (selectionType) {
                case "cell":
                    selection = SpreadsheetSelection.parseCell(selectionText);
                    break;
                case "cell-range":
                    selection = SpreadsheetSelection.parseCellRange(selectionText);
                    break;
                case "column":
                    selection = SpreadsheetSelection.parseColumn(selectionText);
                    break;
                case "column-range":
                    selection = SpreadsheetSelection.parseColumnRange(selectionText);
                    break;
                case "label":
                    selection = SpreadsheetSelection.labelName(selectionText);
                    break;
                case "row":
                    selection = SpreadsheetSelection.parseRow(selectionText);
                    break;
                case "row-range":
                    selection = SpreadsheetSelection.parseRowRange(selectionText);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Invalid parameter " +
                                    CharSequences.quoteAndEscape(SELECTION_TYPE.toString()) +
                                    " value " +
                                    CharSequences.quoteAndEscape(selectionText)
                    );
            }
        } else {
            selection = null;
        }

        return selection;
    }

    /**
     * Holds the type of the selection parameter. This is necessary due to ambiguities between column and labels.
     */
    // @VisibleForTesting
    final static UrlParameterName SELECTION_TYPE = UrlParameterName.with("selectionType");

    /**
     * The {@link SpreadsheetSelection} in text form, eg "A" for column, "B2" for cell, "C:D" for column range etc.
     */
    // @VisibleForTesting
    final static UrlParameterName SELECTION = UrlParameterName.with("selection");

    private static Optional<SpreadsheetViewportSelectionAnchor> anchor(final Map<HttpRequestAttribute<?>, Object> parameters) {
        SpreadsheetViewportSelectionAnchor anchor;
        final Optional<String> maybeAnchor = SELECTION_ANCHOR.firstParameterValue(parameters);
        if (maybeAnchor.isPresent()) {
            anchor = SpreadsheetViewportSelectionAnchor.valueOf(maybeAnchor.get());
        } else {
            anchor = null;
        }
        return Optional.ofNullable(anchor);
    }

    /**
     * The {@link SpreadsheetViewportSelectionAnchor} in text form, eg "TOP_BOTTOM"
     */
    // @VisibleForTesting
    final static UrlParameterName SELECTION_ANCHOR = UrlParameterName.with("selectionAnchor");

    /**
     * Stop creation.
     */
    private SpreadsheetEngineHttps() {
        throw new UnsupportedOperationException();
    }
}
