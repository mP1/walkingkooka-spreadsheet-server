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

import org.junit.jupiter.api.Test;
import walkingkooka.locale.HasLocale;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetLocaleDefaultDateTimeFormatTest extends SpreadsheetFormatterTestCase2<SpreadsheetLocaleDefaultDateTimeFormat> {

    @Test
    public void testFormatNullDateTimeFails() {
        assertThrows(NullPointerException.class,
                () -> this.createObject().format(null,
                        new HasLocale() {
                            @Override
                            public Locale locale() {
                                throw new UnsupportedOperationException();
                            }
                        }));
    }

    @Test
    public void testFormatNullHasLocaleFails() {
        assertThrows(NullPointerException.class,
                () -> this.createObject().format(LocalDateTime.now(),
                        null));
    }

    @Test
    public void testFormatEnAU() {
        this.formatAndCheck(LocalDateTime.of(1999, 12, 31, 12, 58, 59),
                Locale.forLanguageTag("EN-AU"),
                "31 December 1999, 12:58:59 pm");
    }

    @Test
    public void testFormatEs() {
        this.formatAndCheck(LocalDateTime.of(1999, 12, 31, 12, 58, 59),
                Locale.forLanguageTag("ES"),
                "31 de diciembre de 1999 12:58:59");
    }

    private void formatAndCheck(final LocalDateTime dateTime,
                                final Locale locale,
                                final String formatted) {
        this.checkEquals(formatted,
                this.createObject().format(dateTime, new HasLocale() {
                    @Override
                    public Locale locale() {
                        return locale;
                    }
                }),
                () -> "format " + dateTime + " with locale " + locale);
    }

    // Json..............................................................................................................

    @Override
    public void testUnmarshallNullFails() {
        throw new UnsupportedOperationException();
    }

    @Test
    public void testFromJsonNodeNull() {
        this.unmarshallAndCheck("null", SpreadsheetLocaleDefaultDateTimeFormat.INSTANCE);
    }

    @Test
    public void testToJsonNode() {
        this.marshallAndCheck(SpreadsheetLocaleDefaultDateTimeFormat.INSTANCE, JsonNode.number(1));
    }

    @Override
    public void testMarshallRoundtripList() {
    }

    @Override
    public void testMarshallRoundtripSet() {
    }

    @Override
    public void testMarshallRoundtripMap() {
    }

    // Object............................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createObject(), SpreadsheetLocaleDefaultDateTimeFormat.class.getSimpleName());
    }

    @Override
    public SpreadsheetLocaleDefaultDateTimeFormat createObject() {
        return SpreadsheetLocaleDefaultDateTimeFormat.INSTANCE;
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetLocaleDefaultDateTimeFormat> type() {
        return SpreadsheetLocaleDefaultDateTimeFormat.class;
    }

    // json.............................................................................................................

    @Override
    public SpreadsheetLocaleDefaultDateTimeFormat unmarshall(final JsonNode node,
                                                             final JsonNodeUnmarshallContext context) {
        return SpreadsheetLocaleDefaultDateTimeFormat.unmarshall(node, context);
    }
}
