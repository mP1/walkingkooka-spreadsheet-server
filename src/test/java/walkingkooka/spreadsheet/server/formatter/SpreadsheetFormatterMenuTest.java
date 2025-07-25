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
import walkingkooka.convert.ConverterContext;
import walkingkooka.plugin.PluginSelectorMenuLikeTesting;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContextDelegator;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderSamplesContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetFormatterMenuTest implements PluginSelectorMenuLikeTesting<SpreadsheetFormatterMenu, SpreadsheetFormatterSelector, SpreadsheetFormatterName>,
    JsonNodeMarshallingTesting<SpreadsheetFormatterMenu>,
    SpreadsheetMetadataTesting {

    @Override
    public SpreadsheetFormatterMenu createPluginSelectorMenu(final String label,
                                                             final SpreadsheetFormatterSelector selector) {
        return SpreadsheetFormatterMenu.with(
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
            () -> SpreadsheetFormatterMenu.prepare(
                null
            )
        );
    }

    @Test
    public void testPrepare() {
        this.checkEquals(
            Lists.of(
                SpreadsheetFormatterMenu.with(
                    "Short",
                    SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("d/m/yy")
                ),
                SpreadsheetFormatterMenu.with(
                    "Medium",
                    SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("d mmm yyyy")
                ),
                SpreadsheetFormatterMenu.with(
                    "Long",
                    SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("d mmmm yyyy")
                ),
                SpreadsheetFormatterMenu.with(
                    "Full",
                    SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("dddd, d mmmm yyyy")
                ),
                SpreadsheetFormatterMenu.with(
                    "Short",
                    SpreadsheetFormatterName.DATE_TIME_FORMAT_PATTERN.setValueText("d/m/yy, h:mm AM/PM")
                ),
                SpreadsheetFormatterMenu.with(
                    "Medium",
                    SpreadsheetFormatterName.DATE_TIME_FORMAT_PATTERN.setValueText("d mmm yyyy, h:mm:ss AM/PM")
                ),
                SpreadsheetFormatterMenu.with(
                    "Long",
                    SpreadsheetFormatterName.DATE_TIME_FORMAT_PATTERN.setValueText("d mmmm yyyy \\a\\t h:mm:ss AM/PM")
                ),
                SpreadsheetFormatterMenu.with(
                    "Full",
                    SpreadsheetFormatterName.DATE_TIME_FORMAT_PATTERN.setValueText("dddd, d mmmm yyyy \\a\\t h:mm:ss AM/PM")
                ),
                SpreadsheetFormatterMenu.with(
                    "Default",
                    SpreadsheetFormatterName.TEXT_FORMAT_PATTERN.setValueText("@")
                ),
                SpreadsheetFormatterMenu.with(
                    "General",
                    SpreadsheetFormatterName.GENERAL.setValueText("")
                ),
                SpreadsheetFormatterMenu.with(
                    "Number",
                    SpreadsheetFormatterName.NUMBER_FORMAT_PATTERN.setValueText("\"#,##0.###\"")
                ),
                SpreadsheetFormatterMenu.with(
                    "Integer",
                    SpreadsheetFormatterName.NUMBER_FORMAT_PATTERN.setValueText("#,##0")
                ),
                SpreadsheetFormatterMenu.with(
                    "Percent",
                    SpreadsheetFormatterName.NUMBER_FORMAT_PATTERN.setValueText("#,##0%")
                ),
                SpreadsheetFormatterMenu.with(
                    "Currency",
                    SpreadsheetFormatterName.NUMBER_FORMAT_PATTERN.setValueText("$#,##0.00")
                ),
                SpreadsheetFormatterMenu.with(
                    "Default",
                    SpreadsheetFormatterName.TEXT_FORMAT_PATTERN.setValueText("@")
                ),
                SpreadsheetFormatterMenu.with(
                    "Short",
                    SpreadsheetFormatterName.TIME_FORMAT_PATTERN.setValueText("h:mm AM/PM")
                ),
                SpreadsheetFormatterMenu.with(
                    "Long",
                    SpreadsheetFormatterName.TIME_FORMAT_PATTERN.setValueText("h:mm:ss AM/PM")
                )
            ),
            SpreadsheetFormatterMenu.prepare(
                new TestSpreadsheetFormatterMenuContext()
            ),
            "prepare"
        );
    }

    static class TestSpreadsheetFormatterMenuContext implements SpreadsheetFormatterMenuContext,
        SpreadsheetFormatterProviderDelegator,
        SpreadsheetFormatterContextDelegator,
        ProviderContextDelegator {

        @Override
        public SpreadsheetFormatterProviderSamplesContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetFormatterContext spreadsheetFormatterContext() {
            return SPREADSHEET_FORMATTER_CONTEXT;
        }

        @Override
        public SpreadsheetFormatterProvider spreadsheetFormatterProvider() {
            return SPREADSHEET_FORMATTER_PROVIDER;
        }

        @Override
        public ProviderContext providerContext() {
            return PROVIDER_CONTEXT;
        }

        @Override
        public ConverterContext canConvert() {
            return SPREADSHEET_FORMATTER_CONTEXT;
        }

        @Override
        public LocalDateTime now() {
            return SPREADSHEET_FORMATTER_CONTEXT.now();
        }
    }

    // json.............................................................................................................

    @Override
    public SpreadsheetFormatterMenu unmarshall(final JsonNode json,
                                               final JsonNodeUnmarshallContext context) {
        return SpreadsheetFormatterMenu.unmarshall(
            json,
            context
        );
    }

    @Override
    public SpreadsheetFormatterMenu createJsonNodeMarshallingValue() {
        return SpreadsheetFormatterMenu.with(
            "Label123",
            this.createPluginSelector()
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetFormatterMenu> type() {
        return SpreadsheetFormatterMenu.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
