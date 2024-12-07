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

package walkingkooka.spreadsheet.server;

import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.text.CharSequences;

import java.util.List;
import java.util.Map;

/**
 * A collection of common query parameters used by various APIs
 */
public final class SpreadsheetUrlQueryParameters implements PublicStaticHelper {

    /**
     * Returns the count parameter as an integer.
     */
    public static int count(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return get(
                COUNT,
                parameters
        );
    }

    // @VisibleForTesting
    public final static UrlParameterName COUNT = UrlParameterName.with("count");

    /**
     * Returns the from parameter as an integer, failing if it is missing.
     */
    public static int from(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return get(
                FROM,
                parameters
        );
    }

    // @VisibleForTesting
    public final static UrlParameterName FROM = UrlParameterName.with("from");

    private static int get(final UrlParameterName parameter,
                           final Map<HttpRequestAttribute<?>, Object> parameters) {
        final List<String> values = (List<String>) parameters.get(parameter);
        if (null == values) {
            throw new IllegalArgumentException("Missing " + parameter + " parameter");
        }
        switch (values.size()) {
            case 0:
                throw new IllegalArgumentException("Missing " + parameter + " parameter");
            default:
                final String valueAsString = values.get(0);
                try {
                    return Integer.parseInt(valueAsString);
                } catch (final NumberFormatException cause) {
                    throw new IllegalArgumentException("Invalid " + parameter + " parameter got " + CharSequences.quoteAndEscape(valueAsString));
                }
        }
    }

    private SpreadsheetUrlQueryParameters() {
        throw new UnsupportedOperationException();
    }
}
