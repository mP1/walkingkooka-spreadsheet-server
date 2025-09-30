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
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Objects;

final class BasicLocaleHateosResourceHandlerContext implements LocaleHateosResourceHandlerContext,
    LocaleContextDelegator,
    HateosResourceHandlerContextDelegator {

    static BasicLocaleHateosResourceHandlerContext with(final LocaleContext localeContext,
                                                        final HateosResourceHandlerContext hateosResourceHandlerContext) {
        return new BasicLocaleHateosResourceHandlerContext(
            Objects.requireNonNull(localeContext, "localeContext"),
            Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext")
        );
    }

    private BasicLocaleHateosResourceHandlerContext(final LocaleContext localeContext,
                                                    final HateosResourceHandlerContext hateosResourceHandlerContext) {
        super();

        this.localeContext = localeContext;
        this.hateosResourceHandlerContext = hateosResourceHandlerContext;
    }

    // LocaleContextDelegator...........................................................................................

    @Override
    public LocaleContext localeContext() {
        return this.localeContext;
    }

    private final LocaleContext localeContext;

    // HateosResourceHandlerContext.....................................................................................

    @Override
    public HateosResourceHandlerContext hateosResourceHandlerContext() {
        return this.hateosResourceHandlerContext;
    }

    private final HateosResourceHandlerContext hateosResourceHandlerContext;

    @Override
    public LocaleHateosResourceHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        return this.setHateosResourceHandlerContext(
            this.hateosResourceHandlerContext.setObjectPostProcessor(processor)
        );
    }

    @Override
    public LocaleHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.setHateosResourceHandlerContext(
            this.hateosResourceHandlerContext.setPreProcessor(processor)
        );
    }

    private LocaleHateosResourceHandlerContext setHateosResourceHandlerContext(final HateosResourceHandlerContext context) {
        return this.hateosResourceHandlerContext.equals(context) ?
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
            this.hateosResourceHandlerContext;
    }
}
