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
import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.reference.SpreadsheetRowRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewport;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewportWindows;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class SpreadsheetDeltaPatchHateosHttpEntityHandlerRowTest extends SpreadsheetDeltaPatchHateosHttpEntityHandlerTestCase<SpreadsheetDeltaPatchHateosHttpEntityHandlerRow,
    SpreadsheetRowReference,
    SpreadsheetRowRangeReference> {
    final static SpreadsheetRowReference ROW = SpreadsheetSelection.parseRow("2");

    final static SpreadsheetRowReference ROW2 = SpreadsheetSelection.parseRow("3");

    final static SpreadsheetRowRangeReference ROW_RANGE = SpreadsheetSelection.parseRowRange("2:3");

    private final static SpreadsheetViewportWindows WINDOWS = SpreadsheetViewportWindows.parse("A1:Z99");

    // handleOne........................................................................................................

    @Test
    public void testHandleOneWithPatchRowOutsideFails() {
        final IllegalArgumentException thrown = this.handleOneFails(
            ROW,
            this.httpEntity(
                SpreadsheetDelta.EMPTY
                    .setRows(
                        Sets.of(
                            SpreadsheetSelection.parseRow("99")
                                .row()
                        )
                    )
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(),
            this.path(),
            this.context(
                this.spreadsheetEngine(
                    ROW_RANGE
                )
            ),
            IllegalArgumentException.class
        );

        this.checkEquals(
            "Patch row(s): " + ROW + " includes invalid row 99",
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleOne() {
        final Set<SpreadsheetRow> loadedRows = Sets.of(
            ROW.row()
                .setHidden(false)
        );
        final Set<SpreadsheetRow> savedRows = Sets.of(
            ROW.row()
                .setHidden(true)
        );

        final Set<SpreadsheetRow> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleOneAndCheck(
            ROW,
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(),
            this.path(),
            this.context(
                this.spreadsheetEngine(
                    ROW_RANGE,
                    loadedRows,
                    cells,
                    storeSaved::add
                )
            ),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
                    .setCells(cells)
            ) // expected loadedCells + savedRows
        );

        this.checkEquals(
            savedRows,
            storeSaved
        );
    }

    @Test
    public void testHandleOneWithQueryStringViewport() {
        final Set<SpreadsheetRow> loadedRows = Sets.of(
            ROW.row()
                .setHidden(false)
        );
        final Set<SpreadsheetRow> savedRows = Sets.of(
            ROW.row()
                .setHidden(true)
        );

        final Set<SpreadsheetRow> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleOneAndCheck(
            ROW,
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=" + CELL + "&width=" + WIDTH + "&height=" + HEIGHT + "&selectionType=cell&selection=" + CELL + "&includeFrozenColumnsRows=true" // queryString
            ),
            this.path(),
            this.context(
                this.spreadsheetEngine(
                    ROW_RANGE,
                    loadedRows,
                    cells,
                    storeSaved::add
                )
            ),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
                    .setCells(cells)
                    .setViewport(
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
            ) // expected loadedCells + savedRows
        );

        this.checkEquals(
            savedRows,
            storeSaved
        );
    }

    @Test
    public void testHandleOneWithRowInsideQueryStringWindow() {
        final Set<SpreadsheetRow> loadedRows = Sets.of(
            ROW.row()
                .setHidden(false)
        );
        final Set<SpreadsheetRow> savedRows = Sets.of(
            ROW.row()
                .setHidden(true)
        );

        final Set<SpreadsheetRow> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleOneAndCheck(
            ROW,
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=A1&width=" + WIDTH + "&height=" + HEIGHT + "&window=" + CELL // queryString
            ),
            this.path(),
            this.context(
                this.spreadsheetEngine(
                    ROW_RANGE,
                    loadedRows,
                    cells,
                    storeSaved::add
                )
            ),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
                    .setCells(cells)
                    .setViewport(
                        Optional.of(
                            SpreadsheetSelection.A1.viewportRectangle(
                                WIDTH,
                                HEIGHT
                            ).viewport()
                        )
                    ).setWindow(
                        SpreadsheetViewportWindows.parse(CELL.toString())
                    )
            ) // expected loadedCells + savedRows
        );

        this.checkEquals(
            savedRows,
            storeSaved
        );
    }

    @Test
    public void testHandleOneWithRowOutsideQueryStringWindow() {
        final Set<SpreadsheetRow> loadedRows = Sets.of(
            ROW.row()
                .setHidden(false)
        );
        final Set<SpreadsheetRow> savedRows = Sets.of(
            ROW.row()
                .setHidden(true)
        );

        final Set<SpreadsheetRow> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleOneAndCheck(
            ROW,
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=A1&width=" + WIDTH + "&height=" + HEIGHT + "&window=" + OUTSIDE_WINDOW // queryString
            ),
            this.path(),
            this.context(
                this.spreadsheetEngine(
                    ROW_RANGE,
                    loadedRows,
                    cells,
                    storeSaved::add
                )
            ),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setViewport(
                    Optional.of(
                        SpreadsheetSelection.A1.viewportRectangle(
                            WIDTH,
                            HEIGHT
                        ).viewport()
                    )
                ).setWindow(
                    SpreadsheetViewportWindows.parse(OUTSIDE_WINDOW)
                )
            ) // expected loadedCells + savedRows
        );

        this.checkEquals(
            savedRows,
            storeSaved
        );
    }

    // handleRange......................................................................................................

    @Test
    public void testHandleRangeWithPatchRowOutsideFails() {
        final IllegalArgumentException thrown = this.handleRangeFails(
            ROW_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY
                    .setRows(
                        Sets.of(
                            SpreadsheetSelection.parseRow("99")
                                .row()
                        )
                    )
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(),
            this.path(),
            this.context(
                this.spreadsheetEngine(
                    ROW_RANGE
                )
            ),
            IllegalArgumentException.class
        );

        this.checkEquals(
            "Patch row(s): " + ROW_RANGE + " includes invalid row 99",
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleRange() {
        final Set<SpreadsheetRow> loadedRows = Sets.of(
            ROW.row()
                .setHidden(false),
            ROW2.row()
                .setHidden(false)
        );
        final Set<SpreadsheetRow> savedRows = Sets.of(
            ROW.row()
                .setHidden(true),
            ROW2.row()
                .setHidden(true)
        );

        final Set<SpreadsheetRow> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY),
            CELL2.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleRangeAndCheck(
            ROW_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(),
            this.path(),
            this.context(
                this.spreadsheetEngine(
                    ROW_RANGE,
                    loadedRows,
                    cells,
                    storeSaved::add
                )
            ),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
                    .setCells(cells)
            ) // expected loadedCells + savedRows
        );

        this.checkEquals(
            savedRows,
            storeSaved
        );
    }

    @Test
    public void testHandleRangeWithQueryStringViewport() {
        final Set<SpreadsheetRow> loadedRows = Sets.of(
            ROW.row()
                .setHidden(false)
        );
        final Set<SpreadsheetRow> savedRows = Sets.of(
            ROW.row()
                .setHidden(true)
        );

        final Set<SpreadsheetRow> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleRangeAndCheck(
            ROW_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=" + CELL + "&width=" + WIDTH + "&height=" + HEIGHT + "&selectionType=cell&selection=" + CELL + "&includeFrozenColumnsRows=true" // queryString
            ),
            this.path(),
            this.context(
                this.spreadsheetEngine(
                    ROW_RANGE,
                    loadedRows,
                    cells,
                    storeSaved::add
                )
            ),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
                    .setCells(cells)
                    .setViewport(
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
            ) // expected loadedCells + savedRows
        );

        this.checkEquals(
            savedRows,
            storeSaved
        );
    }

    @Test
    public void testHandleRangeWithRowInsideQueryStringWindow() {
        final Set<SpreadsheetRow> loadedRows = Sets.of(
            ROW.row()
                .setHidden(false)
        );
        final Set<SpreadsheetRow> savedRows = Sets.of(
            ROW.row()
                .setHidden(true)
        );

        final Set<SpreadsheetRow> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleRangeAndCheck(
            ROW_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=A1&width=" + WIDTH + "&height=" + HEIGHT + "&window=" + CELL // queryString
            ),
            this.path(),
            this.context(
                this.spreadsheetEngine(
                    ROW_RANGE,
                    loadedRows,
                    cells,
                    storeSaved::add
                )
            ),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
                    .setCells(cells)
                    .setViewport(
                        Optional.of(
                            SpreadsheetSelection.A1.viewportRectangle(
                                WIDTH,
                                HEIGHT
                            ).viewport()
                        )
                    ).setWindow(
                        SpreadsheetViewportWindows.parse(CELL.toString())
                    )
            ) // expected loadedCells + savedRows
        );

        this.checkEquals(
            savedRows,
            storeSaved
        );
    }

    @Test
    public void testHandleRangeWithRowOutsideQueryStringWindow() {
        final Set<SpreadsheetRow> loadedRows = Sets.of(
            ROW.row()
                .setHidden(false)
        );
        final Set<SpreadsheetRow> savedRows = Sets.of(
            ROW.row()
                .setHidden(true)
        );

        final Set<SpreadsheetRow> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleRangeAndCheck(
            ROW_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=A1&width=" + WIDTH + "&height=" + HEIGHT + "&window=" + OUTSIDE_WINDOW // queryString
            ),
            this.path(),
            this.context(
                this.spreadsheetEngine(
                    ROW_RANGE,
                    loadedRows,
                    cells,
                    storeSaved::add
                )
            ),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setViewport(
                    Optional.of(
                        SpreadsheetSelection.A1.viewportRectangle(
                            WIDTH,
                            HEIGHT
                        ).viewport()
                    )
                ).setWindow(
                    SpreadsheetViewportWindows.parse(OUTSIDE_WINDOW)
                )
            ) // expected loadedCells + savedRows
        );

        this.checkEquals(
            savedRows,
            storeSaved
        );
    }

    @Test
    public void testHandleRangeWithRowInsideAndOutsideQueryStringWindow() {
        final Set<SpreadsheetRow> loadedRows = Sets.of(
            ROW.row()
                .setHidden(false),
            ROW2.row()
                .setHidden(false)
        );

        final SpreadsheetRow row2 = ROW2.row()
            .setHidden(true);

        final Set<SpreadsheetRow> savedRows = Sets.of(
            ROW.row()
                .setHidden(true),
            row2
        );

        final Set<SpreadsheetRow> storeSaved = Sets.ordered();

        final SpreadsheetCell cell = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("outside window"));
        final SpreadsheetCell cell2 = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("inside window"));

        final Set<SpreadsheetCell> cells = Sets.of(
            cell,
            cell2
        );

        final String outsideWindow = "C3:Z99";

        this.handleRangeAndCheck(
            ROW_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setRows(savedRows) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=A1&width=" + WIDTH + "&height=" + HEIGHT + "&window=" + outsideWindow // queryString
            ),
            this.path(),
            this.context(
                this.spreadsheetEngine(
                    ROW_RANGE,
                    loadedRows,
                    cells,
                    storeSaved::add
                )
            ),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(cell2)
                ).setRows(
                    Sets.of(row2)
                ).setViewport(
                    Optional.of(
                        SpreadsheetSelection.A1.viewportRectangle(
                            WIDTH,
                            HEIGHT
                        ).viewport()
                    )
                ).setWindow(
                    SpreadsheetViewportWindows.parse(outsideWindow)
                )
            ) // expected loadedCells + savedRows
        );

        this.checkEquals(
            savedRows,
            storeSaved
        );
    }

    // helpers..........................................................................................................

    private SpreadsheetEngine spreadsheetEngine(final SpreadsheetRowRangeReference range) {
        return this.spreadsheetEngine(
            range,
            Sets.empty(),
            Sets.empty(),
            (row) -> {
                throw new UnsupportedOperationException("Unexpected save row " + row);
            }
        );
    }

    private SpreadsheetEngine spreadsheetEngine(final SpreadsheetRowRangeReference rowRange,
                                                final Set<SpreadsheetRow> loadedRows,
                                                final Set<SpreadsheetCell> loadedCells,
                                                final Consumer<SpreadsheetRow> savedRows) {
        return new FakeSpreadsheetEngine() {

            @Override
            public SpreadsheetDelta loadRow(final SpreadsheetRowReference row,
                                            final SpreadsheetEngineContext context) {
                checkRow(row);

                return SpreadsheetDelta.EMPTY
                    .setCells(
                        loadedCells.stream()
                            .filter(cell -> cell.reference().testRow(row))
                            .collect(Collectors.toSet())
                    ).setRows(
                        loadedRows.stream()
                            .filter(c -> c.reference().testRow(row))
                            .collect(Collectors.toSet())
                    );
            }

            @Override
            public SpreadsheetDelta saveRow(final SpreadsheetRow row,
                                            final SpreadsheetEngineContext context) {
                checkRow(row.reference());
                savedRows.accept(row);

                return SpreadsheetDelta.EMPTY
                    .setCells(
                        loadedCells.stream()
                            .filter(cell -> cell.reference().testRow(row.reference()))
                            .collect(Collectors.toSet())
                    ).setRows(
                        Sets.of(row)
                    );
            }

            private void checkRow(final SpreadsheetRowReference row) {
                if (false == rowRange.testRow(row)) {
                    throw new IllegalArgumentException("Row " + row + " is not within " + rowRange);
                }
            }

            @Override
            public Optional<SpreadsheetViewport> navigate(final SpreadsheetViewport viewport,
                                                          final SpreadsheetEngineContext context) {
                return Optional.of(viewport);
            }

            @Override
            public SpreadsheetViewportWindows window(final SpreadsheetViewport viewport,
                                                     final SpreadsheetEngineContext context) {
                return WINDOWS;
            }
        };
    }

    @Override
    public SpreadsheetDeltaPatchHateosHttpEntityHandlerRow createHandler() {
        return SpreadsheetDeltaPatchHateosHttpEntityHandlerRow.INSTANCE;
    }

    @Override
    public SpreadsheetRowReference id() {
        return ROW;
    }

    @Override
    public Set<SpreadsheetRowReference> manyIds() {
        return Sets.of(
            ROW,
            ROW2
        );
    }

    @Override
    public Range<SpreadsheetRowReference> range() {
        return ROW_RANGE.range();
    }

    @Override
    public String typeNameSuffix() {
        return "Row";
    }

    @Override
    public Class<SpreadsheetDeltaPatchHateosHttpEntityHandlerRow> type() {
        return SpreadsheetDeltaPatchHateosHttpEntityHandlerRow.class;
    }
}
