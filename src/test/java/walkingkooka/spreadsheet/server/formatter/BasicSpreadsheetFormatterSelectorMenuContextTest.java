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
import walkingkooka.ToStringTesting;
import walkingkooka.math.DecimalNumberContext;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderSamplesContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderSamplesContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;

import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicSpreadsheetFormatterSelectorMenuContextTest implements SpreadsheetFormatterSelectorMenuContextTesting<BasicSpreadsheetFormatterSelectorMenuContext>,
        SpreadsheetMetadataTesting,
        ToStringTesting<BasicSpreadsheetFormatterSelectorMenuContext> {

    @Test
    public void testWithNullSpreadsheetFormatterProviderFails() {
        assertThrows(
                NullPointerException.class,
                () -> BasicSpreadsheetFormatterSelectorMenuContext.with(
                        null,
                        SpreadsheetFormatterProviderSamplesContexts.basic(
                                SPREADSHEET_FORMATTER_CONTEXT
                        )
                )
        );
    }

    @Test
    public void testWithNullSpreadsheetFormatterProviderSamplesContextFails() {
        assertThrows(
                NullPointerException.class,
                () -> BasicSpreadsheetFormatterSelectorMenuContext.with(
                        SPREADSHEET_FORMATTER_PROVIDER,
                        null
                )
        );
    }

    @Override
    public BasicSpreadsheetFormatterSelectorMenuContext createContext() {
        return BasicSpreadsheetFormatterSelectorMenuContext.with(
                SPREADSHEET_FORMATTER_PROVIDER,
                SpreadsheetFormatterProviderSamplesContexts.basic(
                        SPREADSHEET_FORMATTER_CONTEXT
                )
        );
    }

    @Override
    public String currencySymbol() {
        return DECIMAL_NUMBER_CONTEXT.currencySymbol();
    }

    @Override
    public char decimalSeparator() {
        return DECIMAL_NUMBER_CONTEXT.decimalSeparator();
    }

    @Override
    public String exponentSymbol() {
        return DECIMAL_NUMBER_CONTEXT.exponentSymbol();
    }

    @Override
    public char groupSeparator() {
        return DECIMAL_NUMBER_CONTEXT.groupSeparator();
    }

    @Override
    public MathContext mathContext() {
        return DECIMAL_NUMBER_CONTEXT.mathContext();
    }

    @Override
    public char negativeSign() {
        return DECIMAL_NUMBER_CONTEXT.negativeSign();
    }

    @Override
    public char percentageSymbol() {
        return DECIMAL_NUMBER_CONTEXT.percentageSymbol();
    }

    @Override
    public char positiveSign() {
        return DECIMAL_NUMBER_CONTEXT.positiveSign();
    }

    private final static DecimalNumberContext DECIMAL_NUMBER_CONTEXT = METADATA_EN_AU.decimalNumberContext();

    // toString.........................................................................................................

    @Test
    public void testToString() {
        final SpreadsheetFormatterProviderSamplesContext samplesContext = SpreadsheetFormatterProviderSamplesContexts.basic(
                SPREADSHEET_FORMATTER_CONTEXT
        );

        this.toStringAndCheck(
                BasicSpreadsheetFormatterSelectorMenuContext.with(
                        SPREADSHEET_FORMATTER_PROVIDER,
                        samplesContext
                ),
                SPREADSHEET_FORMATTER_PROVIDER + " " + samplesContext
        );
    }

    // class............................................................................................................

    @Override
    public Class<BasicSpreadsheetFormatterSelectorMenuContext> type() {
        return BasicSpreadsheetFormatterSelectorMenuContext.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}