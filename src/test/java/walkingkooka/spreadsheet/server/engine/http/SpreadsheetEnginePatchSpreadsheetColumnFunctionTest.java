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
import walkingkooka.collect.set.Sets;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.FakeHttpRequest;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReferenceRange;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelectionAnchor;
import walkingkooka.tree.json.JsonNode;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetEnginePatchSpreadsheetColumnFunctionTest extends SpreadsheetEnginePatchTestCase<SpreadsheetEnginePatchSpreadsheetColumnFunction, SpreadsheetColumnReferenceRange> {

    private final static SpreadsheetColumnReferenceRange RANGE = SpreadsheetExpressionReference.parseColumnRange("C:D");
    private final static SpreadsheetCellRange WINDOW = SpreadsheetSelection.parseCellRange("B2:E5");

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
                () -> {
                    SpreadsheetEnginePatchSpreadsheetColumnFunction.with(
                            new FakeHttpRequest() {
                                @Override
                                public RelativeUrl url() {
                                    return Url.parseRelative("/column/" + RANGE + "?window=" + WINDOW);
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
                    ).apply(patch);
                });
        this.checkEquals(
                "Patch columns: C:D includes invalid column Z",
                thrown.getMessage(),
                "message"
        );
    }

    @Test
    public void testApply() {
        this.applyAndCheck2("", Optional.empty());
    }

    @Test
    public void testApplySelectionQueryParameter() {
        this.applyAndCheck2(
                "?selectionType=cell&selection=C3",
                Optional.of(
                        SpreadsheetSelection.parseCell("C3")
                                .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
                )
        );
    }

    private void applyAndCheck2(final String queryString,
                                final Optional<SpreadsheetViewportSelection> viewportSelection) {
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
                        Sets.of(WINDOW)
                );
        final SpreadsheetDelta response = SpreadsheetDelta.EMPTY
                .setColumns(
                        Sets.of(
                                c, d
                        )
                ).setSelection(viewportSelection)
                .setWindow(
                        Sets.of(WINDOW)
                );

        final Set<SpreadsheetColumn> saved = Sets.ordered();

        this.applyAndCheck(
                SpreadsheetEnginePatchSpreadsheetColumnFunction.with(
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
                            public SpreadsheetDelta loadCells(final Set<SpreadsheetCellRange> range,
                                                              final SpreadsheetEngineEvaluation evaluation,
                                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                              final SpreadsheetEngineContext context) {
                                return SpreadsheetDelta.EMPTY;
                            }

                            @Override
                            public Optional<SpreadsheetViewportSelection> navigate(final SpreadsheetViewportSelection selection,
                                                                                   final SpreadsheetEngineContext context) {
                                return Optional.of(selection);
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
        final String queryString = "?selectionType=cell&selection=C3";

        final Optional<SpreadsheetViewportSelection> viewportSelection = Optional.of(
                SpreadsheetSelection.parseCell("C3")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
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
                        Sets.of(WINDOW)
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
                ).setSelection(viewportSelection)
                .setWindow(
                        Sets.of(WINDOW)
                ).setCells(
                        Sets.of(
                                c3, d4
                        )
                );

        final Set<SpreadsheetColumn> saved = Sets.ordered();

        this.applyAndCheck(
                SpreadsheetEnginePatchSpreadsheetColumnFunction.with(
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
                            public SpreadsheetDelta loadCells(final Set<SpreadsheetCellRange> range,
                                                              final SpreadsheetEngineEvaluation evaluation,
                                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                              final SpreadsheetEngineContext context) {
                                assertEquals(Sets.of(WINDOW), range, "window");

                                return SpreadsheetDelta.EMPTY
                                        .setCells(
                                                Sets.of(
                                                        c3, d4
                                                )
                                        );
                            }

                            @Override
                            public Optional<SpreadsheetViewportSelection> navigate(final SpreadsheetViewportSelection selection,
                                                                                   final SpreadsheetEngineContext context) {
                                return Optional.of(selection);
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
    public SpreadsheetEnginePatchSpreadsheetColumnFunction createFunction(final HttpRequest request,
                                                                          final SpreadsheetEngine engine,
                                                                          final SpreadsheetEngineContext context) {
        return SpreadsheetEnginePatchSpreadsheetColumnFunction.with(
                request,
                engine,
                context
        );
    }

    @Override
    public Class<SpreadsheetEnginePatchSpreadsheetColumnFunction> type() {
        return SpreadsheetEnginePatchSpreadsheetColumnFunction.class;
    }
}
