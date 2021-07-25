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
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetViewport;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCellTest
        extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell,
                SpreadsheetCellReference> {

    private final static SpreadsheetEngineEvaluation EVALUATION = SpreadsheetEngineEvaluation.FORCE_RECOMPUTE;

    @Test
    public void testWithNullEvaluationFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.with(null, this.engine(), this.engineContext()));
    }

    // handle...........................................................................................................

    @Test
    public void testLoadCell() {
        this.handleOneAndCheck(this.id(),
                this.resource(),
                this.parameters(),
                Optional.of(this.spreadsheetDelta()));
    }

    @Test
    public void testLoadCellAndFilter() {
        final SpreadsheetCellReference id = this.id();
        final List<SpreadsheetRange> window = this.window();

        final double width = 50;
        final double height = 20;

        this.handleOneAndCheck(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.with(EVALUATION,
                new FakeSpreadsheetEngine() {
                    @Override
                    public SpreadsheetDelta loadCell(final SpreadsheetCellReference cell,
                                                     final SpreadsheetEngineEvaluation evaluation,
                                                     final SpreadsheetEngineContext context) {
                        assertSame(EVALUATION, evaluation, "evaluation");
                        assertNotNull(context, "context");

                        return SpreadsheetDelta.with(cells())
                                .setLabels(labels());
                    }

                    @Override
                    public double columnWidth(final SpreadsheetColumnReference column,
                                              final SpreadsheetEngineContext context) {
                        return width;
                    }

                    @Override
                    public double rowHeight(final SpreadsheetRowReference row,
                                            final SpreadsheetEngineContext context) {
                        return height;
                    }
                },
                this.engineContext()),
                id,
                Optional.of(SpreadsheetDelta.with(SpreadsheetDelta.NO_CELLS).setWindow(window)),
                this.parameters(),
                Optional.of(
                        SpreadsheetDelta.with(this.cellsWithinWindow())
                        .setLabels(this.labels())
                        .setColumnWidths(Maps.of(SpreadsheetColumnReference.parseColumn("A"), width))
                        .setMaxRowHeights(Maps.of(SpreadsheetRowReference.parseRow("99"), height))
                )
        );
    }

    // handleRange.................................................................................................

    @Test
    public void testBatchLoad() {
        final SpreadsheetCell b1 = this.b1();
        final SpreadsheetCell b2 = this.b2();
        final SpreadsheetCell b3 = this.b3();

        final SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell handler = SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.with(
                EVALUATION,
                new FakeSpreadsheetEngine() {


                    @Override
                    public SpreadsheetDelta loadCells(final SpreadsheetRange range,
                                                      final SpreadsheetEngineEvaluation evaluation,
                                                      final SpreadsheetEngineContext context) {
                        assertSame(EVALUATION, evaluation, "evaluation");
                        assertNotNull(context, "context");

                        return SpreadsheetDelta.with(Sets.of(b1, b2, b3));
                    }

                    @Override
                    public double columnWidth(final SpreadsheetColumnReference column,
                                              final SpreadsheetEngineContext context) {
                        return 0;
                    }

                    @Override
                    public double rowHeight(final SpreadsheetRowReference row,
                                            final SpreadsheetEngineContext context) {
                        return 0;
                    }

                    @Override
                    public String toString() {
                        return "loadCells";
                    }
                },
                this.engineContext()
        );
        this.handleRangeAndCheck(
                handler,
                this.range(),
                this.collectionResource(),
                this.parameters(),
                Optional.of(SpreadsheetDelta.with(Sets.of(b1, b2, b3)))
        );
    }

    @Test
    public void testBatchLoadIndividuallyAndFilterWindow() {
        // B1, B2, B3
        // C1, C2, C3

        final SpreadsheetCell b1 = this.b1();
        final SpreadsheetCell b2 = this.b2();
        final SpreadsheetCell b3 = this.b3();

        final SpreadsheetCell c1 = this.c1();
        final SpreadsheetCell c2 = this.c2();
        final SpreadsheetCell c3 = this.c3();

        final List<SpreadsheetCell> cells = Lists.of(b1, b2, b3, c1, c2, c3);

        final Range<SpreadsheetCellReference> range = this.range();
        final List<SpreadsheetRange> window = this.window();

        this.handleRangeAndCheck(
                SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.with(
                        EVALUATION,
                        new FakeSpreadsheetEngine() {

                            @Override
                            public SpreadsheetDelta loadCells(final SpreadsheetRange range,
                                                              final SpreadsheetEngineEvaluation evaluation,
                                                              final SpreadsheetEngineContext context) {
                                assertSame(EVALUATION, evaluation, "evaluation");
                                assertNotNull(context, "context");

                                final Set<SpreadsheetCell> loaded = cells.stream()
                                        .filter(c -> range.test(c.reference()))
                                        .collect(Collectors.toCollection(() -> Sets.ordered()));

                                loaded.add(cellOutsideWindow());

                                return SpreadsheetDelta.with(loaded);
                            }

                            @Override
                            public double columnWidth(final SpreadsheetColumnReference column,
                                                      final SpreadsheetEngineContext context) {
                                return 0;
                            }

                            @Override
                            public double rowHeight(final SpreadsheetRowReference row,
                                                    final SpreadsheetEngineContext context) {
                                return 0;
                            }
                        },
                        this.engineContext()),
                range,
                Optional.of(SpreadsheetDelta.with(SpreadsheetDelta.NO_CELLS).setWindow(window)),
                this.parameters(),
                Optional.of(SpreadsheetDelta.with(Sets.of(b1, b2, b3))
                )
        );
    }

    // handleAll........................................................................................................

    @Test
    public void testHandleAllMissingHomeParameterFails() {
        this.handleAllFails2(Maps.empty(), "Missing parameter \"home\"");
    }

    @Test
    public void testHandleAllMissingXOffsetParameterFails() {
        this.handleAllFails2(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1")
                ),
                "Missing parameter \"xOffset\""
        );
    }

    @Test
    public void testHandleAllMissingYOffsetParameterFails() {
        this.handleAllFails2(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.X_OFFSET, Lists.of("0")
                ),
                "Missing parameter \"yOffset\""
        );
    }

    @Test
    public void testHandleAllMissingWidthParameterFails() {
        this.handleAllFails2(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.X_OFFSET, Lists.of("0"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.Y_OFFSET, Lists.of("0")
                ),
                "Missing parameter \"width\""
        );
    }

    @Test
    public void testHandleAllMissingHeightParameterFails() {
        this.handleAllFails2(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.X_OFFSET, Lists.of("0"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.Y_OFFSET, Lists.of("0"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("0")
                ),
                "Missing parameter \"height\""
        );
    }

    private void handleAllFails2(final Map<HttpRequestAttribute<?>, Object> parameters, final String message) {
        final IllegalArgumentException thrown = this.handleAllFails(
                Optional.<SpreadsheetDelta>empty(),
                parameters,
                IllegalArgumentException.class
        );
        assertEquals(message, thrown.getMessage(), "message");
    }

    @Test
    public void testHandleAllFiltered() {
        // B1, B2, B3
        // C1, C2, C3

        final SpreadsheetCell b1 = this.b1();
        final SpreadsheetCell b2 = this.b2();
        final SpreadsheetCell b3 = this.b3();

        final SpreadsheetCell c1 = this.c1();
        final SpreadsheetCell c2 = this.c2();
        final SpreadsheetCell c3 = this.c3();

        final Range<SpreadsheetCellReference> range = this.range();
        final List<SpreadsheetRange> window = this.window();

        this.handleAllAndCheck(
                SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.with(
                        EVALUATION,
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta loadCells(final SpreadsheetRange r,
                                                              final SpreadsheetEngineEvaluation evaluation,
                                                              final SpreadsheetEngineContext context) {
                                assertEquals(SpreadsheetRange.with(range), r, "range");
                                assertEquals(EVALUATION, evaluation, "evaluation");

                                return SpreadsheetDelta.with(Sets.of(b1, b2, b3, c1, c2, c3));
                            }

                            @Override
                            public SpreadsheetRange range(final SpreadsheetViewport viewport,
                                                          final SpreadsheetEngineContext context) {
                                assertEquals(SpreadsheetViewport.with(SpreadsheetCellReference.parseCellReference("B2"), 11.0, 22.0, 33.0, 44.0), viewport, "viewport");
                                return SpreadsheetRange.with(range);
                            }

                            @Override
                            public double columnWidth(final SpreadsheetColumnReference column,
                                                      final SpreadsheetEngineContext context) {
                                return 0;
                            }

                            @Override
                            public double rowHeight(final SpreadsheetRowReference row,
                                                    final SpreadsheetEngineContext context) {
                                return 0;
                            }
                        },
                        this.engineContext()),
                //range,
                Optional.of(SpreadsheetDelta.with(SpreadsheetDelta.NO_CELLS).setWindow(window)),
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("B2"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.X_OFFSET, Lists.of("11"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.Y_OFFSET, Lists.of("22"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("33"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("44")
                ),
                Optional.of(
                        SpreadsheetDelta.with(Sets.of(b1, b2, b3))
                )
        );
    }

    // helpers..........................................................................................................

    private SpreadsheetCell b1() {
        return this.cell("B1", "1");
    }

    private SpreadsheetCell b2() {
        return this.cell("B2", "2");
    }

    private SpreadsheetCell b3() {
        return this.cell("B3", "3");
    }

    private SpreadsheetCell c1() {
        return this.cell("c1", "4");
    }

    private SpreadsheetCell c2() {
        return this.cell("c2", "5");
    }

    private SpreadsheetCell c3() {
        return this.cell("c3", "6");
    }

    private SpreadsheetCell z99() {
        return this.cell("z99", "99");
    }

    private SpreadsheetDelta delta(final SpreadsheetCell... cells) {
        return SpreadsheetDelta.with(Sets.of(cells));
    }

    private Optional<SpreadsheetDelta> result(final SpreadsheetCell... cells) {
        return Optional.of(SpreadsheetDelta.with(Sets.of(cells)));
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createHandler().toString(), "SpreadsheetEngine.loadCell " + EVALUATION);
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell createHandler(final SpreadsheetEngine engine,
                                                                         final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.with(EVALUATION,
                engine,
                context);
    }

    @Override
    public SpreadsheetCellReference id() {
        return this.spreadsheetCellReference();
    }

    private SpreadsheetCellReference spreadsheetCellReference() {
        return SpreadsheetExpressionReference.parseCellReference("B2");
    }

    @Override
    public Range<SpreadsheetCellReference> range() {
        return SpreadsheetRange.parseRange("B1:C3").range();
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
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosHandler.NO_PARAMETERS;
    }

    @Override
    SpreadsheetEngine engine() {
        return new FakeSpreadsheetEngine() {
            @Override
            public SpreadsheetDelta loadCell(final SpreadsheetCellReference id,
                                             final SpreadsheetEngineEvaluation evaluation,
                                             final SpreadsheetEngineContext context) {
                Objects.requireNonNull(id, "id");
                Objects.requireNonNull(evaluation, "evaluation");
                Objects.requireNonNull(context, "context");

                assertEquals(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCellTest.this.spreadsheetCellReference(), id, "spreadsheetCellReference");
                assertEquals(EVALUATION, evaluation, "evaluation");
                assertNotEquals(null, context, "context");

                return spreadsheetDelta();
            }

            @Override
            public double columnWidth(final SpreadsheetColumnReference column,
                                      final SpreadsheetEngineContext context) {
                return 0;
            }

            @Override
            public double rowHeight(final SpreadsheetRowReference row,
                                    final SpreadsheetEngineContext context) {
                return 0;
            }
        };
    }

    private SpreadsheetDelta spreadsheetDelta() {
        return SpreadsheetDelta.with(Sets.of(this.cell()));
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.class;
    }
}
