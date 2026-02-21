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

import walkingkooka.currency.CurrencyContext;
import walkingkooka.currency.CurrencyContextDelegator;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Currency;
import java.util.Objects;
import java.util.Optional;

final class BasicCurrencyHateosResourceHandlerContext implements CurrencyHateosResourceHandlerContext,
    CurrencyContextDelegator,
    HateosResourceHandlerContextDelegator {

    static BasicCurrencyHateosResourceHandlerContext with(final CurrencyContext currencyContext,
                                                          final HateosResourceHandlerContext hateosResourceHandlerContext) {
        return new BasicCurrencyHateosResourceHandlerContext(
            Objects.requireNonNull(currencyContext, "currencyContext"),
            Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext")
        );
    }

    private BasicCurrencyHateosResourceHandlerContext(final CurrencyContext currencyContext,
                                                      final HateosResourceHandlerContext hateosResourceHandlerContext) {
        super();

        this.currencyContext = currencyContext;
        this.hateosResourceHandlerContext = hateosResourceHandlerContext;
    }

    // CurrencyContextDelegator.........................................................................................

    @Override
    public Optional<Currency> currencyForCurrencyCode(final String currencyCode) {
        return this.currencyContext.currencyForCurrencyCode(currencyCode);
    }

    @Override
    public CurrencyContext currencyContext() {
        return this.currencyContext;
    }

    private final CurrencyContext currencyContext;

    // HateosResourceHandlerContext.....................................................................................

    @Override
    public HateosResourceHandlerContext hateosResourceHandlerContext() {
        return this.hateosResourceHandlerContext;
    }

    private final HateosResourceHandlerContext hateosResourceHandlerContext;

    @Override
    public CurrencyHateosResourceHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        return this.setHateosResourceHandlerContext(
            this.hateosResourceHandlerContext.setObjectPostProcessor(processor)
        );
    }

    @Override
    public CurrencyHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.setHateosResourceHandlerContext(
            this.hateosResourceHandlerContext.setPreProcessor(processor)
        );
    }

    private CurrencyHateosResourceHandlerContext setHateosResourceHandlerContext(final HateosResourceHandlerContext context) {
        return this.hateosResourceHandlerContext.equals(context) ?
            this :
            with(
                this.currencyContext,
                context
            );
    }

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.currencyContext +
            " " +
            this.hateosResourceHandlerContext;
    }
}
