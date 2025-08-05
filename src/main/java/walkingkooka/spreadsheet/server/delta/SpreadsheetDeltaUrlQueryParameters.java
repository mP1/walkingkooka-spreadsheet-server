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

package walkingkooka.spreadsheet.server.delta;

import walkingkooka.build.MissingBuilder;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.compare.SpreadsheetColumnOrRowSpreadsheetComparatorNames;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.viewport.AnchoredSpreadsheetSelection;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewport;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewportAnchor;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewportNavigation;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewportNavigationList;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewportRectangle;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewportWindows;
import walkingkooka.text.CharSequences;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Collection of helpers to read URL query parameters.
 */
public final class SpreadsheetDeltaUrlQueryParameters implements PublicStaticHelper {

    public final static UrlParameterName FROM = UrlParameterName.with("from");

    // delta properties parameters.....................................................................................

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

    /**
     * Adds support for passing the window as a url query parameter.
     */
    public final static UrlParameterName WINDOW = UrlParameterName.with("window");

    private static Optional<String> includeFrozenColumnsRows(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return INCLUDE_FROZEN_COLUMNS_ROWS.firstParameterValue(parameters);
    }

    public final static UrlParameterName INCLUDE_FROZEN_COLUMNS_ROWS = UrlParameterName.with("includeFrozenColumnsRows");

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
    public final static UrlParameterName HOME = UrlParameterName.with("home");

    private static double height(final String value) {
        return parseDoubleQueryParameter(
            value,
            HEIGHT
        );
    }

    public final static UrlParameterName HEIGHT = UrlParameterName.with("height");

    private static double width(final String value) {
        return parseDoubleQueryParameter(
            value,
            WIDTH
        );
    }

    public final static UrlParameterName WIDTH = UrlParameterName.with("width");

    private static AnchoredSpreadsheetSelection selection(final String selection,
                                                          final String type,
                                                          final Optional<String> anchor) {
        final SpreadsheetSelection spreadsheetSelection = SpreadsheetSelection.parse(
            selection,
            type
        );

        return spreadsheetSelection.setAnchor(
            anchor.map(
                SpreadsheetDeltaUrlQueryParameters::parseAnchor
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
    public final static UrlParameterName SELECTION_ANCHOR = UrlParameterName.with("selectionAnchor");

    /**
     * Holds the type of the selection parameter. This is necessary due to ambiguities between column and labels.
     */
    // @VisibleForTesting
    public final static UrlParameterName SELECTION_TYPE = UrlParameterName.with("selectionType");

    /**
     * The {@link SpreadsheetSelection} in text form, eg "A" for column, "B2" for cell, "C:D" for column range etc.
     */
    // @VisibleForTesting
    public final static UrlParameterName SELECTION = UrlParameterName.with("selection");

    private static SpreadsheetViewportNavigationList navigations(final String value) {
        return parseQueryParameter(
            value,
            SpreadsheetViewportNavigationList::parse,
            NAVIGATION
        );
    }

    /**
     * The {@link SpreadsheetViewportNavigation} in text form, eg "extend-right"
     */
    // @VisibleForTesting
    public final static UrlParameterName NAVIGATION = UrlParameterName.with("navigation");

    static List<SpreadsheetColumnOrRowSpreadsheetComparatorNames> comparators(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return COMPARATORS.firstParameterValue(parameters)
            .map(SpreadsheetColumnOrRowSpreadsheetComparatorNames::parseList)
            .orElseThrow(() -> new IllegalArgumentException("Missing required " + COMPARATORS));
    }

    /**
     * Required parameter holding the columns/rows to be sorted.
     */
    public final static UrlParameterName COMPARATORS = UrlParameterName.with("comparators");

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

    private static Map<HttpRequestAttribute<?>, Object> checkParameters(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return Objects.requireNonNull(parameters, "parameters");
    }

    private static SpreadsheetEngineContext checkContext(final SpreadsheetEngineContext context) {
        return Objects.requireNonNull(context, "context");
    }

    /**
     * Stop creation
     */
    private SpreadsheetDeltaUrlQueryParameters() {
        throw new UnsupportedOperationException();
    }
}
