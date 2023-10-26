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

import walkingkooka.build.MissingBuilder;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetViewportRectangle;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.AnchoredSpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportAnchor;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportNavigation;
import walkingkooka.text.CharSequences;
import walkingkooka.tree.json.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
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
     * {@see SpreadsheetEnginePatchSpreadsheetCellFunction}
     */
    public static UnaryOperator<JsonNode> patchCell(final HttpRequest request,
                                                    final SpreadsheetEngine engine,
                                                    final SpreadsheetEngineContext context) {
        return SpreadsheetEnginePatchSpreadsheetCellFunction.with(
                request,
                engine,
                context
        );
    }

    /**
     * {@see SpreadsheetEnginePatchSpreadsheetColumnFunction}
     */
    public static UnaryOperator<JsonNode> patchColumn(final HttpRequest request,
                                                      final SpreadsheetEngine engine,
                                                      final SpreadsheetEngineContext context) {
        return SpreadsheetEnginePatchSpreadsheetColumnFunction.with(
                request,
                engine,
                context
        );
    }

    /**
     * {@see SpreadsheetEnginePatchSpreadsheetRowFunction}
     */
    public static UnaryOperator<JsonNode> patchRow(final HttpRequest request,
                                                   final SpreadsheetEngine engine,
                                                   final SpreadsheetEngineContext context) {
        return SpreadsheetEnginePatchSpreadsheetRowFunction.with(
                request,
                engine,
                context
        );
    }

    // query parameters................................................................................................

    /**
     * Checks if a {@link SpreadsheetViewport} are present in the {@link SpreadsheetDelta} or parameters,
     * honouring any navigation if present.
     */
    static Optional<SpreadsheetViewport> viewportAndNavigate(final Map<HttpRequestAttribute<?>, Object> parameters,
                                                             final Optional<SpreadsheetDelta> delta,
                                                             final SpreadsheetEngine engine,
                                                             final SpreadsheetEngineContext context) {
        // if viewport not present in parameters then get from SpreadsheetDelta
        Optional<SpreadsheetViewport> viewport = viewport(parameters);
        if (false == viewport.isPresent()) {
            viewport = delta.isPresent() ?
                    delta.get()
                            .viewport() :
                    SpreadsheetDelta.NO_VIEWPORT_SELECTION;
        }

        // SpreadsheetViewport read from delta or parameters, present so perform navigate
        if (viewport.isPresent()) {
            viewport = engine.navigate(
                    viewport.get(),
                    context
            );
        }

        return viewport;
    }

    /**
     * Attempts to read a {@link SpreadsheetViewport} from the provided parameters.
     */
    // @VisibleForTesting
    static Optional<SpreadsheetViewport> viewport(final Map<HttpRequestAttribute<?>, Object> parameters) {
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
        final Optional<String> navigations = SELECTION_NAVIGATION.firstParameterValue(parameters); // optional

        SpreadsheetViewport viewport = null;

        if (home.isPresent() || width.isPresent() || height.isPresent() || selectionType.isPresent() || selectionString.isPresent() || anchor.isPresent() || navigations.isPresent()) {
            // selection present require home/width/height/selectionType/selection/ maybe anchor
            if (selectionType.isPresent() || selectionString.isPresent() || anchor.isPresent() || navigations.isPresent()) {
                missing.addIfEmpty(selectionType, SELECTION_TYPE.toString());
                missing.addIfEmpty(selectionString, SELECTION.toString());

                if (missing.missing() != 0) {
                    throw new IllegalArgumentException(
                            missingParameters(missing)
                    );
                }

                viewport = viewportRectangle(
                        home.get(),
                        width.get(),
                        height.get()
                ).viewport()
                        .setSelection(
                                Optional.of(
                                        selection(
                                                selectionString.get(),
                                                selectionType.get(),
                                                anchor
                                        )
                                )
                        );

                if (navigations.isPresent()) {
                    viewport = viewport.setNavigations(
                            navigations(
                                    navigations.get()
                            )
                    );
                }

            } else {
                // require home/width/height
                if (missing.missing() != 0) {
                    throw new IllegalArgumentException(
                            missingParameters(missing)
                    );
                }

                // if any selectionType/selection/anchor/navigations error
                final MissingBuilder notRequired = MissingBuilder.empty();
                notRequired.addIfFalse(false == selectionType.isPresent(), SELECTION_TYPE.value());
                notRequired.addIfFalse(false == selectionString.isPresent(), SELECTION.value());
                notRequired.addIfFalse(false == anchor.isPresent(), SELECTION_ANCHOR.value());
                notRequired.addIfFalse(false == navigations.isPresent(), SELECTION_NAVIGATION.value());

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
     * Tries to read the window from the parameters or the given {@link SpreadsheetDelta} throwing a {@link IllegalArgumentException}
     * if both are missing a window.
     */
    static SpreadsheetViewportWindows notEmptyWindow(final Map<HttpRequestAttribute<?>, Object> parameters,
                                                     final Optional<SpreadsheetDelta> delta,
                                                     final SpreadsheetEngine engine,
                                                     final SpreadsheetEngineContext context) {
        return window0(
                parameters,
                delta,
                engine,
                context,
                SpreadsheetEngineHttps::notEmptyWindowFail
        );
    }

    private static SpreadsheetViewportWindows notEmptyWindowFail() {
        final MissingBuilder missing = MissingBuilder.empty();
        missing.add(HOME.toString());
        missing.add(WIDTH.toString());
        missing.add(HEIGHT.toString());
        missing.add(INCLUDE_FROZEN_COLUMNS_ROWS.toString());

        throw new IllegalArgumentException(
                missingParameters(missing) + " or " + WINDOW
        );
    }

    /**
     * Retrieves the window from any present {@link SpreadsheetDelta} and then tries the parameters.
     */
    static SpreadsheetViewportWindows window(final Map<HttpRequestAttribute<?>, Object> parameters,
                                             final Optional<SpreadsheetDelta> delta,
                                             final SpreadsheetEngine engine,
                                             final SpreadsheetEngineContext context) {
        return window0(
                parameters,
                delta,
                engine,
                context,
                () -> SpreadsheetViewportWindows.EMPTY
        );
    }

    /**
     * Retrieves the window from any present {@link SpreadsheetDelta} and then tries the parameters.
     */
    private static SpreadsheetViewportWindows window0(final Map<HttpRequestAttribute<?>, Object> parameters,
                                                      final Optional<SpreadsheetDelta> delta,
                                                      final SpreadsheetEngine engine,
                                                      final SpreadsheetEngineContext context,
                                                      final Supplier<SpreadsheetViewportWindows> missingWindow) {
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
            final Optional<String> includeFrozenColumnsRows = INCLUDE_FROZEN_COLUMNS_ROWS.firstParameterValue(parameters);

            if (home.isPresent() || width.isPresent() || height.isPresent() || includeFrozenColumnsRows.isPresent()) {
                final MissingBuilder missing = MissingBuilder.empty();
                missing.addIfEmpty(home, HOME.toString());
                missing.addIfEmpty(width, WIDTH.toString());
                missing.addIfEmpty(height, HEIGHT.toString());
                missing.addIfEmpty(includeFrozenColumnsRows, INCLUDE_FROZEN_COLUMNS_ROWS.toString());

                if (missing.missing() > 0) {
                    throw new IllegalArgumentException(
                            missingParameters(missing)
                    );
                }

                windows = engine.window(
                        viewportRectangle(
                                home.get(),
                                width.get(),
                                height.get()
                        ),
                        Boolean.parseBoolean(includeFrozenColumnsRows.get()),
                        SpreadsheetEngine.NO_SELECTION,
                        context
                );
            } else {
                if (false == delta.isPresent()) {
                    windows = missingWindow.get();
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
                WINDOW,
                SpreadsheetViewportWindows::parse
        );
    }

    /**
     * Adds support for passing the window as a url query parameter.
     */
    final static UrlParameterName WINDOW = UrlParameterName.with("window");
    final static UrlParameterName INCLUDE_FROZEN_COLUMNS_ROWS = UrlParameterName.with("includeFrozenColumnsRows");

    // helpers.........................................................................................................

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
                HOME,
                SpreadsheetSelection::parseCell
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
                SELECTION_ANCHOR,
                SpreadsheetViewportAnchor::parse
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
                SELECTION_NAVIGATION,
                SpreadsheetViewportNavigation::parse
        );
    }

    /**
     * The {@link SpreadsheetViewportNavigation} in text form, eg "extend-right"
     */
    // @VisibleForTesting
    final static UrlParameterName SELECTION_NAVIGATION = UrlParameterName.with("selectionNavigation");

    private static double parseDoubleQueryParameter(final String text,
                                                    final UrlParameterName parameterName) {
        final double value;

        try {
            value = Double.parseDouble(text);
        } catch (final IllegalArgumentException cause) {
            throw invalidQueryParameter(
                    text,
                    parameterName,
                    cause
            );
        }

        if (value <= 0) {
            throw new IllegalArgumentException(
                    invalidQueryParameterMessage(text, parameterName) + " <= 0"
            );
        }

        return value;
    }

    private static <T> T parseQueryParameter(final String text,
                                             final UrlParameterName queryParameter,
                                             final Function<String, T> parser) {
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

    private static String missingParameters(final MissingBuilder missing) {
        return "Missing: " + missing.build();
    }

    /**
     * Stop creation.
     */
    private SpreadsheetEngineHttps() {
        throw new UnsupportedOperationException();
    }
}
