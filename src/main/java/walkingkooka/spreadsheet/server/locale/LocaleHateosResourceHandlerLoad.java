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

import walkingkooka.collect.set.SortedSets;
import walkingkooka.locale.LocaleContext;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.server.net.SpreadsheetUrlQueryParameters;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link HateosResourceHandler} that invokes {@link LocaleContext#availableLocales()} to supply the requested {@link LocaleHateosResource}.
 */
final class LocaleHateosResourceHandlerLoad implements HateosResourceHandler<LocaleTag, LocaleHateosResource, LocaleHateosResourceSet, LocaleHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleNone<LocaleTag, LocaleHateosResource, LocaleHateosResourceSet, LocaleHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<LocaleTag, LocaleHateosResource, LocaleHateosResourceSet, LocaleHateosResourceHandlerContext> {

    private final static int DEFAULT_COUNT = 20;

    private final static int MAX_COUNT = 40;

    /**
     * Singleton
     */
    final static LocaleHateosResourceHandlerLoad INSTANCE = new LocaleHateosResourceHandlerLoad();

    private LocaleHateosResourceHandlerLoad() {
    }

    @Override
    public Optional<LocaleHateosResource> handleOne(final LocaleTag id,
                                                    final Optional<LocaleHateosResource> resource,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters,
                                                    final UrlPath path,
                                                    final LocaleHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(id);
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return context.availableLocales()
            .stream()
            .filter((final Locale l) ->
                id.equals(
                    LocaleTag.fromLocale(l)
                )
            ).flatMap((Locale l) -> fromLocale(l, context))
            .findFirst();
    }

    @Override
    public Optional<LocaleHateosResourceSet> handleAll(final Optional<LocaleHateosResourceSet> resource,
                                                       final Map<HttpRequestAttribute<?>, Object> parameters,
                                                       final UrlPath path,
                                                       final LocaleHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        final int offset = SpreadsheetUrlQueryParameters.offset(parameters)
            .orElse(0);
        final int count = SpreadsheetUrlQueryParameters.count(parameters)
            .orElse(DEFAULT_COUNT);

        final SortedSet<LocaleHateosResource> all = SortedSets.tree();
        context.availableLocales()
            .stream()
            .flatMap((Locale l) -> fromLocale(l, context))
            .skip(offset)
            .limit(count)
            .forEach(all::add);

        return Optional.of(
            LocaleHateosResourceSet.with(all)
        );
    }

    @Override
    public Optional<LocaleHateosResourceSet> handleMany(final Set<LocaleTag> ids,
                                                        final Optional<LocaleHateosResourceSet> resource,
                                                        final Map<HttpRequestAttribute<?>, Object> parameters,
                                                        final UrlPath path,
                                                        final LocaleHateosResourceHandlerContext context) {
        HateosResourceHandler.checkManyIds(ids);
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            LocaleHateosResourceSet.with(
                context.availableLocales()
                    .stream()
                    .filter((Locale l) -> ids.contains(
                            LocaleTag.fromLocale(l)
                        )
                    ).flatMap((Locale l) -> fromLocale(l, context))
                    .collect(Collectors.toCollection(SortedSets::tree))
            )
        );
    }

    private static Stream<LocaleHateosResource> fromLocale(final Locale locale,
                                                           final LocaleContext context) {
        final Optional<String> text = context.localeText(locale);

        // Optional#stream not supported by GWT JRE's Locale
        return text.isPresent() ?
            Stream.of(
                LocaleHateosResource.with(
                    LocaleTag.fromLocale(locale),
                    text.get()
                )
            ) :
            Stream.empty();
    }

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
