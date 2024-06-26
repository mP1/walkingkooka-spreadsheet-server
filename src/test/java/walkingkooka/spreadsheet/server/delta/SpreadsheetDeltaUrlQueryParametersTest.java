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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.PublicStaticHelperTesting;
import walkingkooka.spreadsheet.SpreadsheetViewportRectangle;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.expression.SpreadsheetFunctionName;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReferencePath;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportNavigation;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportNavigationList;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.TextCursorSavePoint;
import walkingkooka.tree.expression.Expression;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetDeltaUrlQueryParametersTest implements PublicStaticHelperTesting<SpreadsheetDeltaUrlQueryParameters> {

    // cell-range-path properties.......................................................................................

    @Test
    public void testCellRangePathNullParametersFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.cellRangePath(
                        null
                )
        );
    }

    @Test
    public void testCellRangePathMissing() {
        this.cellRangePathAndCheck(
                Maps.empty()
        );
    }

    @Test
    public void testCellRangePathEmpty() {
        this.cellRangePathAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.CELL_RANGE_PATH,
                        Lists.of()
                )
        );
    }

    @Test
    public void testCellRangePathLrtd() {
        this.cellRangePathAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.CELL_RANGE_PATH,
                        Lists.of(
                                "tdlr"
                        )
                ),
                SpreadsheetCellRangeReferencePath.TDLR
        );
    }

    @Test
    public void testCellRangePathRlbu() {
        this.cellRangePathAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.CELL_RANGE_PATH,
                        Lists.of(
                                "rlbu"
                        )
                ),
                SpreadsheetCellRangeReferencePath.RLBU
        );
    }

    private void cellRangePathAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters) {
        this.cellRangePathAndCheck(
                parameters,
                Optional.empty()
        );
    }

    private void cellRangePathAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                                       final SpreadsheetCellRangeReferencePath expected) {
        this.cellRangePathAndCheck(
                parameters,
                Optional.of(expected)
        );
    }

    private void cellRangePathAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                                       final Optional<SpreadsheetCellRangeReferencePath> expected) {
        this.checkEquals(
                expected,
                SpreadsheetDeltaUrlQueryParameters.cellRangePath(
                        parameters
                ),
                () -> parameters.toString()
        );
    }

    // delta properties................................................................................................

    @Test
    public void testDeltaPropertiesNullParametersFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.deltaProperties(
                        null
                )
        );
    }

    @Test
    public void testDeltaPropertiesMissing() {
        this.deltaPropertiesAndCheck(
                Maps.empty(),
                SpreadsheetDeltaProperties.ALL
        );
    }

    @Test
    public void testDeltaPropertiesEmpty() {
        this.deltaPropertiesAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.DELTA_PROPERTIES,
                        Lists.empty()
                ),
                SpreadsheetDeltaProperties.ALL
        );
    }

    @Test
    public void testDeltaPropertiesAll() {
        this.deltaPropertiesAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.DELTA_PROPERTIES,
                        Lists.of("*")
                ),
                SpreadsheetDeltaProperties.ALL
        );
    }

    @Test
    public void testDeltaPropertiesPresent() {
        this.deltaPropertiesAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.DELTA_PROPERTIES,
                        Lists.of("cells,labels,column-count")
                ),
                Sets.of(
                        SpreadsheetDeltaProperties.CELLS,
                        SpreadsheetDeltaProperties.COLUMN_COUNT,
                        SpreadsheetDeltaProperties.LABELS
                )
        );
    }

    private void deltaPropertiesAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                                         final Set<SpreadsheetDeltaProperties> expected) {
        this.checkEquals(
                expected,
                SpreadsheetDeltaUrlQueryParameters.deltaProperties(
                        parameters
                ),
                () -> parameters.toString()
        );
    }

    // max properties...................................................................................................

    @Test
    public void testMaxNullParametersFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.max(
                        null
                )
        );
    }

    @Test
    public void testMaxNegativeFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.max(
                        Maps.of(
                                SpreadsheetDeltaUrlQueryParameters.MAX,
                                Lists.of("-1")
                        )
                )
        );
    }

    @Test
    public void testMaxDecimalPointFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.max(
                        Maps.of(
                                SpreadsheetDeltaUrlQueryParameters.MAX,
                                Lists.of("23.0")
                        )
                )
        );
    }

    @Test
    public void testMaxMissing() {
        this.maxAndCheck(
                Maps.empty()
        );
    }

    @Test
    public void testMaxEmpty() {
        this.maxAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.MAX,
                        Lists.of()
                )
        );
    }

    @Test
    public void testMaxZero() {
        this.maxAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.MAX,
                        Lists.of(
                                "0"
                        )
                ),
                0
        );
    }

    @Test
    public void testMaxOne() {
        this.maxAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.MAX,
                        Lists.of(
                                "1"
                        )
                ),
                1
        );
    }

    private void maxAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters) {
        this.maxAndCheck(
                parameters,
                Optional.empty()
        );
    }

    private void maxAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                             final int expected) {
        this.maxAndCheck(
                parameters,
                Optional.of(expected)
        );
    }

    private void maxAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                             final Optional<Integer> expected) {
        this.checkEquals(
                expected,
                SpreadsheetDeltaUrlQueryParameters.max(
                        parameters
                ),
                () -> parameters.toString()
        );
    }

    // offset properties...................................................................................................

    @Test
    public void testOffsetNullParametersFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.offset(
                        null
                )
        );
    }

    @Test
    public void testOffsetNegativeFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.offset(
                        Maps.of(
                                SpreadsheetDeltaUrlQueryParameters.OFFSET,
                                Lists.of("-1")
                        )
                )
        );
    }

    @Test
    public void testOffsetDecimalPointFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.offset(
                        Maps.of(
                                SpreadsheetDeltaUrlQueryParameters.OFFSET,
                                Lists.of("23.0")
                        )
                )
        );
    }

    @Test
    public void testOffsetMissing() {
        this.offsetAndCheck(
                Maps.empty()
        );
    }

    @Test
    public void testOffsetEmpty() {
        this.offsetAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.OFFSET,
                        Lists.of()
                )
        );
    }

    @Test
    public void testOffsetZero() {
        this.offsetAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.OFFSET,
                        Lists.of(
                                "0"
                        )
                ),
                0
        );
    }

    @Test
    public void testOffsetOne() {
        this.offsetAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.OFFSET,
                        Lists.of(
                                "1"
                        )
                ),
                1
        );
    }

    private void offsetAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters) {
        this.offsetAndCheck(
                parameters,
                Optional.empty()
        );
    }

    private void offsetAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                                final int expected) {
        this.offsetAndCheck(
                parameters,
                Optional.of(expected)
        );
    }

    private void offsetAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                                final Optional<Integer> expected) {
        this.checkEquals(
                expected,
                SpreadsheetDeltaUrlQueryParameters.offset(
                        parameters
                ),
                () -> parameters.toString()
        );
    }

    // query...........................................................................................................

    @Test
    public void testQueryNullParametersFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.query(
                        null,
                        SpreadsheetEngineContexts.fake()
                )
        );
    }

    @Test
    public void testQueryNullContextFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.query(
                        Maps.empty(),
                        null
                )
        );
    }

    @Test
    public void testQueryParameterMissing() {
        this.queryAndCheck(
                Maps.empty(),
                SpreadsheetEngineContexts.fake(),
                Optional.empty()
        );
    }

    @Test
    public void testQueryParameterPresent() {
        final String query = "=true()";
        final SpreadsheetParserToken token = SpreadsheetParserToken.functionName(
                SpreadsheetFunctionName.with("true"),
                "true"
        );
        final Optional<Expression> expression = Optional.of(
                Expression.value(true)
        );

        this.queryAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.QUERY,
                        Lists.of(query)
                ),
                new FakeSpreadsheetEngineContext() {
                    @Override
                    public SpreadsheetParserToken parseFormula(final TextCursor formula) {
                        final TextCursorSavePoint begin = formula.save();
                        formula.end();

                        checkEquals(
                                query,
                                begin.textBetween()
                                        .toString()
                        );

                        return token;
                    }

                    @Override
                    public Optional<Expression> toExpression(final SpreadsheetParserToken t) {
                        checkEquals(
                                token,
                                t
                        );
                        return expression;
                    }
                },
                expression
        );
    }

    private void queryAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                               final SpreadsheetEngineContext context,
                               final Optional<Expression> expected) {
        this.checkEquals(
                expected,
                SpreadsheetDeltaUrlQueryParameters.query(
                        parameters,
                        context
                ),
                () -> parameters.toString()
        );
    }

    // window...........................................................................................................

    @Test
    public void testWindowsNullParametersFails() {
        this.windowFails(
                null,
                Optional.empty(),
                SpreadsheetEngines.fake(),
                SpreadsheetEngineContexts.fake()
        );
    }

    @Test
    public void testWindowsNullDeltaFails() {
        this.windowFails(
                Maps.empty(),
                null,
                SpreadsheetEngines.fake(),
                SpreadsheetEngineContexts.fake()
        );
    }

    @Test
    public void testWindowsNullEngineFails() {
        this.windowFails(
                Maps.empty(),
                Optional.empty(),
                null,
                SpreadsheetEngineContexts.fake()
        );
    }

    @Test
    public void testWindowsNullContextFails() {
        this.windowFails(
                Maps.empty(),
                Optional.empty(),
                SpreadsheetEngines.fake(),
                null
        );
    }

    private void windowFails(final Map<HttpRequestAttribute<?>, Object> parameters,
                             final Optional<SpreadsheetDelta> delta,
                             final SpreadsheetEngine engine,
                             final SpreadsheetEngineContext context) {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.window(
                        parameters,
                        delta,
                        engine,
                        context
                )
        );
    }

    @Test
    public void testWindowHomeMissingHomeFails() {
        this.windowFails(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("111"),
                        SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("22"),
                        SpreadsheetDeltaUrlQueryParameters.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false")
                ),
                "Missing: home"
        );
    }

    @Test
    public void testWindowHomeMissingWidthFails() {
        this.windowFails(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("A1"),
                        SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("22"),
                        SpreadsheetDeltaUrlQueryParameters.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false")
                ),
                "Missing: width"
        );
    }

    @Test
    public void testWindowHomeMissingHeightFails() {
        this.windowFails(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("A1"),
                        SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("22"),
                        SpreadsheetDeltaUrlQueryParameters.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false")
                ),
                "Missing: height"
        );
    }


    @Test
    public void testWindowHomeMissingWidthHeightIncludeFails() {
        this.windowFails(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("A1")
                ),
                "Missing: width, height, includeFrozenColumnsRows"
        );
    }

    private void windowFails(final Map<HttpRequestAttribute<?>, Object> parameters,
                             final String expected) {
        final IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.window(
                        parameters,
                        Optional.empty(),
                        SpreadsheetEngines.fake(),
                        SpreadsheetEngineContexts.fake()
                )
        );
        this.checkEquals(
                expected,
                thrown.getMessage()
        );
    }

    @Test
    public void testWindowWithHomeWidthHeightIncludeFrozenColumnsRowsFalse() {
        final SpreadsheetViewportWindows windows = SpreadsheetViewportWindows.parse("A1:B2");

        this.windowAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("B2"),
                        SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("111"),
                        SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("222"),
                        SpreadsheetDeltaUrlQueryParameters.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false")
                ),
                Optional.empty(),
                new FakeSpreadsheetEngine() {
                    public SpreadsheetViewportWindows window(final SpreadsheetViewportRectangle rectangle,
                                                             final boolean includeFrozenColumnsRows,
                                                             final Optional<SpreadsheetSelection> selection,
                                                             final SpreadsheetEngineContext context) {
                        checkEquals(SpreadsheetSelection.parseCell("B2")
                                        .viewportRectangle(111, 222),
                                rectangle, "rectangle");
                        checkEquals(false, includeFrozenColumnsRows, "includeFrozenColumnsRows");
                        checkEquals(Optional.empty(), selection);

                        return windows;
                    }
                },
                SpreadsheetEngineContexts.fake(),
                windows
        );
    }

    @Test
    public void testWindowWithHomeWidthHeightIncludeFrozenColumnsRowsTrue() {
        final SpreadsheetViewportWindows windows = SpreadsheetViewportWindows.parse("A1:B2");

        this.windowAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("B2"),
                        SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("111"),
                        SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("222"),
                        SpreadsheetDeltaUrlQueryParameters.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("true")
                ),
                Optional.empty(),
                new FakeSpreadsheetEngine() {
                    public SpreadsheetViewportWindows window(final SpreadsheetViewportRectangle rectangle,
                                                             final boolean includeFrozenColumnsRows,
                                                             final Optional<SpreadsheetSelection> selection,
                                                             final SpreadsheetEngineContext context) {
                        checkEquals(SpreadsheetSelection.parseCell("B2")
                                        .viewportRectangle(111, 222),
                                rectangle, "rectangle");
                        checkEquals(true, includeFrozenColumnsRows, "includeFrozenColumnsRows");
                        checkEquals(Optional.empty(), selection);

                        return windows;
                    }
                },
                SpreadsheetEngineContexts.fake(),
                windows
        );
    }

    @Test
    public void testWindowWithWindowParameter() {
        final String windows = "A1:B2";

        this.windowAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.WINDOW, Lists.of(windows)
                ),
                Optional.empty(),
                SpreadsheetEngines.fake(),
                SpreadsheetEngineContexts.fake(),
                SpreadsheetViewportWindows.parse(windows)
        );
    }

    @Test
    public void testWindowWithWindowIgnoresHome() {
        final String windows = "A1:B2";

        this.windowAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.WINDOW, Lists.of(windows),
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("Z1")
                ),
                Optional.empty(),
                SpreadsheetEngines.fake(),
                SpreadsheetEngineContexts.fake(),
                SpreadsheetViewportWindows.parse(windows)
        );
    }

    @Test
    public void testWindowWithWindowIgnoresSelection() {
        final String windows = "A1:B2";

        this.windowAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.WINDOW, Lists.of(windows),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION_TYPE, Lists.of("row"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION, Lists.of("3"),
                        SpreadsheetDeltaUrlQueryParameters.NAVIGATION, Lists.of("left column")
                ),
                Optional.empty(),
                SpreadsheetEngines.fake(),
                SpreadsheetEngineContexts.fake(),
                SpreadsheetViewportWindows.parse(windows)
        );
    }

    private void windowAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                                final Optional<SpreadsheetDelta> delta,
                                final SpreadsheetEngine engine,
                                final SpreadsheetEngineContext context,
                                final SpreadsheetViewportWindows expected) {
        this.checkEquals(
                expected,
                SpreadsheetDeltaUrlQueryParameters.window(
                        parameters,
                        delta,
                        engine,
                        context
                )
        );
    }

    // viewport.........................................................................................................

    @Test
    public void testViewportNullParametersFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.viewport(
                        null, // parameters
                        false // includeNavigations
                )
        );
    }

    @Test
    public void testViewportEmpty() {
        this.viewportAndCheck(
                Maps.empty()
        );
    }

    @Test
    public void testViewportMissingHomeFails() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("1"),
                        SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("1")
                ),
                "Missing: home"
        );
    }

    @Test
    public void testViewportMissingWidthFails() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("A1"),
                        SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("1")
                ),
                "Missing: width"
        );
    }

    @Test
    public void testViewportMissingHeightFails() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("A1"),
                        SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("1")
                ),
                "Missing: height"
        );
    }

    @Test
    public void testViewportNoSelection() {
        this.viewportAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("A123"),
                        SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("11"),
                        SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("22")
                ),
                SpreadsheetSelection.parseCell("A123")
                        .viewportRectangle(11, 22)
                        .viewport()
        );
    }

    @Test
    public void testViewportSelectionMissingHome() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("1"),
                        SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("2"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION_TYPE, Lists.of("cell"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION, Lists.of("A1")
                ),
                "Missing: home"
        );
    }

    @Test
    public void testViewportSelectionMissingWidth() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("A1"),
                        SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("1"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION_TYPE, Lists.of("cell"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION, Lists.of("A1")
                ),
                "Missing: width"
        );
    }

    @Test
    public void testViewportSelectionMissingHeight() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("A1"),
                        SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("1"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION_TYPE, Lists.of("cell"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION, Lists.of("A1")
                ),
                "Missing: height"
        );
    }

    @Test
    public void testViewportSelectionMissingWidthHeight() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("A1"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION_TYPE, Lists.of("cell"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION, Lists.of("A1")
                ),
                "Missing: width, height"
        );
    }

    @Test
    public void testViewportSelectionMissingHomeWidthHeight() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.SELECTION_TYPE, Lists.of("cell"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION, Lists.of("A1")
                ),
                "Missing: home, width, height"
        );
    }

    @Test
    public void testViewportSelection() {
        this.viewportAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("A123"),
                        SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("11"),
                        SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("22"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION_TYPE, Lists.of("column"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION, Lists.of("B")
                ),
                SpreadsheetSelection.parseCell("A123")
                        .viewportRectangle(11, 22)
                        .viewport()
                        .setAnchoredSelection(
                                Optional.of(
                                        SpreadsheetSelection.parseColumn("B")
                                                .setDefaultAnchor()
                                )
                        )
        );
    }

    @Test
    public void testViewportSelectionAnchorDefaulted() {
        this.viewportAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("A123"),
                        SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("11"),
                        SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("22"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION_TYPE, Lists.of("column"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION, Lists.of("B")
                ),
                SpreadsheetSelection.parseCell("A123")
                        .viewportRectangle(11, 22)
                        .viewport()
                        .setAnchoredSelection(
                                Optional.of(
                                        SpreadsheetSelection.parseColumn("B")
                                                .setDefaultAnchor()
                                )
                        )
        );
    }

    @Test
    public void testViewportNavigation() {
        this.viewportAndCheck(
                Maps.of(
                        SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("A123"),
                        SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("11"),
                        SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("22"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION_TYPE, Lists.of("row"),
                        SpreadsheetDeltaUrlQueryParameters.SELECTION, Lists.of("3"),
                        SpreadsheetDeltaUrlQueryParameters.NAVIGATION, Lists.of("left column")
                ),
                true, // includeNavigation
                SpreadsheetSelection.parseCell("A123")
                        .viewportRectangle(11, 22)
                        .viewport()
                        .setAnchoredSelection(
                                Optional.of(
                                        SpreadsheetSelection.parseRow("3")
                                                .setDefaultAnchor()
                                )
                        ).setNavigations(
                                SpreadsheetViewportNavigationList.EMPTY.concat(
                                        SpreadsheetViewportNavigation.leftColumn()
                                )
                        )
        );
    }

    private void viewportFails(final Map<HttpRequestAttribute<?>, Object> parameters,
                               final String expected) {
        this.viewportFails(
                parameters,
                false, // includeNavigation
                expected
        );
    }

    private void viewportFails(final Map<HttpRequestAttribute<?>, Object> parameters,
                               final boolean includeNavigation,
                               final String expected) {
        final IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetDeltaUrlQueryParameters.viewport(
                        parameters,
                        includeNavigation
                )
        );

        this.checkEquals(
                expected,
                thrown.getMessage()
        );
    }

    private void viewportAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters) {
        this.viewportAndCheck(
                parameters,
                false // includeNavigation,
        );
    }

    private void viewportAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                                  final boolean includeNavigation) {
        this.viewportAndCheck(
                parameters,
                includeNavigation,
                Optional.empty()
        );
    }

    private void viewportAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                                  final SpreadsheetViewport viewport) {
        this.viewportAndCheck(
                parameters,
                false, // includeNavigation,
                viewport
        );
    }

    private void viewportAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                                  final boolean includeNavigation,
                                  final SpreadsheetViewport viewport) {
        this.viewportAndCheck(
                parameters,
                includeNavigation,
                Optional.of(viewport)
        );
    }

    private void viewportAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                                  final boolean includeNavigation,
                                  final Optional<SpreadsheetViewport> viewport) {
        this.checkEquals(
                viewport,
                SpreadsheetDeltaUrlQueryParameters.viewport(
                        parameters,
                        includeNavigation
                )
        );
    }


    @Override
    public Class<SpreadsheetDeltaUrlQueryParameters> type() {
        return SpreadsheetDeltaUrlQueryParameters.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }

    @Override
    public boolean canHavePublicTypes(final Method method) {
        return false;
    }
}
