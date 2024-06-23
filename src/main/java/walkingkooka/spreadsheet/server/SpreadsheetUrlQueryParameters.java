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
        final List<String> counts = (List<String>) parameters.get(COUNT);
        if (null == counts) {
            throw new IllegalArgumentException("Missing count parameter");
        }
        switch (counts.size()) {
            case 0:
                throw new IllegalArgumentException("Missing count parameter");
            default:
                final String countString = counts.get(0);
                try {
                    return Integer.parseInt(countString);
                } catch (final NumberFormatException cause) {
                    throw new IllegalArgumentException("Invalid count parameter got " + CharSequences.quoteAndEscape(countString));
                }
        }
    }

    // @VisibleForTesting
    public final static UrlParameterName COUNT = UrlParameterName.with("count");

    private SpreadsheetUrlQueryParameters() {
        throw new UnsupportedOperationException();
    }
}
