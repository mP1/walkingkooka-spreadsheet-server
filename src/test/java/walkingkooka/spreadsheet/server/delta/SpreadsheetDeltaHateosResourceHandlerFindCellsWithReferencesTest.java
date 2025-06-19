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
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetCellFindQuery;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContexts;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferencesTest extends SpreadsheetDeltaHateosResourceHandlerTestCase2<SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences,
    SpreadsheetCellReference> {

    private final static int DEFAULT_COUNT = 99;

    @Test
    public void testWithNegativeDefaultCount() {
        assertThrows(
            IllegalArgumentException.class,
            () -> SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences.with(
                -1,
                SpreadsheetEngines.fake()
            )
        );
    }

    private final static int OFFSET = 12;
    private final static int COUNT = 34;

    private final static SpreadsheetLabelName LABEL = SpreadsheetSelection.labelName("Label123");

    @Test
    public void testHandleOne() {
        final SpreadsheetCellReference a1 = SpreadsheetSelection.A1;
        final SpreadsheetDelta expected = SpreadsheetDelta.EMPTY.setLabels(
            Sets.of(
                LABEL.setLabelMappingReference(a1)
            )
        );

        this.handleOneAndCheck(
            SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences.with(
                99,
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta findCellsWithReference(final SpreadsheetExpressionReference reference,
                                                                   final int offset,
                                                                   final int count,
                                                                   final SpreadsheetEngineContext context) {
                        checkEquals(a1, reference);
                        checkEquals(OFFSET, offset);
                        checkEquals(COUNT, count);
                        return expected;
                    }
                }
            ),
            a1, // reference
            Optional.empty(), // resource
            Maps.of(
                SpreadsheetCellFindQuery.OFFSET, Lists.of("" + OFFSET),
                SpreadsheetCellFindQuery.COUNT, Lists.of("" + COUNT)
            ), // parameters
            UrlPath.EMPTY,
            SpreadsheetEngineHateosResourceHandlerContexts.fake(),
            Optional.of(
                expected
            )
        );
    }

    @Test
    public void testHandleOneMissingOffsetAndMissingCount() {
        final SpreadsheetCellReference a1 = SpreadsheetSelection.A1;
        final SpreadsheetDelta expected = SpreadsheetDelta.EMPTY.setLabels(
            Sets.of(
                LABEL.setLabelMappingReference(a1)
            )
        );

        this.handleOneAndCheck(
            SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences.with(
                99,
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta findCellsWithReference(final SpreadsheetExpressionReference reference,
                                                                   final int offset,
                                                                   final int count,
                                                                   final SpreadsheetEngineContext context) {
                        checkEquals(a1, reference);
                        checkEquals(0, offset);
                        checkEquals(DEFAULT_COUNT, count);
                        return expected;
                    }
                }
            ),
            a1, // reference
            Optional.empty(), // resource
            HateosResourceHandler.NO_PARAMETERS, // parameters
            UrlPath.EMPTY,
            SpreadsheetEngineHateosResourceHandlerContexts.fake(),
            Optional.of(
                expected
            )
        );
    }

    @Test
    public void testHandleRange() {
        final SpreadsheetCellRangeReference b2c3 = SpreadsheetSelection.parseCellRange("B2:C3");
        final SpreadsheetDelta expected = SpreadsheetDelta.EMPTY.setLabels(
            Sets.of(
                LABEL.setLabelMappingReference(b2c3)
            )
        );

        this.handleRangeAndCheck(
            SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences.with(
                99,
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta findCellsWithReference(final SpreadsheetExpressionReference reference,
                                                                   final int offset,
                                                                   final int count,
                                                                   final SpreadsheetEngineContext context) {
                        checkEquals(b2c3, reference);
                        checkEquals(OFFSET, offset);
                        checkEquals(COUNT, count);
                        return expected;
                    }
                }
            ),
            b2c3.range(), // reference
            Optional.empty(), // resource
            Maps.of(
                SpreadsheetCellFindQuery.OFFSET, Lists.of("" + OFFSET),
                SpreadsheetCellFindQuery.COUNT, Lists.of("" + COUNT)
            ), // parameters
            UrlPath.EMPTY,
            SpreadsheetEngineHateosResourceHandlerContexts.fake(),
            Optional.of(
                expected
            )
        );
    }

    @Test
    public void testHandleAll() {
        final SpreadsheetDelta expected = SpreadsheetDelta.EMPTY.setLabels(
            Sets.of(
                LABEL.setLabelMappingReference(SpreadsheetSelection.A1)
            )
        );

        this.handleAllAndCheck(
            SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences.with(
                99,
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta findCellsWithReference(final SpreadsheetExpressionReference reference,
                                                                   final int offset,
                                                                   final int count,
                                                                   final SpreadsheetEngineContext context) {
                        checkEquals(SpreadsheetSelection.ALL_CELLS, reference);
                        checkEquals(OFFSET, offset);
                        checkEquals(COUNT, count);
                        return expected;
                    }
                }
            ),
            Optional.empty(), // resource
            Maps.of(
                SpreadsheetCellFindQuery.OFFSET, Lists.of("" + OFFSET),
                SpreadsheetCellFindQuery.COUNT, Lists.of("" + COUNT)
            ), // parameters
            UrlPath.EMPTY,
            SpreadsheetEngineHateosResourceHandlerContexts.fake(),
            Optional.of(
                expected
            )
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createHandler(),
            SpreadsheetEngine.class.getSimpleName() + ".findCellsWithReferences"
        );
    }

    @Override
    SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences createHandler(final SpreadsheetEngine engine) {
        return SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences.with(
            DEFAULT_COUNT,
            engine
        );
    }

    @Override
    SpreadsheetEngine engine() {
        return SpreadsheetEngines.fake();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public SpreadsheetCellReference id() {
        return SpreadsheetSelection.A1;
    }

    @Override
    public Range<SpreadsheetCellReference> range() {
        return SpreadsheetSelection.parseCellRange("A1:B2")
            .range(); // url has TO
    }

    @Override
    public Optional<SpreadsheetDelta> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetDelta> collectionResource() {
        return Optional.empty();
    }

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return SpreadsheetEngineHateosResourceHandlerContexts.fake();
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences> type() {
        return SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences.class;
    }
}
