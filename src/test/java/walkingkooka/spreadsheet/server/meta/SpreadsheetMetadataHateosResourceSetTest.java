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

package walkingkooka.spreadsheet.server.meta;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.set.ImmutableSortedSetTesting;
import walkingkooka.collect.set.Sets;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.net.http.server.hateos.HateosResourceSetTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.PublicClassTesting;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.SortedSet;

public final class SpreadsheetMetadataHateosResourceSetTest implements HateosResourceSetTesting2<SpreadsheetMetadataHateosResourceSet, SpreadsheetMetadata, SpreadsheetId>,
    ImmutableSortedSetTesting<SpreadsheetMetadataHateosResourceSet, SpreadsheetMetadata>,
    SpreadsheetMetadataTesting,
    PublicClassTesting<SpreadsheetMetadataHateosResourceSet> {

    // Set..............................................................................................................

    @Override
    public SpreadsheetMetadataHateosResourceSet createSet() {
        final SortedSet<SpreadsheetMetadata> sortedSet = SortedSets.tree(SpreadsheetMetadataHateosResourceSet.COMPARATOR);

        sortedSet.add(SpreadsheetMetadataTesting.METADATA_EN_AU);

        return SpreadsheetMetadataHateosResourceSet.withCopy(sortedSet);
    }

    @Test
    @Override
    public void testSetElementsSame() {
        HateosResourceSetTesting2.super.testSetElementsSame();
    }

    @Test
    @Override
    public void testSetElementsNullFails() {
        HateosResourceSetTesting2.super.testSetElementsNullFails();
    }

    // json.............................................................................................................

    @Test
    public void testMarshallEmpty() {
        this.marshallAndCheck(
            SpreadsheetMetadataHateosResourceSet.withCopy(
                SortedSets.empty()
            ),
            JsonNode.array()
        );
    }

    @Test
    public void testMarshallNotEmpty() {
        final SpreadsheetMetadataHateosResourceSet set = SpreadsheetMetadataHateosResourceSet.EMPTY.concat(
            SpreadsheetMetadata.EMPTY
        );

        this.marshallAndCheck(
            set,
            JsonNode.array()
                .appendChild(
                    JsonNode.object()
                )
        );
    }

    @Test
    public void testMarshallNotEmpty2() {
        final SpreadsheetMetadataHateosResourceSet set = SpreadsheetMetadataHateosResourceSet.EMPTY.concat(
            SpreadsheetMetadata.EMPTY.set(
                SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                SpreadsheetId.with(1)
            )
        );

        this.marshallAndCheck(
            set,
            "[\n" +
                "  {\n" +
                "    \"spreadsheetId\": \"1\"\n" +
                "  }\n" +
                "]"
        );
    }

    // json............................................................................................................

    @Override
    public SpreadsheetMetadataHateosResourceSet unmarshall(final JsonNode node,
                                                           final JsonNodeUnmarshallContext context) {
        return SpreadsheetMetadataHateosResourceSet.unmarshall(
            node,
            context
        );
    }

    @Override
    public SpreadsheetMetadataHateosResourceSet createJsonNodeMarshallingValue() {
        return SpreadsheetMetadataHateosResourceSet.EMPTY.setElements(
            Sets.of(
                SpreadsheetMetadata.EMPTY.set(
                    SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                    SpreadsheetId.with(1)
                ),
                SpreadsheetMetadata.EMPTY.set(
                    SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                    SpreadsheetId.with(2)
                )
            )
        );
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrint() {
        this.treePrintAndCheck(
            this.createSet(),
            "SpreadsheetMetadataHateosResourceSet\n" +
                "  SpreadsheetMetadataNonEmpty\n" +
                "    auditInfo: \n" +
                "      AuditInfo\n" +
                "        created\n" +
                "          user123@example.com 1999-12-31T12:58:59\n" +
                "        modified\n" +
                "          user123@example.com 1999-12-31T12:58:59\n" +
                "    autoHideScrollbars: false\n" +
                "    cellCharacterWidth: 1\n" +
                "    color1: black\n" +
                "    color2: white\n" +
                "    colorBlack: 1\n" +
                "    colorWhite: 2\n" +
                "    comparators: \n" +
                "      background-color\n" +
                "      border-bottom-color\n" +
                "      border-color\n" +
                "      border-left-color\n" +
                "      border-right-color\n" +
                "      border-top-color\n" +
                "      color\n" +
                "      currency\n" +
                "      custom-list\n" +
                "      custom-list-case-insensitive\n" +
                "      date\n" +
                "      date-time\n" +
                "      day-of-month\n" +
                "      day-of-week\n" +
                "      error\n" +
                "      formatter\n" +
                "      hour-of-am-pm\n" +
                "      hour-of-day\n" +
                "      locale\n" +
                "      minute-of-hour\n" +
                "      month-of-year\n" +
                "      nano-of-second\n" +
                "      number\n" +
                "      outline-color\n" +
                "      parser\n" +
                "      seconds-of-minute\n" +
                "      text\n" +
                "      text-case-insensitive\n" +
                "      text-decoration-color\n" +
                "      text-with-numbers\n" +
                "      text-with-numbers-case-insensitive\n" +
                "      time\n" +
                "      validator\n" +
                "      value-type\n" +
                "      year\n" +
                "    converters: \n" +
                "      basic\n" +
                "      binary\n" +
                "      binary-to-text\n" +
                "      boolean\n" +
                "      boolean-to-text\n" +
                "      collection\n" +
                "      collection-to\n" +
                "      collection-to-list\n" +
                "      color\n" +
                "      color-to-color\n" +
                "      color-to-number\n" +
                "      csv\n" +
                "      currency\n" +
                "      currency-code-to-currency\n" +
                "      currency-value-to\n" +
                "      currency-value-to-number\n" +
                "      date-time\n" +
                "      date-time-symbols\n" +
                "      decimal-number-symbols\n" +
                "      environment\n" +
                "      environment-to-binary\n" +
                "      environment-to-text\n" +
                "      error-throwing\n" +
                "      error-to-error\n" +
                "      error-to-number\n" +
                "      expression\n" +
                "      form-and-validation\n" +
                "      format-pattern-to-string\n" +
                "      has-formatter-selector\n" +
                "      has-parser-selector\n" +
                "      has-spreadsheet-selection\n" +
                "      has-validator-selector\n" +
                "      json\n" +
                "      json-to\n" +
                "      locale\n" +
                "      locale-to-text\n" +
                "      logging\n" +
                "      net\n" +
                "      null-to-number\n" +
                "      number\n" +
                "      number-to-color\n" +
                "      number-to-currency-value\n" +
                "      number-to-number\n" +
                "      number-to-text\n" +
                "      optional-to\n" +
                "      plugins\n" +
                "      properties\n" +
                "      properties-to-date-time-symbols\n" +
                "      properties-to-decimal-number-symbols\n" +
                "      properties-to-spreadsheet-metadata\n" +
                "      properties-to-text-style\n" +
                "      spreadsheet-cell-set\n" +
                "      spreadsheet-id-to-spreadsheet-metadata\n" +
                "      spreadsheet-metadata\n" +
                "      spreadsheet-selection\n" +
                "      spreadsheet-selection-to-spreadsheet-selection\n" +
                "      spreadsheet-selection-to-text\n" +
                "      storage\n" +
                "      storage-binary-to-storage-value-binary\n" +
                "      storage-binary-to-storage-value-csv\n" +
                "      storage-binary-to-storage-value-environment\n" +
                "      storage-binary-to-storage-value-expression\n" +
                "      storage-binary-to-storage-value-json\n" +
                "      storage-binary-to-storage-value-properties\n" +
                "      storage-binary-to-storage-value-tsv\n" +
                "      storage-binary-to-storage-value-txt\n" +
                "      storage-value-info-list-to-text\n" +
                "      storage-value-to-storage-binary-binary\n" +
                "      storage-value-to-storage-binary-csv\n" +
                "      storage-value-to-storage-binary-environment\n" +
                "      storage-value-to-storage-binary-expression\n" +
                "      storage-value-to-storage-binary-json\n" +
                "      storage-value-to-storage-binary-properties\n" +
                "      storage-value-to-storage-binary-tsv\n" +
                "      storage-value-to-storage-binary-txt\n" +
                "      style\n" +
                "      system\n" +
                "      template\n" +
                "      text\n" +
                "      text-node\n" +
                "      text-to-binary\n" +
                "      text-to-boolean-list\n" +
                "      text-to-border\n" +
                "      text-to-charset\n" +
                "      text-to-color\n" +
                "      text-to-csv-string-list\n" +
                "      text-to-csv-string-set\n" +
                "      text-to-currency\n" +
                "      text-to-currency-code\n" +
                "      text-to-currency-value\n" +
                "      text-to-date-list\n" +
                "      text-to-date-time-list\n" +
                "      text-to-email-address\n" +
                "      text-to-environment\n" +
                "      text-to-environment-value-name\n" +
                "      text-to-error\n" +
                "      text-to-expression\n" +
                "      text-to-flag\n" +
                "      text-to-form-name\n" +
                "      text-to-has-host-address\n" +
                "      text-to-host-address\n" +
                "      text-to-indentation\n" +
                "      text-to-json\n" +
                "      text-to-json-pointer\n" +
                "      text-to-json-selector\n" +
                "      text-to-line-ending\n" +
                "      text-to-locale\n" +
                "      text-to-locale-language-tag\n" +
                "      text-to-logging-level\n" +
                "      text-to-margin\n" +
                "      text-to-media-type\n" +
                "      text-to-number-list\n" +
                "      text-to-object\n" +
                "      text-to-padding\n" +
                "      text-to-path\n" +
                "      text-to-properties\n" +
                "      text-to-spreadsheet-color-name\n" +
                "      text-to-spreadsheet-formatter-selector\n" +
                "      text-to-spreadsheet-id\n" +
                "      text-to-spreadsheet-metadata\n" +
                "      text-to-spreadsheet-metadata-color\n" +
                "      text-to-spreadsheet-metadata-property-name\n" +
                "      text-to-spreadsheet-name\n" +
                "      text-to-spreadsheet-selection\n" +
                "      text-to-spreadsheet-text\n" +
                "      text-to-storage-path\n" +
                "      text-to-string-list\n" +
                "      text-to-template-value-name\n" +
                "      text-to-text\n" +
                "      text-to-text-node\n" +
                "      text-to-text-style\n" +
                "      text-to-text-style-property-name\n" +
                "      text-to-time-list\n" +
                "      text-to-tsv-string-list\n" +
                "      text-to-tsv-string-set\n" +
                "      text-to-url\n" +
                "      text-to-url-fragment\n" +
                "      text-to-url-query-string\n" +
                "      text-to-validation-error\n" +
                "      text-to-validator-selector\n" +
                "      text-to-value-type\n" +
                "      text-to-zone-offset\n" +
                "      to-binary\n" +
                "      to-boolean\n" +
                "      to-csv-string-list\n" +
                "      to-date-time-symbols\n" +
                "      to-decimal-number-symbols\n" +
                "      to-environment\n" +
                "      to-host-address\n" +
                "      to-json-node\n" +
                "      to-locale\n" +
                "      to-locale-language-tag\n" +
                "      to-multi-line-text\n" +
                "      to-number\n" +
                "      to-properties\n" +
                "      to-string\n" +
                "      to-style\n" +
                "      to-styleable\n" +
                "      to-text-node\n" +
                "      to-tsv-string-list\n" +
                "      to-validation-checkbox\n" +
                "      to-validation-choice\n" +
                "      to-validation-choice-list\n" +
                "      to-validation-error-list\n" +
                "      to-value\n" +
                "      tsv\n" +
                "      url\n" +
                "      url-to-hyperlink\n" +
                "      url-to-image\n" +
                "      value\n" +
                "    currency: AUD (java.util.Currency)\n" +
                "    dateFormatter: \n" +
                "      date\n" +
                "        \"yyyy/mm/dd\"\n" +
                "    dateParser: \n" +
                "      date\n" +
                "        \"yyyy/mm/dd\"\n" +
                "    dateTimeFormatter: \n" +
                "      date-time\n" +
                "        \"yyyy/mm/dd hh:mm\"\n" +
                "    dateTimeOffset: -25569L\n" +
                "    dateTimeParser: \n" +
                "      date-time\n" +
                "        \"yyyy/mm/dd hh:mm\"\n" +
                "    dateTimeSymbols: \n" +
                "      DateTimeSymbols\n" +
                "        ampms\n" +
                "          am\n" +
                "          pm\n" +
                "        monthNames\n" +
                "          January\n" +
                "          February\n" +
                "          March\n" +
                "          April\n" +
                "          May\n" +
                "          June\n" +
                "          July\n" +
                "          August\n" +
                "          September\n" +
                "          October\n" +
                "          November\n" +
                "          December\n" +
                "        monthNameAbbreviations\n" +
                "          Jan.\n" +
                "          Feb.\n" +
                "          Mar.\n" +
                "          Apr.\n" +
                "          May\n" +
                "          Jun.\n" +
                "          Jul.\n" +
                "          Aug.\n" +
                "          Sep.\n" +
                "          Oct.\n" +
                "          Nov.\n" +
                "          Dec.\n" +
                "        weekDayNames\n" +
                "          Sunday\n" +
                "          Monday\n" +
                "          Tuesday\n" +
                "          Wednesday\n" +
                "          Thursday\n" +
                "          Friday\n" +
                "          Saturday\n" +
                "        weekDayNameAbbreviations\n" +
                "          Sun.\n" +
                "          Mon.\n" +
                "          Tue.\n" +
                "          Wed.\n" +
                "          Thu.\n" +
                "          Fri.\n" +
                "          Sat.\n" +
                "    decimalNumberDigitCount: 8\n" +
                "    decimalNumberSymbols: \n" +
                "      DecimalNumberSymbols\n" +
                "        negativeSign\n" +
                "          '-'\n" +
                "        positiveSign\n" +
                "          '+'\n" +
                "        zeroDigit\n" +
                "          '0'\n" +
                "        currencySymbol\n" +
                "          \"$\"\n" +
                "        decimalSeparator\n" +
                "          '.'\n" +
                "        exponentSymbol\n" +
                "          \"e\"\n" +
                "        groupSeparator\n" +
                "          ','\n" +
                "        infinitySymbol\n" +
                "          \"∞\"\n" +
                "        monetaryDecimalSeparator\n" +
                "          '.'\n" +
                "        nanSymbol\n" +
                "          \"NaN\"\n" +
                "        percentSymbol\n" +
                "          '%'\n" +
                "        permillSymbol\n" +
                "          '‰'\n" +
                "    defaultFormHandler: \n" +
                "      basic\n" +
                "    defaultYear: 2000\n" +
                "    errorFormatter: \n" +
                "      badge-error\n" +
                "        \"text @\"\n" +
                "    exporters: \n" +
                "      collection\n" +
                "      empty\n" +
                "      json\n" +
                "    expressionNumberKind: BIG_DECIMAL\n" +
                "    formHandlers: \n" +
                "    formatters: \n" +
                "      accounting\n" +
                "      automatic\n" +
                "      badge-error\n" +
                "      collection\n" +
                "      currency\n" +
                "      date\n" +
                "      date-time\n" +
                "      default-text\n" +
                "      expression\n" +
                "      full-date\n" +
                "      full-date-time\n" +
                "      full-time\n" +
                "      general\n" +
                "      hyperlinking\n" +
                "      long-date\n" +
                "      long-date-time\n" +
                "      long-time\n" +
                "      medium-date\n" +
                "      medium-date-time\n" +
                "      medium-time\n" +
                "      number\n" +
                "      percent\n" +
                "      scientific\n" +
                "      short-date\n" +
                "      short-date-time\n" +
                "      short-time\n" +
                "      text\n" +
                "      time\n" +
                "    formattingConverter: \n" +
                "      collection\n" +
                "        \"(text, boolean, number, date-time, environment, locale, value, error-throwing, color, expression, currency, plugins, properties, style, text-node, template, net, basic)\"\n" +
                "    formattingCurrencyExchangeRater: \n" +
                "      storage-path-properties\n" +
                "        \"(\\\"/samples/CurrencyExchangeRates.properties\\\")\"\n" +
                "    formattingFunctions: \n" +
                "    formulaConverter: \n" +
                "      collection\n" +
                "        \"(text, boolean, number, date-time, environment, locale, value, error-throwing, color, expression, json, currency, plugins, properties, spreadsheet-metadata, storage, style, text-node, template, net, basic)\"\n" +
                "    formulaCurrencyExchangeRater: \n" +
                "      storage-path-properties\n" +
                "        \"(\\\"/samples/CurrencyExchangeRates.properties\\\")\"\n" +
                "    formulaFunctions: \n" +
                "    functions: \n" +
                "    importers: \n" +
                "      collection\n" +
                "      empty\n" +
                "      json\n" +
                "    locale: en_AU (java.util.Locale)\n" +
                "    numberFormatter: \n" +
                "      number\n" +
                "        \"0.#;0.#;0\"\n" +
                "    numberParser: \n" +
                "      number\n" +
                "        \"0.#;0.#;0\"\n" +
                "    parsers: \n" +
                "      date\n" +
                "      date-time\n" +
                "      full-date\n" +
                "      full-date-time\n" +
                "      full-time\n" +
                "      general\n" +
                "      long-date\n" +
                "      long-date-time\n" +
                "      long-time\n" +
                "      medium-date\n" +
                "      medium-date-time\n" +
                "      medium-time\n" +
                "      number\n" +
                "      short-date\n" +
                "      short-date-time\n" +
                "      short-time\n" +
                "      time\n" +
                "      whole-number\n" +
                "    plugins: \n" +
                "    precision: 7\n" +
                "    queryConverter: \n" +
                "      collection\n" +
                "        \"(text, boolean, number, date-time, environment, locale, value, error-throwing, color, expression, properties, spreadsheet-metadata, style, text-node, template, net, basic)\"\n" +
                "    queryFunctions: \n" +
                "    roundingMode: HALF_UP\n" +
                "    scriptingConverter: \n" +
                "      collection\n" +
                "        \"(text, boolean, number, date-time, environment, locale, value, error-throwing, color, expression, json, currency, plugins, properties, spreadsheet-metadata, storage, style, text-node, template, net, basic)\"\n" +
                "    scriptingCurrencyExchangeRater: \n" +
                "      storage-path-properties\n" +
                "        \"(\\\"/samples/CurrencyExchangeRates.properties\\\")\"\n" +
                "    scriptingFunctions: \n" +
                "    showFormulaEditor: true\n" +
                "    showFormulas: false\n" +
                "    showGridLines: true\n" +
                "    showHeadings: true\n" +
                "    sortComparators: \n" +
                "      [background-color, border-bottom-color, border-color, border-left-color, border-right-color, border-top-color, color, currency, custom-list, custom-list-case-insensitive, date, datetime, day-of-month, day-of-year, formatter, hour-of-ampm, hour-of-day, locale, minute-of-hour, month-of-year, nano-of-second, number, outline-color, parser, seconds-of-minute, text, text-case-insensitive, time, validator, value-type, year]\n" +
                "    sortConverter: \n" +
                "      collection\n" +
                "        \"(text, boolean, number, date-time, locale, value, basic)\"\n" +
                "    style: \n" +
                "      TextStyle\n" +
                "        height=50px\n" +
                "        width=100px\n" +
                "    textFormatter: \n" +
                "      text\n" +
                "        \"@\"\n" +
                "    timeFormatter: \n" +
                "      time\n" +
                "        \"hh:mm:ss\"\n" +
                "    timeParser: \n" +
                "      time\n" +
                "        \"hh:mm:ss\"\n" +
                "    twoDigitYear: 50\n" +
                "    validationConverter: \n" +
                "      collection\n" +
                "        \"(text, boolean, number, date-time, environment, value, error-throwing, expression, form-and-validation, locale, plugins, properties, template, json, basic)\"\n" +
                "    validationCurrencyExchangeRater: \n" +
                "      storage-path-properties\n" +
                "        \"(\\\"/samples/CurrencyExchangeRates.properties\\\")\"\n" +
                "    validationFunctions: \n" +
                "    validationValidators: \n" +
                "      absolute-url\n" +
                "      checkbox\n" +
                "      choice-list\n" +
                "      collection\n" +
                "      email-address\n" +
                "      expression\n" +
                "      non-null\n" +
                "      text-length\n" +
                "      text-mask\n" +
                "    validators: \n" +
                "      absolute-url\n" +
                "      checkbox\n" +
                "      choice-list\n" +
                "      collection\n" +
                "      email-address\n" +
                "      expression\n" +
                "      non-null\n" +
                "      text-length\n" +
                "      text-mask\n" +
                "    valueSeparator: ','\n"
        );
    }

    // Class............................................................................................................

    @Override
    public Class<SpreadsheetMetadataHateosResourceSet> type() {
        return SpreadsheetMetadataHateosResourceSet.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
