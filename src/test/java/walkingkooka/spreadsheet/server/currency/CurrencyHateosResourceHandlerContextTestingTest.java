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

package walkingkooka.spreadsheet.server.currency;

import walkingkooka.currency.CurrencyContext;
import walkingkooka.currency.CurrencyContextDelegator;
import walkingkooka.net.header.MediaType;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.currency.CurrencyHateosResourceHandlerContextTestingTest.TestCurrencyHateosResourceHandlerContext;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.MathContext;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class CurrencyHateosResourceHandlerContextTestingTest implements CurrencyHateosResourceHandlerContextTesting<TestCurrencyHateosResourceHandlerContext>,
    SpreadsheetMetadataTesting {

    @Override
    public void testSetObjectPostProcessorSame() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetPreProcessorSame() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestCurrencyHateosResourceHandlerContext createContext() {
        return new TestCurrencyHateosResourceHandlerContext();
    }

    @Override
    public Class<TestCurrencyHateosResourceHandlerContext> type() {
        return TestCurrencyHateosResourceHandlerContext.class;
    }

    final static class TestCurrencyHateosResourceHandlerContext implements CurrencyHateosResourceHandlerContext, CurrencyContextDelegator,
        JsonNodeMarshallUnmarshallContextDelegator {

        @Override
        public Optional<Currency> currencyForCurrencyCode(final String currencyCode) {
            return CurrencyContextDelegator.super.currencyForCurrencyCode(currencyCode);
        }

        @Override
        public CurrencyContext currencyContext() {
            return CURRENCY_CONTEXT;
        }

        @Override
        public MediaType contentType() {
            return MediaType.TEXT_PLAIN;
        }

        @Override
        public Indentation indentation() {
            return INDENTATION;
        }

        @Override
        public LineEnding lineEnding() {
            return EOL;
        }

        @Override
        public CurrencyHateosResourceHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
            Objects.requireNonNull(processor, "processor");

            return new TestCurrencyHateosResourceHandlerContext();
        }

        @Override
        public CurrencyHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
            Objects.requireNonNull(processor, "processor");

            return new TestCurrencyHateosResourceHandlerContext();
        }

        @Override
        public JsonNodeMarshallUnmarshallContext jsonNodeMarshallUnmarshallContext() {
            return JsonNodeMarshallUnmarshallContexts.basic(
                JsonNodeMarshallContexts.basic(),
                JsonNodeUnmarshallContexts.basic(
                    (String cc) -> Optional.ofNullable(
                        Currency.getInstance(cc)
                    ),
                    (String lt) -> Optional.of(
                        Locale.forLanguageTag(lt)
                    ),
                    ExpressionNumberKind.BIG_DECIMAL,
                    MathContext.UNLIMITED
                )
            );
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }
}
