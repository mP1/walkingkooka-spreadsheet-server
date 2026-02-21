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

import walkingkooka.Value;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Currency;
import java.util.Objects;

/**
 * An id that uniquely identifies a {@link Currency} for a {@link walkingkooka.net.http.server.hateos.HateosResourceHandler}.
 */
public final class CurrencyCode implements Comparable<CurrencyCode>, Value<Currency> {

    public static CurrencyCode parse(final String text) {
        return with(
            Currency.getInstance(text)
        );
    }

    public static CurrencyCode with(final Currency currency) {
        return new CurrencyCode(
            Objects.requireNonNull(currency)
        );
    }

    private CurrencyCode(final Currency currency) {
        super();
        this.currency = currency;
    }

    // Value............................................................................................................

    @Override
    public Currency value() {
        return this.currency;
    }

    private final Currency currency;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return this.currency.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
            (other instanceof CurrencyCode &&
                this.equals0((CurrencyCode) other));
    }

    private boolean equals0(final CurrencyCode other) {
        return this.currency.equals(other.currency);
    }

    @Override
    public String toString() {
        return this.currency.getCurrencyCode();
    }

    // Comparable.......................................................................................................

    @Override
    public int compareTo(final CurrencyCode other) {
        return this.currency.getCurrencyCode()
            .compareTo(other.currency.getCurrencyCode());
    }

    // json.............................................................................................................

    static CurrencyCode unmarshall(final JsonNode node,
                                   final JsonNodeUnmarshallContext context) {
        return with(
            context.unmarshall(
                node,
                Currency.class
            )
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshall(
            this.currency
        );
    }

    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(CurrencyCode.class),
            CurrencyCode::unmarshall,
            CurrencyCode::marshall,
            CurrencyCode.class
        );
    }
}
