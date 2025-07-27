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
 * A collection of {@link SpreadsheetFormatterMenu} for a {@link SpreadsheetFormatterSelector}.
 */
public final class SpreadsheetFormatterMenuList extends AbstractList<SpreadsheetFormatterMenu>
    implements ImmutableListDefaults<SpreadsheetFormatterMenuList, SpreadsheetFormatterMenu> {

    public static SpreadsheetFormatterMenuList with(final List<SpreadsheetFormatterMenu> menus) {
        Objects.requireNonNull(menus, "menus");

        return menus instanceof SpreadsheetFormatterMenuList ?
            (SpreadsheetFormatterMenuList) menus :
            withCopy(menus);
    }

    private static SpreadsheetFormatterMenuList withCopy(final List<SpreadsheetFormatterMenu> menus) {
        Objects.requireNonNull(menus, "menus");

        final List<SpreadsheetFormatterMenu> copy = Lists.array();
        for (final SpreadsheetFormatterMenu menu : menus) {
            copy.add(
                Objects.requireNonNull(menu, "includes null menu")
            );
        }

        return new SpreadsheetFormatterMenuList(copy);
    }

    private SpreadsheetFormatterMenuList(final List<SpreadsheetFormatterMenu> menus) {
        this.menus = menus;
    }

    @Override
    public SpreadsheetFormatterMenu get(int index) {
        return this.menus.get(index);
    }

    @Override
    public int size() {
        return this.menus.size();
    }

    private final List<SpreadsheetFormatterMenu> menus;

    @Override
    public void elementCheck(final SpreadsheetFormatterMenu menu) {
        Objects.requireNonNull(menu, "menu");
    }

    @Override
    public SpreadsheetFormatterMenuList setElements(final List<SpreadsheetFormatterMenu> menus) {
        final SpreadsheetFormatterMenuList copy = with(menus);
        return this.equals(copy) ?
            this :
            copy;
    }

    // json.............................................................................................................

    static SpreadsheetFormatterMenuList unmarshall(final JsonNode node,
                                                   final JsonNodeUnmarshallContext context) {
        return with(
            Cast.to(
                context.unmarshallList(
                    node,
                    SpreadsheetFormatterMenu.class
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
        SpreadsheetFormatterMenu.with(
            "label",
            SpreadsheetFormatterSelector.DEFAULT_TEXT_FORMAT
        ); // force json-registry

        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(SpreadsheetFormatterMenuList.class),
            SpreadsheetFormatterMenuList::unmarshall,
            SpreadsheetFormatterMenuList::marshall,
            SpreadsheetFormatterMenuList.class
        );
    }
}
