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
import walkingkooka.collect.set.SortedSets;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginSet;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.spreadsheet.server.SpreadsheetUrlQueryParameters;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;

/**
 * Provides end points to retrieve one or more {@link Plugin}.
 */
final class PluginHateosResourceHandlerLoad implements HateosResourceHandler<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleMany<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleNone<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static PluginHateosResourceHandlerLoad INSTANCE = new PluginHateosResourceHandlerLoad();

    private PluginHateosResourceHandlerLoad() {
        super();
    }

    @Override
    public Optional<PluginSet> handleAll(final Optional<PluginSet> infos,
                                         final Map<HttpRequestAttribute<?>, Object> parameters,
                                         final PluginHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(infos);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        final int offset = SpreadsheetUrlQueryParameters.offset(parameters)
                .orElse(0);
        final int count = SpreadsheetUrlQueryParameters.count(parameters)
                .orElse(DEFAULT_COUNT);

        return pluginSet(
                context.pluginStore()
                        .values(
                                offset,
                                Math.min(
                                        MAX_COUNT,
                                        count
                                )
                        )
        );
    }

    private final static int DEFAULT_COUNT = 20;

    private final static int MAX_COUNT = 40;

    @Override
    public Optional<Plugin> handleOne(final PluginName name,
                                      final Optional<Plugin> info,
                                      final Map<HttpRequestAttribute<?>, Object> parameters,
                                      final PluginHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(name);
        HateosResourceHandler.checkResource(info);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return context.pluginStore()
                .load(name);
    }

    @Override
    public Optional<PluginSet> handleRange(final Range<PluginName> range,
                                           final Optional<PluginSet> resource,
                                           final Map<HttpRequestAttribute<?>, Object> parameters,
                                           final PluginHateosResourceHandlerContext context) {
        HateosResourceHandler.checkIdRange(range);
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return pluginSet(
                context.pluginStore()
                        .between(
                                range.lowerBound()
                                        .value()
                                        .orElseThrow(() -> new IllegalArgumentException("Range missing begin")),
                                range.upperBound()
                                        .value()
                                        .orElseThrow(() -> new IllegalArgumentException("Range missing end"))
                        )
        );
    }

    private static Optional<PluginSet> pluginSet(final Collection<Plugin> plugins) {
        final SortedSet<Plugin> all = SortedSets.tree();
        all.addAll(plugins);

        return Optional.of(
                PluginSet.with(all)
        );
    }

    @Override
    public String toString() {
        return HttpMethod.GET + " " + PluginStore.class.getSimpleName();
    }
}
