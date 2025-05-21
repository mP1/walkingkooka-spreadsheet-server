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

import walkingkooka.math.DecimalNumberContext;
import walkingkooka.math.DecimalNumberContextDelegator;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderDelegator;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterSelectorMenuContextTestingTest.TestSpreadsheetFormatterSelectorMenuContext;

import java.math.MathContext;

public final class SpreadsheetFormatterSelectorMenuContextTestingTest implements SpreadsheetFormatterSelectorMenuContextTesting<TestSpreadsheetFormatterSelectorMenuContext>,
    SpreadsheetMetadataTesting,
    DecimalNumberContextDelegator {
    @Override
    public TestSpreadsheetFormatterSelectorMenuContext createContext() {
        return new TestSpreadsheetFormatterSelectorMenuContext();
    }

    @Override
    public MathContext mathContext() {
        return DECIMAL_NUMBER_CONTEXT.mathContext();
    }

    // DecimalNumberContextDelegator....................................................................................

    @Override
    public DecimalNumberContext decimalNumberContext() {
        return DECIMAL_NUMBER_CONTEXT;
    }

    private final static DecimalNumberContext DECIMAL_NUMBER_CONTEXT = METADATA_EN_AU.decimalNumberContext(SpreadsheetMetadata.NO_CELL);

    // class............................................................................................................

    @Override
    public Class<TestSpreadsheetFormatterSelectorMenuContext> type() {
        return TestSpreadsheetFormatterSelectorMenuContext.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    static class TestSpreadsheetFormatterSelectorMenuContext implements SpreadsheetFormatterSelectorMenuContext,
        SpreadsheetFormatterContextDelegator,
        SpreadsheetFormatterProviderDelegator {

        @Override
        public SpreadsheetFormatterContext spreadsheetFormatterContext() {
            return SPREADSHEET_FORMATTER_CONTEXT;
        }

        @Override
        public SpreadsheetFormatterProvider spreadsheetFormatterProvider() {
            return SPREADSHEET_FORMATTER_PROVIDER;
        }
    }

    @Override
    public void testCheckToStringOverridden() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testTestNaming() {
        throw new UnsupportedOperationException();
    }
}
