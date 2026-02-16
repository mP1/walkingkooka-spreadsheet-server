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
 * A {@link HateosResourceHandler} that invokes {@link LocaleContext#decimalNumberSymbolsForLocale(Locale)}.
 */
final class DecimalNumberSymbolsHateosResourceHandlerLoad implements HateosResourceHandler<LocaleTag, DecimalNumberSymbolsHateosResource, DecimalNumberSymbolsHateosResourceSet, LocaleHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleAll<LocaleTag, DecimalNumberSymbolsHateosResource, DecimalNumberSymbolsHateosResourceSet, LocaleHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleMany<LocaleTag, DecimalNumberSymbolsHateosResource, DecimalNumberSymbolsHateosResourceSet, LocaleHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleNone<LocaleTag, DecimalNumberSymbolsHateosResource, DecimalNumberSymbolsHateosResourceSet, LocaleHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleOne<LocaleTag, DecimalNumberSymbolsHateosResource, DecimalNumberSymbolsHateosResourceSet, LocaleHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<LocaleTag, DecimalNumberSymbolsHateosResource, DecimalNumberSymbolsHateosResourceSet, LocaleHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static DecimalNumberSymbolsHateosResourceHandlerLoad INSTANCE = new DecimalNumberSymbolsHateosResourceHandlerLoad();

    private DecimalNumberSymbolsHateosResourceHandlerLoad() {
    }

    @Override
    public Optional<DecimalNumberSymbolsHateosResource> handleOne(final LocaleTag id,
                                                             final Optional<DecimalNumberSymbolsHateosResource> resource,
                                                             final Map<HttpRequestAttribute<?>, Object> parameters,
                                                             final UrlPath path,
                                                             final LocaleHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(id);
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return context.decimalNumberSymbolsForLocale(
            id.value()
        ).map(
            d -> DecimalNumberSymbolsHateosResource.with(
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
