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
import walkingkooka.plugin.PluginSelectorMenuLikeTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetFormatterSelectorMenuTest implements PluginSelectorMenuLikeTesting<SpreadsheetFormatterSelectorMenu, SpreadsheetFormatterSelector, SpreadsheetFormatterName>,
        JsonNodeMarshallingTesting<SpreadsheetFormatterSelectorMenu>,
        SpreadsheetMetadataTesting {

    @Override
    public SpreadsheetFormatterSelectorMenu createPluginSelectorMenu(final String label,
                                                                     final SpreadsheetFormatterSelector selector) {
        return SpreadsheetFormatterSelectorMenu.with(
                label,
                selector
        );
    }

    @Override
    public SpreadsheetFormatterSelector createPluginSelector() {
        return SpreadsheetFormatterSelector.DEFAULT_TEXT_FORMAT;
    }

    @Override
    public SpreadsheetFormatterSelector createDifferentPluginSelector() {
        return SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("yyyy/mm");
    }

    // prepare..........................................................................................................

    @Test
    public void testPrepareWithContextFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetFormatterSelectorMenu.prepare(
                        null
                )
        );
    }

    @Test
    public void testPrepare() {
        this.checkEquals(
                Lists.of(
                        SpreadsheetFormatterSelectorMenu.with(
                                "Short",
                                SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("d/m/yy")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Medium",
                                SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("d mmm yyyy")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Long",
                                SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("d mmmm yyyy")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Full",
                                SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("dddd, d mmmm yyyy")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Short",
                                SpreadsheetFormatterName.DATE_TIME_FORMAT_PATTERN.setValueText("d/m/yy, h:mm AM/PM")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Medium",
                                SpreadsheetFormatterName.DATE_TIME_FORMAT_PATTERN.setValueText("d mmm yyyy, h:mm:ss AM/PM")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Long",
                                SpreadsheetFormatterName.DATE_TIME_FORMAT_PATTERN.setValueText("d mmmm yyyy \\a\\t h:mm:ss AM/PM")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Full",
                                SpreadsheetFormatterName.DATE_TIME_FORMAT_PATTERN.setValueText("dddd, d mmmm yyyy \\a\\t h:mm:ss AM/PM")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "General",
                                SpreadsheetFormatterName.GENERAL.setValueText("")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Number",
                                SpreadsheetFormatterName.NUMBER_FORMAT_PATTERN.setValueText("\"#,##0.###\"")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Integer",
                                SpreadsheetFormatterName.NUMBER_FORMAT_PATTERN.setValueText("#,##0")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Percent",
                                SpreadsheetFormatterName.NUMBER_FORMAT_PATTERN.setValueText("#,##0%")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Currency",
                                SpreadsheetFormatterName.NUMBER_FORMAT_PATTERN.setValueText("$#,##0.00")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Default",
                                SpreadsheetFormatterName.TEXT_FORMAT_PATTERN.setValueText("@")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Short",
                                SpreadsheetFormatterName.TIME_FORMAT_PATTERN.setValueText("h:mm AM/PM")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Long",
                                SpreadsheetFormatterName.TIME_FORMAT_PATTERN.setValueText("h:mm:ss AM/PM")
                        )
                ),
                SpreadsheetFormatterSelectorMenu.prepare(
                        new TestSpreadsheetFormatterSelectorMenuContext()
                ),
                "prepare"
        );
    }

    static class TestSpreadsheetFormatterSelectorMenuContext implements SpreadsheetFormatterSelectorMenuContext,
            SpreadsheetFormatterProviderDelegator,
            SpreadsheetFormatterContextDelegator {
        @Override
        public SpreadsheetFormatterContext spreadsheetFormatterContext() {
            return SPREADSHEET_FORMATTER_CONTEXT;
        }

        @Override
        public SpreadsheetFormatterProvider spreadsheetFormatterProvider() {
            return SPREADSHEET_FORMATTER_PROVIDER;
        }
    }

    // json.............................................................................................................

    @Override
    public SpreadsheetFormatterSelectorMenu unmarshall(final JsonNode json,
                                                       final JsonNodeUnmarshallContext context) {
        return SpreadsheetFormatterSelectorMenu.unmarshall(
                json,
                context
        );
    }

    @Override
    public SpreadsheetFormatterSelectorMenu createJsonNodeMarshallingValue() {
        return SpreadsheetFormatterSelectorMenu.with(
                "Label123",
                this.createPluginSelector()
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetFormatterSelectorMenu> type() {
        return SpreadsheetFormatterSelectorMenu.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
