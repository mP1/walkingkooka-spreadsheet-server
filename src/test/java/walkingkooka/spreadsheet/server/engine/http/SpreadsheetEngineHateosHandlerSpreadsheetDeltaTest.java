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
import walkingkooka.Cast;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportAnchor;

import java.util.Map;
import java.util.Optional;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaTest extends SpreadsheetEngineHateosHandlerTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDelta<?>> {

    @Test
    public void testViewportAbsent() {
        this.prepareResponseAndCheck(
                Optional.of(SpreadsheetDelta.EMPTY),
                HateosHandler.NO_PARAMETERS,
                SpreadsheetDelta.NO_VIEWPORT
        );
    }

    @Test
    public void testViewportPresentInputSpreadsheetDelta() {
        final Optional<SpreadsheetViewport> viewport = Optional.of(
                SpreadsheetSelection.A1.viewportRectangle(
                                100,
                                30
                        ).viewport()
                        .setSelection(
                                Optional.of(
                                        SpreadsheetSelection.parseCell("B2")
                                                .setDefaultAnchor()
                                )
                        )
        );

        this.prepareResponseAndCheck(
                Optional.of(
                        SpreadsheetDelta.EMPTY.setViewport(viewport)
                ),
                HateosHandler.NO_PARAMETERS,
                viewport
        );
    }

    @Test
    public void testSelectionQueryParameters() {
        final SpreadsheetCellReference home = SpreadsheetSelection.parseCell("B2");
        final int width = 23;
        final int height = 45;
        final SpreadsheetSelection selection = SpreadsheetSelection.parseCellRange("C3:D4");
        final SpreadsheetViewportAnchor anchor = SpreadsheetViewportAnchor.TOP_LEFT;

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
                        SpreadsheetEngineHttps.WINDOW, Lists.of("")
                ),
                Optional.of(
                        home.viewportRectangle(
                                        width,
                                        height
                                ).viewport()
                                .setSelection(
                                        Optional.of(
                                                selection.setAnchor(anchor)
                                        )
                                )
                )
        );
    }

    private void prepareResponseAndCheck(final Optional<SpreadsheetDelta> input,
                                         final Map<HttpRequestAttribute<?>, Object> parameters,
                                         final Optional<SpreadsheetViewport> expected) {
        final SpreadsheetDelta response = new SpreadsheetEngineHateosHandlerSpreadsheetDelta<Integer>(
                new FakeSpreadsheetEngine() {
                    @Override
                    public Optional<SpreadsheetViewport> navigate(final SpreadsheetViewport viewport,
                                                                  final SpreadsheetEngineContext context) {
                        return Optional.of(viewport);
                    }
                },
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
        }.prepareResponse(input, parameters, SpreadsheetDelta.EMPTY);

        this.checkEquals(
                expected,
                response.viewport(),
                () -> "input=" + input + " parameters=" + parameters
        );
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDelta<?>> type() {
        return Cast.to(SpreadsheetEngineHateosHandlerSpreadsheetDelta.class);
    }
}
