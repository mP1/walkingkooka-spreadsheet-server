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

package walkingkooka.spreadsheet.server.meta;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.Converters;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.environment.AuditInfo;
import walkingkooka.locale.LocaleContext;
import walkingkooka.locale.LocaleContexts;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.AcceptCharset;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.FakeHttpRequest;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestParameterName;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.plugin.FakeProviderContext;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetName;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviders;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellReferencesStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelReferencesStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.spreadsheet.validation.form.store.SpreadsheetFormStores;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewport;
import walkingkooka.storage.StorageStores;
import walkingkooka.store.Store;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;
import walkingkooka.tree.expression.function.provider.FakeExpressionFunctionProvider;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContexts;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicSpreadsheetMetadataHateosResourceHandlerContextTest implements SpreadsheetEngineHateosResourceHandlerContextTesting<BasicSpreadsheetMetadataHateosResourceHandlerContext>,
    SpreadsheetMetadataTesting {

    private final static MediaType CONTENT_TYPE = MediaType.APPLICATION_JSON;

    private final static LocaleContext LOCALE_CONTEXT = LocaleContexts.fake();

    private final static SpreadsheetMetadataStore METADATA_STORE = SpreadsheetMetadataTesting.spreadsheetMetadataStore();

    private final static HateosResourceHandlerContext HATEOS_RESOURCE_HANDLER_CONTEXT = HateosResourceHandlerContexts.basic(
        INDENTATION,
        EOL,
        JsonNodeMarshallUnmarshallContexts.basic(
            JSON_NODE_MARSHALL_CONTEXT,
            JSON_NODE_UNMARSHALL_CONTEXT
        )
    );

    @Test
    public void testWithNullServerUrlFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetMetadataHateosResourceHandlerContext.with(
                null,
                LOCALE_CONTEXT,
                SPREADSHEET_PROVIDER,
                PROVIDER_CONTEXT,
                METADATA_STORE,
                this::spreadsheetIdToSpreadsheetProvider,
                this::spreadsheetIdToRepository,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullLocaleContextFails() {
        final AbsoluteUrl serverUrl = this.base();
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetMetadataHateosResourceHandlerContext.with(
                serverUrl,
                null,
                SPREADSHEET_PROVIDER,
                PROVIDER_CONTEXT,
                METADATA_STORE,
                this::spreadsheetIdToSpreadsheetProvider,
                this::spreadsheetIdToRepository,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSystemSpreadsheetProviderFails() {
        final AbsoluteUrl serverUrl = this.base();
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetMetadataHateosResourceHandlerContext.with(
                serverUrl,
                LOCALE_CONTEXT,
                null,
                PROVIDER_CONTEXT,
                METADATA_STORE,
                this::spreadsheetIdToSpreadsheetProvider,
                this::spreadsheetIdToRepository,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSystemProviderContextFails() {
        final AbsoluteUrl serverUrl = this.base();
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetMetadataHateosResourceHandlerContext.with(
                serverUrl,
                LOCALE_CONTEXT,
                SPREADSHEET_PROVIDER,
                null,
                METADATA_STORE,
                this::spreadsheetIdToSpreadsheetProvider,
                this::spreadsheetIdToRepository,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }


    @Test
    public void testWithNullMetadataStoreFails() {
        final AbsoluteUrl serverUrl = this.base();
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetMetadataHateosResourceHandlerContext.with(
                serverUrl,
                LOCALE_CONTEXT,
                SPREADSHEET_PROVIDER,
                PROVIDER_CONTEXT,
                null,
                this::spreadsheetIdToSpreadsheetProvider,
                this::spreadsheetIdToRepository,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetIdToSpreadsheetProviderFails() {
        final AbsoluteUrl serverUrl = this.base();
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetMetadataHateosResourceHandlerContext.with(
                serverUrl,
                LOCALE_CONTEXT,
                SPREADSHEET_PROVIDER,
                PROVIDER_CONTEXT,
                METADATA_STORE,
                null,
                this::spreadsheetIdToRepository,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetIdRepositoryFails() {
        final AbsoluteUrl serverUrl = this.base();
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetMetadataHateosResourceHandlerContext.with(
                serverUrl,
                LOCALE_CONTEXT,
                SPREADSHEET_PROVIDER,
                PROVIDER_CONTEXT,
                METADATA_STORE,
                this::spreadsheetIdToSpreadsheetProvider,
                null,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullHateosResourceHandlerContextFails() {
        final AbsoluteUrl serverUrl = this.base();
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetMetadataHateosResourceHandlerContext.with(
                serverUrl,
                LOCALE_CONTEXT,
                SPREADSHEET_PROVIDER,
                PROVIDER_CONTEXT,
                METADATA_STORE,
                this::spreadsheetIdToSpreadsheetProvider,
                this::spreadsheetIdToRepository,
                null
            )
        );
    }

    @Test
    public void testHateosRouter() {
        this.checkNotEquals(null, this.createContext().httpRouter(this.spreadsheetId()));
    }

    @Test
    public void testHateosRouterThenSaveThenLoadClearValueErrorSkipEvaluate() {
        this.hateosRouterThenSaveThenLoadAndCheck(
            SpreadsheetEngineEvaluation.CLEAR_VALUE_ERROR_SKIP_EVALUATE,
            "{\n" +
                "  \"cells\": {\n" +
                "    \"B2\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"=1+2\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"=\",\n" +
                "                  \"text\": \"=\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": [\n" +
                "                    {\n" +
                "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": [\n" +
                "                          {\n" +
                "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                "                            \"value\": {\n" +
                "                              \"value\": \"1\",\n" +
                "                              \"text\": \"1\"\n" +
                "                            }\n" +
                "                          }\n" +
                "                        ],\n" +
                "                        \"text\": \"1\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"+\",\n" +
                "                        \"text\": \"+\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": [\n" +
                "                          {\n" +
                "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                "                            \"value\": {\n" +
                "                              \"value\": \"2\",\n" +
                "                              \"text\": \"2\"\n" +
                "                            }\n" +
                "                          }\n" +
                "                        ],\n" +
                "                        \"text\": \"2\"\n" +
                "                      }\n" +
                "                    }\n" +
                "                  ],\n" +
                "                  \"text\": \"1+2\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"=1+2\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"B\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"2\": 30\n" +
                "  },\n" +
                "  \"columnCount\": 2,\n" +
                "  \"rowCount\": 2\n" +
                "}"
        );
    }

    @Test
    public void testHateosRouterThenSaveThenLoadComputeIfNecessary() {
        this.hateosRouterThenSaveThenLoadAndCheck(
            SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
            "{\n" +
                "  \"cells\": {\n" +
                "    \"B2\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"=1+2\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"=\",\n" +
                "                  \"text\": \"=\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": [\n" +
                "                    {\n" +
                "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": [\n" +
                "                          {\n" +
                "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                "                            \"value\": {\n" +
                "                              \"value\": \"1\",\n" +
                "                              \"text\": \"1\"\n" +
                "                            }\n" +
                "                          }\n" +
                "                        ],\n" +
                "                        \"text\": \"1\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"+\",\n" +
                "                        \"text\": \"+\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": [\n" +
                "                          {\n" +
                "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                "                            \"value\": {\n" +
                "                              \"value\": \"2\",\n" +
                "                              \"text\": \"2\"\n" +
                "                            }\n" +
                "                          }\n" +
                "                        ],\n" +
                "                        \"text\": \"2\"\n" +
                "                      }\n" +
                "                    }\n" +
                "                  ],\n" +
                "                  \"text\": \"1+2\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"=1+2\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"add-expression\",\n" +
                "          \"value\": [\n" +
                "            {\n" +
                "              \"type\": \"value-expression\",\n" +
                "              \"value\": {\n" +
                "                \"type\": \"expression-number\",\n" +
                "                \"value\": \"1\"\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"type\": \"value-expression\",\n" +
                "              \"value\": {\n" +
                "                \"type\": \"expression-number\",\n" +
                "                \"value\": \"2\"\n" +
                "              }\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        \"value\": {\n" +
                "          \"type\": \"expression-number\",\n" +
                "          \"value\": \"3\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Number 003.000\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"B\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"2\": 30\n" +
                "  },\n" +
                "  \"columnCount\": 2,\n" +
                "  \"rowCount\": 2\n" +
                "}"
        );
    }

    @Test
    public void testHateosRouterThenSaveThenLoadForceRecompute() {
        this.hateosRouterThenSaveThenLoadAndCheck(
            SpreadsheetEngineEvaluation.FORCE_RECOMPUTE,
            "{\n" +
                "  \"cells\": {\n" +
                "    \"B2\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"=1+2\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"=\",\n" +
                "                  \"text\": \"=\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": [\n" +
                "                    {\n" +
                "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": [\n" +
                "                          {\n" +
                "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                "                            \"value\": {\n" +
                "                              \"value\": \"1\",\n" +
                "                              \"text\": \"1\"\n" +
                "                            }\n" +
                "                          }\n" +
                "                        ],\n" +
                "                        \"text\": \"1\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"+\",\n" +
                "                        \"text\": \"+\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": [\n" +
                "                          {\n" +
                "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                "                            \"value\": {\n" +
                "                              \"value\": \"2\",\n" +
                "                              \"text\": \"2\"\n" +
                "                            }\n" +
                "                          }\n" +
                "                        ],\n" +
                "                        \"text\": \"2\"\n" +
                "                      }\n" +
                "                    }\n" +
                "                  ],\n" +
                "                  \"text\": \"1+2\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"=1+2\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"add-expression\",\n" +
                "          \"value\": [\n" +
                "            {\n" +
                "              \"type\": \"value-expression\",\n" +
                "              \"value\": {\n" +
                "                \"type\": \"expression-number\",\n" +
                "                \"value\": \"1\"\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"type\": \"value-expression\",\n" +
                "              \"value\": {\n" +
                "                \"type\": \"expression-number\",\n" +
                "                \"value\": \"2\"\n" +
                "              }\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        \"value\": {\n" +
                "          \"type\": \"expression-number\",\n" +
                "          \"value\": \"3\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Number 003.000\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"B\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"2\": 30\n" +
                "  },\n" +
                "  \"columnCount\": 2,\n" +
                "  \"rowCount\": 2\n" +
                "}"
        );
    }

    @Test
    public void testHateosRouterThenSaveThenLoadSkipEvaluate() {
        this.hateosRouterThenSaveThenLoadAndCheck(
            SpreadsheetEngineEvaluation.SKIP_EVALUATE,
            "{\n" +
                "  \"cells\": {\n" +
                "    \"B2\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"=1+2\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"=\",\n" +
                "                  \"text\": \"=\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": [\n" +
                "                    {\n" +
                "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": [\n" +
                "                          {\n" +
                "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                "                            \"value\": {\n" +
                "                              \"value\": \"1\",\n" +
                "                              \"text\": \"1\"\n" +
                "                            }\n" +
                "                          }\n" +
                "                        ],\n" +
                "                        \"text\": \"1\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"+\",\n" +
                "                        \"text\": \"+\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": [\n" +
                "                          {\n" +
                "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                "                            \"value\": {\n" +
                "                              \"value\": \"2\",\n" +
                "                              \"text\": \"2\"\n" +
                "                            }\n" +
                "                          }\n" +
                "                        ],\n" +
                "                        \"text\": \"2\"\n" +
                "                      }\n" +
                "                    }\n" +
                "                  ],\n" +
                "                  \"text\": \"1+2\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"=1+2\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"add-expression\",\n" +
                "          \"value\": [\n" +
                "            {\n" +
                "              \"type\": \"value-expression\",\n" +
                "              \"value\": {\n" +
                "                \"type\": \"expression-number\",\n" +
                "                \"value\": \"1\"\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"type\": \"value-expression\",\n" +
                "              \"value\": {\n" +
                "                \"type\": \"expression-number\",\n" +
                "                \"value\": \"2\"\n" +
                "              }\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        \"value\": {\n" +
                "          \"type\": \"expression-number\",\n" +
                "          \"value\": \"3\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Number 003.000\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"B\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"2\": 30\n" +
                "  },\n" +
                "  \"columnCount\": 2,\n" +
                "  \"rowCount\": 2\n" +
                "}"
        );
    }

    private void hateosRouterThenSaveThenLoadAndCheck(final SpreadsheetEngineEvaluation evaluation,
                                                      final String expectedBody) {
        final AtomicReference<LocalDateTime> now = new AtomicReference<>();
        now.set(NOW.now());
        final BasicSpreadsheetMetadataHateosResourceHandlerContext context = this.createContext(
            new FakeProviderContext() {
                @Override
                public LocalDateTime now() {
                    return now.get();
                }

                @Override
                public PluginStore pluginStore() {
                    return PROVIDER_CONTEXT.pluginStore();
                }

                @Override
                public Optional<EmailAddress> user() {
                    return PROVIDER_CONTEXT.user();
                }
            }
        );
        final SpreadsheetId id = this.spreadsheetId();

        final Router<HttpRequestAttribute<?>, HttpHandler> router = context.httpRouter(id);

        final SpreadsheetCellReference cellReference = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCell cell = cellReference
            .setFormula(
                SpreadsheetFormula.EMPTY
                    .setText("=1+2")
            );

        final LocalDateTime updatedTimestamp = LocalDateTime.of(
            2000,
            1,
            1,
            1,
            23,
            45
        );
        now.set(updatedTimestamp);

        // save a cell
        {
            final HttpRequest request = new TestHttpRequest() {
                @Override
                public HttpMethod method() {
                    return HttpMethod.POST;
                }

                @Override
                public RelativeUrl url() {
                    return Url.parseRelative("/api987/123def/cell/B2");
                }

                @SuppressWarnings("UnnecessaryBoxing")
                @Override
                public Map<HttpHeaderName<?>, List<?>> headers() {
                    return headersMap(
                        HttpHeaderName.ACCEPT, CONTENT_TYPE.accept(),
                        HttpHeaderName.ACCEPT_CHARSET, AcceptCharset.parse("UTF-8"),
                        HttpHeaderName.CONTENT_LENGTH, Long.valueOf(this.bodyText().length()),
                        HttpHeaderName.CONTENT_TYPE, CONTENT_TYPE
                    );
                }

                @Override
                public Map<HttpRequestParameterName, List<String>> parameters() {
                    return HttpRequest.NO_PARAMETERS;
                }

                @Override
                public String bodyText() {
                    return JSON_NODE_MARSHALL_CONTEXT
                        .marshall(SpreadsheetDelta.EMPTY.setCells(Sets.of(cell)))
                        .toString();
                }

                @Override
                public String toString() {
                    return this.method() + " " + this.url();
                }
            };

            final Optional<HttpHandler> mapped = router.route(request.routerParameters());
            this.checkNotEquals(Optional.empty(), mapped, "request " + request.routerParameters());

            final HttpResponse response = HttpResponses.recording();
            @SuppressWarnings("OptionalGetWithoutIsPresent") final HttpHandler httpHandler = mapped.get();
            httpHandler.handle(
                request,
                response
            );
        }

        // load cell back
        {
            final HttpRequest request = new TestHttpRequest() {
                @Override
                public HttpMethod method() {
                    return HttpMethod.GET;
                }

                @Override
                public RelativeUrl url() {
                    return Url.parseRelative("/api987/123def/cell/B2/" + evaluation.toLinkRelation().toString());
                }

                @Override
                public Map<HttpHeaderName<?>, List<?>> headers() {
                    return headersMap(
                        HttpHeaderName.ACCEPT, CONTENT_TYPE.accept(),
                        HttpHeaderName.ACCEPT_CHARSET, AcceptCharset.parse("UTF-8"),
                        HttpHeaderName.CONTENT_LENGTH, Long.valueOf(this.bodyText().length()),
                        HttpHeaderName.CONTENT_TYPE, CONTENT_TYPE
                    );
                }

                @Override
                public Map<HttpRequestParameterName, List<String>> parameters() {
                    return HttpRequest.NO_PARAMETERS;
                }

                @Override
                public String bodyText() {
                    return "";
                }
            };

            final Optional<HttpHandler> mapped = router.route(request.routerParameters());
            this.checkNotEquals(Optional.empty(), mapped, "request " + request.parameters());

            final HttpResponse response = HttpResponses.recording();
            //noinspection OptionalGetWithoutIsPresent
            final HttpHandler httpHandler = mapped.get();
            httpHandler.handle(
                request,
                response
            );

            final HttpResponse expected = HttpResponses.recording();
            expected.setStatus(HttpStatusCode.OK.status());

            expected.setEntity(
                HttpEntity.EMPTY
                    .setContentType(CONTENT_TYPE.setCharset(CharsetName.UTF_8))
                    .addHeader(HateosResourceMappings.X_CONTENT_TYPE_NAME, SpreadsheetDelta.class.getSimpleName())
                    .setBodyText(expectedBody)
                    .setContentLength()
            );

            this.checkEquals(expected, response, () -> "consumer: " + httpHandler + ", request: " + request);
        }

        this.checkEquals(
            updatedTimestamp,
            context.metadataStore()
                .loadOrFail(id)
                .getOrFail(SpreadsheetMetadataPropertyName.AUDIT_INFO).modifiedTimestamp(),
            () -> "Metadata " + SpreadsheetMetadataPropertyName.AUDIT_INFO + ".modifiedTimestamp not updated when cell saved"
        );
    }

    @Test
    public void testHateosRouterAndRouteInvalidRequest() {
        final BasicSpreadsheetMetadataHateosResourceHandlerContext context = this.createContext();
        final SpreadsheetId id = this.spreadsheetId();
        final Router<HttpRequestAttribute<?>, HttpHandler> router = context.httpRouter(id);

        final HttpRequest request = new TestHttpRequest() {
            @Override
            public HttpMethod method() {
                return HttpMethod.POST;
            }

            @Override
            public RelativeUrl url() {
                return Url.parseRelative("/INVALID");
            }

            @Override
            public Map<HttpHeaderName<?>, List<?>> headers() {
                return HttpRequest.NO_HEADERS;
            }

            @Override
            public Map<HttpRequestParameterName, List<String>> parameters() {
                return HttpRequest.NO_PARAMETERS;
            }

            @Override
            public byte[] body() {
                return new byte[0];
            }
        };

        final Optional<HttpHandler> mapped = router.route(request.routerParameters());
        this.checkEquals(
            Optional.empty(),
            mapped,
            "request " + request.parameters()
        );
    }

    @Test
    public void testStoreRepositoryUnknownSpreadsheetId() {
        final BasicSpreadsheetMetadataHateosResourceHandlerContext context = this.createContext();
        final SpreadsheetId id = SpreadsheetId.with(123);

        final SpreadsheetStoreRepository repository = context.storeRepository(id);
        this.checkNotEquals(null, repository);

        this.countAndCheck(repository.cells(), 0);
        this.countAndCheck(repository.cellReferences(), 0);
        this.countAndCheck(repository.groups(), 0);
        this.countAndCheck(repository.labels(), 0);
        this.countAndCheck(repository.labelReferences(), 0);
        this.countAndCheck(repository.rangeToCells(), 0);
        this.countAndCheck(repository.rangeToConditionalFormattingRules(), 0);
        this.countAndCheck(repository.users(), 0);

        repository.cells()
            .save(
                SpreadsheetSelection.A1
                    .setFormula(
                        SpreadsheetFormula.EMPTY
                            .setText("1+2")
                    )
            );
        this.countAndCheck(repository.cells(), 1);
    }

    @Test
    public void testStoreRepositoryDifferentSpreadsheetId() {
        final BasicSpreadsheetMetadataHateosResourceHandlerContext context = this.createContext();

        final SpreadsheetId id1 = SpreadsheetId.with(111);
        final SpreadsheetStoreRepository repository1 = context.storeRepository(id1);
        this.checkNotEquals(null, repository1);

        final SpreadsheetId id2 = SpreadsheetId.with(222);
        final SpreadsheetStoreRepository repository2 = context.storeRepository(id2);
        this.checkNotEquals(null, repository2);
    }

    @Test
    public void testStoreRepositorySameSpreadsheetId() {
        final BasicSpreadsheetMetadataHateosResourceHandlerContext context = this.createContext();

        final SpreadsheetId id1 = SpreadsheetId.with(111);
        final SpreadsheetStoreRepository repository1 = context.storeRepository(id1);
        assertSame(repository1, context.storeRepository(id1));
    }

    private void countAndCheck(final Store<?, ?> store, final int count) {
        this.checkEquals(count, store.count(), () -> "" + store.all());
    }

    // saveMetadata.....................................................................................................

    @Test
    public void testSaveMetadata() {
        final BasicSpreadsheetMetadataHateosResourceHandlerContext context = this.createContext();

        final SpreadsheetMetadata metadata = context.metadataStore()
            .create(
                USER,
                Optional.empty()
            ).set(
                SpreadsheetMetadataPropertyName.LOCALE,
                Locale.forLanguageTag("EN-AU")
            );

        final SpreadsheetMetadataPropertyName<SpreadsheetName> propertyName = SpreadsheetMetadataPropertyName.SPREADSHEET_NAME;
        final SpreadsheetName name = SpreadsheetName.with("Spreadsheet234");

        final SpreadsheetMetadata updated = metadata.set(
            propertyName,
            name
        );

        final SpreadsheetMetadata saved = context.saveMetadata(updated);

        this.checkEquals(
            updated,
            saved
        );
    }

    @Test
    public void testSaveMetadataViewportSelectionCell() {
        final BasicSpreadsheetMetadataHateosResourceHandlerContext context = this.createContext();

        final SpreadsheetMetadata metadata = this.createMetadata(Optional.empty())
            .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"));

        final SpreadsheetMetadataPropertyName<SpreadsheetViewport> propertyName = SpreadsheetMetadataPropertyName.VIEWPORT;
        final SpreadsheetViewport viewport = SpreadsheetViewport.with(
            SpreadsheetSelection.parseCell("B2")
                .viewportRectangle(
                    123,
                    456
                )
        ).setAnchoredSelection(
            Optional.of(SpreadsheetSelection.A1.setDefaultAnchor())
        );

        final SpreadsheetMetadata updated = metadata.set(
            propertyName,
            viewport
        );

        final SpreadsheetMetadata saved = context.saveMetadata(updated);

        this.checkEquals(
            updated,
            saved
        );
    }

    @Test
    public void testSaveMetadataViewportSelectionUnknownLabelCleared() {
        final BasicSpreadsheetMetadataHateosResourceHandlerContext context = this.createContext();

        final SpreadsheetMetadata metadata = this.createMetadata(Optional.empty())
            .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"));

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("UnknownLabel123");

        final SpreadsheetMetadataPropertyName<SpreadsheetViewport> propertyName = SpreadsheetMetadataPropertyName.VIEWPORT;
        final SpreadsheetViewport viewport = SpreadsheetViewport.with(
            SpreadsheetSelection.parseCell("B2")
                .viewportRectangle(
                    123,
                    456
                )
        ).setAnchoredSelection(
            Optional.of(label.setDefaultAnchor())
        );

        final SpreadsheetMetadata updated = metadata.set(
            propertyName,
            viewport
        );

        final SpreadsheetMetadata saved = context.saveMetadata(updated);

        this.checkEquals(
            updated.set(
                propertyName,
                viewport.setAnchoredSelection(SpreadsheetViewport.NO_ANCHORED_SELECTION)
            ),
            saved
        );
    }

    @Test
    public void testSaveMetadataViewportSelectionExistingLabel() {
        final BasicSpreadsheetMetadataHateosResourceHandlerContext context = this.createContext();

        final SpreadsheetMetadata metadata = this.createMetadata(Optional.empty())
            .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"));

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("ExistingLabel123");
        context.storeRepository(metadata.id().get())
            .labels()
            .save(
                label.setLabelMappingReference(SpreadsheetSelection.A1)
            );

        final SpreadsheetMetadataPropertyName<SpreadsheetViewport> propertyName = SpreadsheetMetadataPropertyName.VIEWPORT;
        final SpreadsheetViewport viewport = SpreadsheetViewport.with(
            SpreadsheetSelection.parseCell("B2")
                .viewportRectangle(
                    123,
                    456
                )
        ).setAnchoredSelection(
            Optional.of(label.setDefaultAnchor())
        );

        final SpreadsheetMetadata updated = metadata.set(
            propertyName,
            viewport
        );

        final SpreadsheetMetadata saved = context.saveMetadata(updated);

        this.checkEquals(
            updated,
            saved
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        final BasicSpreadsheetMetadataHateosResourceHandlerContext context = this.createContext();
        context.storeRepository(SpreadsheetId.with(111));

        this.toStringAndCheck(
            context,
            "https://example.com/api987"
        );
    }

    // SpreadsheetMetadataHateosResourceHandlerContext...............................................................................................

    @Override
    public BasicSpreadsheetMetadataHateosResourceHandlerContext createContext() {
        return this.createContext(PROVIDER_CONTEXT);
    }

    private BasicSpreadsheetMetadataHateosResourceHandlerContext createContext(final ProviderContext providerContext) {
        return BasicSpreadsheetMetadataHateosResourceHandlerContext.with(
            this.base(),
            LOCALE_CONTEXT,
            SPREADSHEET_PROVIDER,
            providerContext,
            METADATA_STORE,
            this::spreadsheetIdToSpreadsheetProvider,
            this::spreadsheetIdToRepository,
            HATEOS_RESOURCE_HANDLER_CONTEXT
        );
    }

    private AbsoluteUrl base() {
        return Url.parseAbsolute("https://example.com/api987");
    }

    private SpreadsheetMetadata createMetadata(final Optional<Locale> locale) {
        SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY
            .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(999))
            .set(
                SpreadsheetMetadataPropertyName.AUDIT_INFO,
                AuditInfo.with(
                    EmailAddress.parse("creator@example.com"),
                    LocalDateTime.of(1999, 12, 31, 12, 58, 59),
                    EmailAddress.parse("modified@example.com"),
                    LocalDateTime.of(2000, 1, 2, 12, 58, 59)
                )
            );
        if (locale.isPresent()) {
            metadata = metadata.set(SpreadsheetMetadataPropertyName.LOCALE, locale.get());
        }
        return metadata;
    }

    private SpreadsheetProvider spreadsheetIdToSpreadsheetProvider(final SpreadsheetId id) {
        return METADATA_STORE.loadOrFail(id)
            .spreadsheetProvider(
                SpreadsheetProviders.basic(
                    CONVERTER_PROVIDER,
                    new FakeExpressionFunctionProvider<>() {
                        @Override
                        public ExpressionFunctionInfoSet expressionFunctionInfos() {
                            return SpreadsheetExpressionFunctions.infoSet(
                                Sets.of(
                                    SpreadsheetExpressionFunctions.info(
                                        Url.parseAbsolute("https://example.com/expression-function-1"),
                                        SpreadsheetExpressionFunctions.name("ExpressionFunction1")
                                    ),
                                    SpreadsheetExpressionFunctions.info(
                                        Url.parseAbsolute("https://example.com/expression-function-2"),
                                        SpreadsheetExpressionFunctions.name("ExpressionFunction2")
                                    )
                                )
                            );
                        }
                    },
                    SPREADSHEET_COMPARATOR_PROVIDER,
                    SPREADSHEET_EXPORTER_PROVIDER,
                    SPREADSHEET_FORMATTER_PROVIDER,
                    FORM_HANDLER_PROVIDER,
                    SPREADSHEET_IMPORTER_PROVIDER,
                    SPREADSHEET_PARSER_PROVIDER,
                    VALIDATOR_PROVIDER
                )
            );
    }

    private SpreadsheetStoreRepository spreadsheetIdToRepository(final SpreadsheetId id) {
        Objects.requireNonNull(id, "id");

        SpreadsheetStoreRepository repository = this.spreadsheetIdToRepository.get(id);

        if (null == repository) {
            final EmailAddress creator = EmailAddress.parse("user123@exaple.com");
            final LocalDateTime now = NOW.now();

            final Locale locale = Locale.ENGLISH;

            METADATA_STORE.save(
                SpreadsheetMetadataTesting.METADATA_EN_AU
                    .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, id)
                    .set(
                        SpreadsheetMetadataPropertyName.AUDIT_INFO,
                        AuditInfo.with(
                            creator,
                            now,
                            creator,
                            now
                        )
                    ).set(SpreadsheetMetadataPropertyName.DATE_TIME_OFFSET, Converters.JAVA_EPOCH_OFFSET)
                    .set(SpreadsheetMetadataPropertyName.LOCALE, locale)
                    .loadFromLocale(
                        LocaleContexts.jre(locale)
                    ).set(SpreadsheetMetadataPropertyName.CELL_CHARACTER_WIDTH, 1)
                    .set(SpreadsheetMetadataPropertyName.DECIMAL_NUMBER_SYMBOLS, DECIMAL_NUMBER_SYMBOLS)
                    .set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, 1900)
                    .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, EXPRESSION_NUMBER_KIND)
                    .set(SpreadsheetMetadataPropertyName.FORMULA_CONVERTER, ConverterSelector.parse("general"))
                    .set(SpreadsheetMetadataPropertyName.GENERAL_NUMBER_FORMAT_DIGIT_COUNT, 8)
                    .set(SpreadsheetMetadataPropertyName.PRECISION, 10)
                    .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                    .set(SpreadsheetMetadataPropertyName.STYLE, SpreadsheetMetadata.NON_LOCALE_DEFAULTS.getOrFail(SpreadsheetMetadataPropertyName.STYLE))
                    .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 20)
                    .set(SpreadsheetMetadataPropertyName.DATE_FORMATTER, SpreadsheetPattern.parseDateFormatPattern("\"Date\" yyyy mm dd").spreadsheetFormatterSelector())
                    .set(SpreadsheetMetadataPropertyName.DATE_PARSER, SpreadsheetPattern.parseDateParsePattern("\"Date\" yyyy mm dd").spreadsheetParserSelector())
                    .set(SpreadsheetMetadataPropertyName.DATE_TIME_FORMATTER, SpreadsheetPattern.parseDateTimeFormatPattern("\"DateTime\" yyyy hh").spreadsheetFormatterSelector())
                    .set(SpreadsheetMetadataPropertyName.DATE_TIME_PARSER, SpreadsheetPattern.parseDateTimeParsePattern("\"DateTime\" yyyy hh").spreadsheetParserSelector())
                    .set(SpreadsheetMetadataPropertyName.NUMBER_FORMATTER, SpreadsheetPattern.parseNumberFormatPattern("\"Number\" 000.000").spreadsheetFormatterSelector())
                    .set(SpreadsheetMetadataPropertyName.NUMBER_PARSER, SpreadsheetPattern.parseNumberParsePattern("\"Number\" 000.000").spreadsheetParserSelector())
                    .set(SpreadsheetMetadataPropertyName.TEXT_FORMATTER, SpreadsheetPattern.parseTextFormatPattern("\"Text\" @").spreadsheetFormatterSelector())
                    .set(SpreadsheetMetadataPropertyName.TIME_FORMATTER, SpreadsheetPattern.parseTimeFormatPattern("\"Time\" ss hh").spreadsheetFormatterSelector())
                    .set(SpreadsheetMetadataPropertyName.TIME_PARSER, SpreadsheetPattern.parseTimeParsePattern("\"Time\" ss hh").spreadsheetParserSelector()));
            repository = SpreadsheetStoreRepositories.basic(
                SpreadsheetCellStores.treeMap(),
                SpreadsheetCellReferencesStores.treeMap(),
                SpreadsheetColumnStores.treeMap(),
                SpreadsheetFormStores.treeMap(),
                SpreadsheetGroupStores.treeMap(),
                SpreadsheetLabelStores.treeMap(),
                SpreadsheetLabelReferencesStores.treeMap(),
                METADATA_STORE,
                SpreadsheetCellRangeStores.treeMap(),
                SpreadsheetCellRangeStores.treeMap(),
                SpreadsheetRowStores.treeMap(),
                StorageStores.tree(STORAGE_STORE_CONTEXT),
                SpreadsheetUserStores.treeMap()
            );
            this.spreadsheetIdToRepository.put(id, repository);
        }

        return repository;
    }

    private final Map<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository = Maps.sorted();

    private SpreadsheetId spreadsheetId() {
        return SpreadsheetId.with(0x123def);
    }

    static <T1, T2, T3, T4> Map<HttpHeaderName<?>, List<?>> headersMap(final HttpHeaderName<T1> header1,
                                                                       final T1 value1,
                                                                       final HttpHeaderName<T2> header2,
                                                                       final T2 value2,
                                                                       final HttpHeaderName<T3> header3,
                                                                       final T3 value3,
                                                                       final HttpHeaderName<T4> header4,
                                                                       final T4 value4) {
        return Maps.of(
            header1, Lists.of(value1),
            header2, Lists.of(value2),
            header3, Lists.of(value3),
            header4, Lists.of(value4)
        );
    }

    abstract static class TestHttpRequest extends FakeHttpRequest {
        TestHttpRequest() {
            super();
        }

        @Override
        public final long bodyLength() {
            return this.bodyText().length();
        }
    }

    @Override
    public Class<BasicSpreadsheetMetadataHateosResourceHandlerContext> type() {
        return BasicSpreadsheetMetadataHateosResourceHandlerContext.class;
    }
}
