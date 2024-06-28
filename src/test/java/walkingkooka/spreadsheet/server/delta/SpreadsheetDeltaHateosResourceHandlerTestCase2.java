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
import walkingkooka.ToStringTesting;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProviders;
import walkingkooka.spreadsheet.conditionalformat.SpreadsheetConditionalFormattingRule;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviders;
import walkingkooka.spreadsheet.format.SpreadsheetParserProviders;
import walkingkooka.spreadsheet.format.SpreadsheetParserSelector;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetParsePattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.parser.SpreadsheetParserContext;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.engine.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStore;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetExpressionReferenceStore;
import walkingkooka.spreadsheet.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStore;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.parser.Parser;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProviders;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class SpreadsheetDeltaHateosResourceHandlerTestCase2<H extends SpreadsheetDeltaHateosResourceHandler<I>, I extends Comparable<I>>
        extends SpreadsheetDeltaHateosResourceHandlerTestCase<H>
        implements HateosResourceHandlerTesting<H, I, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
        SpreadsheetMetadataTesting,
        ToStringTesting<H> {

    SpreadsheetDeltaHateosResourceHandlerTestCase2() {
        super();
    }

    @Test
    public final void testWithNullEngineFails() {
        assertThrows(
                NullPointerException.class,
                () -> this.createHandler(
                        null
                )
        );
    }

    @Override
    public final H createHandler() {
        return this.createHandler(
                this.engine()
        );
    }

    abstract H createHandler(final SpreadsheetEngine engine);

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
        ).setFormattedValue(Optional.of(Text.text(text)));
    }

    final SpreadsheetCell cell() {
        return this.cell("A99", "1+2");
    }

    final SpreadsheetCell cell(final String cellReference, final String formula) {
        return SpreadsheetSelection.parseCell(cellReference)
                .setFormula(
                        SpreadsheetFormula.EMPTY
                                .setText(formula)
                );
    }

    final Set<SpreadsheetCell> cells() {
        return Sets.of(this.cell(), this.cellOutsideWindow());
    }

    final Set<SpreadsheetCell> cellsWithinWindow() {
        return Sets.of(this.cell());
    }

    final Set<SpreadsheetLabelMapping> labels() {
        return Sets.of(
                this.label().mapping(
                        this.cell().reference()
                )
        );
    }

    final SpreadsheetLabelName label() {
        return SpreadsheetSelection.labelName("Label1a");
    }

    final SpreadsheetViewportWindows window() {
        final SpreadsheetViewportWindows window = SpreadsheetViewportWindows.parse("A1:B99");

        this.checkEquals(
                true,
                window.test(this.cell().reference())
        );

        this.checkEquals(
                false,
                window.test(cellOutsideWindow().reference())
        );

        return window;
    }

    final SpreadsheetCell cellOutsideWindow() {
        return this.cell("Z99", "99");
    }

    @Override
    public Set<I> manyIds() {
        return Sets.of(this.id());
    }

    final static MediaType CONTENT_TYPE = MediaType.APPLICATION_JSON;

    final static SpreadsheetMetadata METADATA = SpreadsheetMetadata.EMPTY
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
            .set(SpreadsheetMetadataPropertyName.TEXT_FORMATTER, SpreadsheetParsePattern.DEFAULT_TEXT_FORMAT_PATTERN.spreadsheetFormatterSelector())
            .set(
                    SpreadsheetMetadataPropertyName.STYLE,
                    TextStyle.EMPTY
                            .set(TextStylePropertyName.WIDTH, Length.pixel(COLUMN_WIDTH))
                            .set(TextStylePropertyName.HEIGHT, Length.pixel(ROW_HEIGHT))
            );

    static class TestSpreadsheetEngineHateosResourceHandlerContext extends FakeSpreadsheetEngineHateosResourceHandlerContext {
        @Override
        public MediaType contentType() {
            return CONTENT_TYPE;
        }

        @Override
        public SpreadsheetMetadata spreadsheetMetadata() {
            return METADATA;
        }
    }

    TestSpreadsheetEngineHateosResourceHandlerContext context(final SpreadsheetCellStore store) {
        final SpreadsheetEngineContext engineContext = SpreadsheetEngineContexts.basic(
                METADATA,
                SpreadsheetComparatorProviders.spreadsheetComparators(),
                SpreadsheetFormatterProviders.spreadsheetFormatPattern(),
                ExpressionFunctionProviders.fake(),
                SpreadsheetParserProviders.spreadsheetParsePattern(),
                SpreadsheetDeltaHateosResourceHandlerTestCase2.this.engine(),
                (b) -> {
                    throw new UnsupportedOperationException();
                },
                new FakeSpreadsheetStoreRepository() {
                    @Override
                    public SpreadsheetCellStore cells() {
                        return store;
                    }

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
                },
                Url.parseAbsolute("https://example.com"),
                LocalDateTime::now
        );

        return new TestSpreadsheetEngineHateosResourceHandlerContext() {

            @Override
            public SpreadsheetParserToken parseFormula(final TextCursor cursor) {
                return engineContext.parseFormula(cursor);
            }

            @Override
            public SpreadsheetCell formatValueAndStyle(final SpreadsheetCell cell,
                                                       final Optional<SpreadsheetFormatter> formatter) {
                return engineContext.formatValueAndStyle(
                        cell,
                        formatter
                );
            }

            @Override
            public Optional<Parser<SpreadsheetParserContext>> spreadsheetParser(final SpreadsheetParserSelector spreadsheetParserSelector) {
                return engineContext.spreadsheetParser(spreadsheetParserSelector);
            }

            @Override
            public Optional<Expression> toExpression(final SpreadsheetParserToken spreadsheetParserToken) {
                return engineContext.toExpression(spreadsheetParserToken);
            }

            @Override
            public Object evaluate(final Expression expression,
                                   final Optional<SpreadsheetCell> cell) {
                return engineContext.evaluate(
                        expression,
                        cell
                );
            }

            @Override
            public SpreadsheetStoreRepository storeRepository() {
                return engineContext.storeRepository();
            }
        };
    }
}
