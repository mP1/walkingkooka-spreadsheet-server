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

package walkingkooka.spreadsheet.server.parser;

import org.junit.jupiter.api.Test;
import walkingkooka.math.DecimalNumberContext;
import walkingkooka.math.DecimalNumberContextDelegator;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContexts;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetFormatPattern;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetParsePattern;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviders;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.parser.SpreadsheetParserContexts;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserProviders;

import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetParserSelectorEditContextBasicTest implements SpreadsheetParserSelectorEditContextTesting<SpreadsheetParserSelectorEditContextBasic>,
    SpreadsheetMetadataTesting,
    DecimalNumberContextDelegator {

    @Test
    public void testWithNullSpreadsheetParserProviderFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetParserSelectorEditContextBasic.with(
                null,
                SpreadsheetParserContexts.fake(),
                SpreadsheetFormatterContexts.fake(),
                SpreadsheetFormatterProviders.fake(),
                PROVIDER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetParserContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetParserSelectorEditContextBasic.with(
                SpreadsheetParserProviders.fake(),
                null,
                SpreadsheetFormatterContexts.fake(),
                SpreadsheetFormatterProviders.fake(),
                PROVIDER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetFormatterContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetParserSelectorEditContextBasic.with(
                SpreadsheetParserProviders.fake(),
                SpreadsheetParserContexts.fake(),
                null,
                SpreadsheetFormatterProviders.fake(),
                PROVIDER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetFormatterProviderFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetParserSelectorEditContextBasic.with(
                SpreadsheetParserProviders.fake(),
                SpreadsheetParserContexts.fake(),
                SpreadsheetFormatterContexts.fake(),
                null,
                PROVIDER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullProviderContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetParserSelectorEditContextBasic.with(
                SpreadsheetParserProviders.fake(),
                SpreadsheetParserContexts.fake(),
                SpreadsheetFormatterContexts.fake(),
                SpreadsheetFormatterProviders.fake(),
                null
            )
        );
    }

    // SpreadsheetFormatterSelector.....................................................................................

    @Test
    public void testSpreadsheetFormatterSelectorDateTextFormat() {
        final SpreadsheetFormatPattern dateTextFormat = SpreadsheetPattern.parseDateFormatPattern("yyyy/mm/dd");

        this.spreadsheetFormatterAndCheck(
            dateTextFormat.spreadsheetFormatterSelector(),
            PROVIDER_CONTEXT,
            dateTextFormat.formatter()
        );
    }

    @Test
    public void testSpreadsheetParserSelectorDateTextParser() {
        final SpreadsheetParsePattern dateTextParser = SpreadsheetPattern.parseDateParsePattern("yyyy/mm/dd");

        this.spreadsheetParserAndCheck(
            dateTextParser.spreadsheetParserSelector(),
            PROVIDER_CONTEXT,
            dateTextParser.parser()
        );
    }

    @Override
    public SpreadsheetParserSelectorEditContextBasic createContext() {
        return SpreadsheetParserSelectorEditContextBasic.with(
            SPREADSHEET_PARSER_PROVIDER,
            SPREADSHEET_PARSER_CONTEXT,
            SPREADSHEET_FORMATTER_CONTEXT,
            SPREADSHEET_FORMATTER_PROVIDER,
            PROVIDER_CONTEXT
        );
    }

    @Override
    public int decimalNumberDigitCount() {
        return SPREADSHEET_FORMATTER_CONTEXT.decimalNumberDigitCount();
    }

    @Override
    public MathContext mathContext() {
        return SPREADSHEET_FORMATTER_CONTEXT.mathContext();
    }

    // DecimalNumberContextDelegator....................................................................................

    @Override
    public DecimalNumberContext decimalNumberContext() {
        return SPREADSHEET_FORMATTER_CONTEXT;
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetParserSelectorEditContextBasic> type() {
        return SpreadsheetParserSelectorEditContextBasic.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    @Override
    public void testTypeNaming() {
        throw new UnsupportedOperationException();
    }
}
