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
import walkingkooka.collect.set.Sets;
import walkingkooka.net.UrlPath;
import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginSet;
import walkingkooka.reflect.StaticHelper;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.server.SpreadsheetServerLinkRelations;

final class PluginHttpMappings implements StaticHelper {

    static HateosResourceMappings<PluginName,
        Plugin,
        PluginSet,
        Plugin,
        PluginHateosResourceHandlerContext> pluginHateosResourceHandlerContext() {

        // plugin ......................................................................................................

        return HateosResourceMappings.with(
            Plugin.HATEOS_RESOURCE_NAME,
            PluginHttpMappings::parseSelector,
            Plugin.class,
            PluginSet.class,
            Plugin.class,
            PluginHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            PluginHateosResourceHandlerLoad.INSTANCE
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.DELETE,
            PluginHateosResourceHandlerDelete.INSTANCE
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.POST,
            PluginHateosResourceHandlerSave.INSTANCE
        ).setHateosResourceHandler(
            SpreadsheetServerLinkRelations.FILTER,
            HttpMethod.GET,
            PluginHateosResourceHandlerFilter.INSTANCE
        ).setHateosHttpEntityHandler(
            SpreadsheetServerLinkRelations.UPLOAD,
            HttpMethod.POST,
            PluginHateosHttpEntityHandlerUpload.INSTANCE
        ).setHateosHttpEntityHandler(
            SpreadsheetServerLinkRelations.LIST,
            HttpMethod.GET,
            PluginHateosHttpEntityHandlerList.INSTANCE
        );
    }

    /**
     * Handles parsing the text into a {@link HateosResourceSelection}.
     */
    private static HateosResourceSelection<PluginName> parseSelector(final String text,
                                                                     final PluginHateosResourceHandlerContext context) {
        final HateosResourceSelection<PluginName> result;

        switch (text) {
            case HateosResourceSelection.NONE:
                result = HateosResourceSelection.none();
                break;
            case HateosResourceSelection.ALL:
                result = HateosResourceSelection.all();
                break;
            default:
                final int separator = text.indexOf(PluginName.SEPARATOR.character());
                switch (separator) {
                    case -1:
                        result = HateosResourceSelection.one(
                            PluginName.with(text)
                        );
                        break;
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

        return result;
    }

    // router...........................................................................................................

    /**
     * {@see PluginHateosResourceHandlersRouter}
     */
    static Router<HttpRequestAttribute<?>, HttpHandler> router(final UrlPath basePath,
                                                               final PluginHateosResourceHandlerContext context) {
        return HateosResourceMappings.router(
            basePath,
            Sets.of(
                PluginHttpMappings.pluginHateosResourceHandlerContext()
            ),
            context
        );
    }

    /**
     * Stop creation.
     */
    private PluginHttpMappings() {
        throw new UnsupportedOperationException();
    }
}
