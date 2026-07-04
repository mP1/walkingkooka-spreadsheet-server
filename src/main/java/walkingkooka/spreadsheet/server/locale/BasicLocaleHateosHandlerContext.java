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

import walkingkooka.locale.LocaleContext;
import walkingkooka.locale.LocaleContextDelegator;
import walkingkooka.locale.LocaleLanguageTag;
import walkingkooka.net.http.server.hateos.HateosHandlerContext;
import walkingkooka.net.http.server.hateos.HateosHandlerContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

final class BasicLocaleHateosHandlerContext implements LocaleHateosHandlerContext,
    LocaleContextDelegator,
    HateosHandlerContextDelegator {

    static BasicLocaleHateosHandlerContext with(final LocaleContext localeContext,
                                                final HateosHandlerContext hateosHandlerContext) {
        return new BasicLocaleHateosHandlerContext(
            Objects.requireNonNull(localeContext, "localeContext"),
            Objects.requireNonNull(hateosHandlerContext, "hateosHandlerContext")
        );
    }

    private BasicLocaleHateosHandlerContext(final LocaleContext localeContext,
                                            final HateosHandlerContext hateosHandlerContext) {
        super();

        this.localeContext = localeContext;
        this.hateosHandlerContext = hateosHandlerContext;
    }

    // LocaleContextDelegator...........................................................................................

    @Override
    public Optional<Locale> localeForLanguageTag(final LocaleLanguageTag languageTag) {
        return this.localeContext.localeForLanguageTag(languageTag);
    }

    @Override
    public LocaleContext localeContext() {
        return this.localeContext;
    }

    private final LocaleContext localeContext;

    // HateosHandlerContext.....................................................................................

    @Override
    public HateosHandlerContext hateosHandlerContext() {
        return this.hateosHandlerContext;
    }

    private final HateosHandlerContext hateosHandlerContext;

    @Override
    public LocaleHateosHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        return this.setHateosHandlerContext(
            this.hateosHandlerContext.setObjectPostProcessor(processor)
        );
    }

    @Override
    public LocaleHateosHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.setHateosHandlerContext(
            this.hateosHandlerContext.setPreProcessor(processor)
        );
    }

    private LocaleHateosHandlerContext setHateosHandlerContext(final HateosHandlerContext context) {
        return this.hateosHandlerContext.equals(context) ?
            this :
            with(
                this.localeContext,
                context
            );
    }

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.localeContext +
            " " +
            this.hateosHandlerContext;
    }
}
