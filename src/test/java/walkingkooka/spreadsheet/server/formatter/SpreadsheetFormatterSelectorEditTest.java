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
import walkingkooka.collect.list.Lists;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelectorToken;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelectorTokenAlternative;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.test.ParseStringTesting;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetFormatterSelectorEditTest implements ParseStringTesting<SpreadsheetFormatterSelectorEdit>,
    TreePrintableTesting,
    JsonNodeMarshallingTesting<SpreadsheetFormatterSelectorEdit>,
    SpreadsheetMetadataTesting,
    ClassTesting<SpreadsheetFormatterSelectorEdit> {

    @Test
    public void testParseWithNullContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetFormatterSelectorEdit.parse(
                "",
                null
            )
        );
    }

    @Override
    public void testParseStringEmptyFails() {
        throw new UnsupportedOperationException();
    }

    @Test
    public void testParseStringEmpty() {
        this.parseStringAndCheck(
            "",
            SpreadsheetFormatterSelectorEdit.with(
                Optional.empty(), // selector
                "Empty \"text\"",
                Lists.empty(),
                Optional.empty(),
                Lists.empty()
            )
        );
    }

    @Test
    public void testParseInvalidSpreadsheetFormatterName() {
        this.parseStringAndCheck(
            "123",
            SpreadsheetFormatterSelectorEdit.with(
                Optional.empty(),
                "Invalid character '1' at 0",
                Lists.empty(),
                Optional.empty(),
                Lists.empty()
            )
        );
    }

    @Test
    public void testParseOnlySpreadsheetFormatterName() {
        final String text = "";

        this.parseStringAndCheck(
            SpreadsheetFormatterName.DATE.value(),
            SpreadsheetFormatterSelectorEdit.with(
                Optional.of(
                    SpreadsheetFormatterName.DATE.setValueText(text)
                ),
                "Empty \"text\"",
                Lists.empty(),
                Optional.empty(),
                dateFormatSamples("")
            )
        );
    }

    @Test
    public void testParseSpreadsheetFormatterNameInvalidPattern() {
        final String selector = SpreadsheetFormatterName.DATE + " !";
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> SpreadsheetFormatterSelector.parse(selector)
                .spreadsheetFormatPattern()
        );

        this.parseStringAndCheck(
            selector,
            SpreadsheetFormatterSelectorEdit.with(
                Optional.of(SpreadsheetFormatterName.DATE.setValueText("!")),
                thrown.getMessage(),
                Lists.empty(),
                Optional.empty(),
                dateFormatSamples("!")
            )
        );
    }

    @Test
    public void testParse() {
        final String text = "yyyy";

        this.parseStringAndCheck(
            SpreadsheetFormatterName.DATE + " " + text,
            SpreadsheetFormatterSelectorEdit.with(
                Optional.of(SpreadsheetFormatterName.DATE.setValueText(text)),
                "",
                Lists.of(
                    SpreadsheetFormatterSelectorToken.with(
                        "yyyy",
                        "yyyy",
                        Lists.of(
                            SpreadsheetFormatterSelectorTokenAlternative.with(
                                "yy",
                                "yy"
                            )
                        )
                    )
                ),
                Optional.of(
                    SpreadsheetFormatterSelectorToken.with(
                        "",
                        "",
                        Lists.of(
                            SpreadsheetFormatterSelectorTokenAlternative.with(
                                "d",
                                "d"
                            ),
                            SpreadsheetFormatterSelectorTokenAlternative.with(
                                "dd",
                                "dd"
                            ),
                            SpreadsheetFormatterSelectorTokenAlternative.with(
                                "ddd",
                                "ddd"
                            ),
                            SpreadsheetFormatterSelectorTokenAlternative.with(
                                "dddd",
                                "dddd"
                            ),
                            SpreadsheetFormatterSelectorTokenAlternative.with(
                                "m",
                                "m"
                            ),
                            SpreadsheetFormatterSelectorTokenAlternative.with(
                                "mm",
                                "mm"
                            ),
                            SpreadsheetFormatterSelectorTokenAlternative.with(
                                "mmm",
                                "mmm"
                            ),
                            SpreadsheetFormatterSelectorTokenAlternative.with(
                                "mmmm",
                                "mmmm"
                            ),
                            SpreadsheetFormatterSelectorTokenAlternative.with(
                                "mmmmm",
                                "mmmmm"
                            )
                        )
                    )
                ),
                dateFormatSamples(text)
            )
        );
    }

    private static List<SpreadsheetFormatterSample> dateFormatSamples(final String text) {
        return SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatterSamples(
            SpreadsheetFormatterName.DATE.setValueText(text),
            SpreadsheetFormatterProvider.INCLUDE_SAMPLES,
            SPREADSHEET_FORMATTER_PROVIDER_SAMPLES_CONTEXT
        );
    }

    @Override
    public SpreadsheetFormatterSelectorEdit parseString(final String selector) {
        return SpreadsheetFormatterSelectorEdit.parse(
            selector,
            SpreadsheetFormatterSelectorEditContexts.basic(
                SPREADSHEET_FORMATTER_CONTEXT,
                SPREADSHEET_FORMATTER_PROVIDER,
                PROVIDER_CONTEXT
            )
        );
    }

    @Override
    public Class<? extends RuntimeException> parseStringFailedExpected(final Class<? extends RuntimeException> thrown) {
        return thrown;
    }

    @Override
    public RuntimeException parseStringFailedExpected(final RuntimeException thrown) {
        return thrown;
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrint() {
        this.treePrintAndCheck(
            this.parseString(SpreadsheetFormatterName.DATE + " yyyy/mm/dd"),
            "SpreadsheetFormatterSelectorEdit\n" +
                "  selector\n" +
                "    date\n" +
                "      \"yyyy/mm/dd\"\n" +
                "  text-components\n" +
                "    yyyy\n" +
                "    yyyy\n" +
                "      yy\n" +
                "      yy\n" +
                "    /\n" +
                "    /\n" +
                "    mm\n" +
                "    mm\n" +
                "      m\n" +
                "      m\n" +
                "      mmm\n" +
                "      mmm\n" +
                "      mmmm\n" +
                "      mmmm\n" +
                "      mmmmm\n" +
                "      mmmmm\n" +
                "    /\n" +
                "    /\n" +
                "    dd\n" +
                "    dd\n" +
                "      d\n" +
                "      d\n" +
                "      ddd\n" +
                "      ddd\n" +
                "      dddd\n" +
                "      dddd\n" +
                "  next\n" +
                "    \n" +
                "    \n" +
                "      m\n" +
                "      m\n" +
                "      mm\n" +
                "      mm\n" +
                "      mmm\n" +
                "      mmm\n" +
                "      mmmm\n" +
                "      mmmm\n" +
                "      mmmmm\n" +
                "      mmmmm\n" +
                "      yy\n" +
                "      yy\n" +
                "      yyyy\n" +
                "      yyyy\n" +
                "  samples\n" +
                "    Short\n" +
                "      date\n" +
                "        \"d/m/yy\"\n" +
                "      Text \"31/12/99\"\n" +
                "    Medium\n" +
                "      date\n" +
                "        \"d mmm yyyy\"\n" +
                "      Text \"31 Dec. 1999\"\n" +
                "    Long\n" +
                "      date\n" +
                "        \"d mmmm yyyy\"\n" +
                "      Text \"31 December 1999\"\n" +
                "    Full\n" +
                "      date\n" +
                "        \"dddd, d mmmm yyyy\"\n" +
                "      Text \"Friday, 31 December 1999\"\n" +
                "    Sample\n" +
                "      date\n" +
                "        \"yyyy/mm/dd\"\n" +
                "      Text \"1999/12/31\"\n"
        );
    }

    // json............................................................................................................

    @Test
    public void testMarshallWithoutMessage() {
        this.marshallAndCheck(
            this.createJsonNodeMarshallingValue(),
            "{\n" +
                "  \"selector\": \"date dd/mm/yyyy\",\n" +
                "  \"tokens\": [\n" +
                "    {\n" +
                "      \"label\": \"dd\",\n" +
                "      \"text\": \"dd\",\n" +
                "      \"alternatives\": [\n" +
                "        {\n" +
                "          \"label\": \"d\",\n" +
                "          \"text\": \"d\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"label\": \"ddd\",\n" +
                "          \"text\": \"ddd\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"label\": \"dddd\",\n" +
                "          \"text\": \"dddd\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"/\",\n" +
                "      \"text\": \"/\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"mm\",\n" +
                "      \"text\": \"mm\",\n" +
                "      \"alternatives\": [\n" +
                "        {\n" +
                "          \"label\": \"m\",\n" +
                "          \"text\": \"m\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"label\": \"mmm\",\n" +
                "          \"text\": \"mmm\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"label\": \"mmmm\",\n" +
                "          \"text\": \"mmmm\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"label\": \"mmmmm\",\n" +
                "          \"text\": \"mmmmm\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"/\",\n" +
                "      \"text\": \"/\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"yyyy\",\n" +
                "      \"text\": \"yyyy\",\n" +
                "      \"alternatives\": [\n" +
                "        {\n" +
                "          \"label\": \"yy\",\n" +
                "          \"text\": \"yy\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"next\": {\n" +
                "    \"alternatives\": [\n" +
                "      {\n" +
                "        \"label\": \"d\",\n" +
                "        \"text\": \"d\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"dd\",\n" +
                "        \"text\": \"dd\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"ddd\",\n" +
                "        \"text\": \"ddd\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"dddd\",\n" +
                "        \"text\": \"dddd\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"m\",\n" +
                "        \"text\": \"m\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"mm\",\n" +
                "        \"text\": \"mm\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"mmm\",\n" +
                "        \"text\": \"mmm\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"mmmm\",\n" +
                "        \"text\": \"mmmm\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"mmmmm\",\n" +
                "        \"text\": \"mmmmm\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"samples\": [\n" +
                "    {\n" +
                "      \"label\": \"Short\",\n" +
                "      \"selector\": \"date d/m/yy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31/12/99\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Medium\",\n" +
                "      \"selector\": \"date d mmm yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31 Dec. 1999\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Long\",\n" +
                "      \"selector\": \"date d mmmm yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31 December 1999\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Full\",\n" +
                "      \"selector\": \"date dddd, d mmmm yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Friday, 31 December 1999\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Sample\",\n" +
                "      \"selector\": \"date dd/mm/yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31/12/1999\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}"
        );
    }

    @Test
    public void testMarshallOnlyMessage() {
        this.marshallAndCheck(
            this.parseString("!invalid"),
            "{\n" +
                "  \"message\": \"Invalid character '!' at 0\"\n" +
                "}"
        );
    }

    @Test
    public void testUnmarshall() {
        this.unmarshallAndCheck(
            "{\n" +
                "  \"selector\": \"date dd/mm/yyyy\",\n" +
                "  \"message\": \"\",\n" +
                "  \"tokens\": [\n" +
                "    {\n" +
                "      \"label\": \"dd\",\n" +
                "      \"text\": \"dd\",\n" +
                "      \"alternatives\": [\n" +
                "        {\n" +
                "          \"label\": \"d\",\n" +
                "          \"text\": \"d\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"label\": \"ddd\",\n" +
                "          \"text\": \"ddd\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"label\": \"dddd\",\n" +
                "          \"text\": \"dddd\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"/\",\n" +
                "      \"text\": \"/\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"mm\",\n" +
                "      \"text\": \"mm\",\n" +
                "      \"alternatives\": [\n" +
                "        {\n" +
                "          \"label\": \"m\",\n" +
                "          \"text\": \"m\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"label\": \"mmm\",\n" +
                "          \"text\": \"mmm\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"label\": \"mmmm\",\n" +
                "          \"text\": \"mmmm\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"label\": \"mmmmm\",\n" +
                "          \"text\": \"mmmmm\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"/\",\n" +
                "      \"text\": \"/\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"yyyy\",\n" +
                "      \"text\": \"yyyy\",\n" +
                "      \"alternatives\": [\n" +
                "        {\n" +
                "          \"label\": \"yy\",\n" +
                "          \"text\": \"yy\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"next\": {\n" +
                "    \"alternatives\": [\n" +
                "      {\n" +
                "        \"label\": \"d\",\n" +
                "        \"text\": \"d\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"dd\",\n" +
                "        \"text\": \"dd\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"ddd\",\n" +
                "        \"text\": \"ddd\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"dddd\",\n" +
                "        \"text\": \"dddd\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"m\",\n" +
                "        \"text\": \"m\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"mm\",\n" +
                "        \"text\": \"mm\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"mmm\",\n" +
                "        \"text\": \"mmm\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"mmmm\",\n" +
                "        \"text\": \"mmmm\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"label\": \"mmmmm\",\n" +
                "        \"text\": \"mmmmm\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"samples\": [\n" +
                "    {\n" +
                "      \"label\": \"Short\",\n" +
                "      \"selector\": \"date d/m/yy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31/12/99\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Medium\",\n" +
                "      \"selector\": \"date d mmm yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31 Dec. 1999\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Long\",\n" +
                "      \"selector\": \"date d mmmm yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31 December 1999\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Full\",\n" +
                "      \"selector\": \"date dddd, d mmmm yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Friday, 31 December 1999\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Sample\",\n" +
                "      \"selector\": \"date dd/mm/yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31/12/1999\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}",
            this.createJsonNodeMarshallingValue()
        );
    }

    @Test
    public void testUnmarshallOnlyMessage() {
        this.unmarshallAndCheck(
            "{\n" +
                "  \"message\": \"Invalid character '!' at 0\"\n" +
                "}",
            this.parseString("!invalid")
        );
    }

    @Override
    public SpreadsheetFormatterSelectorEdit unmarshall(final JsonNode json,
                                                       final JsonNodeUnmarshallContext context) {
        return SpreadsheetFormatterSelectorEdit.unmarshall(
            json,
            context
        );
    }

    @Override
    public SpreadsheetFormatterSelectorEdit createJsonNodeMarshallingValue() {
        return this.parseString(SpreadsheetFormatterName.DATE + " dd/mm/yyyy");
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetFormatterSelectorEdit> type() {
        return SpreadsheetFormatterSelectorEdit.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}