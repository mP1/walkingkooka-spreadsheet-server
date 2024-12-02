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

import walkingkooka.net.header.ContentDispositionFileName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleRange;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;

import java.util.Map;
import java.util.Objects;

/**
 * Handles binary file downloads of a plugin archive.
 */
final class PluginHateosHttpEntityHandlerDownloadPluginArchive implements HateosHttpEntityHandler<PluginName, PluginHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleAll<PluginName, PluginHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleNone<PluginName, PluginHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleMany<PluginName, PluginHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleRange<PluginName, PluginHateosResourceHandlerContext> {

    final static PluginHateosHttpEntityHandlerDownloadPluginArchive INSTANCE = new PluginHateosHttpEntityHandlerDownloadPluginArchive();

    private PluginHateosHttpEntityHandlerDownloadPluginArchive() {
        super();
    }

    @Override
    public HttpEntity handleOne(final PluginName id,
                                final HttpEntity entity,
                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                final PluginHateosResourceHandlerContext context) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(parameters, "parameters");
        Objects.requireNonNull(context, "context");

        HttpHeaderName.ACCEPT.headerOrFail(entity)
                .testOrFail(SpreadsheetServerMediaTypes.BINARY);

        return context.pluginStore()
                .load(id)
                .map(PluginHateosHttpEntityHandlerDownloadPluginArchive::httpEntity)
                .orElse(HttpEntity.EMPTY);
    }

    private static HttpEntity httpEntity(final Plugin plugin) {
        return PluginHttpEntity.httpEntity(
                ContentDispositionFileName.notEncoded(
                        plugin.filename()
                ),
                plugin.archive()
        );
    }

    @Override
    public String toString() {
        return "plugin archive download " + PluginStore.class.getSimpleName();
    }
}
