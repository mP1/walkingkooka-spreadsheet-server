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

package walkingkooka.spreadsheet.server.plugin;

import walkingkooka.collect.Range;
import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginSet;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;

public final class PluginHttpMappings implements PublicStaticHelper {

    public static HateosResourceMapping<PluginName,
            Plugin,
            PluginSet,
            Plugin,
            SpreadsheetHateosResourceHandlerContext> plugin() {

        // plugin GET...................................................................................................

        HateosResourceMapping<PluginName,
                Plugin,
                PluginSet,
                Plugin,
                SpreadsheetHateosResourceHandlerContext> plugin = HateosResourceMapping.with(
                PLUGIN,
                PluginHttpMappings::parsePluginName,
                Plugin.class,
                PluginSet.class,
                Plugin.class,
                SpreadsheetHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.GET,
                PluginHateosResourceHandlerLoad.INSTANCE
        ).setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.DELETE,
                PluginHateosResourceHandlerDelete.INSTANCE
        );

        return plugin;
    }

    /**
     * Handles parsing the text into a {@link HateosResourceSelection}.
     */
    private static HateosResourceSelection<PluginName> parsePluginName(final String text,
                                                                       final SpreadsheetHateosResourceHandlerContext context) {
        final HateosResourceSelection<PluginName> result;

        if (text.isEmpty()) {
            result = HateosResourceSelection.none();
        } else {
            if ("*".equals(text)) {
                result = HateosResourceSelection.all();
            } else {
                final int separator = text.indexOf(PluginName.SEPARATOR.character());
                switch (separator) {
                    case -1:
                        result = HateosResourceSelection.one(
                                PluginName.with(text)
                        );
                        break;
                    case 0:
                        throw new IllegalArgumentException("Missing begin");
                    default:
                        result = HateosResourceSelection.range(
                                Range.parse(
                                        text,
                                        PluginName.SEPARATOR.character(),
                                        PluginName::with
                                )
                        );
                        break;
                }
            }
        }

        return result;
    }

    /**
     * A {@link HateosResourceName} with <code>cell</code>.
     */
    public static final HateosResourceName PLUGIN = HateosResourceName.with("plugin");

    /**
     * Stop creation.
     */
    private PluginHttpMappings() {
        throw new UnsupportedOperationException();
    }
}
