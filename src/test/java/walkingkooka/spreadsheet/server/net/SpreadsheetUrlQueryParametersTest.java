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

package walkingkooka.spreadsheet.server.net;

import org.junit.jupiter.api.Test;
import walkingkooka.Cast;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.locale.FakeLocaleContext;
import walkingkooka.locale.LocaleContext;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.PublicStaticHelperTesting;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public final class SpreadsheetUrlQueryParametersTest implements PublicStaticHelperTesting<SpreadsheetUrlQueryParameters> {

    // count............................................................................................................

    @Test
    public void testCountMissing() {
        this.checkEquals(
            OptionalInt.empty(),
            SpreadsheetUrlQueryParameters.count(
                Maps.empty()
            )
        );
    }

    @Test
    public void testCount() {
        this.checkEquals(
            OptionalInt.of(123),
            SpreadsheetUrlQueryParameters.count(
                Maps.of(
                    SpreadsheetUrlQueryParameters.COUNT,
                    Lists.of("123")
                )
            )
        );
    }

    // locale...........................................................................................................

    private final static Locale LOCALE = Locale.forLanguageTag("en-AU");

    @Test
    public void testLocaleQueryParameterMissing() {
        this.localeAndCheck(
            Optional.empty(), // no query parameters
            LOCALE,
            LOCALE
        );
    }

    @Test
    public void testLocale() {
        this.localeAndCheck(
            Optional.of(LOCALE),
            Locale.FRENCH,
            LOCALE
        );
    }

    private void localeAndCheck(final Optional<Locale> parameters,
                                final Locale context,
                                final Locale expected) {
        this.localeAndCheck(
            Cast.to(
                parameters.map(
                    l -> Maps.of(
                        SpreadsheetUrlQueryParameters.LOCALE,
                        Lists.of(
                            l.toLanguageTag()
                        )
                    )
                ).orElse(Maps.empty())
            ),
            new FakeLocaleContext() {
                @Override
                public Locale locale() {
                    return context;
                }
            },
            expected
        );
    }

    private void localeAndCheck(final Map<HttpRequestAttribute<?>, Object> parameters,
                                final LocaleContext context,
                                final Locale expected) {
        this.checkEquals(
            expected,
            SpreadsheetUrlQueryParameters.locale(
                parameters,
                context
            ),
            () -> parameters.toString() + " " + context
        );
    }

    // offset.............................................................................................................

    @Test
    public void testOffsetMissingParameter() {
        this.checkEquals(
            OptionalInt.empty(),
            SpreadsheetUrlQueryParameters.offset(
                Maps.empty()
            )
        );
    }

    @Test
    public void testOffset() {
        this.checkEquals(
            OptionalInt.of(123),
            SpreadsheetUrlQueryParameters.offset(
                Maps.of(
                    SpreadsheetUrlQueryParameters.OFFSET,
                    Lists.of("123")
                )
            )
        );
    }

    // Class............................................................................................................

    @Override
    public Class<SpreadsheetUrlQueryParameters> type() {
        return SpreadsheetUrlQueryParameters.class;
    }

    @Override
    public boolean canHavePublicTypes(final Method method) {
        return false;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
