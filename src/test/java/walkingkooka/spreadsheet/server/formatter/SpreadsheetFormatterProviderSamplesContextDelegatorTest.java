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
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderSamplesContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderSamplesContextTesting;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderSamplesContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterProviderSamplesContextDelegatorTest.TestSpreadsheetFormatterProviderSamplesContextDelegator;

import java.math.MathContext;

public class SpreadsheetFormatterProviderSamplesContextDelegatorTest implements SpreadsheetFormatterProviderSamplesContextTesting<TestSpreadsheetFormatterProviderSamplesContextDelegator>,
    SpreadsheetMetadataTesting {

    @Override
    public TestSpreadsheetFormatterProviderSamplesContextDelegator createContext() {
        return new TestSpreadsheetFormatterProviderSamplesContextDelegator();
    }

    static class TestSpreadsheetFormatterProviderSamplesContextDelegator implements SpreadsheetFormatterProviderSamplesContextDelegator {

        @Override
        public SpreadsheetFormatterProviderSamplesContext spreadsheetFormatterProviderSamplesContext() {
            return SpreadsheetFormatterProviderSamplesContexts.basic(
                SPREADSHEET_FORMATTER_CONTEXT
            );
        }
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
    public char percentSymbol() {
        return DECIMAL_NUMBER_CONTEXT.percentSymbol();
    }

    @Override
    public char positiveSign() {
        return DECIMAL_NUMBER_CONTEXT.positiveSign();
    }

    private final static DecimalNumberContext DECIMAL_NUMBER_CONTEXT = METADATA_EN_AU.decimalNumberContext();

    @Override
    public void testCheckToStringOverridden() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testTypeNaming() {
        throw new UnsupportedOperationException();
    }

    // class............................................................................................................

    @Override
    public Class<TestSpreadsheetFormatterProviderSamplesContextDelegator> type() {
        return TestSpreadsheetFormatterProviderSamplesContextDelegator.class;
    }
}
