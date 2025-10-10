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

package walkingkooka.spreadsheet.server.sample;

import org.junit.jupiter.api.Test;
import walkingkooka.Cast;
import walkingkooka.collect.list.Lists;
import walkingkooka.convert.ConverterContexts;
import walkingkooka.convert.Converters;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.environment.AuditInfo;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.locale.LocaleContexts;
import walkingkooka.math.DecimalNumberSymbols;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.Accept;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.header.MediaTypeDetectors;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorAliasSet;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorProviders;
import walkingkooka.spreadsheet.convert.provider.SpreadsheetConvertersConverterProviders;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextMode;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.export.provider.SpreadsheetExporterAliasSet;
import walkingkooka.spreadsheet.export.provider.SpreadsheetExporterProviders;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterAliasSet;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviders;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterAliasSet;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterProviders;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserAliasSet;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserProvider;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserProviders;
import walkingkooka.spreadsheet.provider.SpreadsheetProviders;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.server.SpreadsheetHttpServer;
import walkingkooka.spreadsheet.server.SpreadsheetServerContexts;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellReferencesStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelReferencesStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.validation.form.store.SpreadsheetFormStores;
import walkingkooka.storage.Storages;
import walkingkooka.terminal.TerminalContexts;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProviders;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;
import walkingkooka.tree.text.Length;
import walkingkooka.tree.text.TextStyle;
import walkingkooka.tree.text.TextStylePropertyName;
import walkingkooka.validation.form.provider.FormHandlerAliasSet;
import walkingkooka.validation.form.provider.FormHandlerProviders;
import walkingkooka.validation.form.provider.FormHandlerSelector;
import walkingkooka.validation.provider.ValidatorAliasSet;
import walkingkooka.validation.provider.ValidatorProviders;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

public final class Sample implements walkingkooka.text.printer.TreePrintableTesting {

    public static void main(final String[] args) {
        new Sample();
    }

    private Sample() {
        this.testCreateSpreadsheet();
    }

    @Test
    public void testCreateSpreadsheet() {
        final TestHttpServer httpServer = new TestHttpServer();
        spreadsheetHttpServer(httpServer);

        final HttpResponse response = HttpResponses.recording();

        httpServer.handler.handle(
            HttpRequests.value(
                HttpTransport.SECURED,
                HttpMethod.POST,
                Url.parseRelative("/api/spreadsheet"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    Accept.with(
                        Lists.of(SpreadsheetServerMediaTypes.CONTENT_TYPE)
                    )
                )
            ),
            response
        );

        checkEquals(
            Optional.of(
                HttpStatusCode.CREATED.status()
            ),
            response.status(),
            "POST /api/spreadsheet status"
        );

        checkEquals(
            HttpEntity.EMPTY.setContentType(
                MediaType.APPLICATION_JSON.setCharset(CharsetName.UTF_8)
            ).addHeader(
                HttpHeaderName.with("X-Content-Type-Name"),
                Cast.to(SpreadsheetMetadata.class.getSimpleName())
            ).setBodyText(
                "{\n" +
                    "  \"spreadsheetId\": \"1\",\n" +
                    "  \"auditInfo\": {\n" +
                    "    \"createdBy\": \"user@example.com\",\n" +
                    "    \"createdTimestamp\": \"1999-12-31T12:58:59\",\n" +
                    "    \"modifiedBy\": \"user@example.com\",\n" +
                    "    \"modifiedTimestamp\": \"1999-12-31T12:58:59\"\n" +
                    "  },\n" +
                    "  \"cellCharacterWidth\": 10,\n" +
                    "  \"comparators\": \"\",\n" +
                    "  \"converters\": \"basic, boolean, boolean-to-text, collection, collection-to-list, color, color-to-color, color-to-number, date-time, date-time-symbols, decimal-number-symbols, environment, error-throwing, error-to-error, error-to-number, expression, form-and-validation, format-pattern-to-string, has-formatter-selector, has-host-address, has-parser-selector, has-spreadsheet-selection, has-style, has-text-node, has-validator-selector, json, jsonTo, locale, locale-to-text, net, null-to-number, number, number-to-color, number-to-number, number-to-text, plugins, spreadsheet-cell-set, spreadsheet-metadata, spreadsheet-selection-to-spreadsheet-selection, spreadsheet-selection-to-text, spreadsheet-value, style, system, template, text, text-node, text-to-boolean-list, text-to-color, text-to-csv-string-list, text-to-date-list, text-to-date-time-list, text-to-email-address, text-to-environment-value-name, text-to-error, text-to-expression, text-to-form-name, text-to-has-host-address, text-to-host-address, text-to-json, text-to-locale, text-to-number-list, text-to-object, text-to-spreadsheet-color-name, text-to-spreadsheet-formatter-selector, text-to-spreadsheet-id, text-to-spreadsheet-metadata, text-to-spreadsheet-metadata-color, text-to-spreadsheet-metadata-property-name, text-to-spreadsheet-name, text-to-spreadsheet-selection, text-to-spreadsheet-text, text-to-string-list, text-to-template-value-name, text-to-text, text-to-text-node, text-to-text-style, text-to-text-style-property-name, text-to-time-list, text-to-url, text-to-url-fragment, text-to-url-query-string, text-to-validation-error, text-to-validator-selector, text-to-value-type, to-boolean, to-json, to-number, to-styleable, to-validation-choice-list, to-validation-error-list, url, url-to-hyperlink, url-to-image\",\n" +
                    "  \"dateFormatter\": \"date-format-pattern DD/MM/YYYY\",\n" +
                    "  \"dateParser\": \"date-parse-pattern DD/MM/YYYYDDMMYYYY\",\n" +
                    "  \"dateTimeFormatter\": \"date-time-format-pattern DD/MM/YYYY hh:mm\",\n" +
                    "  \"dateTimeOffset\": \"0\",\n" +
                    "  \"dateTimeParser\": \"date-time-parse-pattern DD/MM/YYYY hh:mmDDMMYYYYHHMMDDMMYYYY HHMM\",\n" +
                    "  \"decimalNumberSymbols\": {\n" +
                    "    \"negativeSign\": \"-\",\n" +
                    "    \"positiveSign\": \"+\",\n" +
                    "    \"zeroDigit\": \"0\",\n" +
                    "    \"currencySymbol\": \"$AUD\",\n" +
                    "    \"decimalSeparator\": \".\",\n" +
                    "    \"exponentSymbol\": \"E\",\n" +
                    "    \"groupSeparator\": \",\",\n" +
                    "    \"infinitySymbol\": \"Infinity!\",\n" +
                    "    \"monetaryDecimalSeparator\": \".\",\n" +
                    "    \"nanSymbol\": \"Nan!\",\n" +
                    "    \"percentSymbol\": \"%\",\n" +
                    "    \"permillSymbol\": \"^\"\n" +
                    "  },\n" +
                    "  \"defaultFormHandler\": \"non-null\",\n" +
                    "  \"defaultYear\": 1900,\n" +
                    "  \"exporters\": \"\",\n" +
                    "  \"expressionNumberKind\": \"DOUBLE\",\n" +
                    "  \"formHandlers\": \"basic\",\n" +
                    "  \"formatters\": \"simple\",\n" +
                    "  \"formattingConverter\": \"simple\",\n" +
                    "  \"formulaConverter\": \"collection(text, number, basic, spreadsheet-value)\",\n" +
                    "  \"formulaFunctions\": \"@\",\n" +
                    "  \"frozenColumns\": \"A:B\",\n" +
                    "  \"frozenRows\": \"1:2\",\n" +
                    "  \"functions\": \"@\",\n" +
                    "  \"generalNumberFormatDigitCount\": 8,\n" +
                    "  \"importers\": \"\",\n" +
                    "  \"locale\": \"en-AU\",\n" +
                    "  \"numberFormatter\": \"number-format-pattern #0.0\",\n" +
                    "  \"numberParser\": \"number-parse-pattern #\",\n" +
                    "  \"parsers\": \"\",\n" +
                    "  \"precision\": 123,\n" +
                    "  \"roundingMode\": \"FLOOR\",\n" +
                    "  \"style\": {\n" +
                    "    \"height\": \"50px\",\n" +
                    "    \"width\": \"50px\"\n" +
                    "  },\n" +
                    "  \"textFormatter\": \"text-format-pattern @@\",\n" +
                    "  \"timeFormatter\": \"time-format-pattern hh:mm\",\n" +
                    "  \"timeParser\": \"time-parse-pattern hh:mmhh:mm:ss.000\",\n" +
                    "  \"twoDigitYear\": 31,\n" +
                    "  \"validationFunctions\": \"@\",\n" +
                    "  \"validationValidators\": \"\",\n" +
                    "  \"validators\": \"\",\n" +
                    "  \"valueSeparator\": \",\"\n" +
                    "}"
            ).setContentLength(),
            response.entity(),
            "POST /api/spreadsheet response"
        );
    }

    private static SpreadsheetHttpServer spreadsheetHttpServer(final TestHttpServer httpServer) {
        final SpreadsheetId createdId = SpreadsheetId.with(1);
        final LocalDateTime now = LocalDateTime.of(1999, 12, 31, 12, 58, 59);
        final Locale locale = Locale.forLanguageTag("en-AU");

        final ExpressionNumberKind expressionNumberKind = ExpressionNumberKind.DOUBLE;

        final SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY.set(SpreadsheetMetadataPropertyName.CELL_CHARACTER_WIDTH, 10)
            .set(SpreadsheetMetadataPropertyName.COMPARATORS, SpreadsheetComparatorAliasSet.EMPTY)
            .set(SpreadsheetMetadataPropertyName.CONVERTERS, SpreadsheetConvertersConverterProviders.ALL.aliasSet())
            .set(SpreadsheetMetadataPropertyName.DATE_FORMATTER, SpreadsheetPattern.parseDateFormatPattern("DD/MM/YYYY").spreadsheetFormatterSelector())
            .set(SpreadsheetMetadataPropertyName.DATE_PARSER, SpreadsheetPattern.parseDateParsePattern("DD/MM/YYYYDDMMYYYY").spreadsheetParserSelector())
            .set(SpreadsheetMetadataPropertyName.DATE_TIME_FORMATTER, SpreadsheetPattern.parseDateTimeFormatPattern("DD/MM/YYYY hh:mm").spreadsheetFormatterSelector())
            .set(SpreadsheetMetadataPropertyName.DATE_TIME_OFFSET, Converters.JAVA_EPOCH_OFFSET)
            .set(SpreadsheetMetadataPropertyName.DATE_TIME_PARSER, SpreadsheetPattern.parseDateTimeParsePattern("DD/MM/YYYY hh:mmDDMMYYYYHHMMDDMMYYYY HHMM").spreadsheetParserSelector())
            .set(
                SpreadsheetMetadataPropertyName.DECIMAL_NUMBER_SYMBOLS,
                DecimalNumberSymbols.with(
                    '-',
                    '+',
                    '0',
                    "$AUD",
                    '.',
                    "E",
                    ',',
                    "Infinity!",
                    '.',
                    "Nan!",
                    '%',
                    '^'
                )
            ).set(
                SpreadsheetMetadataPropertyName.DEFAULT_FORM_HANDLER,
                FormHandlerSelector.parse("non-null")
            ).set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, 1900)
            .set(SpreadsheetMetadataPropertyName.EXPORTERS, SpreadsheetExporterAliasSet.EMPTY)
            .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, expressionNumberKind)
            .set(SpreadsheetMetadataPropertyName.FORM_HANDLERS, FormHandlerAliasSet.parse("basic"))
            .set(SpreadsheetMetadataPropertyName.FORMATTERS, SpreadsheetFormatterAliasSet.parse("simple"))
            .set(SpreadsheetMetadataPropertyName.FORMATTING_CONVERTER, ConverterSelector.parse("simple"))
            .set(SpreadsheetMetadataPropertyName.FORMULA_CONVERTER, ConverterSelector.parse("collection(text, number, basic, spreadsheet-value)"))
            .set(SpreadsheetMetadataPropertyName.FORMULA_FUNCTIONS, SpreadsheetExpressionFunctions.EMPTY_ALIAS_SET)
            .set(SpreadsheetMetadataPropertyName.FROZEN_COLUMNS, SpreadsheetSelection.parseColumnRange("A:B"))
            .set(SpreadsheetMetadataPropertyName.FROZEN_ROWS, SpreadsheetSelection.parseRowRange("1:2"))
            .set(SpreadsheetMetadataPropertyName.FUNCTIONS, SpreadsheetExpressionFunctions.EMPTY_ALIAS_SET)
            .set(SpreadsheetMetadataPropertyName.IMPORTERS, SpreadsheetImporterAliasSet.EMPTY)
            .set(SpreadsheetMetadataPropertyName.LOCALE, locale)
            .set(SpreadsheetMetadataPropertyName.NUMBER_FORMATTER, SpreadsheetPattern.parseNumberFormatPattern("#0.0").spreadsheetFormatterSelector())
            .set(SpreadsheetMetadataPropertyName.GENERAL_NUMBER_FORMAT_DIGIT_COUNT, 8)
            .set(SpreadsheetMetadataPropertyName.NUMBER_PARSER, SpreadsheetPattern.parseNumberParsePattern("#").spreadsheetParserSelector())
            .set(SpreadsheetMetadataPropertyName.PARSERS, SpreadsheetParserAliasSet.EMPTY)
            .set(SpreadsheetMetadataPropertyName.PRECISION, 123)
            .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.FLOOR)
            .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, createdId)
            .set(
                SpreadsheetMetadataPropertyName.STYLE,
                TextStyle.EMPTY.set(TextStylePropertyName.WIDTH, Length.pixel(50.0))
                    .set(TextStylePropertyName.HEIGHT, Length.pixel(50.0)))
            .set(SpreadsheetMetadataPropertyName.TEXT_FORMATTER, SpreadsheetPattern.parseTextFormatPattern("@@").spreadsheetFormatterSelector())
            .set(SpreadsheetMetadataPropertyName.TIME_FORMATTER, SpreadsheetPattern.parseTimeFormatPattern("hh:mm").spreadsheetFormatterSelector())
            .set(SpreadsheetMetadataPropertyName.TIME_PARSER, SpreadsheetPattern.parseTimeParsePattern("hh:mmhh:mm:ss.000").spreadsheetParserSelector())
            .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 31)
            .set(SpreadsheetMetadataPropertyName.VALIDATORS, ValidatorAliasSet.EMPTY)
            .set(SpreadsheetMetadataPropertyName.VALIDATION_FUNCTIONS, SpreadsheetExpressionFunctions.EMPTY_ALIAS_SET)
            .set(SpreadsheetMetadataPropertyName.VALIDATION_VALIDATORS, ValidatorAliasSet.EMPTY)
            .set(SpreadsheetMetadataPropertyName.VALUE_SEPARATOR, ',');

        final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();

        final SpreadsheetFormatterProvider spreadsheetFormatterProvider = SpreadsheetFormatterProviders.spreadsheetFormatters();
        final SpreadsheetParserProvider spreadsheetParserProvider = SpreadsheetParserProviders.spreadsheetParsePattern(
            spreadsheetFormatterProvider
        );

        return SpreadsheetHttpServer.with(
            MediaTypeDetectors.fake(),
            (url) -> {
                throw new UnsupportedOperationException(); // fileServer
            },
            (handler) -> {
                httpServer.handler = handler;
                return httpServer;
            },
            SpreadsheetServerContexts.basic(
                Url.parseAbsolute("https://example.com"),
                () -> SpreadsheetStoreRepositories.basic(
                    SpreadsheetCellStores.treeMap(),
                    SpreadsheetCellReferencesStores.treeMap(),
                    SpreadsheetColumnStores.treeMap(),
                    SpreadsheetFormStores.treeMap(),
                    SpreadsheetGroupStores.fake(),
                    SpreadsheetLabelStores.treeMap(),
                    SpreadsheetLabelReferencesStores.treeMap(),
                    metadataStore,
                    SpreadsheetCellRangeStores.treeMap(),
                    SpreadsheetRowStores.treeMap(),
                    Storages.tree(),
                    SpreadsheetUserStores.fake()
                ),
                SpreadsheetProviders.basic(
                    SpreadsheetConvertersConverterProviders.spreadsheetConverters(
                        (ProviderContext p) -> SpreadsheetMetadata.EMPTY.set(
                            SpreadsheetMetadataPropertyName.LOCALE,
                            locale
                        ).dateTimeConverter(
                            spreadsheetFormatterProvider,
                            spreadsheetParserProvider,
                            p
                        )
                    ), // converterProvider
                    ExpressionFunctionProviders.empty(SpreadsheetExpressionFunctions.NAME_CASE_SENSITIVITY),
                    SpreadsheetComparatorProviders.spreadsheetComparators(),
                    SpreadsheetExporterProviders.spreadsheetExport(),
                    spreadsheetFormatterProvider,
                    FormHandlerProviders.validation(),
                    SpreadsheetImporterProviders.spreadsheetImport(),
                    spreadsheetParserProvider,
                    ValidatorProviders.validators()
                ),
                (c) -> SpreadsheetEngineContexts.basic(
                    SpreadsheetEngineContextMode.FORMULA,
                    c,
                    TerminalContexts.fake()
                ),
                EnvironmentContexts.map(
                    EnvironmentContexts.empty(
                        locale,
                        () -> now,
                        Optional.of(
                            EmailAddress.parse("user@example.com")
                        )
                    )
                ),
                LocaleContexts.jre(locale),
                SpreadsheetMetadataContexts.basic(
                    (e, dl) -> metadataStore.save(
                        metadata.set(
                            SpreadsheetMetadataPropertyName.AUDIT_INFO,
                            AuditInfo.with(
                                e,
                                now,
                                e,
                                now
                            )
                        )
                    ),
                    metadataStore
                ),
                HateosResourceHandlerContexts.basic(
                    Indentation.SPACES2,
                    LineEnding.NL,
                    JsonNodeMarshallUnmarshallContexts.basic(
                        JsonNodeMarshallContexts.basic(),
                        JsonNodeUnmarshallContexts.basic(
                            expressionNumberKind,
                            MathContext.DECIMAL32
                        )
                    )
                ),
                ProviderContexts.basic(
                    ConverterContexts.fake(), // CanConvert
                    EnvironmentContexts.map(
                        EnvironmentContexts.empty(
                            locale,
                            LocalDateTime::now,
                            Optional.of(
                                EmailAddress.parse("user@example.com")
                            )
                        )
                    ),
                    PluginStores.treeMap()
                )
            )
        );
    }

    static final class TestHttpServer implements HttpServer {

        TestHttpServer() {
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        HttpHandler handler;
    }
}
