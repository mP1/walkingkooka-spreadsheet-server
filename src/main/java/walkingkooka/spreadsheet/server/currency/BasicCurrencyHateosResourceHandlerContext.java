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

import walkingkooka.currency.CurrencyCode;
import walkingkooka.currency.CurrencyContext;
import walkingkooka.currency.CurrencyContextDelegator;
import walkingkooka.net.http.server.hateos.HateosHandlerContext;
import walkingkooka.net.http.server.hateos.HateosHandlerContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Currency;
import java.util.Objects;
import java.util.Optional;

final class BasicCurrencyHateosHandlerContext implements CurrencyHateosHandlerContext,
    CurrencyContextDelegator,
    HateosHandlerContextDelegator {

    static BasicCurrencyHateosHandlerContext with(final CurrencyContext currencyContext,
                                                          final HateosHandlerContext hateosHandlerContext) {
        return new BasicCurrencyHateosHandlerContext(
            Objects.requireNonNull(currencyContext, "currencyContext"),
            Objects.requireNonNull(hateosHandlerContext, "hateosHandlerContext")
        );
    }

    private BasicCurrencyHateosHandlerContext(final CurrencyContext currencyContext,
                                                      final HateosHandlerContext hateosHandlerContext) {
        super();

        this.currencyContext = currencyContext;
        this.hateosHandlerContext = hateosHandlerContext;
    }

    // CurrencyContextDelegator.........................................................................................

    @Override
    public Optional<Currency> currencyForCurrencyCode(final CurrencyCode currencyCode) {
        return this.currencyContext.currencyForCurrencyCode(currencyCode);
    }

    @Override
    public CurrencyContext currencyContext() {
        return this.currencyContext;
    }

    private final CurrencyContext currencyContext;

    // HateosHandlerContext.....................................................................................

    @Override
    public HateosHandlerContext hateosHandlerContext() {
        return this.hateosHandlerContext;
    }

    private final HateosHandlerContext hateosHandlerContext;

    @Override
    public CurrencyHateosHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        return this.setHateosHandlerContext(
            this.hateosHandlerContext.setObjectPostProcessor(processor)
        );
    }

    @Override
    public CurrencyHateosHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.setHateosHandlerContext(
            this.hateosHandlerContext.setPreProcessor(processor)
        );
    }

    private CurrencyHateosHandlerContext setHateosHandlerContext(final HateosHandlerContext context) {
        return this.hateosHandlerContext.equals(context) ?
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
            this.hateosHandlerContext;
    }
}
