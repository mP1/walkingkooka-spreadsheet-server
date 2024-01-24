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
import walkingkooka.ToStringTesting;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.HateosHandlerTesting;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.conditionalformat.SpreadsheetConditionalFormattingRule;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetParsePattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStore;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetExpressionReferenceStore;
import walkingkooka.spreadsheet.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStore;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.text.Length;
import walkingkooka.tree.text.Text;
import walkingkooka.tree.text.TextStyle;
import walkingkooka.tree.text.TextStylePropertyName;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class SpreadsheetEngineHateosHandlerTestCase2<H extends SpreadsheetEngineHateosHandler<I, V, C>, I extends Comparable<I>, V, C>
        extends SpreadsheetEngineHateosHandlerTestCase<H>
        implements HateosHandlerTesting<H, I, V, C>,
        ToStringTesting<H> {

    SpreadsheetEngineHateosHandlerTestCase2() {
        super();
    }

    @Test
    public final void testWithNullEngineFails() {
        assertThrows(NullPointerException.class, () -> this.createHandler(null, this.engineContext()));
    }

    @Test
    public final void testWithNullEngineContextSupplierFails() {
        assertThrows(NullPointerException.class, () -> this.createHandler(this.engine(), null));
    }

    @Override
    public final H createHandler() {
        return this.createHandler(this.engine(), this.engineContext());
    }

    abstract H createHandler(final SpreadsheetEngine engine,
                             final SpreadsheetEngineContext context);

    final static double COLUMN_WIDTH = 100;

    static Map<SpreadsheetColumnReference, Double> columnWidths(final String columns) {
        final Map<SpreadsheetColumnReference, Double> map = Maps.sorted();

        Arrays.stream(columns.split(","))
                .forEach(c ->
                        map.put(
                                SpreadsheetSelection.parseColumn(c),
                                COLUMN_WIDTH
                        )
                );

        return map;
    }

    final static double ROW_HEIGHT = 30;

    static Map<SpreadsheetRowReference, Double> rowHeights(final String rows) {
        final Map<SpreadsheetRowReference, Double> map = Maps.sorted();

        Arrays.stream(rows.split(","))
                .forEach(r ->
                        map.put(
                                SpreadsheetSelection.parseRow(r),
                                ROW_HEIGHT
                        )
                );

        return map;
    }

    abstract SpreadsheetEngine engine();

    abstract SpreadsheetEngineContext engineContext();

    /**
     * Creates a {@link SpreadsheetEngineContext} with the provided engine and metadata with a limited {@link walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository}
     */
    final SpreadsheetEngineContext engineContext(final SpreadsheetEngine engine) {
        return SpreadsheetEngineContexts.basic(
                this.metadata(),
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

                    private final SpreadsheetCellStore cellStore = SpreadsheetCellStores.treeMap();

                    @Override
                    public SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferences() {
                        return this.cellReferences;
                    }

                    private final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferences = SpreadsheetExpressionReferenceStores.treeMap();

                    @Override
                    public SpreadsheetColumnStore columns() {
                        return this.columnStore;
                    }

                    private final SpreadsheetColumnStore columnStore = SpreadsheetColumnStores.treeMap();

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

                    @Override
                    public SpreadsheetCellRangeStore<SpreadsheetConditionalFormattingRule> rangeToConditionalFormattingRules() {
                        return this.rangeToConditionalFormattingRules;
                    }

                    private final SpreadsheetCellRangeStore<SpreadsheetConditionalFormattingRule> rangeToConditionalFormattingRules = SpreadsheetCellRangeStores.treeMap();

                    @Override
                    public SpreadsheetRowStore rows() {
                        return this.rowStore;
                    }

                    private final SpreadsheetRowStore rowStore = SpreadsheetRowStores.treeMap();

                    @Override
                    public String toString() {
                        return "cells: " + this.cells() +
                                ", cellReferences: " + this.cellReferences() +
                                ", columns: " + this.columns() +
                                ", labels: " + this.labels() +
                                ", labelReferences: " + this.labelReferences() +
                                ", rangeToCells: " + this.rangeToCells() +
                                ", rangeToConditionalFormattingRules: " + this.rangeToConditionalFormattingRules() +
                                ", rows: " + this.rows();
                    }
                },
                Url.parseAbsolute("https://example.com"),
                LocalDateTime::now
        );
    }

    /**
     * Creates a {@link SpreadsheetMetadata} with id=1 and all the necessary required properties
     */
    private SpreadsheetMetadata metadata() {
        return SpreadsheetMetadata.EMPTY
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
                .set(SpreadsheetMetadataPropertyName.GENERAL_NUMBER_FORMAT_DIGIT_COUNT, 8)
                .set(SpreadsheetMetadataPropertyName.PRECISION, 0)
                .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 50)
                .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetParsePattern.parseTextFormatPattern("@"))
                .set(
                        SpreadsheetMetadataPropertyName.STYLE,
                        TextStyle.EMPTY
                                .set(TextStylePropertyName.WIDTH, Length.pixel(COLUMN_WIDTH))
                                .set(TextStylePropertyName.HEIGHT, Length.pixel(ROW_HEIGHT))
                );
    }

    /**
     * Creates a cell with the formatted text. This does not parse anything else such as a math equation.
     */
    final SpreadsheetCell formattedCell(final SpreadsheetCellReference reference,
                                        final String text) {
        return reference.setFormula(
                SpreadsheetFormula.EMPTY
                        .setText("'" + text)
                        .setToken(
                                Optional.of(
                                        SpreadsheetParserToken.text(
                                                List.of(
                                                        SpreadsheetParserToken.apostropheSymbol("'", "'"),
                                                        SpreadsheetParserToken.textLiteral(text, text)
                                                ),
                                                "'" + text
                                        )
                                )
                        )
                        .setExpression(
                                Optional.of(
                                        Expression.value(text)
                                )
                        )
                        .setValue(Optional.of(text))
        ).setFormatted(Optional.of(Text.text(text)));
    }
}
