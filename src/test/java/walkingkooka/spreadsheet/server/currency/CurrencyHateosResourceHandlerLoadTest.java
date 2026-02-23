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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.RangeBound;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.currency.CurrencyContexts;
import walkingkooka.locale.LocaleContexts;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.server.net.SpreadsheetUrlQueryParameters;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class CurrencyHateosResourceHandlerLoadTest implements HateosResourceHandlerTesting<CurrencyHateosResourceHandlerLoad, CurrencyCode, CurrencyHateosResource, CurrencyHateosResourceSet, CurrencyHateosResourceHandlerContext> {

    private final static CurrencyCode AUD = CurrencyCode.parse("AUD");

    private final static CurrencyCode NZD = CurrencyCode.parse("NZD");

    @Test
    public void testHandleOneWithCurrencyCodeAud() {
        this.handleOneAndCheck(
            AUD,
            Optional.empty(),
            CurrencyHateosResourceHandlerLoad.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(),
            Optional.of(
                CurrencyHateosResource.with(
                    AUD,
                    "Australian Dollar"
                )
            )
        );
    }

    // loading all fails on github because some currencies have slightly different Currency#getDisplayName because
    // it has a slightly different JRE
    @Test
    public void testHandleAllWithCount() {
        this.handleAllAndCheck(
            Optional.empty(),
            Maps.of(
                SpreadsheetUrlQueryParameters.COUNT, Lists.of("10")
            ),
            UrlPath.EMPTY,
            this.context(),
            Optional.of(
                CurrencyHateosResourceSet.EMPTY.concatAll(
                    Currency.getAvailableCurrencies()
                        .stream()
                        .filter((c) -> false == c.getDisplayName().isEmpty())
                        .map(CurrencyHateosResource::fromCurrency)
                        .sorted()
                        .limit(10)
                        .collect(Collectors.toList())
                )
            )
        );
    }

    //  AED
    //    United Arab Emirates Dirham
    //  AFA
    //    Afghan Afghani (1927–2002)
    @Test
    public void testHandleAllWithOffsetAndCount() {
        this.handleAllAndCheck(
            Optional.empty(),
            Maps.of(
                SpreadsheetUrlQueryParameters.OFFSET, Lists.of("1"),
                SpreadsheetUrlQueryParameters.COUNT, Lists.of("2")
            ),
            UrlPath.EMPTY,
            this.context(),
            Optional.of(
                CurrencyHateosResourceSet.EMPTY.concat(
                        CurrencyHateosResource.fromCurrency(
                            Currency.getInstance("AED")
                        )
                ).concat(
                    CurrencyHateosResource.fromCurrency(
                        Currency.getInstance("AFA")
                    )
                )
            )
        );
    }

    @Test
    public void testHandleMany() {
        this.handleManyAndCheck(
            Sets.of(
                AUD,
                NZD
            ),
            Optional.empty(),
            CurrencyHateosResourceHandlerLoad.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(),
            Optional.of(
                CurrencyHateosResourceSet.EMPTY.concat(
                    CurrencyHateosResource.fromCurrency(
                        AUD.value()
                    )
                ).concat(
                    CurrencyHateosResource.fromCurrency(
                        NZD.value()
                    )
                )
            )
        );
    }

    @Override
    public CurrencyHateosResourceHandlerLoad createHandler() {
        return CurrencyHateosResourceHandlerLoad.INSTANCE;
    }

    @Override
    public CurrencyCode id() {
        return AUD;
    }

    @Override
    public Set<CurrencyCode> manyIds() {
        return Set.of(
            AUD,
            NZD
        );
    }

    @Override
    public Range<CurrencyCode> range() {
        return Range.with(
            RangeBound.inclusive(
                AUD
            ),
            RangeBound.inclusive(
                NZD
            )
        );
    }

    @Override
    public Optional<CurrencyHateosResource> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<CurrencyHateosResourceSet> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosResourceHandler.NO_PARAMETERS;
    }

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    @Override
    public CurrencyHateosResourceHandlerContext context() {
        final Locale locale = Locale.forLanguageTag("en-AU");

        return CurrencyHateosResourceHandlerContexts.basic(
            CurrencyContexts.jre(
                Currency.getInstance(locale),
                (Currency from, Currency to) -> {
                    throw new UnsupportedOperationException();
                },
                LocaleContexts.jre(locale)
            ),
            HateosResourceHandlerContexts.fake()
        );
    }

    @Override
    public Class<CurrencyHateosResourceHandlerLoad> type() {
        return CurrencyHateosResourceHandlerLoad.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    @Override
    public String typeNamePrefix() {
        return Currency.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return "Load";
    }
}
