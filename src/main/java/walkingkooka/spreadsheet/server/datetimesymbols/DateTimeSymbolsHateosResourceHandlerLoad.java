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

import walkingkooka.locale.LocaleContext;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleOne;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.locale.LocaleTag;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that invokes {@link LocaleContext#dateTimeSymbolsForLocale(Locale)}.
 */
final class DateTimeSymbolsHateosResourceHandlerLoad implements HateosResourceHandler<LocaleTag, DateTimeSymbolsHateosResource, DateTimeSymbolsHateosResourceSet, LocaleHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleAll<LocaleTag, DateTimeSymbolsHateosResource, DateTimeSymbolsHateosResourceSet, LocaleHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleMany<LocaleTag, DateTimeSymbolsHateosResource, DateTimeSymbolsHateosResourceSet, LocaleHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleNone<LocaleTag, DateTimeSymbolsHateosResource, DateTimeSymbolsHateosResourceSet, LocaleHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleOne<LocaleTag, DateTimeSymbolsHateosResource, DateTimeSymbolsHateosResourceSet, LocaleHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<LocaleTag, DateTimeSymbolsHateosResource, DateTimeSymbolsHateosResourceSet, LocaleHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static DateTimeSymbolsHateosResourceHandlerLoad INSTANCE = new DateTimeSymbolsHateosResourceHandlerLoad();

    private DateTimeSymbolsHateosResourceHandlerLoad() {
    }

    @Override
    public Optional<DateTimeSymbolsHateosResource> handleOne(final LocaleTag id,
                                                             final Optional<DateTimeSymbolsHateosResource> resource,
                                                             final Map<HttpRequestAttribute<?>, Object> parameters,
                                                             final UrlPath path,
                                                             final LocaleHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(id);
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return context.dateTimeSymbolsForLocale(
            id.value()
        ).map(
            d -> DateTimeSymbolsHateosResource.with(
                id,
                context.localeTextOrFail(
                    id.value(),
                    context.locale()
                ),
                d
            )
        );
    }

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
