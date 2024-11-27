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

import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginSet;
import walkingkooka.plugin.store.PluginStore;

import java.util.Map;
import java.util.Optional;

/**
 * Provides end points to save a new or replace an existing {@link Plugin}.
 */
final class PluginHateosResourceHandlerSave implements HateosResourceHandler<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleAll<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleMany<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleNone<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleRange<PluginName, Plugin, PluginSet, PluginHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static PluginHateosResourceHandlerSave INSTANCE = new PluginHateosResourceHandlerSave();

    private PluginHateosResourceHandlerSave() {
        super();
    }

    @Override
    public Optional<Plugin> handleOne(final PluginName name,
                                      final Optional<Plugin> plugin,
                                      final Map<HttpRequestAttribute<?>, Object> parameters,
                                      final PluginHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(name);
        HateosResourceHandler.checkResourceNotEmpty(plugin);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
                context.pluginStore()
                        .save(plugin.get())
        );
    }

    @Override
    public String toString() {
        return HttpMethod.POST + " " + PluginStore.class.getSimpleName();
    }
}
