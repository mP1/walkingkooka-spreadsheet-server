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
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.header.ContentDispositionFileName;
import walkingkooka.net.header.ContentDispositionType;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.spreadsheet.server.SpreadsheetServerLinkRelations;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A {@link HttpHandler} that loads the plugin for a given {@link PluginName} and then returns the file in the file-path portion of the URL.
 */
final class PluginFileDownloadHttpHandler implements HttpHandler {

    static PluginFileDownloadHttpHandler with(final AbsoluteUrl base,
                                              final PluginStore pluginStore,
                                              final BiFunction<String, Binary, MediaType> contentTypeDetector) {
        return new PluginFileDownloadHttpHandler(
                Objects.requireNonNull(base, "base"),
                Objects.requireNonNull(pluginStore, "pluginStore"),
                Objects.requireNonNull(contentTypeDetector, "contentTypeDetector")
        );
    }

    private PluginFileDownloadHttpHandler(final AbsoluteUrl base,
                                          final PluginStore pluginStore,
                                          final BiFunction<String, Binary, MediaType> contentTypeDetector) {
        this.base = base;
        this.pluginStore = pluginStore;
        this.contentTypeDetector = contentTypeDetector;
    }

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(response, "response");

        final UrlPath path = request.url()
                .path()
                .normalize();

        // extract plugin name
        final List<UrlPathName> pathNames = path.namesList();

        if (pathNames.size() < 3) {
            throw new IllegalArgumentException("Missing plugin name from url");
        }

        UrlPath download = this.base.path();
        final UrlPathName pathName = pathNames.get(3);
        final PluginName pluginName = PluginName.with(
                pathName.value()
        );
        download = download.append(pathName)
                .append(
                        SpreadsheetServerLinkRelations.DOWNLOAD.toUrlPathName()
                );

        final Optional<Plugin> maybePlugin = this.pluginStore.load(pluginName);
        if (maybePlugin.isPresent()) {
            final Plugin plugin = maybePlugin.get();
            final Binary archive = plugin.archive();
            final BiFunction<String, Binary, MediaType> contentTypeDetector = this.contentTypeDetector;

            // extract trailing path...

            final int filePathBegin = download.value().length();
            final String pathString = path.value();
            if (filePathBegin == pathString.length()) {

                // no file path download whole archive
                final String pluginFilename = plugin.filename();

                response.setStatus(HttpStatusCode.OK.status());
                response.setEntity(
                        HttpEntity.EMPTY.setContentType(
                                        contentTypeDetector.apply(
                                                pluginFilename,
                                                archive
                                        )
                                ).addHeader(
                                        HttpHeaderName.CONTENT_DISPOSITION,
                                        ContentDispositionType.ATTACHMENT.setFilename(
                                                ContentDispositionFileName.notEncoded(pluginFilename)
                                        )
                                )
                                .setBody(archive)
                                .setContentLength()
                );
            } else {
                // file path present extract file and send that
                final String filePath = pathString.substring(filePathBegin);

                try {
                    final HttpEntity entity = PluginFileDownloadHttpHandlerFileExtractor.extractFile(
                            archive,
                            filePath,
                            contentTypeDetector
                    );
                    response.setEntity(entity);
                    response.setStatus(
                            entity.isEmpty() ?
                                    HttpStatusCode.NO_CONTENT.status() :
                                    HttpStatusCode.OK.status()
                    );
                } catch (final IOException cause) {
                    throw new RuntimeException(cause);
                }
            }
        } else {
            response.setStatus(HttpStatusCode.NO_CONTENT.status());
            response.setEntity(HttpEntity.EMPTY);
        }
    }

    // http://server/api/plugin/PluginName/download/
    // http://server/api/plugin/PluginName/download/path/to/file

    private final AbsoluteUrl base;

    private final PluginStore pluginStore;

    private final BiFunction<String, Binary, MediaType> contentTypeDetector;

    @Override
    public String toString() {
        return this.base + "/*/download";
    }
}
