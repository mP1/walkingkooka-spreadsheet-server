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
import walkingkooka.Cast;
import walkingkooka.convert.ConverterContexts;
import walkingkooka.convert.Converters;
import walkingkooka.math.DecimalNumberContext;
import walkingkooka.math.DecimalNumberContextDelegator;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.spreadsheet.convert.SpreadsheetConverterContext;
import walkingkooka.spreadsheet.convert.SpreadsheetConverterContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatters;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviders;
import walkingkooka.spreadsheet.meta.HasSpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataLoaders;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.spreadsheet.value.HasSpreadsheetCell;
import walkingkooka.tree.expression.convert.ExpressionNumberConverterContexts;
import walkingkooka.tree.json.convert.JsonNodeConverterContexts;

import java.math.MathContext;
import java.util.Objects;
import java.util.Optional;

public final class SpreadsheetFormatterSelectorEditContextBasicTest implements SpreadsheetFormatterSelectorEditContextTesting<SpreadsheetFormatterSelectorEditContextBasic>,
    HasSpreadsheetMetadataTesting,
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
        return DECIMAL_NUMBER_CONTEXT.decimalNumberDigitCount();
    }

    @Override
    public MathContext mathContext() {
        return MATH_CONTEXT;
    }

    @Override
    public DecimalNumberContext decimalNumberContext() {
        return DECIMAL_NUMBER_CONTEXT;
    }

    @Override
    public SpreadsheetFormatterSelectorEditContextBasic createContext() {
        return SpreadsheetFormatterSelectorEditContextBasic.with(
            this.spreadsheetFormatterContext(),
            SpreadsheetFormatterProviders.spreadsheetFormatters(),
            ProviderContexts.fake()
        );
    }

    private SpreadsheetFormatterContext spreadsheetFormatterContext() {
        return SpreadsheetFormatterContexts.basic(
            HasSpreadsheetCell.EMPTY_HAS_SPREADSHEET_CELL,
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
        return SpreadsheetConverterContexts.basic(
            HAS_USER_DIRECTORIES,
            Optional.of(SPREADSHEET_METADATA),
            SpreadsheetConverterContexts.NO_VALIDATION_REFERENCE,
            Converters.objectToString(),
            MEDIA_TYPE_DETECTOR,
            MULTIPLIER, // multiplier
            SpreadsheetLabelNameResolvers.fake(),
            SpreadsheetMetadataLoaders.empty(),
            JsonNodeConverterContexts.basic(
                ExpressionNumberConverterContexts.basic(
                    Converters.fake(),
                    Cast.to(MULTIPLIER), // BinaryNumberConverterFunction<ExpressionNumberConverterContext>
                    ConverterContexts.basic(
                        false, // canNumbersHaveGroupSeparator
                        Converters.JAVA_EPOCH_OFFSET, // dateOffset
                        ',', // valueSeparator
                        Converters.objectToString(),
                        Cast.to(MULTIPLIER), // BinaryNumberConverterFunction<ConverterContext>
                        BINARY_TEXT_CONTEXT,
                        CURRENCY_LOCALE_CONTEXT,
                        DATE_TIME_CONTEXT,
                        DECIMAL_NUMBER_CONTEXT
                    ),
                    EXPRESSION_NUMBER_KIND
                ),
                ENVIRONMENT_CONTEXT,
                JSON_NODE_MARSHALL_UNMARSHALL_CONTEXT
            ),
            LOCALE_CONTEXT
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetFormatterSelectorEditContextBasic> type() {
        return SpreadsheetFormatterSelectorEditContextBasic.class;
    }

    @Override
    public void testTypeNaming() {
        throw new UnsupportedOperationException();
    }
}
