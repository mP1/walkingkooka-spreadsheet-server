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

package walkingkooka.spreadsheet.server.format;

import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

/**
 * Formats a {@link java.time.LocalDateTime} using the spreadsheet locale using the default format pattern.
 */
public final class SpreadsheetLocaleDefaultDateTimeFormat {

    /**
     * Singleton
     */
    public final static SpreadsheetLocaleDefaultDateTimeFormat INSTANCE = new SpreadsheetLocaleDefaultDateTimeFormat();

    private SpreadsheetLocaleDefaultDateTimeFormat() {
        super();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    // Json.............................................................................................................

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.nullNode();
    }

    static SpreadsheetLocaleDefaultDateTimeFormat unmarshall(final JsonNode node,
                                                             final JsonNodeUnmarshallContext context) {
        return INSTANCE;
    }

    static {
        JsonNodeContext.register("spreadsheet-locale-default-date-time-format",
                SpreadsheetLocaleDefaultDateTimeFormat::unmarshall,
                SpreadsheetLocaleDefaultDateTimeFormat::marshall,
                SpreadsheetLocaleDefaultDateTimeFormat.class);
    }
}
