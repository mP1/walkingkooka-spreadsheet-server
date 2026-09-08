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

package walkingkooka.spreadsheet.server.currencyexchangerate;

import walkingkooka.HasValue;
import walkingkooka.currency.CurrencyExchange;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link CurrencyExchange}.
 */
public final class CurrencyExchangeHateosResource implements HateosResource<CurrencyExchange>,
    HasValue<CurrencyExchange>,
    Comparable<CurrencyExchangeHateosResource>,
    TreePrintable {

    public final static HateosResourceName HATEOS_RESOURCE_NAME = HateosResourceName.with("currencyExchange");

    public static CurrencyExchangeHateosResource with(final CurrencyExchange currencyExchange) {
        return new CurrencyExchangeHateosResource(
            Objects.requireNonNull(currencyExchange, "currencyExchange")
        );
    }

    private CurrencyExchangeHateosResource(final CurrencyExchange currencyExchange) {
        super();
        this.currencyExchange = currencyExchange;
    }

    @Override
    public Optional<CurrencyExchange> id() {
        return Optional.of(this.currencyExchange);
    }

    @Override
    public String hateosLinkId() {
        return this.currencyExchange.text();
    }

    @Override
    public CurrencyExchange value() {
        return this.currencyExchange;
    }

    private final CurrencyExchange currencyExchange;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return this.currencyExchange.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
            (other instanceof CurrencyExchangeHateosResource &&
                this.equals0((CurrencyExchangeHateosResource) other));
    }

    private boolean equals0(final CurrencyExchangeHateosResource other) {
        return this.currencyExchange.equals(other.currencyExchange);
    }

    @Override
    public String toString() {
        return this.currencyExchange.toString();
    }

    // Comparable.......................................................................................................

    @Override
    public int compareTo(final CurrencyExchangeHateosResource other) {
        return this.currencyExchange.compareTo(other.currencyExchange);
    }

    // TreePrintable.....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        printer.println(this.currencyExchange.toString());
    }

    // json.............................................................................................................

    /**
     * Factory that creates a {@link CurrencyExchangeHateosResource} parse a {@link JsonNode}.
     */
    static CurrencyExchangeHateosResource unmarshall(final JsonNode node,
                                                     final JsonNodeUnmarshallContext context) {
        Objects.requireNonNull(node, "node");

        return with(
            context.unmarshall(
                node,
                CurrencyExchange.class
            )
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshall(this.currencyExchange);
    }

    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(CurrencyExchangeHateosResource.class),
            CurrencyExchangeHateosResource::unmarshall,
            CurrencyExchangeHateosResource::marshall,
            CurrencyExchangeHateosResource.class
        );
    }
}
