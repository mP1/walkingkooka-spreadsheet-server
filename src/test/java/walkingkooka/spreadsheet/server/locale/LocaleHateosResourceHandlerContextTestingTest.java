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

package walkingkooka.spreadsheet.server.locale;

import walkingkooka.datetime.DateTimeSymbols;
import walkingkooka.math.DecimalNumberSymbols;
import walkingkooka.net.header.MediaType;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceHandlerContextTestingTest.TestLocaleHateosResourceHandlerContext;
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
import java.util.Set;

public final class LocaleHateosResourceHandlerContextTestingTest implements LocaleHateosResourceHandlerContextTesting<TestLocaleHateosResourceHandlerContext> {

    @Override
    public void testFindByLocaleTextWithNullTextFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testFindByLocaleTextWithNegativeOffsetFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testFindByLocaleTextWithInvalidCountFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetObjectPostProcessorSame() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetPreProcessorSame() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestLocaleHateosResourceHandlerContext createContext() {
        return new TestLocaleHateosResourceHandlerContext();
    }

    @Override
    public Class<TestLocaleHateosResourceHandlerContext> type() {
        return TestLocaleHateosResourceHandlerContext.class;
    }

    static class TestLocaleHateosResourceHandlerContext implements LocaleHateosResourceHandlerContext, JsonNodeMarshallUnmarshallContextDelegator {

        @Override
        public Set<Locale> availableLocales() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<DateTimeSymbols> dateTimeSymbolsForLocale(final Locale locale) {
            Objects.requireNonNull(locale, "locale");

            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<DecimalNumberSymbols> decimalNumberSymbolsForLocale(final Locale locale) {
            Objects.requireNonNull(locale, "locale");

            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Locale> findByLocaleText(final String text,
                                            final int offset,
                                            final int count) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Locale locale() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLocale(final Locale locale) {
            Objects.requireNonNull(locale, "locale");
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Locale> localeForLanguageTag(final String languageTag) {
            Objects.requireNonNull(languageTag, "languageTag");
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<String> localeText(final Locale locale) {
            Objects.requireNonNull(locale, "locale");
            throw new UnsupportedOperationException();
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
        public LocaleHateosResourceHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
            Objects.requireNonNull(processor, "processor");

            return new TestLocaleHateosResourceHandlerContext();
        }

        @Override
        public LocaleHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
            Objects.requireNonNull(processor, "processor");

            return new TestLocaleHateosResourceHandlerContext();
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
