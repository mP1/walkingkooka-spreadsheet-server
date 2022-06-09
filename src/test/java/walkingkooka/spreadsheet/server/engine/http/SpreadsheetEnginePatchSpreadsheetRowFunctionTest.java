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
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReferenceRange;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelectionAnchor;
import walkingkooka.tree.json.JsonNode;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetEnginePatchSpreadsheetRowFunctionTest extends SpreadsheetEnginePatchTestCase<SpreadsheetEnginePatchSpreadsheetRowFunction, SpreadsheetRowReferenceRange> {

    private final static SpreadsheetRowReferenceRange RANGE = SpreadsheetExpressionReference.parseRowRange("3:4");
    private final static SpreadsheetCellRange WINDOW = SpreadsheetSelection.parseCellRange("B2:E5");

    @Test
    public void testPatchRowOutOfRangeFails() {
        final JsonNode patch = marshall(
                SpreadsheetDelta.EMPTY
                        .setRows(
                                Sets.of(
                                        SpreadsheetSelection.parseRow("999")
                                                .row()
                                )
                        )
        );

        final IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    SpreadsheetEnginePatchSpreadsheetRowFunction.with(
                            new FakeHttpRequest() {
                                @Override
                                public RelativeUrl url() {
                                    return Url.parseRelative("/row/" + RANGE + "?window=" + WINDOW);
                                }
                            },
                            new FakeSpreadsheetEngine() {
                                @Override
                                public SpreadsheetDelta loadRow(final SpreadsheetRowReference row,
                                                                final SpreadsheetEngineContext context) {
                                    return SpreadsheetDelta.EMPTY;
                                }


                            },
                            CONTEXT
                    ).apply(patch);
                });
        this.checkEquals(
                "Patch rows: 3:4 includes invalid row 999",
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
        final SpreadsheetRow row3 = SpreadsheetSelection.parseRow("3")
                .row()
                .setHidden(true);
        final SpreadsheetRow row4 = SpreadsheetSelection.parseRow("4")
                .row()
                .setHidden(true);

        final SpreadsheetDelta request = SpreadsheetDelta.EMPTY
                .setRows(
                        Sets.of(
                                row3, row4
                        )
                ).setWindow(
                        Sets.of(WINDOW)
                );
        final SpreadsheetDelta response = SpreadsheetDelta.EMPTY
                .setRows(
                        Sets.of(
                                row3, row4
                        )
                ).setSelection(viewportSelection)
                .setWindow(
                        Sets.of(WINDOW)
                );

        final Set<SpreadsheetRow> saved = Sets.ordered();

        this.applyAndCheck(
                SpreadsheetEnginePatchSpreadsheetRowFunction.with(
                        new FakeHttpRequest() {
                            @Override
                            public RelativeUrl url() {
                                return Url.parseRelative("/row/" + RANGE + queryString);
                            }
                        },
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta loadRow(final SpreadsheetRowReference rowReference,
                                                            final SpreadsheetEngineContext context) {
                                if (row3.reference().equalsIgnoreReferenceKind(rowReference)) {
                                    return SpreadsheetDelta.EMPTY.setRows(
                                            Sets.of(
                                                    row3.setHidden(false)
                                            )
                                    );
                                }
                                if (row4.reference().equalsIgnoreReferenceKind(rowReference)) {
                                    return SpreadsheetDelta.EMPTY.setRows(
                                            Sets.of(
                                                    row4.setHidden(false)
                                            )
                                    );
                                }
                                return SpreadsheetDelta.EMPTY;
                            }

                            @Override
                            public SpreadsheetDelta saveRow(final SpreadsheetRow c,
                                                            final SpreadsheetEngineContext context) {
                                checkEquals(
                                        true,
                                        c.hidden(),
                                        () -> "saved rows should be hidden=" + c
                                );

                                saved.add(c);

                                return SpreadsheetDelta.EMPTY.setRows(
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
                        row3, row4
                ),
                saved,
                "saved rows"
        );
    }

    @Test
    public void testLoadsUnhiddenRowCells() {
        final String queryString = "?selectionType=cell&selection=C3";

        final Optional<SpreadsheetViewportSelection> viewportSelection = Optional.of(
                SpreadsheetSelection.parseCell("C3")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
        );

        final SpreadsheetRow row3 = SpreadsheetSelection.parseRow("3")
                .row()
                .setHidden(true);
        final SpreadsheetRow row4 = SpreadsheetSelection.parseRow("4")
                .row()
                .setHidden(true);

        final SpreadsheetDelta request = SpreadsheetDelta.EMPTY
                .setRows(
                        Sets.of(
                                row3, row4
                        )
                ).setWindow(
                        Sets.of(WINDOW)
                );

        final SpreadsheetCell c3 = row3.reference()
                .setColumn(SpreadsheetSelection.parseColumn("C"))
                .setFormula(
                        SpreadsheetFormula.EMPTY.setText("=1")
                );

        final SpreadsheetCell d4 = row4.reference()
                .setColumn(SpreadsheetSelection.parseColumn("D"))
                .setFormula(
                        SpreadsheetFormula.EMPTY.setText("'D4")
                );

        final SpreadsheetDelta response = SpreadsheetDelta.EMPTY
                .setRows(
                        Sets.of(
                                row3, row4
                        )
                ).setSelection(viewportSelection)
                .setWindow(
                        Sets.of(WINDOW)
                ).setCells(
                        Sets.of(
                                c3, d4
                        )
                );

        final Set<SpreadsheetRow> saved = Sets.ordered();

        this.applyAndCheck(
                SpreadsheetEnginePatchSpreadsheetRowFunction.with(
                        new FakeHttpRequest() {
                            @Override
                            public RelativeUrl url() {
                                return Url.parseRelative("/row/" + RANGE + queryString);
                            }
                        },
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta loadRow(final SpreadsheetRowReference rowReference,
                                                            final SpreadsheetEngineContext context) {
                                if (row3.reference().equalsIgnoreReferenceKind(rowReference)) {
                                    return SpreadsheetDelta.EMPTY.setRows(
                                            Sets.of(
                                                    row3.setHidden(false)
                                            )
                                    );
                                }
                                if (row4.reference().equalsIgnoreReferenceKind(rowReference)) {
                                    return SpreadsheetDelta.EMPTY.setRows(
                                            Sets.of(
                                                    row4.setHidden(false)
                                            )
                                    );
                                }
                                return SpreadsheetDelta.EMPTY;
                            }

                            @Override
                            public SpreadsheetDelta saveRow(final SpreadsheetRow c,
                                                            final SpreadsheetEngineContext context) {
                                checkEquals(
                                        true,
                                        c.hidden(),
                                        () -> "saved rows should be hidden=" + c
                                );

                                saved.add(c);

                                return SpreadsheetDelta.EMPTY.setRows(
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
                        row3, row4
                ),
                saved,
                "saved rows"
        );
    }


    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createFunction(),
                "Patch row: " + REQUEST + " " + ENGINE + " " + CONTEXT
        );
    }

    @Override
    public SpreadsheetEnginePatchSpreadsheetRowFunction createFunction(final HttpRequest request,
                                                                       final SpreadsheetEngine engine,
                                                                       final SpreadsheetEngineContext context) {
        return SpreadsheetEnginePatchSpreadsheetRowFunction.with(
                request,
                engine,
                context
        );
    }

    @Override
    public Class<SpreadsheetEnginePatchSpreadsheetRowFunction> type() {
        return SpreadsheetEnginePatchSpreadsheetRowFunction.class;
    }
}