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

import org.junit.jupiter.api.Test;
import walkingkooka.net.Url;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.header.MediaTypeBoundary;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.server.GetHeadPostOrDeleteHttpHandlerTesting;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.hateos.HateosHandlerContextTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.engine.SpreadsheetMetadataMode;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosHandlerContexts;
import walkingkooka.spreadsheet.storage.SpreadsheetStorageContextTesting;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.storage.StoragePath;
import walkingkooka.storage.StorageValue;
import walkingkooka.storage.Storages;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;

import java.util.Optional;

public final class SpreadsheetStorageGetHeadPostOrDeleteHttpHandlerTest implements GetHeadPostOrDeleteHttpHandlerTesting<SpreadsheetStorageGetHeadPostOrDeleteHttpHandler, SpreadsheetEngineHateosHandlerContext>,
    HateosHandlerContextTesting,
    SpreadsheetMetadataTesting,
    SpreadsheetStorageContextTesting {

    @Test
    public void testHandleGetMissingAccept() {
        final SpreadsheetEngineHateosHandlerContext context = this.createContext();

        this.handleAndCheck(
            SpreadsheetStorageGetHeadPostOrDeleteHttpHandler.INSTANCE,
            HttpRequests.parse(
                HttpTransport.UNSECURED,
                "GET /api/spreadsheet/1/storage/ HTTP/1.0\r\n" +
                    "\r\n"
            ),
            context,
            HttpResponses.parse(
                "HTTP/1.0 400 Missing Accept\r\n" +
                    "\r\n"
            )
        );
    }

    @Test
    public void testHandleGetMissingAcceptIncompatibleContentType() {
        final SpreadsheetEngineHateosHandlerContext context = this.createContext();

        this.handleAndCheck(
            SpreadsheetStorageGetHeadPostOrDeleteHttpHandler.INSTANCE,
            HttpRequests.parse(
                HttpTransport.UNSECURED,
                "GET /api/spreadsheet/1/storage/ HTTP/1.0\r\n" +
                    "Accept: text/plain\r\n" +
                    "\r\n"
            ),
            context,
            HttpResponses.parse(
                "HTTP/1.0 400 Accept: Got application/json require text/plain\r\n" +
                    "\r\n"
            )
        );
    }

    @Test
    public void testHandleGetParentEmptyListing() {
        final SpreadsheetEngineHateosHandlerContext context = this.createContext();

        this.handleAndCheck(
            HttpRequests.parse(
                HttpTransport.UNSECURED,
                "GET /api/spreadsheet/1/storage/ HTTP/1.0\r\n" +
                    "Accept: application/json\r\n" +
                    "\r\n"
            ),
            context,
            HttpResponses.parse(
                "HTTP/1.0 200 OK\r\n" +
                    "Content-Length: 2\r\n" +
                    "Content-Type: application/json; charset=UTF-8\r\n" +
                    "\r\n" +
                    "[]"
            )
        );
    }

    @Test
    public void testHandleGetParentNotEmptyListing() {
        final SpreadsheetEngineHateosHandlerContext context = this.createContext();

        context.saveStorage(
            StorageValue.with(
                StoragePath.parse("/1st.txt")
            ).setValue(
                Optional.of("111")
            )
        );

        context.saveStorage(
            StorageValue.with(
                StoragePath.parse("/2st.txt")
            ).setValue(
                Optional.of("222")
            )
        );

        this.handleAndCheck(
            HttpRequests.parse(
                HttpTransport.UNSECURED,
                "GET /api/spreadsheet/1/storage/ HTTP/1.0\r\n" +
                    "Accept: application/json\r\n" +
                    "\r\n"
            ),
            context,
            HttpResponses.parse(
                "HTTP/1.0 200 OK\r\n" +
                    "Content-Length: 484\r\n" +
                    "Content-Type: application/json; charset=UTF-8\r\n" +
                    "\r\n" +
                    "[\n" +
                    "  {\n" +
                    "    \"path\": \"/1st.txt\",\n" +
                    "    \"auditInfo\": {\n" +
                    "      \"createdBy\": \"user123@example.com\",\n" +
                    "      \"createdTimestamp\": \"1999-12-31T12:58:59\",\n" +
                    "      \"modifiedBy\": \"user123@example.com\",\n" +
                    "      \"modifiedTimestamp\": \"1999-12-31T12:58:59\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"path\": \"/2st.txt\",\n" +
                    "    \"auditInfo\": {\n" +
                    "      \"createdBy\": \"user123@example.com\",\n" +
                    "      \"createdTimestamp\": \"1999-12-31T12:58:59\",\n" +
                    "      \"modifiedBy\": \"user123@example.com\",\n" +
                    "      \"modifiedTimestamp\": \"1999-12-31T12:58:59\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "]"
            )
        );
    }

    @Test
    public void testHandleGetUnknownStorageValue() {
        final SpreadsheetEngineHateosHandlerContext context = this.createContext();

        this.handleAndCheck(
            HttpRequests.parse(
                HttpTransport.UNSECURED,
                "GET /api/spreadsheet/1/storage/storage-value-not-found.txt HTTP/1.0\r\n" +
                    "Accept: */*\r\n" +
                    "\r\n"
            ),
            context,
            HttpResponses.parse(
                "HTTP/1.0 404 Not found\r\n" +
                    "\r\n"
            )
        );
    }

    @Test
    public void testHandleGetStorageValueJson() {
        final SpreadsheetEngineHateosHandlerContext context = this.createContext();

        context.saveStorage(
            StorageValue.with(
                StoragePath.parse("/file123.json")
            ).setValue(
                Optional.of(
                    JsonNode.object()
                        .set(
                            JsonPropertyName.with("hello"),
                            "World 123"
                        )
                )
            )
        );

        this.handleAndCheck(
            HttpRequests.parse(
                HttpTransport.UNSECURED,
                "GET /api/spreadsheet/1/storage/file123.json HTTP/1.0\r\n" +
                    "Accept: */*\r\n" +
                    "\r\n"
            ),
            context,
            HttpResponses.parse(
                "HTTP/1.0 200 OK\r\n" +
                    "Content-Length: 26\r\n" +
                    "Content-Type: application/json; charset=UTF-8\r\n" +
                    "\r\n" +
                    "{\n" +
                    "  \"hello\": \"World 123\"\n" +
                    "}"
            )
        );
    }

    @Test
    public void testHandleGetStorageValueTxt() {
        final SpreadsheetEngineHateosHandlerContext context = this.createContext();

        context.saveStorage(
            StorageValue.with(
                StoragePath.parse("/file123.txt")
            ).setValue(
                Optional.of("File Content 123")
            ).setContentType(
                Optional.of(MediaType.TEXT_PLAIN)
            )
        );

        this.handleAndCheck(
            HttpRequests.parse(
                HttpTransport.UNSECURED,
                "GET /api/spreadsheet/1/storage/file123.txt HTTP/1.0\r\n" +
                    "Accept: */*\r\n" +
                    "\r\n"
            ),
            context,
            HttpResponses.parse(
                "HTTP/1.0 200 OK\r\n" +
                    "Content-Length: 16\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "\r\n" +
                    "File Content 123"
            )
        );
    }

    @Test
    public void testHandlePostTextFile() {
        final SpreadsheetEngineHateosHandlerContext context = this.createContext();

        this.handleAndCheck(
            HttpRequests.parse(
                HttpTransport.UNSECURED,
                "POST /api/spreadsheet/1/storage/uploaded-file.txt HTTP/1.0\r\n" +
                    "Content-Length: 16\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "\r\n" +
                    "Hello"
            ),
            context,
            HttpResponses.parse(
                "HTTP/1.0 200 OK\r\n" +
                    "\r\n"
            )
        );

        this.loadStorageAndCheck(
            context,
            StoragePath.parse("/uploaded-text-file.txt")

        );
    }

    @Test
    public void testHandlePostMultipartTextFile() {
        final SpreadsheetEngineHateosHandlerContext context = this.createContext();

        final String boundary = "delimiter12345";

        final String content = "HelloWorld123";

        final HttpEntity multipart = HttpEntity.EMPTY.setContentType(
            MediaType.MULTIPART_FORM_DATA.setBoundary(MediaTypeBoundary.parse(boundary))
        ).setBodyText(
            "--delimiter12345\r\n" +
                "Content-Disposition: form-data; name=\"field2\"; filename=\"abc.txt\"\r\n" +
                "\r\n" +
                content +
                "\r\n" +
                "--" + boundary + "--"
        );

        this.handleAndCheck(
            HttpRequests.post(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/spreadsheet/1/storage/uploaded-multipart-file.txt"),
                HttpProtocolVersion.VERSION_1_0,
                multipart
            ),
            context,
            HttpResponses.parse(
                "HTTP/1.0 200 OK\r\n" +
                    "\r\n"
            )
        );

        final StoragePath path = StoragePath.parse("/uploaded-multipart-file.txt");

        this.loadStorageAndCheck(
            context,
            path,
            StorageValue.with(path)
                .setValue(
                    Optional.of(content)
                )
        );
    }

    @Test
    public void testHandleDeleteNotFound() {
        this.handleAndCheck(
            HttpRequests.parse(
                HttpTransport.UNSECURED,
                "DELETE /api/spreadsheet/1/storage/fileNotFound.txt HTTP/1.0\r\n" +
                    "\r\n"
            ),
            HttpResponses.parse(
                "HTTP/1.0 404 StorageValue not found\r\n" +
                    "\r\n"
            )
        );
    }

    @Test
    public void testHandleDeleteExisting() {
        final SpreadsheetEngineHateosHandlerContext context = this.createContext();

        final StoragePath path = StoragePath.parse("/deleteMe.txt");

        context.saveStorage(
            StorageValue.with(
                path
            ).setValue(
                Optional.of("111")
            )
        );

        this.handleAndCheck(
            HttpRequests.parse(
                HttpTransport.UNSECURED,
                "DELETE /api/spreadsheet/1/storage/deleteMe.txt HTTP/1.0\r\n" +
                    "\r\n"
            ),
            context,
            HttpResponses.parse(
                "HTTP/1.0 200 OK\r\n" +
                    "\r\n"
            )
        );

        this.loadStorageAndCheck(
            context,
            path
        );
    }

    @Override
    public SpreadsheetStorageGetHeadPostOrDeleteHttpHandler createHttpHandler() {
        return SpreadsheetStorageGetHeadPostOrDeleteHttpHandler.INSTANCE;
    }

    @Override
    public SpreadsheetEngineHateosHandlerContext createContext() {
        final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext = SpreadsheetEnvironmentContexts.basic(
            Storages.treeMapStore(),
            ENVIRONMENT_CONTEXT.cloneEnvironment()
        );

        final SpreadsheetId spreadsheetId = SpreadsheetId.with(1);

        final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();

        metadataStore.save(
            METADATA_EN_AU.set(
                SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                spreadsheetId
            )
        );

        spreadsheetEnvironmentContext.setSpreadsheetId(
            Optional.of(spreadsheetId)
        );

        return SpreadsheetEngineHateosHandlerContexts.basic(
            SPREADSHEET_ENGINE,
            HATEOS_HANDLER_CONTEXT,
            SpreadsheetEngineContexts.spreadsheetContext(
                SpreadsheetMetadataMode.FORMULA,
                SpreadsheetContexts.fixedSpreadsheetId(
                    MEDIA_TYPE_DETECTOR,
                    SPREADSHEET_METADATA_CREATOR,
                    MULTIPLIER,
                    SpreadsheetEngines.basic(),
                    SpreadsheetStoreRepositories.treeMap(metadataStore),
                    (c) -> {
                        throw new UnsupportedOperationException();
                    }, // Function<SpreadsheetEngineContext, Router<HttpRequestAttribute<?>, HttpHandler>> httpRouterFactory
                    CURRENCY_LOCALE_CONTEXT,
                    spreadsheetEnvironmentContext,
                    SPREADSHEET_PROVIDER,
                    PROVIDER_CONTEXT
                ),
                TERMINAL_CONTEXT
            )
        );
    }

    @Override
    public Class<SpreadsheetStorageGetHeadPostOrDeleteHttpHandler> type() {
        return SpreadsheetStorageGetHeadPostOrDeleteHttpHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
