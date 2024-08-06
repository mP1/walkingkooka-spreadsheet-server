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

import walkingkooka.plugin.PluginSelectorMenuLikeTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

public final class SpreadsheetFormatterSelectorMenuTest implements PluginSelectorMenuLikeTesting<SpreadsheetFormatterSelectorMenu, SpreadsheetFormatterSelector, SpreadsheetFormatterName>,
        JsonNodeMarshallingTesting<SpreadsheetFormatterSelectorMenu> {

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
        return SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setText("yyyy/mm");
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
