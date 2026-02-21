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

import walkingkooka.ToStringTesting;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.environment.AuditInfo;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.server.hateos.FakeHateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorAliasSet;
import walkingkooka.spreadsheet.convert.provider.SpreadsheetConvertersConverterProviders;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.engine.SpreadsheetMetadataMode;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.export.provider.SpreadsheetExporterAliasSet;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetParsePattern;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterAliasSet;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.formula.parser.SpreadsheetFormulaParserToken;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterAliasSet;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.parser.SpreadsheetParser;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserAliasSet;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserSelector;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceLoader;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetSelectionMaps;
import walkingkooka.spreadsheet.server.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.meta.SpreadsheetIdRouter;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellReferencesStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellReferencesStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStore;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelReferencesStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelReferencesStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStore;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.spreadsheet.value.SpreadsheetCell;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewportWindows;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;
import walkingkooka.tree.text.Length;
import walkingkooka.tree.text.Text;
import walkingkooka.tree.text.TextStyle;
import walkingkooka.tree.text.TextStylePropertyName;
import walkingkooka.validation.form.provider.FormHandlerAliasSet;
import walkingkooka.validation.provider.ValidatorAliasSet;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class SpreadsheetDeltaHateosResourceHandlerTestCase2<H extends SpreadsheetDeltaHateosResourceHandler<I>, I extends Comparable<I>>
    extends SpreadsheetDeltaHateosResourceHandlerTestCase<H>
    implements HateosResourceHandlerTesting<H, I, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
    SpreadsheetMetadataTesting,
    ToStringTesting<H> {

    final static FakeSpreadsheetEngineHateosResourceHandlerContext CONTEXT = new FakeSpreadsheetEngineHateosResourceHandlerContext() {
        @Override
        public SpreadsheetMetadata spreadsheetMetadata() {
            return SpreadsheetMetadata.EMPTY;
        }
    };

    SpreadsheetDeltaHateosResourceHandlerTestCase2() {
        super();
    }

    final static double COLUMN_WIDTH = 100;

    static Map<SpreadsheetColumnReference, Double> columnWidths(final String columns) {
        final Map<SpreadsheetColumnReference, Double> map = SpreadsheetSelectionMaps.column();

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
        final Map<SpreadsheetRowReference, Double> map = SpreadsheetSelectionMaps.row();

        Arrays.stream(rows.split(","))
            .forEach(r ->
                map.put(
                    SpreadsheetSelection.parseRow(r),
                    ROW_HEIGHT
                )
            );

        return map;
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
                        SpreadsheetFormulaParserToken.text(
                            List.of(
                                SpreadsheetFormulaParserToken.apostropheSymbol("'", "'"),
                                SpreadsheetFormulaParserToken.textLiteral(text, text)
                            ),
                            "'" + text
                        )
                    )
                )
                .setExpression(
                    Optional.of(
                        Expression.value(text)
                    )
                ).setValue(
                    Optional.of(text)
                )
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
            this.label()
                .setLabelMappingReference(
                    this.cell()
                        .reference()
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

    final static ConverterSelector CONVERTER = ConverterSelector.parse("collection(text, number, basic, spreadsheet-value)");

    final static SpreadsheetId SPREADSHEET_ID = SpreadsheetId.with(1);

    final static SpreadsheetMetadata METADATA = SpreadsheetMetadata.EMPTY
        .set(SpreadsheetMetadataPropertyName.LOCALE, LOCALE)
        .loadFromLocale(
            CURRENCY_LOCALE_CONTEXT
        ).set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SPREADSHEET_ID)
        .set(
            SpreadsheetMetadataPropertyName.AUDIT_INFO,
            AuditInfo.with(
                EmailAddress.parse("creator@example.com"),
                LocalDateTime.of(1999, 12, 31, 12, 0),
                EmailAddress.parse("modified@example.com"),
                LocalDateTime.of(1999, 12, 31, 12, 0)
            )
        ).set(SpreadsheetMetadataPropertyName.CELL_CHARACTER_WIDTH, 1)
        .set(SpreadsheetMetadataPropertyName.DATE_TIME_OFFSET, 0L)
        .set(
            SpreadsheetMetadataPropertyName.DECIMAL_NUMBER_DIGIT_COUNT,
            8
        ).set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, 20)
        .set(
            SpreadsheetMetadataPropertyName.ERROR_FORMATTER,
            SpreadsheetFormatterSelector.parse(
                "badge-error " + SpreadsheetParsePattern.DEFAULT_TEXT_FORMAT_PATTERN.spreadsheetFormatterSelector()
            )
        ).set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, ExpressionNumberKind.BIG_DECIMAL)
        .set(SpreadsheetMetadataPropertyName.FORMULA_CONVERTER, CONVERTER)
        .set(SpreadsheetMetadataPropertyName.FORMULA_FUNCTIONS, SpreadsheetExpressionFunctions.EMPTY_ALIAS_SET)
        .set(SpreadsheetMetadataPropertyName.FORMATTING_CONVERTER, CONVERTER)
        .set(SpreadsheetMetadataPropertyName.PRECISION, 0)
        .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
        .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 50)
        .set(SpreadsheetMetadataPropertyName.TEXT_FORMATTER, SpreadsheetParsePattern.DEFAULT_TEXT_FORMAT_PATTERN.spreadsheetFormatterSelector())
        .set(
            SpreadsheetMetadataPropertyName.STYLE,
            TextStyle.EMPTY
                .set(TextStylePropertyName.WIDTH, Length.pixel(COLUMN_WIDTH))
                .set(TextStylePropertyName.HEIGHT, Length.pixel(ROW_HEIGHT))
        ).set(
            SpreadsheetMetadataPropertyName.COMPARATORS,
            SpreadsheetComparatorAliasSet.EMPTY
        ).set(
            SpreadsheetMetadataPropertyName.CONVERTERS,
            SpreadsheetConvertersConverterProviders.ALL.aliasSet()
        ).set(
            SpreadsheetMetadataPropertyName.EXPORTERS,
            SpreadsheetExporterAliasSet.EMPTY
        ).set(
            SpreadsheetMetadataPropertyName.FORM_HANDLERS,
            FormHandlerAliasSet.EMPTY
        ).set(
            SpreadsheetMetadataPropertyName.FORMATTERS,
            SpreadsheetFormatterAliasSet.EMPTY
        ).set(
            SpreadsheetMetadataPropertyName.FUNCTIONS,
            SpreadsheetExpressionFunctions.EMPTY_ALIAS_SET
        ).set(
            SpreadsheetMetadataPropertyName.IMPORTERS,
            SpreadsheetImporterAliasSet.EMPTY
        ).set(
            SpreadsheetMetadataPropertyName.PARSERS,
            SpreadsheetParserAliasSet.EMPTY
        ).set(
            SpreadsheetMetadataPropertyName.VALIDATORS,
            ValidatorAliasSet.EMPTY
        );

    static abstract class TestSpreadsheetEngineHateosResourceHandlerContext extends FakeSpreadsheetEngineHateosResourceHandlerContext {
        @Override
        public MediaType contentType() {
            return CONTENT_TYPE;
        }

        @Override
        abstract public SpreadsheetMetadata spreadsheetMetadata();
    }

    TestSpreadsheetEngineHateosResourceHandlerContext context(final SpreadsheetEngine engine) {
        return this.context(
            engine,
            SpreadsheetCellStores.fake()
        );
    }

    TestSpreadsheetEngineHateosResourceHandlerContext context(final SpreadsheetEngine engine,
                                                              final SpreadsheetCellStore cellStore) {
        final SpreadsheetStoreRepository repos = new FakeSpreadsheetStoreRepository() {
            @Override
            public SpreadsheetCellStore cells() {
                return cellStore;
            }

            @Override
            public SpreadsheetCellReferencesStore cellReferences() {
                return this.cellReferences;
            }

            private final SpreadsheetCellReferencesStore cellReferences = SpreadsheetCellReferencesStores.treeMap();

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
            public SpreadsheetLabelReferencesStore labelReferences() {
                return this.labelReferences;
            }

            private final SpreadsheetLabelReferencesStore labelReferences = SpreadsheetLabelReferencesStores.treeMap();

            @Override
            public SpreadsheetMetadataStore metadatas() {
                return this.metadata;
            }

            private final SpreadsheetMetadataStore metadata = SpreadsheetMetadataStores.treeMap();

            @Override
            public SpreadsheetCellRangeStore<SpreadsheetCellReference> rangeToCells() {
                return this.rangeToCells;
            }

            private final SpreadsheetCellRangeStore<SpreadsheetCellReference> rangeToCells = SpreadsheetCellRangeStores.treeMap();

            @Override
            public SpreadsheetRowStore rows() {
                return this.rowStore;
            }

            private final SpreadsheetRowStore rowStore = SpreadsheetRowStores.treeMap();
        };
        repos.metadatas()
            .save(METADATA);

        final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext = SPREADSHEET_ENVIRONMENT_CONTEXT.cloneEnvironment();
        spreadsheetEnvironmentContext.setEnvironmentValue(
            SpreadsheetEnvironmentContext.SPREADSHEET_ID,
            SPREADSHEET_ID
        );

        final SpreadsheetContext spreadsheetContext = SpreadsheetContexts.fixedSpreadsheetId(
            SpreadsheetEngines.basic(),
            repos,
            (SpreadsheetEngineContext c) -> SpreadsheetIdRouter.create(
                c,
                new FakeHateosResourceHandlerContext() {
                    @Override
                    public HateosResourceHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
                        return this;
                    }

                    @Override
                    public HateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
                        return this;
                    }
                }
            ),
            CURRENCY_LOCALE_CONTEXT,
            spreadsheetEnvironmentContext,
            SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT
        );

        final SpreadsheetEngineContext engineContext = spreadsheetContext.spreadsheetEngineContext();

        return new TestSpreadsheetEngineHateosResourceHandlerContext() {

            @Override
            public SpreadsheetEngine spreadsheetEngine() {
                return engine;
            }

            @Override
            public SpreadsheetFormulaParserToken parseFormula(final TextCursor cursor,
                                                              final Optional<SpreadsheetCell> cell) {
                return engineContext.parseFormula(
                    cursor,
                    cell
                );
            }

            @Override
            public SpreadsheetCell formatValueAndStyle(final SpreadsheetCell cell,
                                                       final Optional<SpreadsheetFormatterSelector> formatter) {
                return engineContext.formatValueAndStyle(
                    cell,
                    formatter
                );
            }

            @Override
            public SpreadsheetParser spreadsheetParser(final SpreadsheetParserSelector selector,
                                                       final ProviderContext context) {
                return engineContext.spreadsheetParser(
                    selector,
                    context
                );
            }

            @Override
            public Optional<Expression> toExpression(final SpreadsheetFormulaParserToken spreadsheetParserToken) {
                return engineContext.toExpression(spreadsheetParserToken);
            }

            @Override
            public SpreadsheetEngineContext setSpreadsheetMetadataMode(final SpreadsheetMetadataMode mode) {
                return this;
            }

            @Override
            public SpreadsheetExpressionEvaluationContext spreadsheetExpressionEvaluationContext(final Optional<SpreadsheetCell> cell,
                                                                                                 final SpreadsheetExpressionReferenceLoader loader) {
                return engineContext.spreadsheetExpressionEvaluationContext(
                    cell,
                    loader
                );
            }

            @Override
            public SpreadsheetStoreRepository storeRepository() {
                return engineContext.storeRepository();
            }

            @Override
            public SpreadsheetMetadata spreadsheetMetadata() {
                return METADATA;
            }

            @Override
            public ProviderContext providerContext() {
                return PROVIDER_CONTEXT;
            }
        };
    }
}
