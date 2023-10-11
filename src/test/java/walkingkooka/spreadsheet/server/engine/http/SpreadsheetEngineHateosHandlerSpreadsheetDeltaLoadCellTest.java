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
import walkingkooka.spreadsheet.SpreadsheetViewportRectangle;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetReferenceKind;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportAnchor;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelectionNavigation;
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
                            public SpreadsheetDelta loadCells(final SpreadsheetSelection selection,
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
    public void testLoadCellRange() {
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
    public void testLoadCellRangeWithWindowParameter() {
        final SpreadsheetCell b1 = this.b1();
        final SpreadsheetCell b2 = this.b2();
        final SpreadsheetCell b3 = this.b3();

        final SpreadsheetCellRange window = b1.reference()
                .toCellRange();

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
                                        SpreadsheetViewportWindows.with(
                                                Sets.of(window)
                                        )
                                )
                )
        );
    }

    // handleAll........................................................................................................

    @Test
    public void testLoadCellRangeMissingHomeParameterFails() {
        this.loadCellRangeFails(Maps.empty(), "Missing parameter \"home\"");
    }

    @Test
    public void testLoadCellRangeMissingWidthParameterFails() {
        this.loadCellRangeFails(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1")
                ),
                "Missing parameter \"width\""
        );
    }

    @Test
    public void testLoadCellRangeInvalidWidthParameterFails() {
        this.loadCellRangeFails(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("*")
                ),
                "Invalid query parameter width=\"*\""
        );
    }

    @Test
    public void testLoadCellRangeInvalidWidthParameterFails2() {
        this.loadCellRangeFails(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("0")
                ),
                "Invalid query parameter width=\"0\" <= 0"
        );
    }

    @Test
    public void testLoadCellRangeInvalidWidthParameterFails3() {
        this.loadCellRangeFails(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("-1")
                ),
                "Invalid query parameter width=\"-1\" <= 0"
        );
    }

    @Test
    public void testLoadCellRangeMissingHeightParameterFails() {
        this.loadCellRangeFails(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("123")
                ),
                "Missing parameter \"height\""
        );
    }

    @Test
    public void testLoadCellRangeInvalidHeightParameterFails() {
        this.loadCellRangeFails(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("123"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("*")
                ),
                "Invalid query parameter height=\"*\""
        );
    }

    @Test
    public void testLoadCellRangeInvalidHeightParameterFails2() {
        this.loadCellRangeFails(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("123"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("0")
                ),
                "Invalid query parameter height=\"0\" <= 0"
        );
    }

    @Test
    public void testLoadCellRangeInvalidHeightParameterFails3() {
        this.loadCellRangeFails(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("123"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("-1")
                ),
                "Invalid query parameter height=\"-1\" <= 0"
        );
    }

    @Test
    public void testLoadCellRangeMissingIncludeFrozenColumnsRowsParameterFails() {
        this.loadCellRangeFails(
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("123"),
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("456")
                ),
                "Missing parameter \"includeFrozenColumnsRows\""
        );
    }

    @Test
    public void testLoadCellRangeSelectionTypePresentAndMissingSelectionParameterFails() {
        final Map<HttpRequestAttribute<?>, Object> parameters = Maps.sorted();

        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("123"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("456"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false"));
        parameters.put(SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("cell"));

        this.loadCellRangeFails(
                parameters,
                "Missing parameter \"selection\""
        );
    }

    @Test
    public void testLoadCellRangeInvalidSelectionTypeParameterFails() {
        final Map<HttpRequestAttribute<?>, Object> parameters = Maps.sorted();

        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of("A1"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("123"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("456"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false"));
        parameters.put(SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of("unknownn?"));
        parameters.put(SpreadsheetEngineHttps.SELECTION, Lists.of("A1"));

        this.loadCellRangeFails(
                parameters,
                "Invalid selectionType \"unknownn?\" value \"A1\""
        );
    }

    private void loadCellRangeFails(final Map<HttpRequestAttribute<?>, Object> parameters, final String message) {
        final IllegalArgumentException thrown = this.handleAllFails(
                Optional.empty(),
                parameters,
                IllegalArgumentException.class
        );
        this.checkEquals(message, thrown.getMessage(), "message");
    }

    @Test
    public void testLoadCellRangeFilteredNone() {
        this.loadCellRangeFilteredAndCheck(
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    public void testLoadCellRangeFilteredCell() {
        this.loadCellRangeFilteredAndCheck(
                "cell",
                "A9",
                null,
                null, // deltaProperties
                SpreadsheetSelection.parseCell("A9")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
        );
    }

    @Test
    public void testLoadCellRangeFilteredCellRange() {
        this.loadCellRangeFilteredAndCheck(
                "cell-range",
                "A9:A99",
                SpreadsheetViewportAnchor.TOP_LEFT.kebabText(),
                null, // deltaProperties
                SpreadsheetSelection.parseCellRange("A9:A99")
                        .setAnchor(SpreadsheetViewportAnchor.TOP_LEFT)
        );
    }

    @Test
    public void testLoadCellRangeFilteredColumn() {
        this.loadCellRangeFilteredAndCheck(
                "column",
                "B",
                null, // anchor
                null, // deltaProperties
                SpreadsheetSelection.parseColumn("B")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
        );
    }

    @Test
    public void testLoadCellRangeFilteredColumnRange() {
        this.loadCellRangeFilteredAndCheck(
                "column-range",
                "B:D",
                SpreadsheetViewportAnchor.LEFT.kebabText(),
                null, // deltaProperties
                SpreadsheetSelection.parseColumnRange("B:D")
                        .setAnchor(SpreadsheetViewportAnchor.LEFT)
        );
    }

    @Test
    public void testLoadCellRangeFilteredRow() {
        this.loadCellRangeFilteredAndCheck(
                "row",
                "99",
                null, // anchor
                null, // deltaProperties
                SpreadsheetSelection.parseRow("99")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
        );
    }

    @Test
    public void testLoadCellRangeFilteredrowRange() {
        this.loadCellRangeFilteredAndCheck(
                "row-range",
                "98:99",
                SpreadsheetViewportAnchor.TOP.kebabText(),
                null, // deltaProperties
                SpreadsheetSelection.parseRowRange("98:99")
                        .setAnchor(SpreadsheetViewportAnchor.TOP)
        );
    }

    @Test
    public void testLoadCellRangeFilteredCells() {
        this.loadCellRangeFilteredAndCheck(
                null,
                null,
                null,
                "cells",
                null
        );
    }

    @Test
    public void testLoadCellRangeFilteredCellsAndLabels() {
        this.loadCellRangeFilteredAndCheck(
                null,
                null,
                null,
                "cells,labels",
                null
        );
    }

    private void loadCellRangeFilteredAndCheck(final String selectionType,
                                               final String selectionText,
                                               final String anchor,
                                               final String deltaProperties,
                                               final SpreadsheetViewport viewportSelection) {
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
                            public SpreadsheetViewportWindows window(final SpreadsheetViewportRectangle viewportRectangle,
                                                                     final boolean includeFrozenColumnsRows,
                                                                     final Optional<SpreadsheetSelection> s,
                                                                     final SpreadsheetEngineContext context) {
                                checkEquals(
                                        SpreadsheetViewportRectangle.with(
                                                SpreadsheetSelection.parseCell("B2"),
                                                33.0,
                                                44.0
                                        ),
                                        viewportRectangle,
                                        "viewport"
                                );
                                return SpreadsheetViewportWindows.with(Sets.of(
                                                SpreadsheetSelection.cellRange(range)
                                        )
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
                            public Optional<SpreadsheetViewport> navigate(final SpreadsheetViewport viewportSelection,
                                                                                   final SpreadsheetEngineContext context) {
                                return Optional.of(viewportSelection);
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

                            @Override
                            public SpreadsheetSelection resolveIfLabel(final SpreadsheetSelection selection) {
                                return selection;
                            }
                        }
                ),
                Optional.empty(),
                parameters,
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(Sets.of(b1, b2, b3, c1, c2, c3))
                                .setWindow(
                                        SpreadsheetViewportWindows.parse("B1:C3")
                                ).setViewport(
                                        Optional.ofNullable(viewportSelection)
                                )
                )
        );
    }

    @Test
    public void testLoadCellRangeNoFrozenColumnRows() {
        this.loadCellRangeFilteredAndCheck(
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
    public void testLoadCellRange1FrozenColumn() {
        this.loadCellRangeFilteredAndCheck(
                "A1",
                1, // frozenColumns
                0, // frozenRows
                "A1:A4,B1:D4", // range
                "A1:A4,B1:D4" // window
        );
    }

    private void loadCellRangeFilteredAndCheck(final String home,
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
                            public SpreadsheetViewportWindows window(final SpreadsheetViewportRectangle viewportRectangle,
                                                                     final boolean includeFrozenColumnsRows,
                                                                     final Optional<SpreadsheetSelection> selection,
                                                                     final SpreadsheetEngineContext context) {
                                if (range.equals("throw")) {
                                    throw new UnsupportedOperationException();
                                }
                                checkEquals(SpreadsheetDelta.NO_VIEWPORT_SELECTION, selection);

                                return SpreadsheetViewportWindows.parse(range);
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
                            public Optional<SpreadsheetViewport> navigate(final SpreadsheetViewport viewportSelection,
                                                                                   final SpreadsheetEngineContext context) {
                                return Optional.of(viewportSelection);
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
                                        SpreadsheetViewportWindows.parse(window)
                                )
                )
        );
    }

    // load cells navigation...........................................................................................

    @Test
    public void testLoadCellRangeNavigateCellLeft() {
        this.loadCellRangeNavigateAndCheck(
                "A1",
                SpreadsheetSelection.parseCell("B2")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
                        .setNavigations(
                                Lists.of(
                                        SpreadsheetViewportSelectionNavigation.leftColumn()
                                )
                        ),
                SpreadsheetSelection.parseCell("A2")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
        );
    }

    @Test
    public void testLoadCellRangeNavigateCellRight() {
        this.loadCellRangeNavigateAndCheck(
                "A1",
                SpreadsheetSelection.parseCell("B2")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
                        .setNavigations(
                                Lists.of(
                                        SpreadsheetViewportSelectionNavigation.rightColumn()
                                )
                        ),
                SpreadsheetSelection.parseCell("C2")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
        );
    }

    @Test
    public void testLoadCellRangeNavigateColumnLeft() {
        this.loadCellRangeNavigateAndCheck(
                "A1",
                SpreadsheetSelection.parseColumn("C")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
                        .setNavigations(
                                Lists.of(
                                        SpreadsheetViewportSelectionNavigation.leftColumn()
                                )
                        ),
                SpreadsheetSelection.parseColumn("B")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
        );
    }

    @Test
    public void testLoadCellRangeNavigateColumnDown() {
        this.loadCellRangeNavigateAndCheck(
                "A1",
                SpreadsheetSelection.parseColumn("C")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
                        .setNavigations(
                                Lists.of(
                                        SpreadsheetViewportSelectionNavigation.downRow()
                                )
                        ),
                SpreadsheetSelection.parseColumn("C")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
        );
    }

    @Test
    public void testLoadCellRangeNavigateRowDown() {
        this.loadCellRangeNavigateAndCheck(
                "A1",
                SpreadsheetSelection.parseRow("3")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
                        .setNavigations(
                                Lists.of(
                                        SpreadsheetViewportSelectionNavigation.downRow()
                                )
                        ),
                SpreadsheetSelection.parseRow("4")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
        );
    }

    @Test
    public void testLoadCellRangeNavigateCellExtendRight() {
        this.loadCellRangeNavigateAndCheck(
                "A1",
                SpreadsheetSelection.parseCell("B2")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
                        .setNavigations(
                                Lists.of(
                                        SpreadsheetViewportSelectionNavigation.extendRightColumn()
                                )
                        ),
                SpreadsheetSelection.parseCellRange("B2:C2")
                        .setAnchor(SpreadsheetViewportAnchor.TOP_LEFT)
        );
    }

    @Test
    public void testLoadCellRangeNavigateCellExtendLeft() {
        this.loadCellRangeNavigateAndCheck(
                "A1",
                SpreadsheetSelection.parseCell("B2")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
                        .setNavigations(
                                Lists.of(
                                        SpreadsheetViewportSelectionNavigation.extendLeftColumn()
                                )
                        ),
                SpreadsheetSelection.parseCellRange("A2:B2")
                        .setAnchor(SpreadsheetViewportAnchor.BOTTOM_RIGHT)
        );
    }

    @Test
    public void testLoadCellRangeNavigateCellRangeExtendRight() {
        this.loadCellRangeNavigateAndCheck(
                "A1",
                SpreadsheetSelection.parseCellRange("A2:B2")
                        .setAnchor(SpreadsheetViewportAnchor.TOP_LEFT)
                        .setNavigations(
                                Lists.of(
                                        SpreadsheetViewportSelectionNavigation.extendRightColumn()
                                )
                        ),
                SpreadsheetSelection.parseCellRange("A2:C2")
                        .setAnchor(SpreadsheetViewportAnchor.TOP_LEFT)
        );
    }

    @Test
    public void testLoadCellRangeNavigateCellRangeExtendRightBecomesCell() {
        this.loadCellRangeNavigateAndCheck(
                "A1",
                SpreadsheetSelection.parseCellRange("B2:C2")
                        .setAnchor(SpreadsheetViewportAnchor.TOP_RIGHT)
                        .setNavigations(
                                Lists.of(
                                        SpreadsheetViewportSelectionNavigation.extendRightColumn()
                                )
                        ),
                SpreadsheetSelection.parseCell("C2")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
        );
    }

    @Test
    public void testLoadCellRangeNavigateCellRangeExtendUpBecomesCell() {
        this.loadCellRangeNavigateAndCheck(
                "A1",
                SpreadsheetSelection.parseCellRange("C1:C2")
                        .setAnchor(SpreadsheetViewportAnchor.TOP_LEFT)
                        .setNavigations(
                                Lists.of(
                                        SpreadsheetViewportSelectionNavigation.extendUpRow()
                                )
                        ),
                SpreadsheetSelection.parseCell("C1")
                        .setAnchor(SpreadsheetViewportAnchor.NONE)
        );
    }

    private void loadCellRangeNavigateAndCheck(final String home,
                                               final SpreadsheetViewport viewportSelection,
                                               final SpreadsheetViewport expected) {
        final Map<HttpRequestAttribute<?>, Object> parameters = Maps.sorted();
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HOME, Lists.of(home));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.WIDTH, Lists.of("400")); // 4x3
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.HEIGHT, Lists.of("150"));
        parameters.put(SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false"));

        final SpreadsheetSelection selection = viewportSelection.selection();
        parameters.put(SpreadsheetEngineHttps.SELECTION, Lists.of(selection.toString()));
        parameters.put(SpreadsheetEngineHttps.SELECTION_TYPE, Lists.of(selection.selectionTypeName()));
        parameters.put(
                SpreadsheetEngineHttps.SELECTION_NAVIGATION,
                Lists.of(
                        SpreadsheetViewport.SEPARATOR.toSeparatedString(
                                viewportSelection.navigations(),
                                SpreadsheetViewportSelectionNavigation::text
                        )
                )
        );
        parameters.put(SpreadsheetEngineHttps.SELECTION_ANCHOR, Lists.of(viewportSelection.anchor().kebabText()));

        final SpreadsheetViewportWindows window = SpreadsheetViewportWindows.parse("A1:E5");

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
                            public SpreadsheetViewportWindows window(final SpreadsheetViewportRectangle viewportRectangle,
                                                                     final boolean includeFrozenColumnsRows,
                                                                     final Optional<SpreadsheetSelection> selection,
                                                                     final SpreadsheetEngineContext context) {
                                return window;
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
                            public Optional<SpreadsheetViewport> navigate(final SpreadsheetViewport s,
                                                                                   final SpreadsheetEngineContext context) {
                                return SpreadsheetEngines.basic()
                                        .navigate(s, context);
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

                            public SpreadsheetSelection resolveIfLabel(final SpreadsheetSelection selection) {
                                if(selection.isLabelName()) {
                                    throw new UnsupportedOperationException("Labels like " + selection + " are not supported in this test");
                                }
                                return selection;
                            }
                        }
                ),
                Optional.empty(),
                parameters,
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setViewport(
                                        Optional.of(expected)
                                ).setWindow(window)
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
            public SpreadsheetDelta loadCells(final SpreadsheetSelection selection,
                                              final SpreadsheetEngineEvaluation evaluation,
                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                              final SpreadsheetEngineContext context) {
                Objects.requireNonNull(selection, "selection");
                Objects.requireNonNull(evaluation, "evaluation");
                Objects.requireNonNull(deltaProperties, "deltaProperties");
                Objects.requireNonNull(context, "context");

                checkEquals(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCellTest.this.spreadsheetCellReference(),
                        selection,
                        "selection"
                );
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
