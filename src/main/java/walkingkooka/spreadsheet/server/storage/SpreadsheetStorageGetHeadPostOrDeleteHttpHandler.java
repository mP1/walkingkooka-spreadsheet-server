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

package walkingkooka.spreadsheet.server.storage;

import walkingkooka.Either;
import walkingkooka.net.UrlPath;
import walkingkooka.net.header.Accept;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.GetHeadPostOrDeleteHttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosHandlerContext;
import walkingkooka.spreadsheet.server.net.SpreadsheetUrlQueryParameters;
import walkingkooka.storage.StorageBinary;
import walkingkooka.storage.StoragePath;
import walkingkooka.storage.StorageValue;
import walkingkooka.storage.StorageValueInfo;
import walkingkooka.tree.json.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link walkingkooka.net.http.server.HttpHandler} that handles all {@link walkingkooka.storage.Storage} CRUD operations.
 */
final class SpreadsheetStorageGetHeadPostOrDeleteHttpHandler implements GetHeadPostOrDeleteHttpHandler<SpreadsheetEngineHateosHandlerContext> {

    private final static int DEFAULT_COUNT = 20;

    private final static int MAX_COUNT = 40;

    /**
     * Singleton
     */
    final static SpreadsheetStorageGetHeadPostOrDeleteHttpHandler INSTANCE = new SpreadsheetStorageGetHeadPostOrDeleteHttpHandler();

    private SpreadsheetStorageGetHeadPostOrDeleteHttpHandler() {
        super();
    }

    @Override
    public void handleGetOrHead(final HttpRequest request,
                                final HttpResponse response,
                                final SpreadsheetEngineHateosHandlerContext context) {
        final HttpHeaderName<Accept> header = HttpHeaderName.ACCEPT;

        response.setVersion(request.protocolVersion());

        Accept accept = header.header(request)
            .orElse(null);
        if (null == accept) {
            response.setStatus(
                HttpStatusCode.BAD_REQUEST.setMessage("Missing " + HttpHeaderName.ACCEPT)
            );
            response.setEntity(HttpEntity.EMPTY);
        } else {
            final StoragePath path = path(request);

            if (path.isParent()) {
                acceptAndListStorage(
                    path,
                    accept,
                    request,
                    response,
                    context
                );

            } else {
                loadStorageValue(
                    path,
                    accept,
                    request,
                    response,
                    context
                );
            }
            //}
        }
    }

    /**
     * Supports producing a JSON response holding the {@link walkingkooka.storage.Storage} listing.
     */
    private static void acceptAndListStorage(final StoragePath path,
                                             final Accept accept,
                                             final HttpRequest request,
                                             final HttpResponse response,
                                             final SpreadsheetEngineHateosHandlerContext context) {
        final MediaType contentType = context.contentType();
        if (false == accept.test(contentType)) {
            response.setStatus(
                HttpStatusCode.BAD_REQUEST.setMessage(accept.requireIncompatibleMessage(contentType))
            );
            response.setEntity(HttpEntity.EMPTY);
        } else {
            listStorage(
                path,
                request,
                response,
                context
            );
        }
    }

    /**
     * Assumes the request content type has been satisfied, and produces the listing as a JSON response.
     */
    private static void listStorage(final StoragePath path,
                                    final HttpRequest request,
                                    final HttpResponse response,
                                    final SpreadsheetEngineHateosHandlerContext context) {
        final Map<HttpRequestAttribute<?>, Object> parameters = request.routerParameters();

        final int offset = SpreadsheetUrlQueryParameters.offset(parameters)
            .orElse(0);
        final int count = SpreadsheetUrlQueryParameters.count(parameters)
            .orElse(DEFAULT_COUNT);

        final List<StorageValueInfo> infos = context.listStorage(
            path,
            offset,
            count
        );

        response.setVersion(request.protocolVersion());
        response.setStatus(
            HttpStatusCode.OK.status()
        );

        // convert StorageValueInfo to JsonNode and then Binary
        response.setEntity(
            HttpEntity.EMPTY.setContentType(
                context.contentType()
            ).setBodyText(
                context.toJsonText(
                context.convertOrFail(
                        infos,
                        JsonNode.class
                    )
                )
            ).setContentLength()
        );
    }

    /**
     * Loads the identified {@link StorageValue} and converts that to the requested {@link MediaType}.
     */
    private static void loadStorageValue(final StoragePath path,
                                         final Accept accept,
                                         final HttpRequest request,
                                         final HttpResponse response,
                                         final SpreadsheetEngineHateosHandlerContext context) {
        response.setVersion(request.protocolVersion());

        final Optional<StorageValue> storageValue = context.loadStorage(path);

        if (storageValue.isPresent()) {
            response.setStatus(
                HttpStatusCode.OK.status()
            );

            final StorageBinary storageBinary = context.convertOrFail(
                storageValue.get(),
                StorageBinary.class
            );

            final MediaType responseContentType = storageBinary.contentType()
                .orElse(MediaType.BINARY);
            if (false == accept.test(responseContentType)) {
                response.setStatus(
                    HttpStatusCode.BAD_REQUEST.setMessage(accept.requireIncompatibleMessage(responseContentType))
                );
                response.setEntity(HttpEntity.EMPTY);
            } else {
                response.setEntity(
                    HttpEntity.EMPTY.setContentType(responseContentType.setCharset(
                            CharsetName.with(
                                context.charset()
                                    .name()
                            )
                        )
                    ).setBody(
                        storageBinary.binary()
                    ).setContentLength()
                );
            }

        } else {
            response.setStatus(
                HttpStatusCode.NOT_FOUND.status()
            );
            response.setEntity(HttpEntity.EMPTY);
        }
    }

    @Override
    public void handleNonMultipartPost(final HttpRequest request,
                                       final HttpEntity httpEntity,
                                       final HttpResponse response,
                                       final SpreadsheetEngineHateosHandlerContext context) {
        response.setVersion(request.protocolVersion());

        final StorageBinary storageBinary = StorageBinary.with(
            path(request),
            httpEntity.binary()
        );

        final Either<StorageValue, String> storageValue = context.convert(
            storageBinary,
            StorageValue.class
        );
        if (storageValue.isRight()) {
            response.setStatus(
                HttpStatusCode.BAD_REQUEST.status()
            );
            response.setEntity(HttpEntity.EMPTY);
        } else {
            context.saveStorage(
                storageValue.leftValue()
            );

            response.setStatus(
                HttpStatusCode.OK.status()
            );
            response.setEntity(HttpEntity.EMPTY);
        }
    }

    @Override
    public void handleDelete(final HttpRequest request,
                             final HttpResponse response,
                             final SpreadsheetEngineHateosHandlerContext context) {
        final StoragePath storagePath = path(request);
        final Optional<StorageValue> loaded = context.loadStorage(storagePath);

        context.deleteStorage(storagePath);

        response.setVersion(request.protocolVersion());
        response.setStatus(
            loaded.isPresent() ?
                HttpStatusCode.OK.status() :
                HttpStatusCode.NOT_FOUND.setMessage("StorageValue not found")
        );
        response.setEntity(HttpEntity.EMPTY);
    }

    /**
     * Helper that extracts the {@link StoragePath} from the {@link HttpRequest#url()}
     */
    // /api/spreadsheet/1/storage
    private static StoragePath path(final HttpRequest request) {
        final UrlPath urlPath = request.url()
            .path();
        return StoragePath.parse(
            urlPath.pathAfter(4)
                .value()
        );
    }
}
