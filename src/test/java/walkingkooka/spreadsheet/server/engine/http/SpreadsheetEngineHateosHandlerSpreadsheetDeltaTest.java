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
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportAnchor;

import java.util.Map;
import java.util.Optional;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaTest extends SpreadsheetEngineHateosHandlerTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDelta<?>> {

    @Test
    public void testViewportSelectionAbsent() {
        this.prepareResponseAndCheck(
                Optional.of(SpreadsheetDelta.EMPTY),
                HateosHandler.NO_PARAMETERS,
                SpreadsheetDelta.NO_VIEWPORT_SELECTION
        );
    }

    @Test
    public void testViewportSelectionPresentInputSpreadsheetDelta() {
        final Optional<SpreadsheetViewport> viewportSelection = Optional.of(
                SpreadsheetSelection.parseCell("B2")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
        );

        this.prepareResponseAndCheck(
                Optional.of(
                        SpreadsheetDelta.EMPTY.setViewport(viewportSelection)
                ),
                HateosHandler.NO_PARAMETERS,
                viewportSelection
        );
    }

    @Test
    public void testSelectionQueryParameters() {
        final SpreadsheetSelection selection = SpreadsheetSelection.parseCell("C3");

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
                Maps.of(
                        SpreadsheetEngineHttps.SELECTION, Lists.of(selection.toString()),
                        SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("cell")
                ),
                Optional.of(
                        selection.setAnchor(SpreadsheetViewportAnchor.NONE)
                )
        );
    }

    @Test
    public void testSelectionQueryParameters2() {
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
                Maps.of(
                        SpreadsheetEngineHttps.SELECTION, Lists.of(selection.toString()),
                        SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("cell-range"),
                        SpreadsheetEngineHttps.SELECTION_ANCHOR, Lists.of(anchor.kebabText())
                ),
                Optional.of(
                        selection.setAnchor(anchor)
                )
        );
    }

    private void prepareResponseAndCheck(final Optional<SpreadsheetDelta> input,
                                         final Map<HttpRequestAttribute<?>, Object> parameters,
                                         final Optional<SpreadsheetViewport> expected) {
        final SpreadsheetDelta response = new SpreadsheetEngineHateosHandlerSpreadsheetDelta<Integer>(
                new FakeSpreadsheetEngine() {
                    @Override
                    public Optional<SpreadsheetViewport> navigate(final SpreadsheetViewport viewportSelection,
                                                                           final SpreadsheetEngineContext context) {
                        return Optional.of(viewportSelection);
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
