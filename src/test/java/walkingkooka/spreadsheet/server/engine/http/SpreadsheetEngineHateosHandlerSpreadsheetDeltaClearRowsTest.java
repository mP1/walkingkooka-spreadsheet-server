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
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetParsePatterns;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.store.SpreadsheetCellRangeStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.tree.expression.ExpressionNumberKind;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRowsTest extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows,
        SpreadsheetRowReference> {

    private final static Optional<SpreadsheetDelta> RESOURCE = Optional.of(SpreadsheetDelta.EMPTY);

    @Test
    public void testClearRow() {
        final SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"))
                .loadFromLocale()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1))
                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("creator@example.com"))
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, LocalDateTime.of(1999, 12, 31, 12, 0))
                .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, EmailAddress.parse("modified@example.com"))
                .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, LocalDateTime.of(1999, 12, 31, 12, 0))
                .set(SpreadsheetMetadataPropertyName.CELL_CHARACTER_WIDTH, 1)
                .set(SpreadsheetMetadataPropertyName.DATETIME_OFFSET, 0L)
                .set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, 20)
                .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, ExpressionNumberKind.BIG_DECIMAL)
                .set(SpreadsheetMetadataPropertyName.PRECISION, 0)
                .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 50)
                .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetParsePatterns.parseTextFormatPattern("@"));

        final SpreadsheetEngine engine = SpreadsheetEngines.basic(metadata);
        final SpreadsheetCellStore cellStore = SpreadsheetCellStores.treeMap();

        final SpreadsheetEngineContext context = SpreadsheetEngineContexts.basic(
                metadata,
                (n) -> {
                    throw new UnsupportedOperationException();
                },
                engine,
                (b) -> {
                    throw new UnsupportedOperationException();
                },
                new FakeSpreadsheetStoreRepository() {
                    @Override
                    public SpreadsheetCellStore cells() {
                        return cellStore;
                    }

                    @Override
                    public SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferences() {
                        return this.cellReferences;
                    }

                    private final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferences = SpreadsheetExpressionReferenceStores.treeMap();

                    @Override
                    public SpreadsheetLabelStore labels() {
                        return this.labels;
                    }

                    private final SpreadsheetLabelStore labels = SpreadsheetLabelStores.treeMap();

                    @Override
                    public SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferences() {
                        return this.labelReferences;
                    }

                    private final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferences = SpreadsheetExpressionReferenceStores.treeMap();

                    @Override
                    public SpreadsheetCellRangeStore<SpreadsheetCellReference> rangeToCells() {
                        return this.rangeToCells;
                    }

                    private final SpreadsheetCellRangeStore<SpreadsheetCellReference> rangeToCells = SpreadsheetCellRangeStores.treeMap();
                }
        );

        final SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows handler = SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows.with(
                engine,
                context
        );

        final SpreadsheetRowReference row1 = SpreadsheetSelection.parseRow("1");

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        final SpreadsheetCellReference row1ColMax = row1.setColumn(SpreadsheetColumnReference.MAX);
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("b2");

        cellStore.save(SpreadsheetCell.with(a1, SpreadsheetFormula.EMPTY));
        cellStore.save(SpreadsheetCell.with(row1ColMax, SpreadsheetFormula.EMPTY));
        cellStore.save(SpreadsheetCell.with(b2, SpreadsheetFormula.EMPTY));

        this.handleOneAndCheck(
                handler,
                row1,
                RESOURCE,
                HateosHandler.NO_PARAMETERS,
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setDeletedCells(
                                        Sets.of(a1, row1ColMax)
                                )
                )
        );

        assertEquals(Optional.empty(), cellStore.load(a1), "a1 should have been deleted");
        assertEquals(Optional.empty(), cellStore.load(row1ColMax), "A1048576 should have been deleted");
        assertNotEquals(Optional.empty(), cellStore.load(b2), "b2 should NOT have been deleted");
    }

    @Test
    public void testClearRowRange() {
        final SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"))
                .loadFromLocale()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1))
                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("creator@example.com"))
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, LocalDateTime.of(1999, 12, 31, 12, 0))
                .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, EmailAddress.parse("modified@example.com"))
                .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, LocalDateTime.of(1999, 12, 31, 12, 0))
                .set(SpreadsheetMetadataPropertyName.CELL_CHARACTER_WIDTH, 1)
                .set(SpreadsheetMetadataPropertyName.DATETIME_OFFSET, 0L)
                .set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, 20)
                .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, ExpressionNumberKind.BIG_DECIMAL)
                .set(SpreadsheetMetadataPropertyName.PRECISION, 0)
                .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 50)
                .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetParsePatterns.parseTextFormatPattern("@"));

        final SpreadsheetEngine engine = SpreadsheetEngines.basic(metadata);
        final SpreadsheetCellStore cellStore = SpreadsheetCellStores.treeMap();

        final SpreadsheetEngineContext context = SpreadsheetEngineContexts.basic(
                metadata,
                (n) -> {
                    throw new UnsupportedOperationException();
                },
                engine,
                (b) -> {
                    throw new UnsupportedOperationException();
                },
                new FakeSpreadsheetStoreRepository() {
                    @Override
                    public SpreadsheetCellStore cells() {
                        return cellStore;
                    }

                    @Override
                    public SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferences() {
                        return this.cellReferences;
                    }

                    private final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferences = SpreadsheetExpressionReferenceStores.treeMap();

                    @Override
                    public SpreadsheetLabelStore labels() {
                        return this.labels;
                    }

                    private final SpreadsheetLabelStore labels = SpreadsheetLabelStores.treeMap();

                    @Override
                    public SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferences() {
                        return this.labelReferences;
                    }

                    private final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferences = SpreadsheetExpressionReferenceStores.treeMap();

                    @Override
                    public SpreadsheetCellRangeStore<SpreadsheetCellReference> rangeToCells() {
                        return this.rangeToCells;
                    }

                    private final SpreadsheetCellRangeStore<SpreadsheetCellReference> rangeToCells = SpreadsheetCellRangeStores.treeMap();
                }
        );

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetCellReference d4 = SpreadsheetSelection.parseCell("D4");

        cellStore.save(SpreadsheetCell.with(a1, SpreadsheetFormula.EMPTY));
        cellStore.save(SpreadsheetCell.with(b2, SpreadsheetFormula.EMPTY));
        cellStore.save(SpreadsheetCell.with(c3, SpreadsheetFormula.EMPTY));

        cellStore.save(SpreadsheetCell.with(d4, SpreadsheetFormula.EMPTY));

        final SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows handler = SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows.with(
                engine,
                context
        );

        this.handleRangeAndCheck(
                handler,
                b2.row().spreadsheetRowRange(c3.row()).range(),
                RESOURCE,
                HateosHandler.NO_PARAMETERS,
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setDeletedCells(
                                        Sets.of(b2, c3)
                                )
                )
        );

        assertNotEquals(Optional.empty(), cellStore.load(a1), "a1 should NOT have been deleted");
        assertEquals(Optional.empty(), cellStore.load(b2), "b2 should have been deleted");
        assertEquals(Optional.empty(), cellStore.load(c3), "c3 should have been deleted");
        assertNotEquals(Optional.empty(), cellStore.load(d4), "d4 should have been deleted");
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(), SpreadsheetEngine.class.getSimpleName() + ".clearRows"
        );
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows createHandler(final SpreadsheetEngine engine,
                                                                          final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows.with(engine, context);
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
    public SpreadsheetRowReference id() {
        return SpreadsheetSelection.parseRow("3");
    }

    @Override
    public Range<SpreadsheetRowReference> range() {
        return SpreadsheetRowReference.parseRowRange(RANGE)
                .range();
    }

    private final static String RANGE = "1:3";

    @Override
    public Optional<SpreadsheetDelta> resource() {
        return RESOURCE;
    }

    @Override
    public Optional<SpreadsheetDelta> collectionResource() {
        return RESOURCE;
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows.class;
    }
}
