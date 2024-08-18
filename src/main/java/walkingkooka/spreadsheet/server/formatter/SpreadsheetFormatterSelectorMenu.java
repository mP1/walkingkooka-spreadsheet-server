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

import walkingkooka.collect.list.Lists;
import walkingkooka.plugin.PluginSelectorMenu;
import walkingkooka.plugin.PluginSelectorMenuLike;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a single menu item for a {@link SpreadsheetFormatterSelector}. A {@link walkingkooka.spreadsheet.format.SpreadsheetFormatterName#DATE_FORMAT_PATTERN},
 * might include three items one for SHORT, MEDIUM and LONG patterns.
 */
public final class SpreadsheetFormatterSelectorMenu implements PluginSelectorMenuLike<SpreadsheetFormatterSelector, SpreadsheetFormatterName> {

    /**
     * Prepares a flat list of {@link SpreadsheetFormatterSelectorMenu} for all {@link SpreadsheetFormatterSelector}.
     */
    public static SpreadsheetFormatterSelectorMenuList prepare(final SpreadsheetFormatterSelectorMenuContext context) {
        Objects.requireNonNull(context, "context");

        return SpreadsheetFormatterSelectorMenuList.with(
                context.spreadsheetFormatterInfos()
                        .stream()
                        .flatMap(i -> menus(i, context).stream())
                        .collect(Collectors.toList())
        );
    }

    private static List<SpreadsheetFormatterSelectorMenu> menus(final SpreadsheetFormatterInfo info,
                                                                final SpreadsheetFormatterSelectorMenuContext context) {
        List<SpreadsheetFormatterSelectorMenu> menus;

        try {
            menus = context.spreadsheetFormatterSamples(
                            info.name(),
                            context
                    ).stream()
                    .map(s -> SpreadsheetFormatterSelectorMenu.with(s.label(), s.selector()))
                    .distinct()
                    .collect(Collectors.toList());
        } catch (final RuntimeException ignore) {
            // ignore failed samples
            menus = Lists.empty();
        }

        return menus;
    }

    static SpreadsheetFormatterSelectorMenu with(final String label,
                                                 final SpreadsheetFormatterSelector selector) {
        return new SpreadsheetFormatterSelectorMenu(
                PluginSelectorMenu.with(
                        label,
                        selector
                )
        );
    }

    private SpreadsheetFormatterSelectorMenu(final PluginSelectorMenu<SpreadsheetFormatterSelector, SpreadsheetFormatterName> menu) {
        this.menu = Objects.requireNonNull(
                menu,
                "menu"
        );
    }

    @Override
    public String label() {
        return this.menu.label();
    }

    @Override
    public SpreadsheetFormatterSelector selector() {
        return this.menu.selector();
    }

    private final PluginSelectorMenu<SpreadsheetFormatterSelector, SpreadsheetFormatterName> menu;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return this.menu.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return other == this ||
                other instanceof SpreadsheetFormatterSelectorMenu && this.equals0((SpreadsheetFormatterSelectorMenu) other);
    }

    private boolean equals0(final SpreadsheetFormatterSelectorMenu other) {
        return this.menu.equals(other.menu);
    }

    @Override
    public String toString() {
        return this.menu.toString();
    }

    // TreePrintable....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        this.menu.printTree(printer);
    }

    // json.............................................................................................................

    /**
     * Factory that creates a {@link SpreadsheetFormatterSelectorMenu} parse a {@link JsonNode}.
     */
    static SpreadsheetFormatterSelectorMenu unmarshall(final JsonNode node,
                                                       final JsonNodeUnmarshallContext context) {
        return new SpreadsheetFormatterSelectorMenu(
                PluginSelectorMenu.unmarshall(
                        node,
                        context,
                        SpreadsheetFormatterSelector.class
                )
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return this.menu.marshall(context);
    }

    static {
        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(SpreadsheetFormatterSelectorMenu.class),
                SpreadsheetFormatterSelectorMenu::unmarshall,
                SpreadsheetFormatterSelectorMenu::marshall,
                SpreadsheetFormatterSelectorMenu.class
        );
    }
}
