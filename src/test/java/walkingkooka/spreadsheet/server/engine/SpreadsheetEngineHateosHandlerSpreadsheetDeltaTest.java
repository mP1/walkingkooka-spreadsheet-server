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

import org.junit.jupiter.api.Test;
import walkingkooka.Cast;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportAnchor;

import java.util.Map;
import java.util.Optional;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaTest extends SpreadsheetEngineHateosHandlerTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDelta<?>> {

    @Test
    public void testPrepareResponseViewportAbsent() {
        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setDeletedCells(
                Sets.of(
                        SpreadsheetSelection.A1
                )
        );

        this.prepareResponseAndCheck(
                Optional.of(delta),
                HateosHandler.NO_PARAMETERS,
                delta,
                delta
        );
    }

    @Test
    public void testPrepareResponseViewportWithoutSelectionIgnored() {
        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setDeletedCells(
                Sets.of(
                        SpreadsheetSelection.A1
                )
        );

        final Optional<SpreadsheetViewport> viewport = Optional.of(
                SpreadsheetSelection.A1.viewportRectangle(
                        100,
                        30
                ).viewport()
        );

        this.prepareResponseAndCheck(
                Optional.of(
                        delta.setViewport(viewport)
                ),
                HateosHandler.NO_PARAMETERS,
                delta,
                delta
        );
    }

    @Test
    public void testPrepareResponseViewportWithSelectionIgnored() {
        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setDeletedCells(
                Sets.of(
                        SpreadsheetSelection.A1
                )
        );

        final Optional<SpreadsheetViewport> viewport = Optional.of(
                SpreadsheetSelection.A1.viewportRectangle(
                                100,
                                30
                        ).viewport()
                        .setAnchoredSelection(
                                Optional.of(
                                        SpreadsheetSelection.parseCell("B2")
                                                .setDefaultAnchor()
                                )
                        )
        );

        this.prepareResponseAndCheck(
                Optional.of(
                        delta.setViewport(viewport)
                ),
                HateosHandler.NO_PARAMETERS,
                delta,
                delta
        );
    }

    @Test
    public void testPrepareResponseWindowParameter() {
        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setDeletedCells(
                Sets.of(
                        SpreadsheetSelection.A1
                )
        );

        final Optional<SpreadsheetViewport> viewport = Optional.of(
                SpreadsheetSelection.A1.viewportRectangle(
                                100,
                                30
                        ).viewport()
                        .setAnchoredSelection(
                                Optional.of(
                                        SpreadsheetSelection.parseCell("B2")
                                                .setDefaultAnchor()
                                )
                        )
        );

        final SpreadsheetViewportWindows windows = SpreadsheetViewportWindows.parse("A1:B2,C3:D4");

        this.prepareResponseAndCheck(
                Optional.of(
                        delta.setViewport(viewport)
                ),
                Map.of(
                        SpreadsheetEngineHttps.WINDOW, Lists.of(windows.toString())
                ),
                delta,
                delta.setWindow(windows)
        );
    }

    @Test
    public void testPrepareResponseWindowIgnoresSelectionQueryParameters() {
        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setDeletedCells(
                Sets.of(
                        SpreadsheetSelection.A1
                )
        );

        final SpreadsheetCellReference home = SpreadsheetSelection.parseCell("B2");
        final int width = 23;
        final int height = 45;
        final SpreadsheetSelection selection = SpreadsheetSelection.parseCellRange("C3:D4");
        final SpreadsheetViewportAnchor anchor = SpreadsheetViewportAnchor.TOP_LEFT;

        final SpreadsheetViewportWindows windows = SpreadsheetViewportWindows.parse("A1:B2,C3:D4");

        this.prepareResponseAndCheck(
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("Z9")
                                                        .setFormula(
                                                                SpreadsheetFormula.EMPTY
                                                                        .setText("=1+2")
                                                        )
                                        )
                                )
                ),
                Map.of(
                        SpreadsheetEngineHttps.HOME, Lists.of(home.toString()),
                        SpreadsheetEngineHttps.WIDTH, Lists.of(String.valueOf(width)),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of(String.valueOf(height)),
                        SpreadsheetEngineHttps.SELECTION, Lists.of(selection.toString()),
                        SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("cell-range"),
                        SpreadsheetEngineHttps.SELECTION_ANCHOR, Lists.of(anchor.kebabText()),
                        SpreadsheetEngineHttps.WINDOW, Lists.of(windows.toString())
                ),
                delta,
                delta.setWindow(windows)
        );
    }

    @Test
    public void testPrepareResponseWindowIgnoresInvalidSelectionQueryParameters() {
        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setDeletedCells(
                Sets.of(
                        SpreadsheetSelection.A1
                )
        );

        final SpreadsheetViewportWindows windows = SpreadsheetViewportWindows.parse("A1:B2,C3:D4");

        this.prepareResponseAndCheck(
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("Z9")
                                                        .setFormula(
                                                                SpreadsheetFormula.EMPTY
                                                                        .setText("=1+2")
                                                        )
                                        )
                                )
                ),
                Map.of(
                        SpreadsheetEngineHttps.HOME, Lists.of("!invalidhome"),
                        SpreadsheetEngineHttps.WIDTH, Lists.of("!invalid-width"),
                        SpreadsheetEngineHttps.HEIGHT, Lists.of("!invalid height"),
                        SpreadsheetEngineHttps.SELECTION, Lists.of("!invalid selection"),
                        SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("!invalid selection type"),
                        SpreadsheetEngineHttps.SELECTION_ANCHOR, Lists.of("!invalid anchor"),
                        SpreadsheetEngineHttps.WINDOW, Lists.of(windows.toString())
                ),
                delta,
                delta.setWindow(windows)
        );
    }

    private void prepareResponseAndCheck(final Optional<SpreadsheetDelta> input,
                                         final Map<HttpRequestAttribute<?>, Object> parameters,
                                         final SpreadsheetDelta output,
                                         final SpreadsheetDelta expected) {
        final SpreadsheetDelta response = new SpreadsheetEngineHateosHandlerSpreadsheetDelta<Integer>(
                SpreadsheetEngines.fake(),
                SpreadsheetEngineContexts.fake()
        ) {

            @Override
            public Optional<SpreadsheetDelta> handleAll(final Optional<SpreadsheetDelta> optional,
                                                        final Map<HttpRequestAttribute<?>, Object> map) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<SpreadsheetDelta> handleOne(final Integer integer,
                                                        final Optional<SpreadsheetDelta> resource,
                                                        final Map<HttpRequestAttribute<?>, Object> map) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<SpreadsheetDelta> handleRange(final Range<Integer> range,
                                                          final Optional<SpreadsheetDelta> resource,
                                                          final Map<HttpRequestAttribute<?>, Object> map) {
                throw new UnsupportedOperationException();
            }

            @Override
            String operation() {
                throw new UnsupportedOperationException();
            }
        }.prepareResponse(
                input,
                parameters,
                output
        );

        this.checkEquals(
                expected,
                response,
                () -> "input=" + input + " parameters=" + parameters
        );
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDelta<?>> type() {
        return Cast.to(SpreadsheetEngineHateosHandlerSpreadsheetDelta.class);
    }
}
