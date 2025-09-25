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

package walkingkooka.spreadsheet.server;

import walkingkooka.Either;
import walkingkooka.convert.Converter;
import walkingkooka.convert.ConverterContext;
import walkingkooka.convert.provider.ConverterInfoSet;
import walkingkooka.convert.provider.ConverterName;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.datetime.DateTimeSymbols;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.math.DecimalNumberSymbols;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.FakeHateosResourceHandlerContext;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.compare.SpreadsheetComparator;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorInfoSet;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorSelector;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.export.SpreadsheetExporter;
import walkingkooka.spreadsheet.export.provider.SpreadsheetExporterInfoSet;
import walkingkooka.spreadsheet.export.provider.SpreadsheetExporterName;
import walkingkooka.spreadsheet.export.provider.SpreadsheetExporterSelector;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviderSamplesContext;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelectorToken;
import walkingkooka.spreadsheet.formula.parser.SpreadsheetFormulaParserToken;
import walkingkooka.spreadsheet.importer.SpreadsheetImporter;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterInfoSet;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterName;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterSelector;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.parser.SpreadsheetParser;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserInfoSet;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserName;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserSelector;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserSelectorToken;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceLoader;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionFunctionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionAliasSet;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionSelector;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;
import walkingkooka.tree.text.TextNode;
import walkingkooka.validation.ValidationReference;
import walkingkooka.validation.Validator;
import walkingkooka.validation.ValidatorContext;
import walkingkooka.validation.form.FormHandler;
import walkingkooka.validation.form.FormHandlerContext;
import walkingkooka.validation.form.provider.FormHandlerInfoSet;
import walkingkooka.validation.form.provider.FormHandlerName;
import walkingkooka.validation.form.provider.FormHandlerSelector;
import walkingkooka.validation.provider.ValidatorInfoSet;
import walkingkooka.validation.provider.ValidatorName;
import walkingkooka.validation.provider.ValidatorSelector;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class FakeSpreadsheetEngineHateosResourceHandlerContext extends FakeHateosResourceHandlerContext implements SpreadsheetEngineHateosResourceHandlerContext {

    @Override
    public SpreadsheetEngine spreadsheetEngine() {
        throw new UnsupportedOperationException();
    }

    // SpreadsheetContext...............................................................................................

    @Override
    public SpreadsheetId spreadsheetId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadata createMetadata(final EmailAddress user,
                                              final Optional<Locale> locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetMetadata> loadMetadata(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteMetadata(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SpreadsheetMetadata> findMetadataBySpreadsheetName(final String name,
                                                                   final int offset,
                                                                   final int count) {
        throw new UnsupportedOperationException();
    }

    // SpreadsheetEngineContext.........................................................................................

    @Override
    public AbsoluteUrl serverUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetEngineContext spreadsheetEngineContext(final SpreadsheetMetadataPropertyName<ExpressionFunctionAliasSet> functionAliases) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetFormulaParserToken parseFormula(final TextCursor textCursor,
                                                      final Optional<SpreadsheetCell> cell) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Expression> toExpression(final SpreadsheetFormulaParserToken spreadsheetParserToken) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetExpressionEvaluationContext spreadsheetExpressionEvaluationContext(final Optional<SpreadsheetCell> cell,
                                                                                         final SpreadsheetExpressionReferenceLoader loader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<TextNode> formatValue(final SpreadsheetCell cell,
                                          final Optional<Object> value,
                                          final Optional<SpreadsheetFormatterSelector> formatter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetCell formatValueAndStyle(final SpreadsheetCell cell,
                                               final Optional<SpreadsheetFormatterSelector> formatter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetStoreRepository storeRepository() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDateTime now() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadata spreadsheetMetadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetSelection> resolveLabel(final SpreadsheetLabelName spreadsheetLabelName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPure(final ExpressionFunctionName name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        throw new UnsupportedOperationException();
    }

    // ConverterProvider................................................................................................

    @Override
    public <C extends ConverterContext> Converter<C> converter(final ConverterSelector selector,
                                                               final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C extends ConverterContext> Converter<C> converter(final ConverterName converterName,
                                                               final List<?> values,
                                                               final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConverterInfoSet converterInfos() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetComparator<?> spreadsheetComparator(final SpreadsheetComparatorSelector selector,
                                                          final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetComparator<?> spreadsheetComparator(final SpreadsheetComparatorName name,
                                                          final List<?> values,
                                                          final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetComparatorInfoSet spreadsheetComparatorInfos() {
        throw new UnsupportedOperationException();
    }

    // SpreadsheetExporterProvider......................................................................................

    @Override
    public SpreadsheetExporter spreadsheetExporter(final SpreadsheetExporterSelector selector,
                                                   final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetExporter spreadsheetExporter(final SpreadsheetExporterName name,
                                                   final List<?> values,
                                                   final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetExporterInfoSet spreadsheetExporterInfos() {
        throw new UnsupportedOperationException();
    }

    // FormHandlerProvider..............................................................................................

    @Override
    public <R extends ValidationReference, S, C extends FormHandlerContext<R, S>> FormHandler<R, S, C> formHandler(final FormHandlerSelector selector,
                                                                                                                   final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R extends ValidationReference, S, C extends FormHandlerContext<R, S>> FormHandler<R, S, C> formHandler(final FormHandlerName name,
                                                                                                                   final List<?> values,
                                                                                                                   final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormHandlerInfoSet formHandlerInfos() {
        throw new UnsupportedOperationException();
    }

    // ExpressionFunctionProvider.......................................................................................

    @Override
    public ExpressionFunctionInfoSet expressionFunctionInfos() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionFunction<?, SpreadsheetExpressionEvaluationContext> expressionFunction(final ExpressionFunctionSelector selector,
                                                                                            final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionFunction<?, SpreadsheetExpressionEvaluationContext> expressionFunction(final ExpressionFunctionName name,
                                                                                            final List<?> values,
                                                                                            final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CaseSensitivity expressionFunctionNameCaseSensitivity() {
        throw new UnsupportedOperationException();
    }

    // SpreadsheetFormatterProvider.....................................................................................

    @Override
    public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterName name,
                                                     final List<?> values,
                                                     final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterSelector selector,
                                                     final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetFormatterSelectorToken> spreadsheetFormatterNextToken(final SpreadsheetFormatterSelector selector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SpreadsheetFormatterSample> spreadsheetFormatterSamples(final SpreadsheetFormatterSelector selector,
                                                                        final boolean includeSamples,
                                                                        final SpreadsheetFormatterProviderSamplesContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetFormatterInfoSet spreadsheetFormatterInfos() {
        throw new UnsupportedOperationException();
    }

    // SpreadsheetImporterProvider......................................................................................

    @Override
    public SpreadsheetImporter spreadsheetImporter(final SpreadsheetImporterSelector selector,
                                                   final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetImporter spreadsheetImporter(final SpreadsheetImporterName name,
                                                   final List<?> values,
                                                   final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetImporterInfoSet spreadsheetImporterInfos() {
        throw new UnsupportedOperationException();
    }

    // SpreadsheetParserProvider........................................................................................

    @Override
    public SpreadsheetParser spreadsheetParser(final SpreadsheetParserSelector selector,
                                               final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetParser spreadsheetParser(final SpreadsheetParserName name,
                                               final List<?> values,
                                               final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetParserSelectorToken> spreadsheetParserNextToken(final SpreadsheetParserSelector selector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetFormatterSelector> spreadsheetFormatterSelector(final SpreadsheetParserSelector selector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetParserInfoSet spreadsheetParserInfos() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R extends ValidationReference, C extends ValidatorContext<R>> Validator<R, C> validator(final ValidatorSelector validatorSelector,
                                                                                                    final ProviderContext providerContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R extends ValidationReference, C extends ValidatorContext<R>> Validator<R, C> validator(final ValidatorName validatorName,
                                                                                                    final List<?> values,
                                                                                                    final ProviderContext providerContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValidatorInfoSet validatorInfos() {
        throw new UnsupportedOperationException();
    }

    // LocaleContext....................................................................................................

    @Override
    public Set<Locale> availableLocales() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<DateTimeSymbols> dateTimeSymbolsForLocale(final Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<DecimalNumberSymbols> decimalNumberSymbolsForLocale(final Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Locale> findByLocaleText(final String text,
                                        final int offset,
                                        final int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale locale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> localeText(final Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext setLocale(final Locale locale) {
        throw new UnsupportedOperationException();
    }

    // CanConvert.......................................................................................................

    @Override
    public boolean canConvert(final Object value,
                              final Class<?> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Either<T, String> convert(final Object value,
                                         final Class<T> type) {
        throw new UnsupportedOperationException();
    }

    // EnvironmentContext...............................................................................................

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext cloneEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<T> environmentValue(final EnvironmentValueName<T> name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> SpreadsheetEngineHateosResourceHandlerContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                                 final T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<EnvironmentValueName<?>> environmentValueNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<EmailAddress> user() {
        throw new UnsupportedOperationException();
    }

    // SpreadsheetEngineHateosResourceHandlerContext..........................................................................

    @Override
    public SpreadsheetProvider systemSpreadsheetProvider() {
        throw new UnsupportedOperationException();
    }

    // HasProviderContext...............................................................................................

    @Override
    public ProviderContext providerContext() {
        throw new UnsupportedOperationException();
    }
}
