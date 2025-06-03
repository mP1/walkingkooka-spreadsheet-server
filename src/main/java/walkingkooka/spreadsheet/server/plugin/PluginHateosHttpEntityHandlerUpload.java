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
import walkingkooka.net.header.Accept;
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
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/**
 * Handles uploads of JAR files, supporting multi-part-forms and binary requests.
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

        entity.accept()
            .orElse(Accept.DEFAULT)
            .testOrFail(SpreadsheetServerMediaTypes.BINARY);

        final HttpEntity response;

        final MediaType contentType = entity.contentType()
            .orElse(null);
        if (MediaType.MULTIPART_FORM_DATA.test(contentType)) {
            response = this.multipartUpload(
                entity,
                context
            );
        } else if (SpreadsheetServerMediaTypes.BASE64.test(contentType)) {
            response = this.base64File(
                entity,
                context
            );
        } else if (SpreadsheetServerMediaTypes.BINARY.test(contentType)) {
            response = this.binaryFile(
                entity,
                context
            );
        } else {
            if (null == contentType) {
                throw new IllegalArgumentException("Missing " + HttpHeaderName.CONTENT_TYPE);
            }
            // Content-type: Got text/plain, expected multipart/multipart2 or json/json2
            throw new IllegalArgumentException(
                HttpHeaderName.CONTENT_TYPE +
                    ": Got " +
                    contentType.clearParameters() +
                    ", expected " +
                    MediaType.MULTIPART_FORM_DATA +
                    " or " +
                    SpreadsheetServerMediaTypes.CONTENT_TYPE.clearParameters()
            );
        }

        return response;
    }

    private HttpEntity multipartUpload(final HttpEntity entity,
                                       final PluginHateosResourceHandlerContext context) {
        HttpEntity response = null;

        for (final HttpEntity part : entity.multiparts()) {
            response = this.binaryFile(
                part,
                context
            );
            break;
        }

        if (null == response) {
            throw new IllegalArgumentException("Multipart parts missing file");
        }

        return response;
    }

    private HttpEntity base64File(final HttpEntity entity,
                                  final PluginHateosResourceHandlerContext context) {
        return this.archive(
            this.fileName(entity),
            Binary.with(
                Base64.getDecoder()
                    .decode(
                        entity.bodyText()
                    )
            ),
            context
        );
    }

    private HttpEntity binaryFile(final HttpEntity entity,
                                  final PluginHateosResourceHandlerContext context) {
        return this.archive(
            this.fileName(entity),
            entity.body(),
            context
        );
    }

    private ContentDispositionFileName fileName(final HttpEntity entity) {
        return HttpHeaderName.CONTENT_DISPOSITION.headerOrFail(entity)
            .filename()
            .orElseThrow(() -> new IllegalArgumentException("Missing filename"));
    }

    private HttpEntity archive(final ContentDispositionFileName filename,
                               final Binary archive,
                               final PluginHateosResourceHandlerContext context) {
        final PluginArchiveManifest pluginArchiveManifest = PluginArchiveManifest.fromArchive(archive);

        final Plugin saved = context.pluginStore()
            .save(
                Plugin.with(
                    pluginArchiveManifest.pluginName(), // name
                    filename.value(), // filename
                    archive, // archive
                    context.userOrFail(), //
                    context.now()
                )
            );

        return HttpEntity.EMPTY.setContentType(context.contentType())
            .setBodyText(
                context.marshall(saved)
                    .toString()
            ).setContentLength();
    }

    @Override
    public String toString() {
        return "file upload " + PluginStore.class.getSimpleName();
    }
}
