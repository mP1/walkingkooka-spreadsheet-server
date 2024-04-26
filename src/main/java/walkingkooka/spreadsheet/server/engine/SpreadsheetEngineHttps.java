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

import walkingkooka.build.MissingBuilder;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetValueType;
import walkingkooka.spreadsheet.SpreadsheetViewportRectangle;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.AnchoredSpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReferencePath;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportAnchor;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportNavigation;
import walkingkooka.text.CharSequences;
import walkingkooka.text.cursor.TextCursors;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoList;
import walkingkooka.tree.json.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * A collection of factory methods to create various {@link HateosHandler}.
 */
public final class SpreadsheetEngineHttps implements PublicStaticHelper {

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
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaFindCells}
     */
    public static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> findCells(final int defaultMax,
                                                                                                        final SpreadsheetEngine engine,
                                                                                                        final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaFindCells.with(
                defaultMax,
                engine,
                context
        );
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
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCell}
     */
    public static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> sortCells(final SpreadsheetEngine engine,
                                                                                                        final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCells.with(engine, context);
    }

    /**
     * {@see SpreadsheetEnginePatchFunctionCell}
     */
    public static UnaryOperator<JsonNode> patchCell(final HttpRequest request,
                                                    final SpreadsheetEngine engine,
                                                    final SpreadsheetEngineContext context) {
        return SpreadsheetEnginePatchFunctionCell.with(
                request,
                engine,
                context
        );
    }

    /**
     * {@see SpreadsheetEnginePatchFunctionColumn}
     */
    public static UnaryOperator<JsonNode> patchColumn(final HttpRequest request,
                                                      final SpreadsheetEngine engine,
                                                      final SpreadsheetEngineContext context) {
        return SpreadsheetEnginePatchFunctionColumn.with(
                request,
                engine,
                context
        );
    }

    /**
     * {@see SpreadsheetEnginePatchFunctionRow}
     */
    public static UnaryOperator<JsonNode> patchRow(final HttpRequest request,
                                                   final SpreadsheetEngine engine,
                                                   final SpreadsheetEngineContext context) {
        return SpreadsheetEnginePatchFunctionRow.with(
                request,
                engine,
                context
        );
    }

    // comparators......................................................................................................

    /**
     * {@see SpreadsheetEngineHateosHandlerSpreadsheetComparators}
     */
    public static HateosHandler<SpreadsheetComparatorName, SpreadsheetComparatorInfo, SpreadsheetComparatorInfoList> loadSpreadsheetComparators(final SpreadsheetEngine engine,
                                                                                                                                                final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetComparators.with(
                engine,
                context
        );
    }

    // comparators......................................................................................................

    /**
     * {@see SpreadsheetEngineHateosHandlerExpressionFunctions}
     */
    public static HateosHandler<FunctionExpressionName, ExpressionFunctionInfo, ExpressionFunctionInfoList> loadExpressionFunctions(final SpreadsheetEngine engine,
                                                                                                                                    final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerExpressionFunctions.with(
                engine,
                context
        );
    }

    // cell-range-path parameters.......................................................................................

    /**
     * Attempt to locate a path parameter and then parse it into an {@link SpreadsheetCellRangeReferencePath}.
     */
    public static Optional<SpreadsheetCellRangeReferencePath> cellRangePath(final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkParameters(parameters);

        return CELL_RANGE_PATH.firstParameterValue(parameters)
                .map(
                        s -> parseQueryParameter(
                                s,
                                SpreadsheetCellRangeReferencePath::fromKebabCase,
                                CELL_RANGE_PATH
                        )
                );
    }

    final static UrlParameterName CELL_RANGE_PATH = UrlParameterName.with("cell-range-path");

    // delta properties parameters.....................................................................................


    /**
     * Attempts to read the {@link SpreadsheetDeltaProperties} from the {@link #DELTA_PROPERTIES}.
     */
    public static Set<SpreadsheetDeltaProperties> deltaProperties(final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkParameters(parameters);

        return SpreadsheetDeltaProperties.csv(
                DELTA_PROPERTIES.firstParameterValue(parameters)
                        .orElse(null)
        );
    }

    /**
     * Optional query parameter, where the value is a CSV of camel-case {@link SpreadsheetDeltaProperties}.
     */
    final static UrlParameterName DELTA_PROPERTIES = UrlParameterName.with("properties");

    // max parameter....................................................................................................

    /**
     * Attempt to locate a max parameter and then parse it into an {@link Integer}.
     */
    public static Optional<Integer> max(final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkParameters(parameters);

        return MAX.firstParameterValue(parameters)
                .map(
                        s -> parseIntegerQueryParameter(
                                s,
                                MAX
                        )
                );
    }

    final static UrlParameterName MAX = UrlParameterName.with("max");

    // offset parameter....................................................................................................

    /**
     * Attempt to locate a offset parameter and then parse it into an {@link Integer}.
     */
    public static Optional<Integer> offset(final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkParameters(parameters);

        return OFFSET.firstParameterValue(parameters)
                .map(
                        s -> parseIntegerQueryParameter(
                                s,
                                OFFSET
                        )
                );
    }

    final static UrlParameterName OFFSET = UrlParameterName.with("offset");

    // query parameters................................................................................................

    /**
     * Attempt to locate a query parameter and then parse it into an {@link Expression}.
     */
    public static Optional<Expression> query(final Map<HttpRequestAttribute<?>, Object> parameters,
                                             final SpreadsheetEngineContext context) {
        checkParameters(parameters);
        checkContext(context);

        return QUERY.firstParameterValue(parameters)
                .flatMap(query -> parseQuery(query, context));
    }

    private static Optional<Expression> parseQuery(final String query,
                                                   final SpreadsheetEngineContext context) {
        return parseQueryParameter(
                query,
                (t) -> context.toExpression(
                        context.parseFormula(
                                TextCursors.charSequence(query)
                        )
                ),
                QUERY
        );
    }

    final static UrlParameterName QUERY = UrlParameterName.with("query");

    // valueType parameters.............................................................................................

    /**
     * Attempt to locate a value-type parameter and then parse it into an {@link String}.
     */
    public static Optional<String> valueType(final Map<HttpRequestAttribute<?>, Object> parameters,
                                             final SpreadsheetEngineContext context) {
        checkParameters(parameters);
        checkContext(context);

        return VALUE_TYPE.firstParameterValue(parameters);
    }

    final static UrlParameterName VALUE_TYPE = UrlParameterName.with("value-type");
    
    /**
     * Attempts to read a {@link SpreadsheetViewport} from the provided parameters.
     */
    // @VisibleForTesting
    public static Optional<SpreadsheetViewport> viewport(final Map<HttpRequestAttribute<?>, Object> parameters,
                                                         final boolean includeNavigation) {
        checkParameters(parameters);

        final MissingBuilder missing = MissingBuilder.empty();

        final Optional<String> home = HOME.firstParameterValue(parameters);
        final Optional<String> width = WIDTH.firstParameterValue(parameters);
        final Optional<String> height = HEIGHT.firstParameterValue(parameters);

        missing.addIfEmpty(home, HOME.toString());
        missing.addIfEmpty(width, WIDTH.toString());
        missing.addIfEmpty(height, HEIGHT.toString());

        final Optional<String> selectionType = SELECTION_TYPE.firstParameterValue(parameters);
        final Optional<String> selectionString = SELECTION.firstParameterValue(parameters);
        final Optional<String> anchor = SELECTION_ANCHOR.firstParameterValue(parameters); // optional
        final Optional<String> navigations = includeNavigation ?
                NAVIGATION.firstParameterValue(parameters)
                : Optional.empty(); // optional

        SpreadsheetViewport viewport = null;

        if (home.isPresent() || width.isPresent() || height.isPresent() || selectionType.isPresent() || selectionString.isPresent() || anchor.isPresent() || navigations.isPresent()) {
            // selection present require home/width/height/selectionType/selection/ maybe anchor
            if (selectionType.isPresent() || selectionString.isPresent() || anchor.isPresent() || navigations.isPresent()) {
                failIfMissing(missing);

                viewport = viewportRectangle(
                        home.get(),
                        width.get(),
                        height.get()
                ).viewport();

                {
                    missing.addIfEmpty(selectionType, SELECTION_TYPE.toString());
                    missing.addIfEmpty(selectionString, SELECTION.toString());

                    switch (missing.missing()) {
                        case 0:
                            viewport = viewport.setAnchoredSelection(
                                    Optional.of(
                                            selection(
                                                    selectionString.get(),
                                                    selectionType.get(),
                                                    anchor
                                            )
                                    )
                            );
                            break;
                        case 1:
                            throw new IllegalArgumentException(
                                    missingParameters(missing)
                            );
                        default:
                            break;
                    }
                }

                if (navigations.isPresent()) {
                    viewport = viewport.setNavigations(
                            navigations(
                                    navigations.get()
                            )
                    );
                }

            } else {
                // require home/width/height
                failIfMissing(missing);

                // if any selectionType/selection/anchor/navigations error
                final MissingBuilder notRequired = MissingBuilder.empty();
                notRequired.addIfFalse(false == selectionType.isPresent(), SELECTION_TYPE.value());
                notRequired.addIfFalse(false == selectionString.isPresent(), SELECTION.value());
                notRequired.addIfFalse(false == anchor.isPresent(), SELECTION_ANCHOR.value());
                notRequired.addIfFalse(false == navigations.isPresent(), NAVIGATION.value());

                if (notRequired.missing() > 0) {
                    throw new IllegalArgumentException(
                            "Selection missing: " + notRequired.build()
                    );
                }

                viewport = viewportRectangle(
                        home.get(),
                        width.get(),
                        height.get()
                ).viewport();
            }
        }

        return Optional.ofNullable(viewport);
    }

    /**
     * Retrieves the window from any present {@link SpreadsheetDelta} and then tries the parameters.
     */
    static SpreadsheetViewportWindows window(final Map<HttpRequestAttribute<?>, Object> parameters,
                                             final Optional<SpreadsheetDelta> delta,
                                             final SpreadsheetEngine engine,
                                             final SpreadsheetEngineContext context) {
        checkParameters(parameters);
        Objects.requireNonNull(delta, "delta");
        Objects.requireNonNull(engine, "engine");
        checkContext(context);

        final SpreadsheetViewportWindows windows;

        final Optional<String> windowsString = WINDOW.firstParameterValue(parameters);
        if (windowsString.isPresent()) {
            windows = parseWindow(
                    windowsString.get()
            );
        } else {
            final Optional<String> home = HOME.firstParameterValue(parameters);
            final Optional<String> width = WIDTH.firstParameterValue(parameters);
            final Optional<String> height = HEIGHT.firstParameterValue(parameters);
            final Optional<String> includeFrozenColumnsRows = includeFrozenColumnsRows(parameters);

            if (home.isPresent() || width.isPresent() || height.isPresent() || includeFrozenColumnsRows.isPresent()) {
                final MissingBuilder missing = MissingBuilder.empty();
                missing.addIfEmpty(home, HOME.toString());
                missing.addIfEmpty(width, WIDTH.toString());
                missing.addIfEmpty(height, HEIGHT.toString());
                missing.addIfEmpty(includeFrozenColumnsRows, INCLUDE_FROZEN_COLUMNS_ROWS.toString());

                failIfMissing(missing);

                windows = engine.window(
                        viewportRectangle(
                                home.get(),
                                width.get(),
                                height.get()
                        ),
                        includeFrozenColumnsRows(includeFrozenColumnsRows.get()),
                        SpreadsheetEngine.NO_SELECTION,
                        context
                );
            } else {
                if (false == delta.isPresent()) {
                    windows = SpreadsheetViewportWindows.EMPTY;
                } else {
                    windows = delta.get()
                            .window();
                }
            }
        }

        return windows;
    }

    private static SpreadsheetViewportWindows parseWindow(final String value) {
        return parseQueryParameter(
                value,
                SpreadsheetViewportWindows::parse,
                WINDOW
        );
    }

    private static Map<HttpRequestAttribute<?>, Object> checkParameters(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return Objects.requireNonNull(parameters, "parameters");
    }

    private static SpreadsheetEngineContext checkContext(final SpreadsheetEngineContext context) {
        return Objects.requireNonNull(context, "context");
    }

    /**
     * Adds support for passing the window as a url query parameter.
     */
    final static UrlParameterName WINDOW = UrlParameterName.with("window");

    private static Optional<String> includeFrozenColumnsRows(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return INCLUDE_FROZEN_COLUMNS_ROWS.firstParameterValue(parameters);
    }

    final static UrlParameterName INCLUDE_FROZEN_COLUMNS_ROWS = UrlParameterName.with("includeFrozenColumnsRows");

    // helpers.........................................................................................................

    private static boolean includeFrozenColumnsRows(final String value) {
        return Boolean.parseBoolean(value);
    }

    private static SpreadsheetViewportRectangle viewportRectangle(final String home,
                                                                  final String width,
                                                                  final String height) {
        return home(home)
                .viewportRectangle(
                        width(width),
                        height(height)
                );
    }

    private static SpreadsheetCellReference home(final String value) {
        return parseQueryParameter(
                value,
                SpreadsheetSelection::parseCell,
                HOME
        );
    }

    // @VisibleForTesting
    final static UrlParameterName HOME = UrlParameterName.with("home");

    private static double height(final String value) {
        return parseDoubleQueryParameter(
                value,
                HEIGHT
        );
    }

    final static UrlParameterName HEIGHT = UrlParameterName.with("height");

    private static double width(final String value) {
        return parseDoubleQueryParameter(
                value,
                WIDTH
        );
    }

    final static UrlParameterName WIDTH = UrlParameterName.with("width");

    private static AnchoredSpreadsheetSelection selection(final String selection,
                                                          final String type,
                                                          final Optional<String> anchor) {
        final SpreadsheetSelection spreadsheetSelection = SpreadsheetSelection.parse(
                selection,
                type
        );

        return spreadsheetSelection.setAnchor(
                anchor.map(
                        SpreadsheetEngineHttps::parseAnchor
                ).orElse(spreadsheetSelection.defaultAnchor())
        );
    }

    private static SpreadsheetViewportAnchor parseAnchor(final String text) {
        return parseQueryParameter(
                text,
                SpreadsheetViewportAnchor::parse,
                SELECTION_ANCHOR
        );
    }

    /**
     * The {@link SpreadsheetViewportAnchor} in text form, eg "top-bottom"
     */
    // @VisibleForTesting
    final static UrlParameterName SELECTION_ANCHOR = UrlParameterName.with("selectionAnchor");

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

    private static List<SpreadsheetViewportNavigation> navigations(final String value) {
        return parseQueryParameter(
                value,
                SpreadsheetViewportNavigation::parse,
                NAVIGATION
        );
    }

    /**
     * The {@link SpreadsheetViewportNavigation} in text form, eg "extend-right"
     */
    // @VisibleForTesting
    final static UrlParameterName NAVIGATION = UrlParameterName.with("navigation");

    private static double parseDoubleQueryParameter(final String text,
                                                    final UrlParameterName parameterName) {
        final double value = parseQueryParameter(
                text,
                Double::parseDouble,
                parameterName
        );

        if (value <= 0) {
            throw new IllegalArgumentException(
                    invalidQueryParameterMessage(text, parameterName) + " <= 0"
            );
        }

        return value;
    }

    private static int parseIntegerQueryParameter(final String text,
                                                  final UrlParameterName parameterName) {
        final int value = parseQueryParameter(
                text,
                Integer::parseInt,
                parameterName
        );

        if (value < 0) {
            throw new IllegalArgumentException(
                    invalidQueryParameterMessage(text, parameterName) + " < 0"
            );
        }

        return value;
    }

    private static <T> T parseQueryParameter(final String text,
                                             final Function<String, T> parser,
                                             final UrlParameterName queryParameter) {
        try {
            return parser.apply(text);
        } catch (final IllegalArgumentException cause) {
            throw invalidQueryParameter(
                    text,
                    queryParameter,
                    cause
            );
        }
    }

    private static IllegalArgumentException invalidQueryParameter(final String text,
                                                                  final UrlParameterName parameter,
                                                                  final Throwable cause) {
        return new IllegalArgumentException(
                invalidQueryParameterMessage(
                        text,
                        parameter
                ),
                cause
        );
    }

    private static String invalidQueryParameterMessage(final String text,
                                                       final UrlParameterName parameter) {
        return "Invalid " + parameter + "=" + CharSequences.quoteAndEscape(text);
    }

    private static void failIfMissing(final MissingBuilder missing) {
        if (missing.missing() > 0) {
            throw new IllegalArgumentException(
                    missingParameters(missing)
            );
        }
    }

    private static String missingParameters(final MissingBuilder missing) {
        return "Missing: " + missing.build();
    }

    /**
     * Prepares a {@link SpreadsheetDelta} response honouring any present query and window query parameters.
     */
    static SpreadsheetDelta prepareResponse(final Optional<SpreadsheetDelta> in,
                                            final Map<HttpRequestAttribute<?>, Object> parameters,
                                            final SpreadsheetDelta out,
                                            final SpreadsheetEngine engine,
                                            final SpreadsheetEngineContext context) {

        final Optional<Expression> maybeExpression = SpreadsheetEngineHttps.query(
                parameters,
                context
        );

        final Optional<String> maybeValueType = SpreadsheetEngineHttps.valueType(
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
                SpreadsheetEngineHttps.window(
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
    private SpreadsheetEngineHttps() {
        throw new UnsupportedOperationException();
    }
}
