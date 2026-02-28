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

import walkingkooka.InvalidTextLengthException;
import walkingkooka.Value;
import walkingkooka.compare.Comparators;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Currency;
import java.util.Objects;

/**
 * An id that uniquely identifies a {@link Currency} for a {@link walkingkooka.net.http.server.hateos.HateosResourceHandler}.
 */
public final class CurrencyCode implements Comparable<CurrencyCode>, Value<String> {

    private final static CaseSensitivity CASE_SENSITIVITY = CaseSensitivity.INSENSITIVE;

    public static CurrencyCode parse(final String text) {
        return new CurrencyCode(
            InvalidTextLengthException.throwIfFail(
                "currencyCode",
                text,
                3,
                3
            )
        );
    }

    public static CurrencyCode fromCurrency(final Currency currency) {
        return new CurrencyCode(
            Objects.requireNonNull(currency)
                .getCurrencyCode()
        );
    }

    private CurrencyCode(final String code) {
        super();
        this.code = code;
    }

    // Value............................................................................................................

    @Override
    public String value() {
        return this.code;
    }

    private final String code;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return CASE_SENSITIVITY.hash(this.code);
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
            (other instanceof CurrencyCode &&
                this.equals0((CurrencyCode) other));
    }

    private boolean equals0(final CurrencyCode other) {
        return this.compareTo(other) == Comparators.EQUAL;
    }

    @Override
    public String toString() {
        return this.code;
    }

    // Comparable.......................................................................................................

    @Override
    public int compareTo(final CurrencyCode other) {
        return CASE_SENSITIVITY.comparator()
            .compare(
                this.code,
                other.code
            );
    }

    // json.............................................................................................................

    static CurrencyCode unmarshall(final JsonNode node,
                                   final JsonNodeUnmarshallContext context) {
        return parse(
            context.unmarshall(
                node,
                String.class
            )
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshall(this.code);
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
