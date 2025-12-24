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

package walkingkooka.spreadsheet.server.net;

import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.text.CharSequences;

import java.util.Map;
import java.util.OptionalInt;

/**
 * A collection of common query parameters used by various APIs
 */
public final class SpreadsheetUrlQueryParameters implements PublicStaticHelper {

    /**
     * Returns the count parameter as an integer.
     */
    public static OptionalInt count(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return get(
            COUNT,
            parameters
        );
    }

    // @VisibleForTesting
    public final static UrlParameterName COUNT = UrlParameterName.with("count");

    /**
     * Returns the offset parameter as an integer.
     */
    public static OptionalInt offset(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return get(
            OFFSET,
            parameters
        );
    }

    // @VisibleForTesting
    public final static UrlParameterName OFFSET = UrlParameterName.with("offset");

    private static OptionalInt get(final UrlParameterName parameter,
                                   final Map<HttpRequestAttribute<?>, Object> parameters) {
        return parameter.firstParameterValue(parameters)
            .map(s -> parseInt(s, parameter))
            .orElse(OptionalInt.empty());
    }

    private static OptionalInt parseInt(final String text,
                                        final UrlParameterName parameter) {
        try {
            return OptionalInt.of(
                Integer.parseInt(text)
            );
        } catch (final NumberFormatException cause) {
            throw new IllegalArgumentException("Invalid " + parameter + " parameter got " + CharSequences.quoteAndEscape(text));
        }
    }

    public final static UrlParameterName QUERY = UrlParameterName.with("query");

    private SpreadsheetUrlQueryParameters() {
        throw new UnsupportedOperationException();
    }
}
