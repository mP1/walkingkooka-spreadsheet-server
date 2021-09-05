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

import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.text.CharSequences;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An abstract {@link HateosHandler} that includes uses a {@link SpreadsheetEngine} and {@link SpreadsheetEngineContext} to do things.
 */
abstract class SpreadsheetEngineHateosHandler<I extends Comparable<I>, V, C> implements HateosHandler<I, V, C> {

    /**
     * Holds the type of the selection parameter. This is necessary due to ambiguities between column and labels.
     */
    final static UrlParameterName SELECTION_TYPE = UrlParameterName.with("selectionType");

    /**
     * The {@link SpreadsheetSelection} in text form, eg "A" for column, "B2" for cell, "C:D" for column range etc.
     */
    final static UrlParameterName SELECTION = UrlParameterName.with("selection");

    /**
     * Returns the selection from the request parameters if one was present.
     */
    static Optional<SpreadsheetSelection> selection(final Map<HttpRequestAttribute<?>, Object> parameters) {
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

        return Optional.ofNullable(selection);
    }

    /**
     * Checks required factory method parameters are not null.
     */
    static void check(final SpreadsheetEngine engine,
                      final SpreadsheetEngineContext context) {
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(context, "context");
    }

    /**
     * Package private to limit sub classing.
     */
    SpreadsheetEngineHateosHandler(final SpreadsheetEngine engine,
                                   final SpreadsheetEngineContext context) {
        super();
        this.engine = engine;
        this.context = context;
    }

    @Override
    public final Optional<C> handleList(final List<I> list,
                                        final Optional<C> resource,
                                        final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkList(list);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public final Optional<V> handleNone(final Optional<V> resource,
                                        final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    final SpreadsheetEngine engine;
    final SpreadsheetEngineContext context;

    static void checkWithoutCells(final Optional<SpreadsheetDelta> delta) {
        delta.ifPresent(SpreadsheetEngineHateosHandler::checkWithoutCells0);
    }

    private static void checkWithoutCells0(final SpreadsheetDelta delta) {
        if (!delta.cells().isEmpty()) {
            throw new IllegalArgumentException("Expected delta without cells: " + delta);
        }
    }
}
