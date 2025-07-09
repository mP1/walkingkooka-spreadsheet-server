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

import walkingkooka.Cast;
import walkingkooka.collect.list.ImmutableListDefaults;
import walkingkooka.collect.list.Lists;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;

/**
 * A collection of {@link SpreadsheetFormatterSelectorMenu} for a {@link SpreadsheetFormatterSelector}.
 */
public final class SpreadsheetFormatterSelectorMenuList extends AbstractList<SpreadsheetFormatterSelectorMenu>
    implements ImmutableListDefaults<SpreadsheetFormatterSelectorMenuList, SpreadsheetFormatterSelectorMenu> {

    public static SpreadsheetFormatterSelectorMenuList with(final List<SpreadsheetFormatterSelectorMenu> menus) {
        Objects.requireNonNull(menus, "menus");

        return menus instanceof SpreadsheetFormatterSelectorMenuList ?
            (SpreadsheetFormatterSelectorMenuList) menus :
            withCopy(menus);
    }

    private static SpreadsheetFormatterSelectorMenuList withCopy(final List<SpreadsheetFormatterSelectorMenu> menus) {
        Objects.requireNonNull(menus, "menus");

        final List<SpreadsheetFormatterSelectorMenu> copy = Lists.array();
        for (final SpreadsheetFormatterSelectorMenu menu : menus) {
            copy.add(
                Objects.requireNonNull(menu, "includes null menu")
            );
        }

        return new SpreadsheetFormatterSelectorMenuList(copy);
    }

    private SpreadsheetFormatterSelectorMenuList(final List<SpreadsheetFormatterSelectorMenu> menus) {
        this.menus = menus;
    }

    @Override
    public SpreadsheetFormatterSelectorMenu get(int index) {
        return this.menus.get(index);
    }

    @Override
    public int size() {
        return this.menus.size();
    }

    private final List<SpreadsheetFormatterSelectorMenu> menus;

    @Override
    public void elementCheck(final SpreadsheetFormatterSelectorMenu menu) {
        Objects.requireNonNull(menu, "menu");
    }

    @Override
    public SpreadsheetFormatterSelectorMenuList setElements(final List<SpreadsheetFormatterSelectorMenu> menus) {
        final SpreadsheetFormatterSelectorMenuList copy = with(menus);
        return this.equals(copy) ?
            this :
            copy;
    }

    // json.............................................................................................................

    static SpreadsheetFormatterSelectorMenuList unmarshall(final JsonNode node,
                                                           final JsonNodeUnmarshallContext context) {
        return with(
            Cast.to(
                context.unmarshallList(
                    node,
                    SpreadsheetFormatterSelectorMenu.class
                )
            )
        );
    }

    /**
     * <pre>
     * [
     *   {
     *     "label": "Short",
     *     "selector": "date-format-pattern yy/mm"
     *   },
     *   {
     *     "label": "Long",
     *     "selector": "text-format-pattern yyyy/mm/dd"
     *   }
     * ]
     * </pre>
     */
    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshallCollection(this);
    }

    static {
        SpreadsheetFormatterSelectorMenu.with(
            "label",
            SpreadsheetFormatterSelector.DEFAULT_TEXT_FORMAT
        ); // force json-registry

        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(SpreadsheetFormatterSelectorMenuList.class),
            SpreadsheetFormatterSelectorMenuList::unmarshall,
            SpreadsheetFormatterSelectorMenuList::marshall,
            SpreadsheetFormatterSelectorMenuList.class
        );
    }
}
