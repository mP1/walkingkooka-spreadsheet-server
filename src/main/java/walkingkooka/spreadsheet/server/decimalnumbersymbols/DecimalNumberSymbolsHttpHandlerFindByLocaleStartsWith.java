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

package walkingkooka.spreadsheet.server.decimalnumbersymbols;

import walkingkooka.collect.set.SortedSets;
import walkingkooka.math.DecimalNumberSymbols;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.spreadsheet.server.SpreadsheetUrlQueryParameters;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.locale.LocaleTag;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.SortedSet;

/**
 * A {@link HttpHandler} that finds all {@link Locale} that match a search string and returns the {@link DecimalNumberSymbols} for each.
 */
final class DecimalNumberSymbolsHttpHandlerFindByLocaleStartsWith implements HttpHandler {

    static DecimalNumberSymbolsHttpHandlerFindByLocaleStartsWith with(final int defaultCount,
                                                                      final LocaleHateosResourceHandlerContext context) {
        return new DecimalNumberSymbolsHttpHandlerFindByLocaleStartsWith(
            defaultCount,
            Objects.requireNonNull(context, "context")
        );
    }

    private DecimalNumberSymbolsHttpHandlerFindByLocaleStartsWith(final int defaultCount,
                                                                  final LocaleHateosResourceHandlerContext context) {
        super();

        this.defaultCount = defaultCount;
        this.context = context;
    }

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(response, "response");

        final MediaType requiredContentType = context.contentType();

        HttpHeaderName.ACCEPT.headerOrFail(request)
            .testOrFail(requiredContentType);

        final List<UrlPathName> names = request.url()
            .path()
            .namesList();

        final LocaleHateosResourceHandlerContext context = this.context;

        // /api/decimalNumberSymbols/*/localeStartsWith/StartsWithString
        // 01   2               3 4                5
        final String startsWith = names.get(5)
            .value();

        final SortedSet<DecimalNumberSymbolsHateosResource> all = SortedSets.tree();

        final int offset = SpreadsheetUrlQueryParameters.offset(request.routerParameters())
            .orElse(0);
        final int count = SpreadsheetUrlQueryParameters.count(request.routerParameters())
            .orElse(this.defaultCount);

        for (final Locale locale : context.findByLocaleText(startsWith, offset, count)) {
            final DecimalNumberSymbols decimalNumberSymbols = context.decimalNumberSymbolsForLocale(locale)
                .orElse(null);

            if (null != decimalNumberSymbols) {
                all.add(
                    DecimalNumberSymbolsHateosResource.with(
                        LocaleTag.with(locale),
                        decimalNumberSymbols
                    )
                );
            }
        }

        response.setStatus(
            HttpStatusCode.OK.status()
        );
        response.setEntity(
            HttpEntity.EMPTY.setContentType(
                context.contentType()
            ).addHeader(
                HateosResourceMappings.X_CONTENT_TYPE_NAME,
                DecimalNumberSymbolsHateosResourceSet.class.getSimpleName()
            ).setBodyText(
                context.marshall(
                    DecimalNumberSymbolsHateosResourceSet.with(all)
                ).toString()
            ).setContentLength()
        );
    }

    private final int defaultCount;

    private final LocaleHateosResourceHandlerContext context;

    @Override
    public String toString() {
        return this.context.toString();
    }
}
