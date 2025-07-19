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

package walkingkooka.spreadsheet.server.datetimesymbols;

import walkingkooka.collect.set.SortedSets;
import walkingkooka.datetime.DateTimeSymbols;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosHttpHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.spreadsheet.server.SpreadsheetUrlQueryParameters;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.locale.LocaleTag;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.SortedSet;

/**
 * A {@link HateosHttpHandler} that finds all {@link Locale} that match a search string and returns the {@link DateTimeSymbols} for each.
 */
final class DateTimeSymbolsFindByLocaleStartsWithHateosHttpHandler implements HateosHttpHandler<LocaleHateosResourceHandlerContext> {

    final static DateTimeSymbolsFindByLocaleStartsWithHateosHttpHandler INSTANCE = new DateTimeSymbolsFindByLocaleStartsWithHateosHttpHandler();

    private DateTimeSymbolsFindByLocaleStartsWithHateosHttpHandler() {
        super();
    }

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response,
                       final LocaleHateosResourceHandlerContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(context, "context");

        final MediaType requiredContentType = context.contentType();

        HttpHeaderName.ACCEPT.headerOrFail(request)
            .testOrFail(requiredContentType);

        final List<UrlPathName> names = request.url()
            .path()
            .namesList();

        // /api/dateTimeSymbols/*/localeStartsWith/StartsWithString
        // 01   2               3 4                5
        final String startsWith = names.get(5)
            .value();

        final SortedSet<DateTimeSymbolsHateosResource> all = SortedSets.tree();

        final int offset = SpreadsheetUrlQueryParameters.offset(request.routerParameters())
            .orElse(0);
        final int count = SpreadsheetUrlQueryParameters.count(request.routerParameters())
            .orElse(DEFAULT_COUNT);

        for (final Locale locale : context.findByLocaleText(startsWith, offset, count)) {
            final DateTimeSymbols dateTimeSymbols = context.dateTimeSymbolsForLocale(locale)
                .orElse(null);

            if (null != dateTimeSymbols) {
                all.add(
                    DateTimeSymbolsHateosResource.with(
                        LocaleTag.with(locale),
                        dateTimeSymbols
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
                DateTimeSymbolsHateosResourceSet.class.getSimpleName()
            ).setBodyText(
                context.marshall(
                    DateTimeSymbolsHateosResourceSet.with(all)
                ).toString()
            ).setContentLength()
        );
    }

    private final static int DEFAULT_COUNT = 10;

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
