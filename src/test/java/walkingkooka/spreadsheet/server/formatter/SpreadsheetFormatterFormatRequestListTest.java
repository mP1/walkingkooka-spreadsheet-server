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

package walkingkooka.spreadsheet.server.formatter;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.ImmutableListTesting;
import walkingkooka.collect.list.ListTesting2;
import walkingkooka.collect.list.Lists;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpreadsheetFormatterFormatRequestListTest implements ListTesting2<SpreadsheetFormatterFormatRequestList, SpreadsheetFormatterFormatRequest<?>>,
    ClassTesting<SpreadsheetFormatterFormatRequestList>,
    ImmutableListTesting<SpreadsheetFormatterFormatRequestList, SpreadsheetFormatterFormatRequest<?>>,
    JsonNodeMarshallingTesting<SpreadsheetFormatterFormatRequestList> {

    private final static SpreadsheetFormatterFormatRequest<?> REQUEST1 = SpreadsheetFormatterFormatRequest.with(
        SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("dd/mm/yyyy"),
        LocalDate.of(1999, 12, 31)
    );

    private final static SpreadsheetFormatterFormatRequest<?> REQUEST2 = SpreadsheetFormatterFormatRequest.with(
        SpreadsheetFormatterName.TEXT_FORMAT_PATTERN.setValueText("@@"),
        "Hello"
    );

    @Test
    public void testWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetFormatterFormatRequestList.with(null)
        );
    }

    @Test
    public void testDoesntDoubleWrap() {
        final SpreadsheetFormatterFormatRequestList list = this.createList();
        assertSame(
            list,
            SpreadsheetFormatterFormatRequestList.with(list)
        );
    }

    @Test
    public void testGet() {
        this.getAndCheck(
            this.createList(),
            0, // index
            REQUEST1 // expected
        );
    }

    @Test
    public void testGet2() {
        this.getAndCheck(
            this.createList(),
            1, // index
            REQUEST2 // expected
        );
    }

    @Test
    public void testSetFails() {
        this.setFails(
            this.createList(),
            0, // index
            REQUEST1 // expected
        );
    }

    @Test
    public void testRemoveIndexFails() {
        final SpreadsheetFormatterFormatRequestList list = this.createList();

        this.removeIndexFails(
            list,
            0
        );
    }

    @Test
    public void testRemoveElementFails() {
        final SpreadsheetFormatterFormatRequestList list = this.createList();

        this.removeFails(
            list,
            list.get(0)
        );
    }

    @Override
    public SpreadsheetFormatterFormatRequestList createList() {
        return SpreadsheetFormatterFormatRequestList.with(
            Lists.of(
                REQUEST1,
                REQUEST2
            )
        );
    }

    @Override
    public Class<SpreadsheetFormatterFormatRequestList> type() {
        return SpreadsheetFormatterFormatRequestList.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }

    // Json...........................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
            this.createList(),
            "[\n" +
                "  {\n" +
                "    \"selector\": \"date-format-pattern dd/mm/yyyy\",\n" +
                "    \"value\": {\n" +
                "      \"type\": \"local-date\",\n" +
                "      \"value\": \"1999-12-31\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"selector\": \"text-format-pattern @@\",\n" +
                "    \"value\": \"Hello\"\n" +
                "  }\n" +
                "]"
        );
    }

    @Test
    public void testUnmarshall() {
        this.unmarshallAndCheck(
            "[\n" +
                "  {\n" +
                "    \"selector\": \"date-format-pattern dd/mm/yyyy\",\n" +
                "    \"value\": {\n" +
                "      \"type\": \"local-date\",\n" +
                "      \"value\": \"1999-12-31\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"selector\": \"text-format-pattern @@\",\n" +
                "    \"value\": \"Hello\"\n" +
                "  }\n" +
                "]",
            this.createList()
        );
    }

    @Override
    public SpreadsheetFormatterFormatRequestList unmarshall(final JsonNode json,
                                                            final JsonNodeUnmarshallContext context) {
        return SpreadsheetFormatterFormatRequestList.unmarshall(
            json,
            context
        );
    }

    @Override
    public SpreadsheetFormatterFormatRequestList createJsonNodeMarshallingValue() {
        return this.createList();
    }
}
