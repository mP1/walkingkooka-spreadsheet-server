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

import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleRange;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;

import java.util.Map;

/**
 * Provides a listing of the file/dir entries within a {@link Plugin#archive()}.
 */
final class PluginHateosHttpEntityHandlerList implements HateosHttpEntityHandler<PluginName, PluginHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleAll<PluginName, PluginHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleMany<PluginName, PluginHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleNone<PluginName, PluginHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleRange<PluginName, PluginHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static PluginHateosHttpEntityHandlerList INSTANCE = new PluginHateosHttpEntityHandlerList();

    private PluginHateosHttpEntityHandlerList() {
        super();
    }

    @Override
    public HttpEntity handleOne(final PluginName pluginName,
                                final HttpEntity entity,
                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                final PluginHateosResourceHandlerContext context) {
        HateosHttpEntityHandler.checkId(pluginName);
        HateosHttpEntityHandler.checkHttpEntity(entity);
        HateosHttpEntityHandler.checkParameters(parameters);
        HateosHttpEntityHandler.checkContext(context);

        HttpHeaderName.ACCEPT.headerOrFail(entity)
            .testOrFail(SpreadsheetServerMediaTypes.CONTENT_TYPE);

        return context.pluginStore()
            .load(pluginName)
            .map(p -> HttpEntity.EMPTY.setContentType(SpreadsheetServerMediaTypes.CONTENT_TYPE)
                .addHeader(HateosResourceMapping.X_CONTENT_TYPE_NAME, JarEntryInfoList.class.getSimpleName())
                .setBodyText(
                    context.marshall(
                        JarEntryInfoList.readJarFile(
                            p.archive().inputStream()
                        )
                    ).toString()
                ).setContentLength()
            ).orElse(HttpEntity.EMPTY);
    }

    @Override
    public String toString() {
        return HttpMethod.GET + " " + PluginStore.class.getSimpleName() + "/list";
    }
}
