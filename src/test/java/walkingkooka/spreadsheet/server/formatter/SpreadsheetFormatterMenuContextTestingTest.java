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

import walkingkooka.convert.ConverterContext;
import walkingkooka.math.DecimalNumberContext;
import walkingkooka.math.DecimalNumberContextDelegator;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContextDelegator;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderSamplesContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterMenuContextTestingTest.TestSpreadsheetFormatterMenuContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.math.MathContext;
import java.time.LocalDateTime;

public final class SpreadsheetFormatterMenuContextTestingTest implements SpreadsheetFormatterMenuContextTesting<TestSpreadsheetFormatterMenuContext>,
    SpreadsheetMetadataTesting,
    DecimalNumberContextDelegator {
    @Override
    public TestSpreadsheetFormatterMenuContext createContext() {
        return new TestSpreadsheetFormatterMenuContext();
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

    private final static DecimalNumberContext DECIMAL_NUMBER_CONTEXT = METADATA_EN_AU.decimalNumberContext(
        SpreadsheetMetadata.NO_CELL,
        LOCALE_CONTEXT
    );

    // class............................................................................................................

    @Override
    public Class<TestSpreadsheetFormatterMenuContext> type() {
        return TestSpreadsheetFormatterMenuContext.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    static class TestSpreadsheetFormatterMenuContext implements SpreadsheetFormatterMenuContext,
        SpreadsheetFormatterContextDelegator,
        SpreadsheetFormatterProviderDelegator,
        ProviderContextDelegator {

        @Override
        public SpreadsheetFormatterProviderSamplesContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetFormatterContext spreadsheetFormatterContext() {
            return SPREADSHEET_FORMATTER_CONTEXT;
        }

        @Override
        public SpreadsheetFormatterProvider spreadsheetFormatterProvider() {
            return SPREADSHEET_FORMATTER_PROVIDER;
        }

        @Override
        public ProviderContext providerContext() {
            return PROVIDER_CONTEXT;
        }

        @Override
        public ConverterContext canConvert() {
            return SPREADSHEET_FORMATTER_CONTEXT;
        }

        @Override
        public LocalDateTime now() {
            return SPREADSHEET_FORMATTER_CONTEXT.now();
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
