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
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginSet;
import walkingkooka.plugin.store.PluginStore;

import java.util.Map;
import java.util.Optional;

/**
 * Provides end points to retrieve one or more {@link Plugin}.
 */
final class PluginHateosResourceHandlerDelete implements HateosResourceHandler<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleMany<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleNone<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static PluginHateosResourceHandlerDelete INSTANCE = new PluginHateosResourceHandlerDelete();

    private PluginHateosResourceHandlerDelete() {
        super();
    }

    @Override
    public Optional<PluginSet> handleAll(final Optional<PluginSet> pluginSet,
                                         final Map<HttpRequestAttribute<?>, Object> parameters,
                                         final PluginHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(pluginSet);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        final PluginStore store = context.pluginStore();

        for (final Plugin plugin : store.all()) {
            store.delete(plugin.name());
        }

        return Optional.empty();
    }

    @Override
    public Optional<Plugin> handleOne(final PluginName name,
                                      final Optional<Plugin> plugin,
                                      final Map<HttpRequestAttribute<?>, Object> parameters,
                                      final PluginHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(name);
        HateosResourceHandler.checkResource(plugin);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        context.pluginStore()
                .delete(name);

        return Optional.empty();
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

        final PluginStore store = context.pluginStore();

        for (final Plugin plugin : context.pluginStore()
                .between(
                        range.lowerBound()
                                .value()
                                .orElseThrow(() -> new IllegalArgumentException("Range missing begin")),
                        range.upperBound()
                                .value()
                                .orElseThrow(() -> new IllegalArgumentException("Range missing end"))
                )) {
            store.delete(plugin.name());
        }

        return Optional.empty();
    }

    @Override
    public String toString() {
        return HttpMethod.DELETE + " " + PluginStore.class.getSimpleName();
    }
}
