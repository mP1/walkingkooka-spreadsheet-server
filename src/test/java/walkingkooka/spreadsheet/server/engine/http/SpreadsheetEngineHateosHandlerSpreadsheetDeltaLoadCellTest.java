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
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetViewport;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetReferenceKind;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelectionAnchor;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStore;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStore;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
        this.handleOneAndCheck(
                this.id(),
                this.resource(),
                this.parameters(),
                Optional.of(this.spreadsheetDelta())
        );
    }

    @Test
    public void testLoadCellWithDeltaPropertiesCells() {
        this.loadCellAndCheck(
                "cells",
                null // window
        );
    }

    @Test
    public void testLoadCellWithDeltaPropertiesCellsAndLabels() {
        this.loadCellAndCheck(
                "cells,labels",
                null // window
        );
    }

    private void loadCellAndCheck(final String deltaProperties,
                                  final String window) {
        final SpreadsheetCellReference id = this.id();

        final Map<HttpRequestAttribute<?>, Object> parameters = Maps.sorted();
        if (null != deltaProperties) {
            parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.DELTA_PROPERTIES, Lists.of(deltaProperties));
        }
        if (null != window) {
            parameters.put(SpreadsheetEngineHttps.WINDOW, Lists.of(window));
        }

        this.handleOneAndCheck(
                SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.with(
                        EVALUATION,
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta loadCell(final SpreadsheetCellReference cell,
                                                             final SpreadsheetEngineEvaluation evaluation,
                                                             final Set<SpreadsheetDeltaProperties> dp,
                                                             final SpreadsheetEngineContext context) {
                                assertSame(EVALUATION, evaluation, "evaluation");
                                checkEquals(
                                        SpreadsheetDeltaProperties.csv(deltaProperties),
                                        dp,
                                        "deltaProperties"
                                );
                                assertNotNull(context, "context");

                                return SpreadsheetDelta.EMPTY
                                        .setCells(cells())
                                        .setLabels(labels());
                            }
                        },
                        this.engineContext()),
                id,
                Optional.empty(),
                parameters,
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(this.cells())
                                .setLabels(this.labels())
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
                    public SpreadsheetDelta loadCells(final Set<SpreadsheetCellRange> range,
                                                      final SpreadsheetEngineEvaluation evaluation,
                                                      final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                      final SpreadsheetEngineContext context) {
                        assertSame(EVALUATION, evaluation, "evaluation");
                        assertNotNull(context, "context");

                        return SpreadsheetDelta.EMPTY.setCells(Sets.of(b1, b2, b3));
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
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(Sets.of(b1, b2, b3))
                )
        );
    }

    @Test
    public void testBatchLoadWithWindowParameter() {
        final SpreadsheetCell b1 = this.b1();
        final SpreadsheetCell b2 = this.b2();
        final SpreadsheetCell b3 = this.b3();

        final SpreadsheetCellRange window = b1.reference()
                .cellRange();

        final SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell handler = SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.with(
                EVALUATION,
                new FakeSpreadsheetEngine() {


                    @Override
                    public SpreadsheetDelta loadCells(final Set<SpreadsheetCellRange> range,
                                                      final SpreadsheetEngineEvaluation evaluation,
                                                      final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                      final SpreadsheetEngineContext context) {
                        assertSame(EVALUATION, evaluation, "evaluation");
                        checkEquals(SpreadsheetDeltaProperties.ALL, deltaProperties, "deltaProperties");
                        assertNotNull(context, "context");

                        return SpreadsheetDelta.EMPTY.setCells(
                                Sets.of(b1, b2, b3));
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
                Maps.of(
                        SpreadsheetEngineHttps.WINDOW,
                        List.of(
                                window.toString()
                        )
                ),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(b1, b2)
                                ).setWindow(
                                        Sets.of(window)
                                )
                )
        );
    }

    // handleAll........................................................................................................

    @Test
    public void testHandleAllMissingHomeParameterFails() {
        this.handleAllFails2(Maps.empty(), "Missing parameter \"home\"");
    }

    @Test
    public void testHandleAllMissingWidthParameterFails() {
        this.handleAllFails2(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1")
                ),
                "Missing parameter \"width\""
        );
    }

    @Test
    public void testHandleAllInvalidWidthParameterFails() {
        this.handleAllFails2(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("*")
                ),
                "Invalid query parameter width=\"*\""
        );
    }

    @Test
    public void testHandleAllMissingHeightParameterFails() {
        this.handleAllFails2(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("0")
                ),
                "Missing parameter \"height\""
        );
    }

    @Test
    public void testHandleAllInvalidHeightParameterFails() {
        this.handleAllFails2(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("0"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("*")
                ),
                "Invalid query parameter height=\"*\""
        );
    }

    @Test
    public void testHandleAllMissingIncludeFrozenColumnsRowsParameterFails() {
        this.handleAllFails2(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("0"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("0")
                ),
                "Missing parameter \"includeFrozenColumnsRows\""
        );
    }

    @Test
    public void testHandleAllSelectionTypePresentAndMissingSelectionParameterFails() {
        final Map<HttpRequestAttribute<?>, Object> parameters = Maps.sorted();

        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("0"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("0"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false"));
        parameters.put(SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("cell"));

        this.handleAllFails2(
                parameters,
                "Missing parameter \"selection\""
        );
    }

    @Test
    public void testHandleAllInvalidSelectionTypeParameterFails() {
        final Map<HttpRequestAttribute<?>, Object> parameters = Maps.sorted();

        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("0"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("0"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false"));
        parameters.put(SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("unknownn?"));
        parameters.put(SpreadsheetEngineHttps.SELECTION, Lists.of("A1"));

        this.handleAllFails2(
                parameters,
                "Invalid parameter \"selectionType\" value \"A1\""
        );
    }

    private void handleAllFails2(final Map<HttpRequestAttribute<?>, Object> parameters, final String message) {
        final IllegalArgumentException thrown = this.handleAllFails(
                Optional.empty(),
                parameters,
                IllegalArgumentException.class
        );
        this.checkEquals(message, thrown.getMessage(), "message");
    }

    @Test
    public void testHandleAllFilteredNone() {
        this.handleAllFilteredAndCheck(
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    public void testHandleAllFilteredCell() {
        this.handleAllFilteredAndCheck(
                "cell",
                "A9",
                null,
                null, // deltaProperties
                SpreadsheetSelection.parseCell("A9")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
        );
    }

    @Test
    public void testHandleAllFilteredCellRange() {
        this.handleAllFilteredAndCheck(
                "cell-range",
                "A9:A99",
                SpreadsheetViewportSelectionAnchor.TOP_LEFT.kebabText(),
                null, // deltaProperties
                SpreadsheetSelection.parseCellRange("A9:A99")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.TOP_LEFT)
        );
    }

    @Test
    public void testHandleAllFilteredColumn() {
        this.handleAllFilteredAndCheck(
                "column",
                "B",
                null, // anchor
                null, // deltaProperties
                SpreadsheetSelection.parseColumn("B")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
        );
    }

    @Test
    public void testHandleAllFilteredColumnRange() {
        this.handleAllFilteredAndCheck(
                "column-range",
                "B:D",
                SpreadsheetViewportSelectionAnchor.LEFT.kebabText(),
                null, // deltaProperties
                SpreadsheetSelection.parseColumnRange("B:D")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.LEFT)
        );
    }

    @Test
    public void testHandleAllFilteredRow() {
        this.handleAllFilteredAndCheck(
                "row",
                "99",
                null, // anchor
                null, // deltaProperties
                SpreadsheetSelection.parseRow("99")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
        );
    }

    @Test
    public void testHandleAllFilteredrowRange() {
        this.handleAllFilteredAndCheck(
                "row-range",
                "98:99",
                SpreadsheetViewportSelectionAnchor.TOP.kebabText(),
                null, // deltaProperties
                SpreadsheetSelection.parseRowRange("98:99")
                        .setAnchor(SpreadsheetViewportSelectionAnchor.TOP)
        );
    }

    @Test
    public void testHandleAllFilteredCells() {
        this.handleAllFilteredAndCheck(
                null,
                null,
                null,
                "cells",
                null
        );
    }

    @Test
    public void testHandleAllFilteredCellsAndLabels() {
        this.handleAllFilteredAndCheck(
                null,
                null,
                null,
                "cells,labels",
                null
        );
    }

    private void handleAllFilteredAndCheck(final String selectionType,
                                           final String selectionText,
                                           final String anchor,
                                           final String deltaProperties,
                                           final SpreadsheetViewportSelection viewportSelection) {
        // B1, B2, B3
        // C1, C2, C3

        final SpreadsheetCell b1 = this.b1();
        final SpreadsheetCell b2 = this.b2();
        final SpreadsheetCell b3 = this.b3();

        final SpreadsheetCell c1 = this.c1();
        final SpreadsheetCell c2 = this.c2();
        final SpreadsheetCell c3 = this.c3();

        final Range<SpreadsheetCellReference> range = this.range();

        final Map<HttpRequestAttribute<?>, Object> parameters = Maps.sorted();
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("B2"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("33"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("44"));

        if (null != selectionType) {
            parameters.put(SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of(selectionType));
            parameters.put(SpreadsheetEngineHttps.SELECTION, Lists.of(selectionText));

            if (null != anchor) {
                parameters.put(SpreadsheetEngineHttps.SELECTION_ANCHOR, Lists.of(anchor));
            }
        }

        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false"));
        parameters.put(
                SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.DELTA_PROPERTIES,
                Lists.of(deltaProperties)
        );

        this.handleAllAndCheck(
                SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.with(
                        EVALUATION,
                        new FakeSpreadsheetEngine() {

                            @Override
                            public SpreadsheetDelta loadCells(final Set<SpreadsheetCellRange> r,
                                                              final SpreadsheetEngineEvaluation evaluation,
                                                              final Set<SpreadsheetDeltaProperties> dp,
                                                              final SpreadsheetEngineContext context) {
                                checkEquals(Sets.of(SpreadsheetSelection.cellRange(range)), r, "range");
                                checkEquals(EVALUATION, evaluation, "evaluation");
                                checkEquals(
                                        SpreadsheetDeltaProperties.csv(deltaProperties),
                                        dp,
                                        "deltaProperties"
                                );

                                return SpreadsheetDelta.EMPTY
                                        .setCells(
                                                Sets.of(
                                                        b1, b2, b3, c1, c2, c3
                                                )
                                        );
                            }

                            @Override
                            public Set<SpreadsheetCellRange> window(final SpreadsheetViewport viewport,
                                                                    final boolean includeFrozenColumnsRows,
                                                                    final Optional<SpreadsheetSelection> s,
                                                                    final SpreadsheetEngineContext context) {
                                checkEquals(
                                        SpreadsheetViewport.with(
                                                SpreadsheetSelection.parseCell("B2"),
                                                33.0,
                                                44.0
                                        ),
                                        viewport,
                                        "viewport"
                                );
                                return Sets.of(
                                        SpreadsheetSelection.cellRange(range)
                                );
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
                            public Optional<SpreadsheetViewportSelection> navigate(final SpreadsheetViewportSelection selection,
                                                                                   final SpreadsheetEngineContext context) {
                                return Optional.of(selection);
                            }
                        },
                        new FakeSpreadsheetEngineContext() {
                            @Override
                            public SpreadsheetMetadata metadata() {
                                return SpreadsheetMetadata.EMPTY
                                        .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.ENGLISH)
                                        .loadFromLocale();
                            }

                            @Override
                            public SpreadsheetStoreRepository storeRepository() {
                                return new FakeSpreadsheetStoreRepository() {
                                    @Override
                                    public SpreadsheetCellStore cells() {
                                        return SpreadsheetCellStores.fake();
                                    }
                                };
                            }
                        }
                ),
                Optional.empty(),
                parameters,
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(Sets.of(b1, b2, b3, c1, c2, c3))
                                .setWindow(
                                        SpreadsheetSelection.parseWindow("B1:C3")
                                ).setSelection(
                                        Optional.ofNullable(viewportSelection)
                                )
                )
        );
    }

    @Test
    public void testHandleAllNoFrozenColumnRows() {
        this.handleAllFilteredAndCheck(
                "A1",
                0, // frozenColumns
                0, // frozenRows
                "A1:D4", // range
                "A1:D4" // window
        );
    }

    //   A B C D
    // 1 C v v v
    // 2 C v v v
    // 3 C v v v
    // 4 C v v v
    @Test
    public void testHandleAll1FrozenColumn() {
        this.handleAllFilteredAndCheck(
                "A1",
                1, // frozenColumns
                0, // frozenRows
                "A1:A4,B1:D4", // range
                "A1:A4,B1:D4" // window
        );
    }

    private void handleAllFilteredAndCheck(final String home,
                                           final int frozenColumns,
                                           final int frozenRows,
                                           final String range,
                                           final String window) {
        final Map<HttpRequestAttribute<?>, Object> parameters = Maps.sorted();
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of(home));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("400")); // 4x3
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("150"));

        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("true"));

        this.handleAllAndCheck(
                SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.with(
                        EVALUATION,
                        new FakeSpreadsheetEngine() {

                            @Override
                            public SpreadsheetDelta loadCells(final Set<SpreadsheetCellRange> r,
                                                              final SpreadsheetEngineEvaluation evaluation,
                                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                              final SpreadsheetEngineContext context) {
                                checkEquals(EVALUATION, evaluation, "evaluation");

                                return SpreadsheetDelta.EMPTY;
                            }

                            @Override
                            public Set<SpreadsheetCellRange> window(final SpreadsheetViewport viewport,
                                                                    final boolean includeFrozenColumnsRows,
                                                                    final Optional<SpreadsheetSelection> selection,
                                                                    final SpreadsheetEngineContext context) {
                                if (range.equals("throw")) {
                                    throw new UnsupportedOperationException();
                                }
                                checkEquals(SpreadsheetDelta.NO_SELECTION, selection);

                                return SpreadsheetSelection.parseWindow(range);
                            }

                            @Override
                            public double columnWidth(final SpreadsheetColumnReference column,
                                                      final SpreadsheetEngineContext context) {
                                return 100;
                            }

                            @Override
                            public double rowHeight(final SpreadsheetRowReference row,
                                                    final SpreadsheetEngineContext context) {
                                return 50;
                            }

                            @Override
                            public Optional<SpreadsheetViewportSelection> navigate(final SpreadsheetViewportSelection selection,
                                                                                   final SpreadsheetEngineContext context) {
                                return Optional.of(selection);
                            }
                        },
                        new FakeSpreadsheetEngineContext() {
                            @Override
                            public SpreadsheetMetadata metadata() {
                                return SpreadsheetMetadata.EMPTY
                                        .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.ENGLISH)
                                        .loadFromLocale()
                                        .setOrRemove(SpreadsheetMetadataPropertyName.FROZEN_COLUMNS, frozenColumns > 0 ? SpreadsheetReferenceKind.RELATIVE.firstColumn().columnRange(SpreadsheetReferenceKind.RELATIVE.column(frozenColumns-1)) : null)
                                        .setOrRemove(SpreadsheetMetadataPropertyName.FROZEN_ROWS, frozenRows > 0 ? SpreadsheetReferenceKind.RELATIVE.firstRow().rowRange(SpreadsheetReferenceKind.RELATIVE.row(frozenRows-1)) : null);
                            }

                            @Override
                            public SpreadsheetStoreRepository storeRepository() {
                                return new FakeSpreadsheetStoreRepository() {
                                    @Override
                                    public SpreadsheetCellStore cells() {
                                        return this.cells;
                                    }

                                    private final SpreadsheetCellStore cells = SpreadsheetCellStores.treeMap();

                                    @Override
                                    public SpreadsheetColumnStore columns() {
                                        return this.columns;
                                    }

                                    private final SpreadsheetColumnStore columns = SpreadsheetColumnStores.treeMap();

                                    @Override
                                    public SpreadsheetRowStore rows() {
                                        return this.rows;
                                    }

                                    private final SpreadsheetRowStore rows = SpreadsheetRowStores.treeMap();
                                };
                            }
                        }
                ),
                Optional.empty(),
                parameters,
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setWindow(
                                        SpreadsheetSelection.parseWindow(window)
                                )
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
        return SpreadsheetSelection.parseCell("B2");
    }

    @Override
    public Range<SpreadsheetCellReference> range() {
        return SpreadsheetCellRange.parseCellRange("B1:C3").range();
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
                                             final Set<SpreadsheetDeltaProperties> deltaProperties,
                                             final SpreadsheetEngineContext context) {
                Objects.requireNonNull(id, "id");
                Objects.requireNonNull(evaluation, "evaluation");
                Objects.requireNonNull(deltaProperties, "deltaProperties");
                Objects.requireNonNull(context, "context");

                checkEquals(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCellTest.this.spreadsheetCellReference(), id, "spreadsheetCellReference");
                checkEquals(EVALUATION, evaluation, "evaluation");
                checkNotEquals(null, context, "context");

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

    @Override
    SpreadsheetEngineContext engineContext() {
        return SpreadsheetEngineContexts.fake();
    }

    private SpreadsheetDelta spreadsheetDelta() {
        return SpreadsheetDelta.EMPTY.setCells(Sets.of(this.cell()));
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.class;
    }
}
