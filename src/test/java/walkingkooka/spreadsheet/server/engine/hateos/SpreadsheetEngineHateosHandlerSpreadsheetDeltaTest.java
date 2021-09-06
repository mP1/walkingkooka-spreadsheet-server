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

import org.junit.jupiter.api.Test;
import walkingkooka.Cast;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaTest extends SpreadsheetEngineHateosHandlerTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDelta<?>> {

    @Test
    public void testSelectionAbsent() {
        this.prepareResponseAndCheck(
                Optional.of(SpreadsheetDelta.EMPTY),
                HateosHandler.NO_PARAMETERS,
                SpreadsheetDelta.NO_SELECTION
        );
    }

    @Test
    public void testSelectionPresentInputSpreadsheetDelta() {
        final Optional<SpreadsheetSelection> selection = Optional.of(
                SpreadsheetSelection.parseCellRange("A1:B2")
        );

        this.prepareResponseAndCheck(
                Optional.of(
                        SpreadsheetDelta.EMPTY.setSelection(selection)
                ),
                HateosHandler.NO_PARAMETERS,
                selection
        );
    }

    @Test
    public void testSelectionQueryParameters() {
        final SpreadsheetSelection selection = SpreadsheetSelection.parseCellRange("C3:D4");

        this.prepareResponseAndCheck(
                Optional.of(
                        SpreadsheetDelta.EMPTY
                ),
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDelta.SELECTION, Lists.of(selection.toString()),
                        SpreadsheetEngineHateosHandlerSpreadsheetDelta.SELECTION_TYPE, Lists.of("cell-range")
                ),
                Optional.of(
                        selection
                )
        );
    }

    @Test
    public void testSelectionQueryParameters2() {
        final SpreadsheetSelection selection = SpreadsheetSelection.parseCellRange("C3:D4");

        this.prepareResponseAndCheck(
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetCell.with(
                                                        SpreadsheetSelection.parseCell("Z9"),
                                                        SpreadsheetFormula.with("=1+2")
                                                )
                                        )
                                )
                ),
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDelta.SELECTION, Lists.of(selection.toString()),
                        SpreadsheetEngineHateosHandlerSpreadsheetDelta.SELECTION_TYPE, Lists.of("cell-range")
                ),
                Optional.of(
                        selection
                )
        );
    }

    private void prepareResponseAndCheck(final Optional<SpreadsheetDelta> input,
                                         final Map<HttpRequestAttribute<?>, Object> parameters,
                                         final Optional<SpreadsheetSelection> expected) {
        final SpreadsheetDelta response = new SpreadsheetEngineHateosHandlerSpreadsheetDelta<Integer>(null, null) {

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

        assertEquals(
                expected,
                response.selection(),
                () -> "input=" + input + " parameters=" + parameters
        );
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDelta<?>> type() {
        return Cast.to(SpreadsheetEngineHateosHandlerSpreadsheetDelta.class);
    }
}
