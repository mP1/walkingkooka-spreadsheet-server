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

package walkingkooka.spreadsheet.server.formatter;

import org.junit.jupiter.api.Test;
import walkingkooka.color.Color;
import walkingkooka.convert.ConverterContexts;
import walkingkooka.convert.Converters;
import walkingkooka.datetime.DateTimeContexts;
import walkingkooka.datetime.DateTimeSymbols;
import walkingkooka.locale.LocaleContexts;
import walkingkooka.math.DecimalNumberContext;
import walkingkooka.math.DecimalNumberContextDelegator;
import walkingkooka.math.DecimalNumberContexts;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.convert.SpreadsheetConverterContext;
import walkingkooka.spreadsheet.convert.SpreadsheetConverterContexts;
import walkingkooka.spreadsheet.format.SpreadsheetColorName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatters;
import walkingkooka.spreadsheet.format.SpreadsheetText;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviders;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.storage.HasUserDirectorieses;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.convert.ExpressionNumberConverterContexts;
import walkingkooka.tree.json.convert.JsonNodeConverterContexts;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContexts;

import java.math.MathContext;
import java.text.DateFormatSymbols;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class BasicSpreadsheetFormatterSelectorEditContextTest implements SpreadsheetFormatterSelectorEditContextTesting<BasicSpreadsheetFormatterSelectorEditContext>,
    SpreadsheetMetadataTesting,
    DecimalNumberContextDelegator {

    // locale...........................................................................................................

    @Test
    public void testLocale() {
        this.localeAndCheck(
            this.createContext(),
            LOCALE
        );
    }

    // DecimalNumberContextDelegator....................................................................................

    @Override
    public int decimalNumberDigitCount() {
        return this.spreadsheetConverterContext()
            .decimalNumberDigitCount();
    }

    @Override
    public MathContext mathContext() {
        return this.spreadsheetConverterContext()
            .mathContext();
    }

    @Override
    public DecimalNumberContext decimalNumberContext() {
        return this.spreadsheetConverterContext();
    }

    @Override
    public BasicSpreadsheetFormatterSelectorEditContext createContext() {
        return BasicSpreadsheetFormatterSelectorEditContext.with(
            this.spreadsheetFormatterContext(),
            SpreadsheetFormatterProviders.spreadsheetFormatters(),
            ProviderContexts.fake()
        );
    }

    private SpreadsheetFormatterContext spreadsheetFormatterContext() {
        return SpreadsheetFormatterContexts.basic(
            SpreadsheetFormatterContext.NO_CELL,
            this::numberToColor,
            this::nameToColor,
            1, // cellCharacterWidth
            SpreadsheetFormatters.fake(), // should never be called
            (final Optional<Object> value) -> {
                Objects.requireNonNull(value, "value");
                throw new UnsupportedOperationException();
            },
            this.spreadsheetConverterContext(),
            SPREADSHEET_FORMATTER_PROVIDER,
            PROVIDER_CONTEXT
        );
    }

    private SpreadsheetConverterContext spreadsheetConverterContext() {
        final Locale locale = Locale.forLanguageTag("EN-AU");

        return SpreadsheetConverterContexts.basic(
            HasUserDirectorieses.fake(),
            SpreadsheetConverterContexts.NO_METADATA,
            SpreadsheetConverterContexts.NO_VALIDATION_REFERENCE,
            Converters.objectToString(),
            SpreadsheetLabelNameResolvers.fake(),
            JsonNodeConverterContexts.basic(
                ExpressionNumberConverterContexts.basic(
                    Converters.fake(),
                    ConverterContexts.basic(
                        (l) -> {
                            Objects.requireNonNull(l, "locale");
                            throw new UnsupportedOperationException();
                        }, // canCurrencyForLocale
                        (l) -> {
                            throw new UnsupportedOperationException();
                        }, // canDateTimeSymbolsForLocale
                        (l) -> {
                            throw new UnsupportedOperationException();
                        }, // canDecimalNumberSymbolsForLocale
                        false, // canNumbersHaveGroupSeparator
                        Converters.JAVA_EPOCH_OFFSET, // dateOffset
                        Indentation.SPACES2,
                        LineEnding.NL,
                        ',', // valueSeparator
                        Converters.objectToString(),
                        DateTimeContexts.basic(
                            DateTimeSymbols.fromDateFormatSymbols(
                                new DateFormatSymbols(locale)
                            ),
                            locale,
                            1950, // default year
                            50, // two-digit-year
                            LocalDateTime::now
                        ),
                        DecimalNumberContexts.american(
                            MathContext.DECIMAL32
                        )
                    ),
                    ExpressionNumberKind.BIG_DECIMAL
                ),
                JsonNodeMarshallUnmarshallContexts.fake()
            ),
            LocaleContexts.jre(locale)
        );
    }

    private Optional<Color> numberToColor(final Integer value) {
        return SpreadsheetText.WITHOUT_COLOR; // ignore the colour number
    }

    private Optional<Color> nameToColor(final SpreadsheetColorName name) {
        return SpreadsheetText.WITHOUT_COLOR; // ignore the colour name.
    }

    // class............................................................................................................

    @Override
    public Class<BasicSpreadsheetFormatterSelectorEditContext> type() {
        return BasicSpreadsheetFormatterSelectorEditContext.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
