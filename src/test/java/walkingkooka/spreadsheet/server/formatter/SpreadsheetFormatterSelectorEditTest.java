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
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorToken;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorTokenAlternative;
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
            "1",
            SpreadsheetFormatterSelectorEdit.with(
                Optional.empty(),
                "Invalid character '1' at 0",
                Lists.empty(),
                Optional.empty(),
                Lists.empty()
            )
        );
    }

    private final static List<SpreadsheetFormatterSample> DATE_FORMAT_SAMPLES = SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatterSamples(
        SpreadsheetFormatterName.DATE_FORMAT_PATTERN,
        SPREADSHEET_FORMATTER_PROVIDER_SAMPLES_CONTEXT
    );

    @Test
    public void testParseOnlySpreadsheetFormatterName() {
        this.parseStringAndCheck(
            SpreadsheetFormatterName.DATE_FORMAT_PATTERN.value(),
            SpreadsheetFormatterSelectorEdit.with(
                Optional.of(SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("")),
                "Empty \"text\"",
                Lists.empty(),
                Optional.empty(),
                DATE_FORMAT_SAMPLES
            )
        );
    }

    @Test
    public void testParseOnlySpreadsheetFormatterNameSpaceMissingPattern() {
        this.parseStringAndCheck(
            SpreadsheetFormatterName.DATE_FORMAT_PATTERN + " ",
            SpreadsheetFormatterSelectorEdit.with(
                Optional.of(SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("")),
                "Empty \"text\"",
                Lists.empty(),
                Optional.empty(),
                DATE_FORMAT_SAMPLES
            )
        );
    }

    @Test
    public void testParseSpreadsheetFormatterNameInvalidPattern() {
        final String selector = SpreadsheetFormatterName.DATE_FORMAT_PATTERN + " !";

        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> SpreadsheetFormatterSelector.parse(selector)
                .spreadsheetFormatPattern()
        );

        this.parseStringAndCheck(
            selector,
            SpreadsheetFormatterSelectorEdit.with(
                Optional.of(SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("!")),
                thrown.getMessage(),
                Lists.empty(),
                Optional.empty(),
                DATE_FORMAT_SAMPLES
            )
        );
    }

    @Test
    public void testParse() {
        this.parseStringAndCheck(
            SpreadsheetFormatterName.DATE_FORMAT_PATTERN + " yyyy",
            SpreadsheetFormatterSelectorEdit.with(
                Optional.of(SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("yyyy")),
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
                DATE_FORMAT_SAMPLES
            )
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
            this.parseString(SpreadsheetFormatterName.DATE_FORMAT_PATTERN + " yyyy/mm/dd"),
            "SpreadsheetFormatterSelectorEdit\n" +
                "  selector\n" +
                "    date-format-pattern\n" +
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
                "      date-format-pattern\n" +
                "        \"d/m/yy\"\n" +
                "      Text \"31/12/99\"\n" +
                "    Medium\n" +
                "      date-format-pattern\n" +
                "        \"d mmm yyyy\"\n" +
                "      Text \"31 Dec. 1999\"\n" +
                "    Long\n" +
                "      date-format-pattern\n" +
                "        \"d mmmm yyyy\"\n" +
                "      Text \"31 December 1999\"\n" +
                "    Full\n" +
                "      date-format-pattern\n" +
                "        \"dddd, d mmmm yyyy\"\n" +
                "      Text \"Friday, 31 December 1999\"\n"
        );
    }

    // json............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
            this.createJsonNodeMarshallingValue(),
            "{\n" +
                "  \"selector\": \"date-format-pattern dd/mm/yyyy\",\n" +
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
                "      \"selector\": \"date-format-pattern d/m/yy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31/12/99\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Medium\",\n" +
                "      \"selector\": \"date-format-pattern d mmm yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31 Dec. 1999\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Long\",\n" +
                "      \"selector\": \"date-format-pattern d mmmm yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31 December 1999\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Full\",\n" +
                "      \"selector\": \"date-format-pattern dddd, d mmmm yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Friday, 31 December 1999\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}"
        );
    }

    @Test
    public void testUnmarshall() {
        this.unmarshallAndCheck(
            "{\n" +
                "  \"selector\": \"date-format-pattern dd/mm/yyyy\",\n" +
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
                "      \"selector\": \"date-format-pattern d/m/yy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31/12/99\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Medium\",\n" +
                "      \"selector\": \"date-format-pattern d mmm yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31 Dec. 1999\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Long\",\n" +
                "      \"selector\": \"date-format-pattern d mmmm yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"31 December 1999\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"Full\",\n" +
                "      \"selector\": \"date-format-pattern dddd, d mmmm yyyy\",\n" +
                "      \"value\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Friday, 31 December 1999\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}",
            this.createJsonNodeMarshallingValue()
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
        return this.parseString(SpreadsheetFormatterName.DATE_FORMAT_PATTERN + " dd/mm/yyyy");
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
