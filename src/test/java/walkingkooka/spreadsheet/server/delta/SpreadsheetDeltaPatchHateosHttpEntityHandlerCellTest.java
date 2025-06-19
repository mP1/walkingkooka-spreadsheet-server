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
import walkingkooka.collect.set.Sets;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetViewportRectangle;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.tree.text.FontStyle;
import walkingkooka.tree.text.TextStyle;
import walkingkooka.tree.text.TextStylePropertyName;

import java.util.Optional;
import java.util.Set;

public final class SpreadsheetDeltaPatchHateosHttpEntityHandlerCellTest extends SpreadsheetDeltaPatchHateosHttpEntityHandlerTestCase<SpreadsheetDeltaPatchHateosHttpEntityHandlerCell,
    SpreadsheetCellReference,
    SpreadsheetCellRangeReference> {

    // handleOne........................................................................................................

    @Test
    public void testHandleOneWithPatchCellOutsideFails() {
        final IllegalArgumentException thrown = this.handleOneFails(
            this.createHandler(
                this.spreadsheetEngine(
                    CELL.toCellRange(),
                    Sets.of(
                        CELL.setFormula(
                            SpreadsheetFormula.EMPTY.setText("=1")
                        )
                    ), // load
                    Sets.empty() // save
                )
            ),
            CELL,
            this.httpEntity(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.parseCell("Z99")
                                .setFormula(
                                    SpreadsheetFormula.EMPTY.setText("=2")
                                )
                        )
                    )
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(),
            this.path(),
            this.context(),
            IllegalArgumentException.class
        );

        this.checkEquals(
            "Patch includes cells Z99 outside " + CELL,
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleOne() {
        final SpreadsheetCell patch = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='patched"));

        final TextStyle style = TextStyle.EMPTY.set(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC);
        final SpreadsheetCell loaded = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='before"))
            .setStyle(style);
        final SpreadsheetCell saved = patch.setStyle(style);

        this.handleOneAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    CELL.toCellRange(),
                    Sets.of(loaded),
                    Sets.of(saved)
                )
            ),
            CELL,
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(patch)
                )
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(saved)
                )
            )
        );
    }

    @Test
    public void testHandleOnWithLabel() {
        final SpreadsheetCell patch = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='patched"));

        final TextStyle style = TextStyle.EMPTY.set(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC);
        final SpreadsheetCell loaded = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='before"))
            .setStyle(style);
        final SpreadsheetCell saved = patch.setStyle(style);

        this.handleOneAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    CELL.toCellRange(),
                    Sets.of(loaded),
                    Sets.of(saved)
                )
            ),
            CELL,
            this.httpEntity(
                this.marshall(
                    SpreadsheetDelta.EMPTY.setCells(
                        Sets.of(patch)
                    )
                ).replace(CELL.toString(), LABEL.toString())
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(saved)
                )
            )
        );
    }

    @Test
    public void testHandleOneWithQueryStringViewport() {
        final SpreadsheetCell patch = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='patched"));

        final TextStyle style = TextStyle.EMPTY.set(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC);
        final SpreadsheetCell loaded = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='before"))
            .setStyle(style);
        final SpreadsheetCell saved = patch.setStyle(style);

        this.handleOneAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    CELL.toCellRange(),
                    Sets.of(loaded),
                    Sets.of(saved)
                )
            ),
            CELL,
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(patch)
                )
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=" + CELL + "&width=" + WIDTH + "&height=" + HEIGHT + "&selectionType=cell&selection=" + CELL + "&includeFrozenColumnsRows=true" // queryString
            ),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(saved)
                    ).setViewport(
                        Optional.of(
                            CELL.viewportRectangle(
                                    WIDTH,
                                    HEIGHT
                                ).viewport()
                                .setAnchoredSelection(
                                    Optional.of(
                                        CELL.setDefaultAnchor()
                                    )
                                )
                        )
                    ).setWindow(WINDOWS)
            )
        );
    }

    @Test
    public void testHandleOneWithCellInsideQueryStringWindow() {
        final SpreadsheetCell patch = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='patched"));

        final TextStyle style = TextStyle.EMPTY.set(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC);
        final SpreadsheetCell loaded = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='before"))
            .setStyle(style);
        final SpreadsheetCell saved = patch.setStyle(style);

        this.handleOneAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    CELL.toCellRange(),
                    Sets.of(loaded),
                    Sets.of(saved)
                )
            ),
            CELL,
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(patch)
                )
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=A1&width=" + WIDTH + "&height=" + HEIGHT + "&window=" + CELL // queryString
            ),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(saved)
                    ).setViewport(
                        Optional.of(
                            SpreadsheetSelection.A1.viewportRectangle(
                                WIDTH,
                                HEIGHT
                            ).viewport()
                        )
                    ).setWindow(
                        SpreadsheetViewportWindows.parse(CELL.toString())
                    )
            )
        );
    }

    @Test
    public void testHandleOneWithCellOutsideQueryStringWindow() {
        final SpreadsheetCell patch = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='patched"));

        final TextStyle style = TextStyle.EMPTY.set(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC);
        final SpreadsheetCell loaded = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='before"))
            .setStyle(style);
        final SpreadsheetCell saved = patch.setStyle(style);

        this.handleOneAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    CELL.toCellRange(),
                    Sets.of(loaded),
                    Sets.of(saved)
                )
            ),
            CELL,
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(patch)
                )
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=A1&width=" + WIDTH + "&height=" + HEIGHT + "&window=" + OUTSIDE_WINDOW // queryString
            ),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setViewport(
                    Optional.of(
                        SpreadsheetSelection.A1.viewportRectangle(
                            WIDTH,
                            HEIGHT
                        ).viewport()
                    )
                ).setWindow(SpreadsheetViewportWindows.parse(OUTSIDE_WINDOW))
            ) // patched cells are outside window so do not appear!
        );
    }

    // handleRange......................................................................................................

    @Test
    public void testHandleRangeWithPatchCellOutsideFails() {
        final IllegalArgumentException thrown = this.handleRangeFails(
            this.createHandler(
                this.spreadsheetEngine(
                    CELL_RANGE,
                    Sets.of(
                        CELL.setFormula(
                            SpreadsheetFormula.EMPTY.setText("=1")
                        )
                    ), // load
                    Sets.empty() // save
                )
            ),
            CELL_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.parseCell("Z99")
                                .setFormula(
                                    SpreadsheetFormula.EMPTY.setText("=2")
                                )
                        )
                    )
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(),
            this.path(),
            this.context(),
            IllegalArgumentException.class
        );

        this.checkEquals(
            thrown.getMessage(),
            "Patch includes cells Z99 outside " + CELL_RANGE
        );
    }

    @Test
    public void testHandleRange() {
        final SpreadsheetCell patch = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='patched"));

        final TextStyle style = TextStyle.EMPTY.set(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC);
        final SpreadsheetCell loaded = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='before"))
            .setStyle(style);
        final SpreadsheetCell saved = patch.setStyle(style);

        this.handleRangeAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    CELL_RANGE,
                    Sets.of(loaded),
                    Sets.of(saved)
                )
            ),
            CELL_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(patch)
                )
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(saved)
                )
            )
        );
    }

    @Test
    public void testHandleRangeWithQueryStringViewport() {
        final SpreadsheetCell patch = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='patched"));

        final TextStyle style = TextStyle.EMPTY.set(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC);
        final SpreadsheetCell loaded = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='before"))
            .setStyle(style);
        final SpreadsheetCell saved = patch.setStyle(style);

        this.handleRangeAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    CELL_RANGE,
                    Sets.of(loaded),
                    Sets.of(saved)
                )
            ),
            CELL_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(patch)
                )
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=" + CELL + "&width=" + WIDTH + "&height=" + HEIGHT + "&selectionType=cell&selection=" + CELL + "&includeFrozenColumnsRows=true" // queryString
            ),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(saved)
                    ).setViewport(
                        Optional.of(
                            CELL.viewportRectangle(
                                    WIDTH,
                                    HEIGHT
                                ).viewport()
                                .setAnchoredSelection(
                                    Optional.of(
                                        CELL.setDefaultAnchor()
                                    )
                                )
                        )
                    ).setWindow(WINDOWS)
            )
        );
    }

    @Test
    public void testHandleRangeWithCellInsideQueryStringWindow() {
        final SpreadsheetCell patch = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='patched"));

        final TextStyle style = TextStyle.EMPTY.set(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC);
        final SpreadsheetCell loaded = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='before"))
            .setStyle(style);
        final SpreadsheetCell saved = patch.setStyle(style);

        this.handleRangeAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    CELL_RANGE,
                    Sets.of(loaded),
                    Sets.of(saved)
                )
            ),
            CELL_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(patch)
                )
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=A1&width=" + WIDTH + "&height=" + HEIGHT + "&window=" + CELL // queryString
            ),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(saved)
                    ).setViewport(
                        Optional.of(
                            SpreadsheetSelection.A1.viewportRectangle(
                                WIDTH,
                                HEIGHT
                            ).viewport()
                        )
                    ).setWindow(SpreadsheetViewportWindows.parse("B2"))
            )
        );
    }

    @Test
    public void testHandleRangeWithCellOutsideQueryStringWindow() {
        final SpreadsheetCell patch = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='patched"));

        final TextStyle style = TextStyle.EMPTY.set(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC);
        final SpreadsheetCell loaded = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("='before"))
            .setStyle(style);
        final SpreadsheetCell saved = patch.setStyle(style);

        this.handleRangeAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    CELL_RANGE,
                    Sets.of(loaded),
                    Sets.of(saved)
                )
            ),
            CELL_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(patch)
                )
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=A1&width=" + WIDTH + "&height=" + HEIGHT + "&window=" + OUTSIDE_WINDOW // queryString
            ),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setViewport(
                    Optional.of(
                        SpreadsheetSelection.A1.viewportRectangle(
                            WIDTH,
                            HEIGHT
                        ).viewport()
                    )
                ).setWindow(SpreadsheetViewportWindows.parse(OUTSIDE_WINDOW))
            ) // patched cells are outside window so do not appear!
        );
    }

    // helpers..........................................................................................................

    SpreadsheetEngine spreadsheetEngine(final SpreadsheetCellRangeReference range,
                                        final Set<SpreadsheetCell> loaded,
                                        final Set<SpreadsheetCell> saved) {
        return new FakeSpreadsheetEngine() {
            @Override
            public SpreadsheetDelta loadCells(final SpreadsheetSelection loadSelection,
                                              final SpreadsheetEngineEvaluation evaluation,
                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                              final SpreadsheetEngineContext context) {
                checkEquals(
                    range,
                    loadSelection,
                    "selection"
                );

                return SpreadsheetDelta.EMPTY
                    .setCells(loaded);
            }

            @Override
            public SpreadsheetDelta saveCells(final Set<SpreadsheetCell> cells,
                                              final SpreadsheetEngineContext context) {
                checkEquals(
                    saved,
                    cells,
                    "saved cells");

                return SpreadsheetDelta.EMPTY
                    .setCells(saved);
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
        };
    }

    @Override
    SpreadsheetDeltaPatchHateosHttpEntityHandlerCell createHandler(final SpreadsheetEngine engine) {
        return SpreadsheetDeltaPatchHateosHttpEntityHandlerCell.with(
            engine
        );
    }

    @Override
    public SpreadsheetCellReference id() {
        return CELL;
    }

    @Override
    public Set<SpreadsheetCellReference> manyIds() {
        return Sets.of(
            SpreadsheetSelection.A1,
            SpreadsheetSelection.parseCell("A2")
        );
    }

    @Override
    public Range<SpreadsheetCellReference> range() {
        return CELL_RANGE.range();
    }

    @Override
    public String typeNameSuffix() {
        return "Cell";
    }

    @Override
    public Class<SpreadsheetDeltaPatchHateosHttpEntityHandlerCell> type() {
        return SpreadsheetDeltaPatchHateosHttpEntityHandlerCell.class;
    }
}
