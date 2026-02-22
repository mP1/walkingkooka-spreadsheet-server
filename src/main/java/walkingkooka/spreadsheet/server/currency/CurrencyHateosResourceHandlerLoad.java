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

import walkingkooka.collect.set.SortedSets;
import walkingkooka.currency.CurrencyContext;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.server.net.SpreadsheetUrlQueryParameters;

import java.util.Currency;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link HateosResourceHandler} that invokes {@link CurrencyContext} to supply the requested {@link CurrencyHateosResource}.
 */
final class CurrencyHateosResourceHandlerLoad implements HateosResourceHandler<CurrencyCode, CurrencyHateosResource, CurrencyHateosResourceSet, CurrencyHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleNone<CurrencyCode, CurrencyHateosResource, CurrencyHateosResourceSet, CurrencyHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<CurrencyCode, CurrencyHateosResource, CurrencyHateosResourceSet, CurrencyHateosResourceHandlerContext> {

    private final static int DEFAULT_COUNT = 20;

    private final static int MAX_COUNT = 40;

    /**
     * Singleton
     */
    final static CurrencyHateosResourceHandlerLoad INSTANCE = new CurrencyHateosResourceHandlerLoad();

    private CurrencyHateosResourceHandlerLoad() {
    }

    @Override
    public Optional<CurrencyHateosResource> handleOne(final CurrencyCode id,
                                                      final Optional<CurrencyHateosResource> resource,
                                                      final Map<HttpRequestAttribute<?>, Object> parameters,
                                                      final UrlPath path,
                                                      final CurrencyHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(id);
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return context.currencyForCurrencyCode(
            id.value()
                .getCurrencyCode()
        ).flatMap(
            c -> fromCurrencyOptional(
                c,
                context
            )
        );
    }

    @Override
    public Optional<CurrencyHateosResourceSet> handleAll(final Optional<CurrencyHateosResourceSet> resource,
                                                         final Map<HttpRequestAttribute<?>, Object> parameters,
                                                         final UrlPath path,
                                                         final CurrencyHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        final int offset = SpreadsheetUrlQueryParameters.offset(parameters)
            .orElse(0);
        final int count = SpreadsheetUrlQueryParameters.count(parameters)
            .orElse(DEFAULT_COUNT);

        final SortedSet<CurrencyHateosResource> all = SortedSets.tree();
        context.availableCurrencies()
            .stream()
            .flatMap((Currency l) -> fromCurrency(l, context))
            .skip(offset)
            .limit(count)
            .forEach(all::add);

        return Optional.of(
            CurrencyHateosResourceSet.with(all)
        );
    }

    @Override
    public Optional<CurrencyHateosResourceSet> handleMany(final Set<CurrencyCode> ids,
                                                          final Optional<CurrencyHateosResourceSet> resource,
                                                          final Map<HttpRequestAttribute<?>, Object> parameters,
                                                          final UrlPath path,
                                                          final CurrencyHateosResourceHandlerContext context) {
        HateosResourceHandler.checkManyIds(ids);
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            CurrencyHateosResourceSet.with(
                context.availableCurrencies()
                    .stream()
                    .filter((Currency l) -> ids.contains(CurrencyCode.with(l)))
                    .flatMap((Currency l) -> fromCurrency(l, context))
                    .collect(Collectors.toCollection(SortedSets::tree))
            )
        );
    }

    private static Stream<CurrencyHateosResource> fromCurrency(final Currency currency,
                                                               final CurrencyContext context) {
        final Optional<String> text = context.currencyText(currency);

        // Optional#stream not supported by GWT JRE's Currency
        return text.isPresent() ?
            Stream.of(
                CurrencyHateosResource.with(
                    CurrencyCode.with(currency),
                    text.get()
                )
            ) :
            Stream.empty();
    }

    private static Optional<CurrencyHateosResource> fromCurrencyOptional(final Currency currency,
                                                                         final CurrencyContext context) {
        return context.currencyText(currency)
            .map(
                t ->
                    CurrencyHateosResource.with(
                        CurrencyCode.with(currency),
                        t
                    )
            );
    }

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
