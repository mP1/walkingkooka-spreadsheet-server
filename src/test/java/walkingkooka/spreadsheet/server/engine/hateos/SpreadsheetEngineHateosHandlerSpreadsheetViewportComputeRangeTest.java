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
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SpreadsheetEngineHateosHandlerSpreadsheetViewportComputeRangeTest extends SpreadsheetEngineHateosHandlerTestCase2<SpreadsheetEngineHateosHandlerSpreadsheetViewportComputeRange,
        SpreadsheetViewport,
        SpreadsheetRange,
        SpreadsheetRange> {

    @Test
    public void testComputeRange() {
        final SpreadsheetViewport viewport = SpreadsheetCellReference.parseCellReference("B99").viewport(100, 20);
        final SpreadsheetRange range = SpreadsheetRange.parseRange("B99:C102");

        this.handleOneAndCheck(this.createHandler(
                new FakeSpreadsheetEngine() {
                    @Override
                    public SpreadsheetRange computeRange(final SpreadsheetViewport v) {
                        assertEquals(viewport, v, "viewport");
                        return range;
                    }
                },
                this.engineContext()),
                viewport,
                Optional.empty(),
                Maps.empty(),
                Optional.of(range));
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createHandler(), "SpreadsheetEngine.computeRange");
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetViewportComputeRange createHandler(final SpreadsheetEngine engine,
                                                                                final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetViewportComputeRange.with(engine, context);
    }

    @Override
    SpreadsheetEngine engine() {
        return SpreadsheetEngines.fake();
    }

    @Override
    public SpreadsheetViewport id() {
        return SpreadsheetCellReference.parseCellReference("A1").viewport(100, 20);
    }

    @Override
    public List<SpreadsheetViewport> list() {
        return Lists.of(this.id());
    }

    @Override
    public Range<SpreadsheetViewport> range() {
        return Range.all();
    }

    @Override
    public Optional<SpreadsheetRange> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetRange> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetViewportComputeRange> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetViewportComputeRange.class;
    }
}