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

import walkingkooka.net.UrlPath;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleOne;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginSet;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.spreadsheet.server.SpreadsheetUrlQueryParameters;

import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Supports executing a query fetching zero or more {@link Plugin}.
 */
// GET /api/plugin/*/filter?query=X&offset=1&count=2
final class PluginHateosResourceHandlerFilter implements HateosResourceHandler<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleMany<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleNone<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleOne<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static PluginHateosResourceHandlerFilter INSTANCE = new PluginHateosResourceHandlerFilter();

    private PluginHateosResourceHandlerFilter() {
        super();
    }

    @Override
    public Optional<PluginSet> handleAll(final Optional<PluginSet> pluginSet,
                                         final Map<HttpRequestAttribute<?>, Object> parameters,
                                         final UrlPath path,
                                         final PluginHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(pluginSet);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            PluginSet.with(
                new TreeSet<>(
                    context.pluginStore()
                        .filter(
                            SpreadsheetUrlQueryParameters.QUERY.firstParameterValue(parameters)
                                .orElse(""), // query
                            SpreadsheetUrlQueryParameters.offset(parameters)
                                .orElse(0),
                            SpreadsheetUrlQueryParameters.count(parameters)
                                .orElseThrow(() -> new IllegalArgumentException("Missing parameter " + SpreadsheetUrlQueryParameters.COUNT))
                        )
                )
            )
        );
    }

    @Override
    public String toString() {
        return HttpMethod.GET + " " + PluginStore.class.getSimpleName() + "/*/filter";
    }
}
