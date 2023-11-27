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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.PublicStaticHelperTesting;
import walkingkooka.spreadsheet.SpreadsheetViewportRectangle;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportNavigation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetEngineHttpsTest implements ClassTesting2<SpreadsheetEngineHttps>,
        PublicStaticHelperTesting<SpreadsheetEngineHttps> {

    // viewportAndNavigate.............................................................................................

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
                () -> SpreadsheetEngineHttps.window(
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
                        SpreadsheetEngineHttps.WIDTH, Lists.of("111"),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of("22"),
                        SpreadsheetEngineHttps.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false")
                ),
                "Missing: home"
        );
    }

    @Test
    public void testWindowHomeMissingWidthFails() {
        this.windowFails(
                Maps.of(
                        SpreadsheetEngineHttps.HOME, Lists.of("A1"),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of("22"),
                        SpreadsheetEngineHttps.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false")
                ),
                "Missing: width"
        );
    }

    @Test
    public void testWindowHomeMissingHeightFails() {
        this.windowFails(
                Maps.of(
                        SpreadsheetEngineHttps.HOME, Lists.of("A1"),
                        SpreadsheetEngineHttps.WIDTH, Lists.of("22"),
                        SpreadsheetEngineHttps.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false")
                ),
                "Missing: height"
        );
    }


    @Test
    public void testWindowHomeMissingWidthHeightIncludeFails() {
        this.windowFails(
                Maps.of(
                        SpreadsheetEngineHttps.HOME, Lists.of("A1")
                ),
                "Missing: width, height, includeFrozenColumnsRows"
        );
    }

    private void windowFails(final Map<HttpRequestAttribute<?>, Object> parameters,
                             final String expected) {
        final IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetEngineHttps.window(
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
                        SpreadsheetEngineHttps.HOME, Lists.of("B2"),
                        SpreadsheetEngineHttps.WIDTH, Lists.of("111"),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of("222"),
                        SpreadsheetEngineHttps.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false")
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
                        SpreadsheetEngineHttps.HOME, Lists.of("B2"),
                        SpreadsheetEngineHttps.WIDTH, Lists.of("111"),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of("222"),
                        SpreadsheetEngineHttps.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("true")
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
                        SpreadsheetEngineHttps.WINDOW, Lists.of(windows)
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
                        SpreadsheetEngineHttps.WINDOW, Lists.of(windows),
                        SpreadsheetEngineHttps.HOME, Lists.of("Z1")
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
                        SpreadsheetEngineHttps.WINDOW, Lists.of(windows),
                        SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("row"),
                        SpreadsheetEngineHttps.SELECTION, Lists.of("3"),
                        SpreadsheetEngineHttps.NAVIGATION, Lists.of("left column")
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
                SpreadsheetEngineHttps.window(
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
                () -> SpreadsheetEngineHttps.viewport(
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
                        SpreadsheetEngineHttps.WIDTH, Lists.of("1"),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of("1")
                ),
                "Missing: home"
        );
    }

    @Test
    public void testViewportMissingWidthFails() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetEngineHttps.HOME, Lists.of("A1"),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of("1")
                ),
                "Missing: width"
        );
    }

    @Test
    public void testViewportMissingHeightFails() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetEngineHttps.HOME, Lists.of("A1"),
                        SpreadsheetEngineHttps.WIDTH, Lists.of("1")
                ),
                "Missing: height"
        );
    }

    @Test
    public void testViewportNoSelection() {
        this.viewportAndCheck(
                Maps.of(
                        SpreadsheetEngineHttps.HOME, Lists.of("A123"),
                        SpreadsheetEngineHttps.WIDTH, Lists.of("11"),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of("22")
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
                        SpreadsheetEngineHttps.WIDTH, Lists.of("1"),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of("2"),
                        SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("cell"),
                        SpreadsheetEngineHttps.SELECTION, Lists.of("A1")
                ),
                "Missing: home"
        );
    }

    @Test
    public void testViewportSelectionMissingWidth() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetEngineHttps.HOME, Lists.of("A1"),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of("1"),
                        SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("cell"),
                        SpreadsheetEngineHttps.SELECTION, Lists.of("A1")
                ),
                "Missing: width"
        );
    }

    @Test
    public void testViewportSelectionMissingHeight() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetEngineHttps.HOME, Lists.of("A1"),
                        SpreadsheetEngineHttps.WIDTH, Lists.of("1"),
                        SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("cell"),
                        SpreadsheetEngineHttps.SELECTION, Lists.of("A1")
                ),
                "Missing: height"
        );
    }

    @Test
    public void testViewportSelectionMissingWidthHeight() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetEngineHttps.HOME, Lists.of("A1"),
                        SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("cell"),
                        SpreadsheetEngineHttps.SELECTION, Lists.of("A1")
                ),
                "Missing: width, height"
        );
    }

    @Test
    public void testViewportSelectionMissingHomeWidthHeight() {
        this.viewportFails(
                Maps.of(
                        SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("cell"),
                        SpreadsheetEngineHttps.SELECTION, Lists.of("A1")
                ),
                "Missing: home, width, height"
        );
    }

    @Test
    public void testViewportSelection() {
        this.viewportAndCheck(
                Maps.of(
                        SpreadsheetEngineHttps.HOME, Lists.of("A123"),
                        SpreadsheetEngineHttps.WIDTH, Lists.of("11"),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of("22"),
                        SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("column"),
                        SpreadsheetEngineHttps.SELECTION, Lists.of("B")
                ),
                SpreadsheetSelection.parseCell("A123")
                        .viewportRectangle(11, 22)
                        .viewport()
                        .setSelection(
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
                        SpreadsheetEngineHttps.HOME, Lists.of("A123"),
                        SpreadsheetEngineHttps.WIDTH, Lists.of("11"),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of("22"),
                        SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("column"),
                        SpreadsheetEngineHttps.SELECTION, Lists.of("B")
                ),
                SpreadsheetSelection.parseCell("A123")
                        .viewportRectangle(11, 22)
                        .viewport()
                        .setSelection(
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
                        SpreadsheetEngineHttps.HOME, Lists.of("A123"),
                        SpreadsheetEngineHttps.WIDTH, Lists.of("11"),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of("22"),
                        SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("row"),
                        SpreadsheetEngineHttps.SELECTION, Lists.of("3"),
                        SpreadsheetEngineHttps.NAVIGATION, Lists.of("left column")
                ),
                true, // includeNavigation
                SpreadsheetSelection.parseCell("A123")
                        .viewportRectangle(11, 22)
                        .viewport()
                        .setSelection(
                                Optional.of(
                                        SpreadsheetSelection.parseRow("3")
                                                .setDefaultAnchor()
                                )
                        ).setNavigations(
                                Lists.of(
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
                () -> SpreadsheetEngineHttps.viewport(
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
                SpreadsheetEngineHttps.viewport(
                        parameters,
                        includeNavigation
                )
        );
    }

    // window...........................................................................................................

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetEngineHttps> type() {
        return SpreadsheetEngineHttps.class;
    }

    @Override
    public boolean canHavePublicTypes(final Method method) {
        return false;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
