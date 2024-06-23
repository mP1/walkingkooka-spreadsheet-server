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
import walkingkooka.collect.set.Sets;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.FakeHttpRequest;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetViewportRectangle;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.tree.json.JsonNode;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetDeltaPatchFunctionColumnTest extends SpreadsheetDeltaPatchFunctionTestCase<SpreadsheetDeltaPatchFunctionColumn, SpreadsheetColumnRangeReference> {

    private final static SpreadsheetColumnRangeReference RANGE = SpreadsheetExpressionReference.parseColumnRange("C:D");
    private final static SpreadsheetCellRangeReference CELL_RANGES = SpreadsheetSelection.parseCellRange("B2:E5");
    private final static SpreadsheetViewportWindows WINDOWS = SpreadsheetViewportWindows.with(
            Sets.of(
                    CELL_RANGES
            )
    );

    @Test
    public void testPatchColumnOutOfRangeFails() {
        final JsonNode patch = marshall(
                SpreadsheetDelta.EMPTY
                        .setColumns(
                                Sets.of(
                                        SpreadsheetSelection.parseColumn("Z")
                                                .column()
                                )
                        )
        );

        final IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetDeltaPatchFunctionColumn.with(
                        new FakeHttpRequest() {
                            @Override
                            public RelativeUrl url() {
                                return Url.parseRelative("/column/" + RANGE + "?window=" + CELL_RANGES);
                            }
                        },
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta loadColumn(final SpreadsheetColumnReference column,
                                                               final SpreadsheetEngineContext context) {
                                return SpreadsheetDelta.EMPTY;
                            }


                        },
                        CONTEXT
                ).apply(patch)
        );
        this.checkEquals(
                "Patch columns: C:D includes invalid column Z",
                thrown.getMessage(),
                "message"
        );
    }

    @Test
    public void testApply() {
        this.applyAndCheck2(
                "", // queryString
                Optional.empty(), // viewport
                SpreadsheetViewportWindows.EMPTY
        );
    }

    @Test
    public void testApplySelectionQueryParameter() {
        this.applyAndCheck2(
                "?home=A1&width=" + WIDTH + "&height=" + HEIGHT + "&selectionType=cell&selection=C3&window=",
                Optional.of(
                        SpreadsheetSelection.A1.viewportRectangle(
                                        WIDTH,
                                        HEIGHT
                                ).viewport()
                                .setAnchoredSelection(
                                        Optional.of(
                                                SpreadsheetSelection.parseCell("C3")
                                                        .setDefaultAnchor()
                                        )
                                )
                ),
                SpreadsheetViewportWindows.EMPTY
        );
    }

    private void applyAndCheck2(final String queryString,
                                final Optional<SpreadsheetViewport> viewport,
                                final SpreadsheetViewportWindows windows) {
        final SpreadsheetColumn c = SpreadsheetSelection.parseColumn("C")
                .column()
                .setHidden(true);
        final SpreadsheetColumn d = SpreadsheetSelection.parseColumn("D")
                .column()
                .setHidden(true);

        final SpreadsheetDelta request = SpreadsheetDelta.EMPTY
                .setColumns(
                        Sets.of(
                                c, d
                        )
                ).setWindow(windows);
        final SpreadsheetDelta response = SpreadsheetDelta.EMPTY
                .setColumns(
                        Sets.of(
                                c, d
                        )
                ).setViewport(viewport)
                .setWindow(windows);

        final Set<SpreadsheetColumn> saved = Sets.ordered();

        this.applyAndCheck(
                SpreadsheetDeltaPatchFunctionColumn.with(
                        new FakeHttpRequest() {
                            @Override
                            public RelativeUrl url() {
                                return Url.parseRelative("/column/" + RANGE + queryString);
                            }
                        },
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta loadColumn(final SpreadsheetColumnReference columnReference,
                                                               final SpreadsheetEngineContext context) {
                                if (c.reference().equalsIgnoreReferenceKind(columnReference)) {
                                    return SpreadsheetDelta.EMPTY.setColumns(
                                            Sets.of(
                                                    c.setHidden(false)
                                            )
                                    );
                                }
                                if (d.reference().equalsIgnoreReferenceKind(columnReference)) {
                                    return SpreadsheetDelta.EMPTY.setColumns(
                                            Sets.of(
                                                    d.setHidden(false)
                                            )
                                    );
                                }
                                return SpreadsheetDelta.EMPTY;
                            }

                            @Override
                            public SpreadsheetDelta saveColumn(final SpreadsheetColumn c,
                                                               final SpreadsheetEngineContext context) {
                                checkEquals(
                                        true,
                                        c.hidden(),
                                        () -> "saved columns should be hidden=" + c
                                );

                                saved.add(c);

                                return SpreadsheetDelta.EMPTY.setColumns(
                                        Sets.of(c)
                                );
                            }

                            @Override
                            public SpreadsheetDelta loadCells(final Set<SpreadsheetCellRangeReference> range,
                                                              final SpreadsheetEngineEvaluation evaluation,
                                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                              final SpreadsheetEngineContext context) {
                                return SpreadsheetDelta.EMPTY;
                            }

                            @Override
                            public Optional<SpreadsheetViewport> navigate(final SpreadsheetViewport viewport,
                                                                                   final SpreadsheetEngineContext context) {
                                return Optional.of(viewport);
                            }
                        },
                        CONTEXT
                ),
                marshall(request),
                marshall(response)
        );

        this.checkEquals(
                Sets.of(
                        c, d
                ),
                saved,
                "saved columns"
        );
    }

    @Test
    public void testLoadsUnhiddenColumnCells() {
        final String queryString = "?home=A1&width=1000&height=500&selectionType=cell&selection=C3&includeFrozenColumnsRows=false";

        final Optional<SpreadsheetViewport> viewport = Optional.of(
                SpreadsheetSelection.A1.viewportRectangle(
                                1000,
                                500
                        ).viewport()
                        .setAnchoredSelection(
                                Optional.of(
                                        SpreadsheetSelection.parseCell("C3")
                                                .setDefaultAnchor()
                                )
                        )
        );

        final SpreadsheetColumn c = SpreadsheetSelection.parseColumn("C")
                .column()
                .setHidden(true);
        final SpreadsheetColumn d = SpreadsheetSelection.parseColumn("D")
                .column()
                .setHidden(true);

        final SpreadsheetDelta request = SpreadsheetDelta.EMPTY
                .setColumns(
                        Sets.of(
                                c, d
                        )
                ).setWindow(
                        SpreadsheetViewportWindows.parse("A1:Z999") // ignored because of query parameters
                );

        final SpreadsheetCell c3 = c.reference()
                .setRow(SpreadsheetSelection.parseRow("3"))
                .setFormula(
                        SpreadsheetFormula.EMPTY.setText("=1")
                );

        final SpreadsheetCell d4 = d.reference()
                .setRow(SpreadsheetSelection.parseRow("4"))
                .setFormula(
                        SpreadsheetFormula.EMPTY.setText("'D4")
                );

        final SpreadsheetDelta response = SpreadsheetDelta.EMPTY
                .setColumns(
                        Sets.of(
                                c, d
                        )
                ).setViewport(viewport)
                .setWindow(WINDOWS)
                .setCells(
                        Sets.of(
                                c3, d4
                        )
                );

        final Set<SpreadsheetColumn> saved = Sets.ordered();

        this.applyAndCheck(
                SpreadsheetDeltaPatchFunctionColumn.with(
                        new FakeHttpRequest() {
                            @Override
                            public RelativeUrl url() {
                                return Url.parseRelative("/column/" + RANGE + queryString);
                            }
                        },
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta loadColumn(final SpreadsheetColumnReference columnReference,
                                                               final SpreadsheetEngineContext context) {
                                if (c.reference().equalsIgnoreReferenceKind(columnReference)) {
                                    return SpreadsheetDelta.EMPTY.setColumns(
                                            Sets.of(
                                                    c.setHidden(false)
                                            )
                                    );
                                }
                                if (d.reference().equalsIgnoreReferenceKind(columnReference)) {
                                    return SpreadsheetDelta.EMPTY.setColumns(
                                            Sets.of(
                                                    d.setHidden(false)
                                            )
                                    );
                                }
                                return SpreadsheetDelta.EMPTY;
                            }

                            @Override
                            public SpreadsheetDelta saveColumn(final SpreadsheetColumn c,
                                                               final SpreadsheetEngineContext context) {
                                checkEquals(
                                        true,
                                        c.hidden(),
                                        () -> "saved columns should be hidden=" + c
                                );

                                saved.add(c);

                                return SpreadsheetDelta.EMPTY.setColumns(
                                        Sets.of(c)
                                );
                            }

                            @Override
                            public SpreadsheetDelta loadCells(final Set<SpreadsheetCellRangeReference> range,
                                                              final SpreadsheetEngineEvaluation evaluation,
                                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                              final SpreadsheetEngineContext context) {
                                assertEquals(Sets.of(CELL_RANGES), range, "window");

                                return SpreadsheetDelta.EMPTY
                                        .setCells(
                                                Sets.of(
                                                        c3, d4
                                                )
                                        );
                            }

                            @Override
                            public Optional<SpreadsheetViewport> navigate(final SpreadsheetViewport viewport,
                                                                          final SpreadsheetEngineContext context) {
                                return Optional.of(viewport);
                            }

                            @Override
                            public SpreadsheetViewportWindows window(final SpreadsheetViewportRectangle viewportRectangle,
                                                                     final boolean includeFrozenColumnsRows,
                                                                     final Optional<SpreadsheetSelection> selection,
                                                                     final SpreadsheetEngineContext context) {
                                return WINDOWS;
                            }
                        },
                        CONTEXT
                ),
                marshall(request),
                marshall(response)
        );

        this.checkEquals(
                Sets.of(
                        c, d
                ),
                saved,
                "saved columns"
        );
    }


    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createFunction(),
                "Patch column: " + REQUEST + " " + ENGINE + " " + CONTEXT
        );
    }

    @Override
    public SpreadsheetDeltaPatchFunctionColumn createFunction(final HttpRequest request,
                                                              final SpreadsheetEngine engine,
                                                              final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaPatchFunctionColumn.with(
                request,
                engine,
                context
        );
    }

    @Override
    public Class<SpreadsheetDeltaPatchFunctionColumn> type() {
        return SpreadsheetDeltaPatchFunctionColumn.class;
    }
}
