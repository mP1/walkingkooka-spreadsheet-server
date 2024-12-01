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

import walkingkooka.Binary;
import walkingkooka.net.header.ContentDisposition;
import walkingkooka.net.header.ContentDispositionFileName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleOne;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleRange;
import walkingkooka.plugin.PluginArchiveManifest;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.text.CharSequences;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Handles uploads of JAR files.
 */
final class PluginHateosHttpEntityHandlerUpload implements HateosHttpEntityHandler<PluginName, PluginHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleNone<PluginName, PluginHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleMany<PluginName, PluginHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleOne<PluginName, PluginHateosResourceHandlerContext>,
        UnsupportedHateosHttpEntityHandlerHandleRange<PluginName, PluginHateosResourceHandlerContext> {


    final static PluginHateosHttpEntityHandlerUpload INSTANCE = new PluginHateosHttpEntityHandlerUpload();

    private PluginHateosHttpEntityHandlerUpload() {
        super();
    }

    @Override
    public HttpEntity handleAll(final HttpEntity entity,
                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                final PluginHateosResourceHandlerContext context) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(parameters, "parameters");
        Objects.requireNonNull(context, "context");

        HttpEntity response = null;

        for (final HttpEntity part : entity.multiparts()) {
            final ContentDisposition contentDisposition = HttpHeaderName.CONTENT_DISPOSITION.headerOrFail(part);
            final ContentDispositionFileName filename = contentDisposition.filename()
                    .orElse(null);
            if (null != filename) {
                final Binary archive = part.body();

                try (final ByteArrayInputStream bytes = new ByteArrayInputStream(archive.value())) {
                    final JarInputStream jarInputStream = new JarInputStream(bytes);

                    final Manifest manifest = jarInputStream.getManifest();
                    if (null == manifest) {
                        throw new IllegalArgumentException("Archive missing " + CharSequences.quoteAndEscape(PluginArchiveManifest.MANIFEST_MF_PATH));
                    }

                    PluginArchiveManifest pluginArchiveManifest = PluginArchiveManifest.with(manifest);

                    context.pluginStore()
                            .save(
                                    Plugin.with(
                                            pluginArchiveManifest.pluginName(), // name
                                            filename.value(), // filename
                                            archive, // archive
                                            context.userOrFail(), //
                                            context.now()
                                    )
                            );

                    response = HttpEntity.EMPTY
                            .setContentType(MediaType.BINARY)
                            .setBody(archive)
                            .setContentLength();
                } catch (final IOException cause) {
                    cause.printStackTrace();
                    throw new IllegalArgumentException(cause);
                }
            }
        }

        if (null == response) {
            throw new IllegalArgumentException("File missing");
        }

        return response;
    }

    @Override
    public String toString() {
        return "file upload " + PluginStore.class.getSimpleName();
    }
}
