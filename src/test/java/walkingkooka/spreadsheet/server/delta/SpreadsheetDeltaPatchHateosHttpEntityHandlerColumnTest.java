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
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.SpreadsheetViewportRectangle;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class SpreadsheetDeltaPatchHateosHttpEntityHandlerColumnTest extends SpreadsheetDeltaPatchHateosHttpEntityHandlerTestCase<SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn,
    SpreadsheetColumnReference, SpreadsheetColumnRangeReference> {

    final static SpreadsheetColumnReference COLUMN = SpreadsheetSelection.parseColumn("B");

    final static SpreadsheetColumnReference COLUMN2 = SpreadsheetSelection.parseColumn("C");

    final static SpreadsheetColumnRangeReference COLUMN_RANGE = SpreadsheetSelection.parseColumnRange("B:C");

    private final static SpreadsheetViewportWindows WINDOWS = SpreadsheetViewportWindows.parse("A1:Z99");

    // handleOne........................................................................................................

    @Test
    public void testHandleOneWithPatchColumnOutsideFails() {
        final IllegalArgumentException thrown = this.handleOneFails(
            this.createHandler(
                this.spreadsheetEngine(
                    COLUMN_RANGE
                )
            ),
            COLUMN,
            this.httpEntity(
                SpreadsheetDelta.EMPTY
                    .setColumns(
                        Sets.of(
                            SpreadsheetSelection.parseColumn("Z")
                                .column()
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
            "Patch column(s): " + COLUMN + " includes invalid column Z",
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleOne() {
        final Set<SpreadsheetColumn> loadedColumns = Sets.of(
            COLUMN.column()
                .setHidden(false)
        );
        final Set<SpreadsheetColumn> savedColumns = Sets.of(
            COLUMN.column()
                .setHidden(true)
        );

        final Set<SpreadsheetColumn> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleOneAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    COLUMN_RANGE,
                    loadedColumns,
                    cells,
                    storeSaved::add
                )
            ),
            COLUMN,
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
                    .setCells(cells)
            ) // expected loadedCells + savedColumns
        );

        this.checkEquals(
            savedColumns,
            storeSaved
        );
    }

    @Test
    public void testHandleOneWithQueryStringViewport() {
        final Set<SpreadsheetColumn> loadedColumns = Sets.of(
            COLUMN.column()
                .setHidden(false)
        );
        final Set<SpreadsheetColumn> savedColumns = Sets.of(
            COLUMN.column()
                .setHidden(true)
        );

        final Set<SpreadsheetColumn> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleOneAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    COLUMN_RANGE,
                    loadedColumns,
                    cells,
                    storeSaved::add
                )
            ),
            COLUMN,
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=" + CELL + "&width=" + WIDTH + "&height=" + HEIGHT + "&selectionType=cell&selection=" + CELL + "&includeFrozenColumnsRows=true" // queryString
            ),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
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
            ) // expected loadedCells + savedColumns
        );

        this.checkEquals(
            savedColumns,
            storeSaved
        );
    }

    @Test
    public void testHandleOneWithColumnInsideQueryStringWindow() {
        final Set<SpreadsheetColumn> loadedColumns = Sets.of(
            COLUMN.column()
                .setHidden(false)
        );
        final Set<SpreadsheetColumn> savedColumns = Sets.of(
            COLUMN.column()
                .setHidden(true)
        );

        final Set<SpreadsheetColumn> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleOneAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    COLUMN_RANGE,
                    loadedColumns,
                    cells,
                    storeSaved::add
                )
            ),
            COLUMN,
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=A1&width=" + WIDTH + "&height=" + HEIGHT + "&window=" + CELL // queryString
            ),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
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
            ) // expected loadedCells + savedColumns
        );

        this.checkEquals(
            savedColumns,
            storeSaved
        );
    }

    @Test
    public void testHandleOneWithColumnOutsideQueryStringWindow() {
        final Set<SpreadsheetColumn> loadedColumns = Sets.of(
            COLUMN.column()
                .setHidden(false)
        );
        final Set<SpreadsheetColumn> savedColumns = Sets.of(
            COLUMN.column()
                .setHidden(true)
        );

        final Set<SpreadsheetColumn> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleOneAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    COLUMN_RANGE,
                    loadedColumns,
                    cells,
                    storeSaved::add
                )
            ),
            COLUMN,
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
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
                ).setWindow(
                    SpreadsheetViewportWindows.parse(OUTSIDE_WINDOW)
                )
            ) // expected loadedCells + savedColumns
        );

        this.checkEquals(
            savedColumns,
            storeSaved
        );
    }

    // handleRange......................................................................................................

    @Test
    public void testHandleRangeWithPatchColumnOutsideFails() {
        final IllegalArgumentException thrown = this.handleRangeFails(
            this.createHandler(
                this.spreadsheetEngine(
                    COLUMN_RANGE
                )
            ),
            COLUMN_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY
                    .setColumns(
                        Sets.of(
                            SpreadsheetSelection.parseColumn("Z")
                                .column()
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
            "Patch column(s): " + COLUMN_RANGE + " includes invalid column Z",
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleRange() {
        final Set<SpreadsheetColumn> loadedColumns = Sets.of(
            COLUMN.column()
                .setHidden(false),
            COLUMN2.column()
                .setHidden(false)
        );
        final Set<SpreadsheetColumn> savedColumns = Sets.of(
            COLUMN.column()
                .setHidden(true),
            COLUMN2.column()
                .setHidden(true)
        );

        final Set<SpreadsheetColumn> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY),
            CELL2.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleRangeAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    COLUMN_RANGE,
                    loadedColumns,
                    cells,
                    storeSaved::add
                )
            ),
            COLUMN_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
                    .setCells(cells)
            ) // expected loadedCells + savedColumns
        );

        this.checkEquals(
            savedColumns,
            storeSaved
        );
    }

    @Test
    public void testHandleRangeWithQueryStringViewport() {
        final Set<SpreadsheetColumn> loadedColumns = Sets.of(
            COLUMN.column()
                .setHidden(false)
        );
        final Set<SpreadsheetColumn> savedColumns = Sets.of(
            COLUMN.column()
                .setHidden(true)
        );

        final Set<SpreadsheetColumn> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleRangeAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    COLUMN_RANGE,
                    loadedColumns,
                    cells,
                    storeSaved::add
                )
            ),
            COLUMN_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=" + CELL + "&width=" + WIDTH + "&height=" + HEIGHT + "&selectionType=cell&selection=" + CELL + "&includeFrozenColumnsRows=true" // queryString
            ),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
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
            ) // expected loadedCells + savedColumns
        );

        this.checkEquals(
            savedColumns,
            storeSaved
        );
    }

    @Test
    public void testHandleRangeWithColumnInsideQueryStringWindow() {
        final Set<SpreadsheetColumn> loadedColumns = Sets.of(
            COLUMN.column()
                .setHidden(false)
        );
        final Set<SpreadsheetColumn> savedColumns = Sets.of(
            COLUMN.column()
                .setHidden(true)
        );

        final Set<SpreadsheetColumn> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleRangeAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    COLUMN_RANGE,
                    loadedColumns,
                    cells,
                    storeSaved::add
                )
            ),
            COLUMN_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=A1&width=" + WIDTH + "&height=" + HEIGHT + "&window=" + CELL // queryString
            ),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
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
            ) // expected loadedCells + savedColumns
        );

        this.checkEquals(
            savedColumns,
            storeSaved
        );
    }

    @Test
    public void testHandleRangeWithColumnOutsideQueryStringWindow() {
        final Set<SpreadsheetColumn> loadedColumns = Sets.of(
            COLUMN.column()
                .setHidden(false)
        );
        final Set<SpreadsheetColumn> savedColumns = Sets.of(
            COLUMN.column()
                .setHidden(true)
        );

        final Set<SpreadsheetColumn> storeSaved = Sets.ordered();

        final Set<SpreadsheetCell> cells = Sets.of(
            CELL.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleRangeAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    COLUMN_RANGE,
                    loadedColumns,
                    cells,
                    storeSaved::add
                )
            ),
            COLUMN_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
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
                ).setWindow(
                    SpreadsheetViewportWindows.parse(OUTSIDE_WINDOW)
                )
            ) // expected loadedCells + savedColumns
        );

        this.checkEquals(
            savedColumns,
            storeSaved
        );
    }

    @Test
    public void testHandleRangeWithColumnInsideAndOutsideQueryStringWindow() {
        final Set<SpreadsheetColumn> loadedColumns = Sets.of(
            COLUMN.column()
                .setHidden(false),
            COLUMN2.column()
                .setHidden(false)
        );

        final SpreadsheetColumn column2 = COLUMN2.column()
            .setHidden(true);

        final Set<SpreadsheetColumn> savedColumns = Sets.of(
            COLUMN.column()
                .setHidden(true),
            column2
        );

        final Set<SpreadsheetColumn> storeSaved = Sets.ordered();

        final SpreadsheetCell cell = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("outside window"));
        final SpreadsheetCell cell2 = CELL.setFormula(SpreadsheetFormula.EMPTY.setText("inside window"));

        final Set<SpreadsheetCell> cells = Sets.of(
            cell,
            cell2
        );

        final String outsideWindow = "C3:Z99";

        this.handleRangeAndCheck(
            this.createHandler(
                this.spreadsheetEngine(
                    COLUMN_RANGE,
                    loadedColumns,
                    cells,
                    storeSaved::add
                )
            ),
            COLUMN_RANGE.range(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setColumns(savedColumns) // save = patch
            ).setAccept(
                CONTENT_TYPE.accept()
            ),
            this.parameters(
                "home=A1&width=" + WIDTH + "&height=" + HEIGHT + "&window=" + outsideWindow // queryString
            ),
            this.path(),
            this.context(),
            this.httpEntity(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(cell2)
                ).setColumns(
                    Sets.of(column2)
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
            ) // expected loadedCells + savedColumns
        );

        this.checkEquals(
            savedColumns,
            storeSaved
        );
    }

    // helpers..........................................................................................................

    private SpreadsheetEngine spreadsheetEngine(final SpreadsheetColumnRangeReference range) {
        return this.spreadsheetEngine(
            range,
            Sets.empty(),
            Sets.empty(),
            (column) -> {
                throw new UnsupportedOperationException("Unexpected save column " + column);
            }
        );
    }

    private SpreadsheetEngine spreadsheetEngine(final SpreadsheetColumnRangeReference columnRange,
                                                final Set<SpreadsheetColumn> loadedColumns,
                                                final Set<SpreadsheetCell> loadedCells,
                                                final Consumer<SpreadsheetColumn> savedColumns) {
        return new FakeSpreadsheetEngine() {

            @Override
            public SpreadsheetDelta loadColumn(final SpreadsheetColumnReference column,
                                               final SpreadsheetEngineContext context) {
                checkColumn(column);

                return SpreadsheetDelta.EMPTY
                    .setCells(
                        loadedCells.stream()
                            .filter(cell -> cell.reference().testColumn(column))
                            .collect(Collectors.toSet())
                    ).setColumns(
                        loadedColumns.stream()
                            .filter(c -> c.reference().testColumn(column))
                            .collect(Collectors.toSet())
                    );
            }

            @Override
            public SpreadsheetDelta saveColumn(final SpreadsheetColumn column,
                                               final SpreadsheetEngineContext context) {
                checkColumn(column.reference());
                savedColumns.accept(column);

                return SpreadsheetDelta.EMPTY
                    .setCells(
                        loadedCells.stream()
                            .filter(cell -> cell.reference().testColumn(column.reference()))
                            .collect(Collectors.toSet())
                    ).setColumns(
                        Sets.of(column)
                    );
            }

            private void checkColumn(final SpreadsheetColumnReference column) {
                if (false == columnRange.testColumn(column)) {
                    throw new IllegalArgumentException("Column " + column + " is not within " + columnRange);
                }
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
    SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn createHandler(final SpreadsheetEngine engine) {
        return SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn.with(
            engine
        );
    }

    @Override
    public SpreadsheetColumnReference id() {
        return COLUMN;
    }

    @Override
    public Set<SpreadsheetColumnReference> manyIds() {
        return Sets.of(
            COLUMN,
            COLUMN2
        );
    }

    @Override
    public Range<SpreadsheetColumnReference> range() {
        return COLUMN_RANGE.range();
    }

    @Override
    public String typeNameSuffix() {
        return "Column";
    }

    @Override
    public Class<SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn> type() {
        return SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn.class;
    }
}
