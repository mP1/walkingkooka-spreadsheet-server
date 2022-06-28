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

package walkingkooka.spreadsheet.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import walkingkooka.Binary;
import walkingkooka.Either;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.Converters;
import walkingkooka.math.Fraction;
import walkingkooka.net.HostAddress;
import walkingkooka.net.IpPort;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlQueryString;
import walkingkooka.net.UrlScheme;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.AcceptCharset;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.ETag;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.header.MediaTypeParameterName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestParameterName;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.net.http.server.WebFile;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.server.context.SpreadsheetContexts;
import walkingkooka.spreadsheet.server.engine.http.SpreadsheetExpressionReferenceSimilarities;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionEvaluationContext;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.UnknownExpressionFunctionException;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class SpreadsheetHttpServerTest extends SpreadsheetHttpServerTestCase<SpreadsheetHttpServer> {

    private final static ExpressionNumberKind EXPRESSION_NUMBER_KIND = ExpressionNumberKind.DEFAULT;

    private final static CharsetName CHARSET = CharsetName.UTF_8;
    private final static MediaType CONTENT_TYPE_UTF8 = HateosContentType.JSON_CONTENT_TYPE.setCharset(CHARSET);
    private final static UrlPath FILE = UrlPath.parse("/file.txt");
    private final static MediaType FILE_CONTENT_TYPE = MediaType.parse("text/custom-file;charset=" + CHARSET.value());
    private final static LocalDateTime FILE_LAST_MODIFIED = LocalDateTime.of(2000, 12, 31, 12, 28, 29);
    private final static Binary FILE_BINARY = Binary.with(bytes("abc123", FILE_CONTENT_TYPE));
    private final static HttpStatus FILE_NOT_FOUND = HttpStatusCode.NOT_FOUND.setMessage("File not found custom message");

    private final static String DELTA = SpreadsheetDelta.class.getSimpleName();

    private final static Optional<String> NO_TRANSACTION_ID = Optional.empty();
    private final static Map<HttpHeaderName<?>, List<?>> NO_HEADERS_TRANSACTION_ID = HttpRequest.NO_HEADERS;

    private final static LocalDateTime MODIFIED_DATE_TIME = LocalDateTime.of(2021, 7, 15, 20, 33);

    @Test
    public void testStartServer() {
        this.startServer();
    }

    @Test
    public void testGetInvalidSpreadsheetIdBadRequest() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/XYZ",
                NO_HEADERS_TRANSACTION_ID,
                "",
                HttpStatusCode.BAD_REQUEST.setMessage("Invalid id \"XYZ\""),
                ""
        );
    }

    @Test
    public void testGetUnknownSpreadsheetNoContent() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/99",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.NO_CONTENT.status())
        );
    }

    @Test
    public void testCreateSpreadsheet() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );
        this.checkNotEquals(null,
                this.metadataStore.load(SpreadsheetId.with(1L)),
                () -> "spreadsheet metadata not created and saved: " + this.metadataStore);
    }

    @Test
    public void testCreateSpreadsheetWithTransactionIdHeader() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );
        this.checkNotEquals(
                null,
                this.metadataStore.load(SpreadsheetId.with(1L)),
                () -> "spreadsheet metadata not created and saved: " + this.metadataStore
        );
    }

    @Test
    public void testCreateThenLoadSpreadsheet() {
        final TestHttpServer server = this.startServer();

        // create spreadsheet
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );
        this.checkNotEquals(
                null,
                this.metadataStore.load(SpreadsheetId.with(1L)),
                () -> "spreadsheet metadata not created and saved: " + this.metadataStore
        );

        // fetch metadata back again.
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );
    }

    @Test
    public void testSaveCellApostropheString() {
        this.createSpreadsheetSaveCellAndCheck(
                "'Hello123'",
                "{\n" +
                        "  \"cells\": {\n" +
                        "    \"A1\": {\n" +
                        "      \"formula\": {\n" +
                        "        \"text\": \"'Hello123'\",\n" +
                        "        \"token\": {\n" +
                        "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                        "          \"value\": {\n" +
                        "            \"value\": [{\n" +
                        "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"'\",\n" +
                        "                \"text\": \"'\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"Hello123'\",\n" +
                        "                \"text\": \"Hello123'\"\n" +
                        "              }\n" +
                        "            }],\n" +
                        "            \"text\": \"'Hello123'\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"expression\": {\n" +
                        "          \"type\": \"value-expression\",\n" +
                        "          \"value\": \"Hello123'\"\n" +
                        "        },\n" +
                        "        \"value\": \"Hello123'\"\n" +
                        "      },\n" +
                        "      \"formatted\": {\n" +
                        "        \"type\": \"text\",\n" +
                        "        \"value\": \"Text Hello123'\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"columnWidths\": {\n" +
                        "    \"A\": 100\n" +
                        "  },\n" +
                        "  \"rowHeights\": {\n" +
                        "    \"1\": 30\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    public void testSaveCellDate() {
        this.createSpreadsheetSaveCellAndCheck(
                "2000/12/31",
                "{\n" +
                        "  \"cells\": {\n" +
                        "    \"A1\": {\n" +
                        "      \"formula\": {\n" +
                        "        \"text\": \"2000/12/31\",\n" +
                        "        \"token\": {\n" +
                        "          \"type\": \"spreadsheet-date-parser-token\",\n" +
                        "          \"value\": {\n" +
                        "            \"value\": [{\n" +
                        "              \"type\": \"spreadsheet-year-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": 2000,\n" +
                        "                \"text\": \"2000\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"/\",\n" +
                        "                \"text\": \"/\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-month-number-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": 12,\n" +
                        "                \"text\": \"12\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"/\",\n" +
                        "                \"text\": \"/\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-day-number-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": 31,\n" +
                        "                \"text\": \"31\"\n" +
                        "              }\n" +
                        "            }],\n" +
                        "            \"text\": \"2000/12/31\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"expression\": {\n" +
                        "          \"type\": \"value-expression\",\n" +
                        "          \"value\": {\n" +
                        "            \"type\": \"local-date\",\n" +
                        "            \"value\": \"2000-12-31\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"value\": {\n" +
                        "          \"type\": \"local-date\",\n" +
                        "          \"value\": \"2000-12-31\"\n" +
                        "        }\n" +
                        "      },\n" +
                        "      \"formatted\": {\n" +
                        "        \"type\": \"text\",\n" +
                        "        \"value\": \"Date 2000/12/31\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"columnWidths\": {\n" +
                        "    \"A\": 100\n" +
                        "  },\n" +
                        "  \"rowHeights\": {\n" +
                        "    \"1\": 30\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    public void testSaveCellDateTime() {
        this.createSpreadsheetSaveCellAndCheck(
                "2000/12/31 12:34",
                "{\n" +
                        "  \"cells\": {\n" +
                        "    \"A1\": {\n" +
                        "      \"formula\": {\n" +
                        "        \"text\": \"2000/12/31 12:34\",\n" +
                        "        \"token\": {\n" +
                        "          \"type\": \"spreadsheet-date-time-parser-token\",\n" +
                        "          \"value\": {\n" +
                        "            \"value\": [{\n" +
                        "              \"type\": \"spreadsheet-year-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": 2000,\n" +
                        "                \"text\": \"2000\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"/\",\n" +
                        "                \"text\": \"/\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-month-number-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": 12,\n" +
                        "                \"text\": \"12\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"/\",\n" +
                        "                \"text\": \"/\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-day-number-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": 31,\n" +
                        "                \"text\": \"31\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \" \",\n" +
                        "                \"text\": \" \"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-hour-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": 12,\n" +
                        "                \"text\": \"12\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \":\",\n" +
                        "                \"text\": \":\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-minute-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": 34,\n" +
                        "                \"text\": \"34\"\n" +
                        "              }\n" +
                        "            }],\n" +
                        "            \"text\": \"2000/12/31 12:34\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"expression\": {\n" +
                        "          \"type\": \"value-expression\",\n" +
                        "          \"value\": {\n" +
                        "            \"type\": \"local-date-time\",\n" +
                        "            \"value\": \"2000-12-31T12:34\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"value\": {\n" +
                        "          \"type\": \"local-date-time\",\n" +
                        "          \"value\": \"2000-12-31T12:34\"\n" +
                        "        }\n" +
                        "      },\n" +
                        "      \"formatted\": {\n" +
                        "        \"type\": \"text\",\n" +
                        "        \"value\": \"DateTime 2000/12/31 12:34\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"columnWidths\": {\n" +
                        "    \"A\": 100\n" +
                        "  },\n" +
                        "  \"rowHeights\": {\n" +
                        "    \"1\": 30\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    public void testSaveCellNumber() {
        this.createSpreadsheetSaveCellAndCheck(
                "123.456",
                "{\n" +
                        "  \"cells\": {\n" +
                        "    \"A1\": {\n" +
                        "      \"formula\": {\n" +
                        "        \"text\": \"123.456\",\n" +
                        "        \"token\": {\n" +
                        "          \"type\": \"spreadsheet-number-parser-token\",\n" +
                        "          \"value\": {\n" +
                        "            \"value\": [{\n" +
                        "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"123\",\n" +
                        "                \"text\": \"123\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-decimal-separator-symbol-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \".\",\n" +
                        "                \"text\": \".\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"456\",\n" +
                        "                \"text\": \"456\"\n" +
                        "              }\n" +
                        "            }],\n" +
                        "            \"text\": \"123.456\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"expression\": {\n" +
                        "          \"type\": \"value-expression\",\n" +
                        "          \"value\": {\n" +
                        "            \"type\": \"expression-number\",\n" +
                        "            \"value\": \"123.456\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"value\": {\n" +
                        "          \"type\": \"expression-number\",\n" +
                        "          \"value\": \"123.456\"\n" +
                        "        }\n" +
                        "      },\n" +
                        "      \"formatted\": {\n" +
                        "        \"type\": \"text\",\n" +
                        "        \"value\": \"Number 123.456\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"columnWidths\": {\n" +
                        "    \"A\": 100\n" +
                        "  },\n" +
                        "  \"rowHeights\": {\n" +
                        "    \"1\": 30\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    public void testSaveCellTime() {
        this.createSpreadsheetSaveCellAndCheck(
                "12:34",
                "{\n" +
                        "  \"cells\": {\n" +
                        "    \"A1\": {\n" +
                        "      \"formula\": {\n" +
                        "        \"text\": \"12:34\",\n" +
                        "        \"token\": {\n" +
                        "          \"type\": \"spreadsheet-time-parser-token\",\n" +
                        "          \"value\": {\n" +
                        "            \"value\": [{\n" +
                        "              \"type\": \"spreadsheet-hour-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": 12,\n" +
                        "                \"text\": \"12\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \":\",\n" +
                        "                \"text\": \":\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-minute-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": 34,\n" +
                        "                \"text\": \"34\"\n" +
                        "              }\n" +
                        "            }],\n" +
                        "            \"text\": \"12:34\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"expression\": {\n" +
                        "          \"type\": \"value-expression\",\n" +
                        "          \"value\": {\n" +
                        "            \"type\": \"local-time\",\n" +
                        "            \"value\": \"12:34\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"value\": {\n" +
                        "          \"type\": \"local-time\",\n" +
                        "          \"value\": \"12:34\"\n" +
                        "        }\n" +
                        "      },\n" +
                        "      \"formatted\": {\n" +
                        "        \"type\": \"text\",\n" +
                        "        \"value\": \"Time 12:34\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"columnWidths\": {\n" +
                        "    \"A\": 100\n" +
                        "  },\n" +
                        "  \"rowHeights\": {\n" +
                        "    \"1\": 30\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    public void testSaveCellExpressionString() {
        this.createSpreadsheetSaveCellAndCheck(
                "=\"Hello 123\"",
                "{\n" +
                        "  \"cells\": {\n" +
                        "    \"A1\": {\n" +
                        "      \"formula\": {\n" +
                        "        \"text\": \"=\\\"Hello 123\\\"\",\n" +
                        "        \"token\": {\n" +
                        "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                        "          \"value\": {\n" +
                        "            \"value\": [{\n" +
                        "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"=\",\n" +
                        "                \"text\": \"=\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-text-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": [{\n" +
                        "                  \"type\": \"spreadsheet-double-quote-symbol-parser-token\",\n" +
                        "                  \"value\": {\n" +
                        "                    \"value\": \"\\\"\",\n" +
                        "                    \"text\": \"\\\"\"\n" +
                        "                  }\n" +
                        "                }, {\n" +
                        "                  \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                        "                  \"value\": {\n" +
                        "                    \"value\": \"Hello 123\",\n" +
                        "                    \"text\": \"Hello 123\"\n" +
                        "                  }\n" +
                        "                }, {\n" +
                        "                  \"type\": \"spreadsheet-double-quote-symbol-parser-token\",\n" +
                        "                  \"value\": {\n" +
                        "                    \"value\": \"\\\"\",\n" +
                        "                    \"text\": \"\\\"\"\n" +
                        "                  }\n" +
                        "                }],\n" +
                        "                \"text\": \"\\\"Hello 123\\\"\"\n" +
                        "              }\n" +
                        "            }],\n" +
                        "            \"text\": \"=\\\"Hello 123\\\"\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"expression\": {\n" +
                        "          \"type\": \"value-expression\",\n" +
                        "          \"value\": \"Hello 123\"\n" +
                        "        },\n" +
                        "        \"value\": \"Hello 123\"\n" +
                        "      },\n" +
                        "      \"formatted\": {\n" +
                        "        \"type\": \"text\",\n" +
                        "        \"value\": \"Text Hello 123\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"columnWidths\": {\n" +
                        "    \"A\": 100\n" +
                        "  },\n" +
                        "  \"rowHeights\": {\n" +
                        "    \"1\": 30\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    public void testSaveCellExpressionNumber() {
        this.createSpreadsheetSaveCellAndCheck(
                "=1+2",
                "{\n" +
                        "  \"cells\": {\n" +
                        "    \"A1\": {\n" +
                        "      \"formula\": {\n" +
                        "        \"text\": \"=1+2\",\n" +
                        "        \"token\": {\n" +
                        "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                        "          \"value\": {\n" +
                        "            \"value\": [{\n" +
                        "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"=\",\n" +
                        "                \"text\": \"=\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-addition-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": [{\n" +
                        "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                        "                  \"value\": {\n" +
                        "                    \"value\": [{\n" +
                        "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                        "                      \"value\": {\n" +
                        "                        \"value\": \"1\",\n" +
                        "                        \"text\": \"1\"\n" +
                        "                      }\n" +
                        "                    }],\n" +
                        "                    \"text\": \"1\"\n" +
                        "                  }\n" +
                        "                }, {\n" +
                        "                  \"type\": \"spreadsheet-plus-symbol-parser-token\",\n" +
                        "                  \"value\": {\n" +
                        "                    \"value\": \"+\",\n" +
                        "                    \"text\": \"+\"\n" +
                        "                  }\n" +
                        "                }, {\n" +
                        "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                        "                  \"value\": {\n" +
                        "                    \"value\": [{\n" +
                        "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                        "                      \"value\": {\n" +
                        "                        \"value\": \"2\",\n" +
                        "                        \"text\": \"2\"\n" +
                        "                      }\n" +
                        "                    }],\n" +
                        "                    \"text\": \"2\"\n" +
                        "                  }\n" +
                        "                }],\n" +
                        "                \"text\": \"1+2\"\n" +
                        "              }\n" +
                        "            }],\n" +
                        "            \"text\": \"=1+2\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"expression\": {\n" +
                        "          \"type\": \"add-expression\",\n" +
                        "          \"value\": [{\n" +
                        "            \"type\": \"value-expression\",\n" +
                        "            \"value\": {\n" +
                        "              \"type\": \"expression-number\",\n" +
                        "              \"value\": \"1\"\n" +
                        "            }\n" +
                        "          }, {\n" +
                        "            \"type\": \"value-expression\",\n" +
                        "            \"value\": {\n" +
                        "              \"type\": \"expression-number\",\n" +
                        "              \"value\": \"2\"\n" +
                        "            }\n" +
                        "          }]\n" +
                        "        },\n" +
                        "        \"value\": {\n" +
                        "          \"type\": \"expression-number\",\n" +
                        "          \"value\": \"3\"\n" +
                        "        }\n" +
                        "      },\n" +
                        "      \"formatted\": {\n" +
                        "        \"type\": \"text\",\n" +
                        "        \"value\": \"Number 003.000\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"columnWidths\": {\n" +
                        "    \"A\": 100\n" +
                        "  },\n" +
                        "  \"rowHeights\": {\n" +
                        "    \"1\": 30\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    public void testSaveCellStampsMetadata() {
        final TestHttpServer server = this.createSpreadsheetSaveCellAndCheck(
                "'Hello123'",
                "{\n" +
                        "  \"cells\": {\n" +
                        "    \"A1\": {\n" +
                        "      \"formula\": {\n" +
                        "        \"text\": \"'Hello123'\",\n" +
                        "        \"token\": {\n" +
                        "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                        "          \"value\": {\n" +
                        "            \"value\": [{\n" +
                        "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"'\",\n" +
                        "                \"text\": \"'\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"Hello123'\",\n" +
                        "                \"text\": \"Hello123'\"\n" +
                        "              }\n" +
                        "            }],\n" +
                        "            \"text\": \"'Hello123'\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"expression\": {\n" +
                        "          \"type\": \"value-expression\",\n" +
                        "          \"value\": \"Hello123'\"\n" +
                        "        },\n" +
                        "        \"value\": \"Hello123'\"\n" +
                        "      },\n" +
                        "      \"formatted\": {\n" +
                        "        \"type\": \"text\",\n" +
                        "        \"value\": \"Text Hello123'\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"columnWidths\": {\n" +
                        "    \"A\": 100\n" +
                        "  },\n" +
                        "  \"rowHeights\": {\n" +
                        "    \"1\": 30\n" +
                        "  }\n" +
                        "}"
        );

        final SpreadsheetMetadata stamped = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, MODIFIED_DATE_TIME);

        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.toJson(stamped),
                        SpreadsheetMetadata.class.getSimpleName()
                )
        );
    }

    @Test
    public void testSaveCellSelectionQueryParameter() {
        this.createSpreadsheetSaveCellAndCheck(
                "=\"Hello 123\"",
                "?selectionType=cell&selection=A2",
                "{\n" +
                        "  \"selection\": {\n" +
                        "    \"selection\": {\n" +
                        "      \"type\": \"spreadsheet-cell-reference\",\n" +
                        "      \"value\": \"A2\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"cells\": {\n" +
                        "    \"A1\": {\n" +
                        "      \"formula\": {\n" +
                        "        \"text\": \"=\\\"Hello 123\\\"\",\n" +
                        "        \"token\": {\n" +
                        "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                        "          \"value\": {\n" +
                        "            \"value\": [{\n" +
                        "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"=\",\n" +
                        "                \"text\": \"=\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-text-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": [{\n" +
                        "                  \"type\": \"spreadsheet-double-quote-symbol-parser-token\",\n" +
                        "                  \"value\": {\n" +
                        "                    \"value\": \"\\\"\",\n" +
                        "                    \"text\": \"\\\"\"\n" +
                        "                  }\n" +
                        "                }, {\n" +
                        "                  \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                        "                  \"value\": {\n" +
                        "                    \"value\": \"Hello 123\",\n" +
                        "                    \"text\": \"Hello 123\"\n" +
                        "                  }\n" +
                        "                }, {\n" +
                        "                  \"type\": \"spreadsheet-double-quote-symbol-parser-token\",\n" +
                        "                  \"value\": {\n" +
                        "                    \"value\": \"\\\"\",\n" +
                        "                    \"text\": \"\\\"\"\n" +
                        "                  }\n" +
                        "                }],\n" +
                        "                \"text\": \"\\\"Hello 123\\\"\"\n" +
                        "              }\n" +
                        "            }],\n" +
                        "            \"text\": \"=\\\"Hello 123\\\"\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"expression\": {\n" +
                        "          \"type\": \"value-expression\",\n" +
                        "          \"value\": \"Hello 123\"\n" +
                        "        },\n" +
                        "        \"value\": \"Hello 123\"\n" +
                        "      },\n" +
                        "      \"formatted\": {\n" +
                        "        \"type\": \"text\",\n" +
                        "        \"value\": \"Text Hello 123\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"columnWidths\": {\n" +
                        "    \"A\": 100\n" +
                        "  },\n" +
                        "  \"rowHeights\": {\n" +
                        "    \"1\": 30\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    public void testSaveCellSelectionQueryParameterWithAnchor() {
        this.createSpreadsheetSaveCellAndCheck(
                "=\"Hello 123\"",
                "?selectionType=cell-range&selection=A1:B2&selectionAnchor=top-right",
                "{\n" +
                        "  \"selection\": {\n" +
                        "    \"selection\": {\n" +
                        "      \"type\": \"spreadsheet-cell-range\",\n" +
                        "      \"value\": \"A1:B2\"\n" +
                        "    },\n" +
                        "    \"anchor\": \"TOP_RIGHT\"\n" +
                        "  },\n" +
                        "  \"cells\": {\n" +
                        "    \"A1\": {\n" +
                        "      \"formula\": {\n" +
                        "        \"text\": \"=\\\"Hello 123\\\"\",\n" +
                        "        \"token\": {\n" +
                        "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                        "          \"value\": {\n" +
                        "            \"value\": [{\n" +
                        "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": \"=\",\n" +
                        "                \"text\": \"=\"\n" +
                        "              }\n" +
                        "            }, {\n" +
                        "              \"type\": \"spreadsheet-text-parser-token\",\n" +
                        "              \"value\": {\n" +
                        "                \"value\": [{\n" +
                        "                  \"type\": \"spreadsheet-double-quote-symbol-parser-token\",\n" +
                        "                  \"value\": {\n" +
                        "                    \"value\": \"\\\"\",\n" +
                        "                    \"text\": \"\\\"\"\n" +
                        "                  }\n" +
                        "                }, {\n" +
                        "                  \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                        "                  \"value\": {\n" +
                        "                    \"value\": \"Hello 123\",\n" +
                        "                    \"text\": \"Hello 123\"\n" +
                        "                  }\n" +
                        "                }, {\n" +
                        "                  \"type\": \"spreadsheet-double-quote-symbol-parser-token\",\n" +
                        "                  \"value\": {\n" +
                        "                    \"value\": \"\\\"\",\n" +
                        "                    \"text\": \"\\\"\"\n" +
                        "                  }\n" +
                        "                }],\n" +
                        "                \"text\": \"\\\"Hello 123\\\"\"\n" +
                        "              }\n" +
                        "            }],\n" +
                        "            \"text\": \"=\\\"Hello 123\\\"\"\n" +
                        "          }\n" +
                        "        },\n" +
                        "        \"expression\": {\n" +
                        "          \"type\": \"value-expression\",\n" +
                        "          \"value\": \"Hello 123\"\n" +
                        "        },\n" +
                        "        \"value\": \"Hello 123\"\n" +
                        "      },\n" +
                        "      \"formatted\": {\n" +
                        "        \"type\": \"text\",\n" +
                        "        \"value\": \"Text Hello 123\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"columnWidths\": {\n" +
                        "    \"A\": 100\n" +
                        "  },\n" +
                        "  \"rowHeights\": {\n" +
                        "    \"1\": 30\n" +
                        "  }\n" +
                        "}"
        );
    }

    @Test
    public void testSaveCellSelectionQueryParameterWithInvalidAnchorBadRequest() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1?selectionType=cell&selection=A1&selectionAnchor=!INVALID",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("A1")
                                                .setFormula(
                                                        formula("=1")
                                                )
                                )
                        )
                ),
                HttpStatusCode.BAD_REQUEST.setMessage("Invalid query parameter selectionAnchor=\"!INVALID\""),
                IllegalArgumentException.class.getSimpleName()
        );
    }

    @Test
    public void testSaveCellSelectionQueryParameterWithInvalidSelectionNavigationBadRequest() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1?selectionType=cell&selection=A1&selectionNavigation=!INVALID",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("A1")
                                                .setFormula(
                                                        formula("=1")
                                                )
                                )
                        )
                ),
                HttpStatusCode.BAD_REQUEST.setMessage("Invalid query parameter selectionNavigation=\"!INVALID\""),
                IllegalArgumentException.class.getSimpleName()
        );
    }

    @Test
    public void testCreateAndPatch() {
        final TestHttpServer server = this.startServer();

        // create spreadsheet
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        final SpreadsheetMetadata loaded = this.metadataStore.loadOrFail(SpreadsheetId.with(1L));
        this.checkNotEquals(
                null,
                loaded,
                () -> "spreadsheet metadata not created and saved: " + this.metadataStore
        );

        // patch metadata
        final String currency = "NSWD";

        server.handleAndCheck(
                HttpMethod.PATCH,
                "/api/spreadsheet/1",
                NO_HEADERS_TRANSACTION_ID,
                JsonNode.object()
                        .set(
                                JsonPropertyName.with(SpreadsheetMetadataPropertyName.CURRENCY_SYMBOL.value()),
                                JsonNode.string(currency)
                        )
                        .toString(),
                this.response(
                        HttpStatusCode.OK.status(),
                        loaded.set(SpreadsheetMetadataPropertyName.CURRENCY_SYMBOL, currency)
                )
        );
    }

    @Test
    public void testPatchFails() {
        final TestHttpServer server = this.startServer();

        // patch metadata will fail with 404
        server.handleAndCheck(
                HttpMethod.PATCH,
                "/api/spreadsheet/1",
                NO_HEADERS_TRANSACTION_ID,
                JsonNode.object()
                        .set(
                                JsonPropertyName.with(SpreadsheetMetadataPropertyName.CURRENCY_SYMBOL.value()),
                                JsonNode.string("NSWD")
                        )
                        .toString(),
                HttpStatusCode.NOT_FOUND.setMessage("Unable to load spreadsheet with id=1"),
                ""
        );
    }

    private TestHttpServer createSpreadsheetSaveCellAndCheck(final String formula,
                                                             final String responseJson) {
        return this.createSpreadsheetSaveCellAndCheck(formula, "", responseJson);
    }

    private TestHttpServer createSpreadsheetSaveCellAndCheck(final String formula,
                                                             final String queryParameters,
                                                             final String responseJson) {
        if(!queryParameters.isEmpty()) {
            UrlQueryString.with(queryParameters);
        }
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1" + queryParameters,
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("A1")
                                                .setFormula(
                                                        formula(formula)
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        responseJson,
                        DELTA
                )
        );

        return server;
    }

    @Test
    public void testSaveCellThenSaveAnotherCellReferencingFirst() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("A1")
                                                .setFormula(
                                                        formula("=1+2")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=1+2\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-addition-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"1\",\n" +
                                "                        \"text\": \"1\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"1\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-plus-symbol-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"+\",\n" +
                                "                    \"text\": \"+\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"2\",\n" +
                                "                        \"text\": \"2\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"2\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"1+2\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=1+2\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"add-expression\",\n" +
                                "          \"value\": [{\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"1\"\n" +
                                "            }\n" +
                                "          }, {\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"2\"\n" +
                                "            }\n" +
                                "          }]\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"3\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 003.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA));

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("B2")
                                                        .setFormula(
                                                                formula("=4+A1")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=4+A1\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-addition-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"4\",\n" +
                                "                        \"text\": \"4\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"4\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-plus-symbol-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"+\",\n" +
                                "                    \"text\": \"+\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-cell-reference-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-column-reference-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"A\",\n" +
                                "                        \"text\": \"A\"\n" +
                                "                      }\n" +
                                "                    }, {\n" +
                                "                      \"type\": \"spreadsheet-row-reference-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"1\",\n" +
                                "                        \"text\": \"1\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"A1\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"4+A1\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=4+A1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"add-expression\",\n" +
                                "          \"value\": [{\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"4\"\n" +
                                "            }\n" +
                                "          }, {\n" +
                                "            \"type\": \"reference-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"spreadsheet-cell-reference\",\n" +
                                "              \"value\": \"A1\"\n" +
                                "            }\n" +
                                "          }]\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"7\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 007.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testSaveCellTwice() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("A1")
                                                        .setFormula(
                                                                formula("=1+2")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=1+2\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-addition-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"1\",\n" +
                                "                        \"text\": \"1\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"1\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-plus-symbol-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"+\",\n" +
                                "                    \"text\": \"+\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"2\",\n" +
                                "                        \"text\": \"2\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"2\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"1+2\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=1+2\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"add-expression\",\n" +
                                "          \"value\": [{\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"1\"\n" +
                                "            }\n" +
                                "          }, {\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"2\"\n" +
                                "            }\n" +
                                "          }]\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"3\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 003.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA));

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(2L))
                )
        );

        this.checkEquals(2, this.metadataStore.count());

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/2/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("A1")
                                                        .setFormula(
                                                                formula("=3+4")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=3+4\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-addition-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"3\",\n" +
                                "                        \"text\": \"3\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"3\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-plus-symbol-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"+\",\n" +
                                "                    \"text\": \"+\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"4\",\n" +
                                "                        \"text\": \"4\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"4\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"3+4\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=3+4\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"add-expression\",\n" +
                                "          \"value\": [{\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"3\"\n" +
                                "            }\n" +
                                "          }, {\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"4\"\n" +
                                "            }\n" +
                                "          }]\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"7\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 007.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA));

        // create another cell in the first spreadsheet
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("B2")
                                                        .setFormula(
                                                                formula("=4+A1")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=4+A1\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-addition-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"4\",\n" +
                                "                        \"text\": \"4\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"4\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-plus-symbol-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"+\",\n" +
                                "                    \"text\": \"+\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-cell-reference-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-column-reference-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"A\",\n" +
                                "                        \"text\": \"A\"\n" +
                                "                      }\n" +
                                "                    }, {\n" +
                                "                      \"type\": \"spreadsheet-row-reference-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"1\",\n" +
                                "                        \"text\": \"1\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"A1\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"4+A1\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=4+A1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"add-expression\",\n" +
                                "          \"value\": [{\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"4\"\n" +
                                "            }\n" +
                                "          }, {\n" +
                                "            \"type\": \"reference-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"spreadsheet-cell-reference\",\n" +
                                "              \"value\": \"A1\"\n" +
                                "            }\n" +
                                "          }]\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"7\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 007.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA)
        );
    }

    @Test
    public void testPatchCellInvalidCellFails() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        server.handleAndCheck(
                HttpMethod.PATCH,
                "/api/spreadsheet/1/cell/!",
                NO_HEADERS_TRANSACTION_ID,
                "{\n" +
                        "  \"formula\": {\n" +
                        "    \"text\": \"=2\"\n" +
                        "  }\n" +
                        "}",
                HttpStatusCode.BAD_REQUEST
                        .setMessage("Invalid character '!' at 0 in \"!\""),
                ""
        );
    }

    @Test
    public void testPatchCell() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("A1")
                                                        .setFormula(
                                                                formula("=1+2")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=1+2\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-addition-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"1\",\n" +
                                "                        \"text\": \"1\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"1\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-plus-symbol-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"+\",\n" +
                                "                    \"text\": \"+\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"2\",\n" +
                                "                        \"text\": \"2\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"2\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"1+2\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=1+2\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"add-expression\",\n" +
                                "          \"value\": [{\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"1\"\n" +
                                "            }\n" +
                                "          }, {\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"2\"\n" +
                                "            }\n" +
                                "          }]\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"3\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 003.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        server.handleAndCheck(
                HttpMethod.PATCH,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                "{\n" +
                        "  \"cells\": {\n" +
                        "     \"a1\": {\n" +
                        "      \"formula\": {\n" +
                        "            \"text\": \"=2\"\n" +
                        "          }\n" +
                        "      }\n" +
                        "    }\n" +
                        "}",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=2\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"2\",\n" +
                                "                    \"text\": \"2\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"2\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=2\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"2\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"2\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 002.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testSaveCellPatchCellUnknownLabelFails() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        server.handleAndCheck(
                HttpMethod.PATCH,
                "/api/spreadsheet/1/cell/UnknownLabel123",
                NO_HEADERS_TRANSACTION_ID,
                "{\n" +
                        "  \"cells\": {\n" +
                        "     \"ZZZ\": {\n" +
                        "        \"formula\": {\n" +
                        "           \"text\": \"'PatchedText123\"\n" +
                        "        }\n" +
                        "     }\n" +
                        "  }\n" +
                        "}",
                HttpStatusCode.NOT_FOUND
                        .setMessage("Unknown Label: UnknownLabel123"),
                "Unknown Label: UnknownLabel123"
        );
    }

    @Test
    public void testSaveCellPatchCellLabel() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        final SpreadsheetLabelName label = SpreadsheetLabelName.labelName("ZZZ");
        final SpreadsheetCellReference cellReference = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetLabelMapping mapping = label.mapping(cellReference);

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/label/" + label,
                NO_HEADERS_TRANSACTION_ID,
                this.toJson(mapping),
                this.response(
                        HttpStatusCode.OK.status(),
                        this.toJson(mapping),
                        SpreadsheetLabelMapping.class.getSimpleName()
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/" + cellReference,
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                cellReference
                                                        .setFormula(
                                                                formula("'Hello")
                                                        )
                                        )
                                )
                ).replace(cellReference.toString(), label.toString()),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello\",\n" +
                                "                \"text\": \"Hello\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"labels\": [{\n" +
                                "    \"label\": \"ZZZ\",\n" +
                                "    \"reference\": \"B2\"\n" +
                                "  }],\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // url and payload use label and not reference
        server.handleAndCheck(
                HttpMethod.PATCH,
                "/api/spreadsheet/1/cell/" + label,
                NO_HEADERS_TRANSACTION_ID,
                "{\n" +
                        "  \"cells\": {\n" +
                        "     \"ZZZ\": {\n" +
                        "        \"formula\": {\n" +
                        "           \"text\": \"'PatchedText123\"\n" +
                        "        }\n" +
                        "     }\n" +
                        "  }\n" +
                        "}",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'PatchedText123\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"PatchedText123\",\n" +
                                "                \"text\": \"PatchedText123\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'PatchedText123\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"PatchedText123\"\n" +
                                "        },\n" +
                                "        \"value\": \"PatchedText123\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text PatchedText123\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"labels\": [{\n" +
                                "    \"label\": \"ZZZ\",\n" +
                                "    \"reference\": \"B2\"\n" +
                                "  }],\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testPatchCellSelectionQueryParameter() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("A1")
                                                        .setFormula(
                                                                formula("=1+2")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=1+2\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-addition-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"1\",\n" +
                                "                        \"text\": \"1\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"1\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-plus-symbol-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"+\",\n" +
                                "                    \"text\": \"+\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"2\",\n" +
                                "                        \"text\": \"2\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"2\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"1+2\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=1+2\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"add-expression\",\n" +
                                "          \"value\": [{\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"1\"\n" +
                                "            }\n" +
                                "          }, {\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"2\"\n" +
                                "            }\n" +
                                "          }]\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"3\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 003.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        server.handleAndCheck(
                HttpMethod.PATCH,
                "/api/spreadsheet/1/cell/A1?selectionType=cell&selection=B2",
                NO_HEADERS_TRANSACTION_ID,
                "{\n" +
                        "  \"cells\": {\n" +
                        "     \"a1\": {\n" +
                        "      \"formula\": {\n" +
                        "            \"text\": \"'PATCHED\"\n" +
                        "          }\n" +
                        "      }\n" +
                        "    }\n" +
                        "}",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"selection\": {\n" +
                                "    \"selection\": {\n" +
                                "      \"type\": \"spreadsheet-cell-reference\",\n" +
                                "      \"value\": \"B2\"\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'PATCHED\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"PATCHED\",\n" +
                                "                \"text\": \"PATCHED\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'PATCHED\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"PATCHED\"\n" +
                                "        },\n" +
                                "        \"value\": \"PATCHED\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text PATCHED\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testLoadViewport() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("A1")
                                                        .setFormula(
                                                                formula("=1+2")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=1+2\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-addition-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"1\",\n" +
                                "                        \"text\": \"1\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"1\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-plus-symbol-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"+\",\n" +
                                "                    \"text\": \"+\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"2\",\n" +
                                "                        \"text\": \"2\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"2\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"1+2\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=1+2\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"add-expression\",\n" +
                                "          \"value\": [{\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"1\"\n" +
                                "            }\n" +
                                "          }, {\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"2\"\n" +
                                "            }\n" +
                                "          }]\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"3\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 003.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA)
        );

        // create another cell in the first spreadsheet
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("B2")
                                                        .setFormula(
                                                                formula("=4+A1")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=4+A1\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-addition-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"4\",\n" +
                                "                        \"text\": \"4\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"4\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-plus-symbol-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"+\",\n" +
                                "                    \"text\": \"+\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-cell-reference-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-column-reference-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"A\",\n" +
                                "                        \"text\": \"A\"\n" +
                                "                      }\n" +
                                "                    }, {\n" +
                                "                      \"type\": \"spreadsheet-row-reference-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"1\",\n" +
                                "                        \"text\": \"1\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"A1\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"4+A1\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=4+A1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"add-expression\",\n" +
                                "          \"value\": [{\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"4\"\n" +
                                "            }\n" +
                                "          }, {\n" +
                                "            \"type\": \"reference-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"spreadsheet-cell-reference\",\n" +
                                "              \"value\": \"A1\"\n" +
                                "            }\n" +
                                "          }]\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"7\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 007.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA)
        );

        // load the cells that fill the viewport
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/*/force-recompute?home=A1&xOffset=0&yOffset=0&width=200&height=60&includeFrozenColumnsRows=false",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=1+2\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-addition-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"1\",\n" +
                                "                        \"text\": \"1\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"1\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-plus-symbol-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"+\",\n" +
                                "                    \"text\": \"+\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"2\",\n" +
                                "                        \"text\": \"2\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"2\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"1+2\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=1+2\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"add-expression\",\n" +
                                "          \"value\": [{\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"1\"\n" +
                                "            }\n" +
                                "          }, {\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"2\"\n" +
                                "            }\n" +
                                "          }]\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"3\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 003.000\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=4+A1\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-addition-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"4\",\n" +
                                "                        \"text\": \"4\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"4\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-plus-symbol-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"+\",\n" +
                                "                    \"text\": \"+\"\n" +
                                "                  }\n" +
                                "                }, {\n" +
                                "                  \"type\": \"spreadsheet-cell-reference-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": [{\n" +
                                "                      \"type\": \"spreadsheet-column-reference-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"A\",\n" +
                                "                        \"text\": \"A\"\n" +
                                "                      }\n" +
                                "                    }, {\n" +
                                "                      \"type\": \"spreadsheet-row-reference-parser-token\",\n" +
                                "                      \"value\": {\n" +
                                "                        \"value\": \"1\",\n" +
                                "                        \"text\": \"1\"\n" +
                                "                      }\n" +
                                "                    }],\n" +
                                "                    \"text\": \"A1\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"4+A1\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=4+A1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"add-expression\",\n" +
                                "          \"value\": [{\n" +
                                "            \"type\": \"value-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"expression-number\",\n" +
                                "              \"value\": \"4\"\n" +
                                "            }\n" +
                                "          }, {\n" +
                                "            \"type\": \"reference-expression\",\n" +
                                "            \"value\": {\n" +
                                "              \"type\": \"spreadsheet-cell-reference\",\n" +
                                "              \"value\": \"A1\"\n" +
                                "            }\n" +
                                "          }]\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"7\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 007.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100,\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30,\n" +
                                "    \"2\": 30\n" +
                                "  },\n" +
                                "  \"window\": \"A1:B2\"\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testLoadViewportWithSelectionQueryParameters() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("A1")
                                                        .setFormula(
                                                                formula("'Hello'")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello'\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello'\",\n" +
                                "                \"text\": \"Hello'\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello'\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello'\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello'\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello'\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA)
        );

        // load the cells that fill the viewport
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/*/force-recompute?home=A1&xOffset=0&yOffset=0&width=200&height=60&selectionType=cell&selection=A1&includeFrozenColumnsRows=false",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"selection\": {\n" +
                                "    \"selection\": {\n" +
                                "      \"type\": \"spreadsheet-cell-reference\",\n" +
                                "      \"value\": \"A1\"\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello'\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello'\",\n" +
                                "                \"text\": \"Hello'\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello'\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello'\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello'\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello'\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  },\n" +
                                "  \"window\": \"A1:B2\"\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testLoadViewportWithSelectionQueryParametersWithAnchor() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("A1")
                                                        .setFormula(
                                                                formula("'Hello'")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello'\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello'\",\n" +
                                "                \"text\": \"Hello'\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello'\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello'\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello'\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello'\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA)
        );

        // load the cells that fill the viewport
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/*/force-recompute?home=A1&xOffset=0&yOffset=0&width=200&height=60&selectionType=cell-range&selection=A1:B2&selectionAnchor=bottom-left&includeFrozenColumnsRows=false",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"selection\": {\n" +
                                "    \"selection\": {\n" +
                                "      \"type\": \"spreadsheet-cell-range\",\n" +
                                "      \"value\": \"A1:B2\"\n" +
                                "    },\n" +
                                "    \"anchor\": \"BOTTOM_LEFT\"\n" +
                                "  },\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello'\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello'\",\n" +
                                "                \"text\": \"Hello'\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello'\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello'\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello'\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello'\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  },\n" +
                                "  \"window\": \"A1:B2\"\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testLoadViewportWithSelectionQueryParametersAnchorDefaulted() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("A1")
                                                        .setFormula(
                                                                formula("'Hello'")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello'\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello'\",\n" +
                                "                \"text\": \"Hello'\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello'\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello'\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello'\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello'\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA)
        );

        // load the cells that fill the viewport
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/*/force-recompute?home=A1&xOffset=0&yOffset=0&width=200&height=60&selectionType=cell-range&selection=A1:B2&includeFrozenColumnsRows=false",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"selection\": {\n" +
                                "    \"selection\": {\n" +
                                "      \"type\": \"spreadsheet-cell-range\",\n" +
                                "      \"value\": \"A1:B2\"\n" +
                                "    },\n" +
                                "    \"anchor\": \"BOTTOM_RIGHT\"\n" +
                                "  },\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello'\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello'\",\n" +
                                "                \"text\": \"Hello'\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello'\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello'\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello'\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello'\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  },\n" +
                                "  \"window\": \"A1:B2\"\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testLoadViewportWithSelectionAndNavigationQueryParameters() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("A1")
                                                        .setFormula(
                                                                formula("'Hello'")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello'\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello'\",\n" +
                                "                \"text\": \"Hello'\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello'\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello'\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello'\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello'\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA)
        );

        // load the cells that fill the viewport
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/*/force-recompute?home=A1&xOffset=0&yOffset=0&width=200&height=60&selectionType=cell&selection=A1&selectionNavigation=extend-right&includeFrozenColumnsRows=false",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"selection\": {\n" +
                                "    \"selection\": {\n" +
                                "      \"type\": \"spreadsheet-cell-range\",\n" +
                                "      \"value\": \"A1:B1\"\n" +
                                "    },\n" +
                                "    \"anchor\": \"TOP_LEFT\"\n" +
                                "  },\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello'\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello'\",\n" +
                                "                \"text\": \"Hello'\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello'\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello'\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello'\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello'\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  },\n" +
                                "  \"window\": \"A1:B2\"\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testLoadViewportWithSelectionLabelAndNavigationQueryParameters() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        final SpreadsheetLabelName label123 = SpreadsheetLabelName.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label123.mapping(a1);

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/label/",
                NO_HEADERS_TRANSACTION_ID,
                toJson(mapping),
                this.response(
                        HttpStatusCode.OK.status(),
                        this.toJson(mapping),
                        SpreadsheetLabelMapping.class.getSimpleName()
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                a1.setFormula(
                                                        formula("'Hello'")
                                                )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello'\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello'\",\n" +
                                "                \"text\": \"Hello'\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello'\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello'\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello'\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello'\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"labels\": [{\n" +
                                "    \"label\": \"Label123\",\n" +
                                "    \"reference\": \"A1\"\n" +
                                "  }],\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA)
        );

        // load the cells that fill the viewport
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/*/force-recompute?home=A1&xOffset=0&yOffset=0&width=200&height=60&selectionType=label&selection=Label123&selectionNavigation=extend-right&includeFrozenColumnsRows=false",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"selection\": {\n" +
                                "    \"selection\": {\n" +
                                "      \"type\": \"spreadsheet-cell-range\",\n" +
                                "      \"value\": \"A1:B1\"\n" +
                                "    },\n" +
                                "    \"anchor\": \"TOP_LEFT\"\n" +
                                "  },\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello'\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello'\",\n" +
                                "                \"text\": \"Hello'\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello'\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello'\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello'\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello'\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"labels\": [{\n" +
                                "    \"label\": \"Label123\",\n" +
                                "    \"reference\": \"A1\"\n" +
                                "  }],\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  },\n" +
                                "  \"window\": \"A1:B2\"\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testLoadCellInvalidWindowQueryParameterBadRequest() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        // load the cells that fill the viewport
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/A1/force-recompute?window=!INVALID",
                NO_HEADERS_TRANSACTION_ID,
                "",
                HttpStatusCode.BAD_REQUEST.setMessage("Invalid query parameter window=\"!INVALID\""),
                IllegalArgumentException.class.getSimpleName()
        );
    }

    // save cell, save metadata, save cell..............................................................................

    @Test
    public void testSaveCellUpdateMetadataLoadCell() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L));

        // create a new spreadsheet.
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        initial
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("A1")
                                                        .setFormula(
                                                                formula("1.25")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"1.25\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"1\",\n" +
                                "                \"text\": \"1\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-decimal-separator-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \".\",\n" +
                                "                \"text\": \".\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"25\",\n" +
                                "                \"text\": \"25\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"1.25\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"1.25\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"1.25\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 001.250\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA)
        );

        // update metadata with a different decimal separator.
        final SpreadsheetMetadata updated = initial.set(SpreadsheetMetadataPropertyName.DECIMAL_SEPARATOR, ',');

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/" + initial.id().get(),
                NO_HEADERS_TRANSACTION_ID,
                updated.jsonNodeMarshallContext().marshall(updated).toString(),
                this.response(
                        HttpStatusCode.OK.status(),
                        updated
                )
        );

        this.checkEquals(1, this.metadataStore.count());

        // reload the saved cell
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/A1/force-recompute",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"1.25\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"1\",\n" +
                                "                \"text\": \"1\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-decimal-separator-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \".\",\n" +
                                "                \"text\": \".\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"25\",\n" +
                                "                \"text\": \"25\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"1.25\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"1.25\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"1.25\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 001,250\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );
    }

    // save cell, save metadata, save cell..............................................................................

    @Test
    public void testSaveCellThenSaveLabel() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L));

        // create a new spreadsheet.
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        initial
                )
        );

        final SpreadsheetLabelName label = SpreadsheetLabelName.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.mapping(SpreadsheetSelection.parseCell("A99"));

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/label/" + label,
                NO_HEADERS_TRANSACTION_ID,
                this.toJson(mapping),
                this.response(
                        HttpStatusCode.OK.status(),
                        this.toJson(mapping),
                        SpreadsheetLabelMapping.class.getSimpleName()
                )
        );
    }

    // cell-reference...................................................................................................

    @Test
    public void testCellReferenceGet() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L));

        // create a new spreadsheet.
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        initial
                )
        );

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");

        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell-reference/" + reference + "?count=100",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cell-reference\": \"A99\"\n" +
                                "}",
                        SpreadsheetExpressionReferenceSimilarities.class.getSimpleName()
                )
        );
    }

    // column...........................................................................................................

    @Test
    public void testClearColumn() {
        final TestHttpServer server = this.startServer();

        // create spreadsheet
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata().set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        // save cell B2
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("B2")
                                                .setFormula(
                                                        formula("'Hello")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello\",\n" +
                                "                \"text\": \"Hello\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // clear B
        server.handleAndCheck(HttpMethod.POST,
                "/api/spreadsheet/1/column/B/clear",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"deletedCells\": \"B2\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // GET deleted cell should return nothing.
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{}",
                        DELTA
                )
        );
    }

    @Test
    public void testClearColumnRange() {
        final TestHttpServer server = this.startServer();

        // create spreadsheet
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata().set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        // save cell B2
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("B2")
                                                .setFormula(
                                                        formula("'Hello")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello\",\n" +
                                "                \"text\": \"Hello\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // clear A:C
        server.handleAndCheck(HttpMethod.POST,
                "/api/spreadsheet/1/column/A:C/clear",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"deletedCells\": \"B2\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // GET deleted cell should return nothing.
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{}",
                        DELTA
                )
        );
    }

    @Test
    public void testColumnInsertAfter() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L));

        // create a new spreadsheet.
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(), initial)
        );

        // save a cell at C3
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/C3",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("C3")
                                                        .setFormula(
                                                                formula("=123")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"C3\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=123\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"123\",\n" +
                                "                    \"text\": \"123\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"123\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=123\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"123\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"123\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 123.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"C\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"3\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA)
        );

        // save a cell at D4
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/D4",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("D4")
                                                        .setFormula(formula("=456")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"D4\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=456\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"456\",\n" +
                                "                    \"text\": \"456\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"456\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"456\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 456.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"D\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"4\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA)
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/column/C/after?count=1",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"E4\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=456\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"456\",\n" +
                                "                    \"text\": \"456\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"456\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"456\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 456.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"deletedCells\": \"D4\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"D\": 100,\n" +
                                "    \"E\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"4\": 30\n" +
                                "  }\n" +
                                "}",
                        SpreadsheetDelta.class.getSimpleName()
                )
        );
    }

    @Test
    public void testColumnInsertBefore() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L));

        // create a new spreadsheet.
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(), initial)
        );

        // save a cell at C3
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/C3",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("C3")
                                                        .setFormula(
                                                                formula("=123")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"C3\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=123\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"123\",\n" +
                                "                    \"text\": \"123\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"123\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=123\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"123\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"123\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 123.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"C\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"3\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // save a cell at D4
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/D4",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("D4")
                                                        .setFormula(
                                                                formula("=456")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"D4\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=456\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"456\",\n" +
                                "                    \"text\": \"456\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"456\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"456\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 456.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"D\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"4\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/column/D/before?count=1",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"E4\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=456\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"456\",\n" +
                                "                    \"text\": \"456\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"456\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"456\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 456.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"deletedCells\": \"D4\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"D\": 100,\n" +
                                "    \"E\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"4\": 30\n" +
                                "  }\n" +
                                "}",
                        SpreadsheetDelta.class.getSimpleName()
                )
        );
    }

    @Test
    public void testColumnDeleteShiftsCell() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L));

        // create a new spreadsheet.
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(), initial)
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/C3",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("C3")
                                                        .setFormula(
                                                                formula("1.25")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"C3\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"1.25\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"1\",\n" +
                                "                \"text\": \"1\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-decimal-separator-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \".\",\n" +
                                "                \"text\": \".\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"25\",\n" +
                                "                \"text\": \"25\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"1.25\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"1.25\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"1.25\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 001.250\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"C\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"3\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        server.handleAndCheck(
                HttpMethod.DELETE,
                "/api/spreadsheet/1/column/B",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B3\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"1.25\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"1\",\n" +
                                "                \"text\": \"1\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-decimal-separator-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \".\",\n" +
                                "                \"text\": \".\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"25\",\n" +
                                "                \"text\": \"25\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"1.25\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"1.25\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"1.25\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 001.250\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"deletedCells\": \"C3\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100,\n" +
                                "    \"C\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"3\": 30\n" +
                                "  }\n" +
                                "}",
                        SpreadsheetDelta.class.getSimpleName()
                )
        );
    }

    // label............................................................................................................

    @Test
    public void testLabelSave() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L));

        // create a new spreadsheet.
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(), initial)
        );

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");
        final SpreadsheetLabelName label = SpreadsheetLabelName.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.mapping(reference);

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/label/",
                NO_HEADERS_TRANSACTION_ID,
                toJson(mapping),
                this.response(
                        HttpStatusCode.OK.status(),
                        this.toJson(mapping),
                        SpreadsheetLabelMapping.class.getSimpleName()
                )
        );
    }

    @Test
    public void testLabelSaveAndLoad() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L));

        // create a new spreadsheet.
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(), initial)
        );

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");
        final SpreadsheetLabelName label = SpreadsheetLabelName.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.mapping(reference);

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/label/",
                NO_HEADERS_TRANSACTION_ID,
                toJson(mapping),
                this.response(
                        HttpStatusCode.OK.status(),
                        this.toJson(mapping),
                        SpreadsheetLabelMapping.class.getSimpleName()
                )
        );

        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/label/" + label,
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.toJson(mapping),
                        SpreadsheetLabelMapping.class.getSimpleName()
                )
        );
    }

    @Test
    public void testLabelSaveAndResolveCellReference() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L));

        // create a new spreadsheet.
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(), initial)
        );

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");
        final SpreadsheetLabelName label = SpreadsheetLabelName.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.mapping(reference);

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/label/",
                NO_HEADERS_TRANSACTION_ID,
                toJson(mapping),
                this.response(
                        HttpStatusCode.OK.status(),
                        this.toJson(mapping),
                        SpreadsheetLabelMapping.class.getSimpleName()
                )
        );

        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell-reference/" + label + "?count=1",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.toJson(
                                SpreadsheetExpressionReferenceSimilarities.with(
                                        SpreadsheetExpressionReferenceSimilarities.NO_CELL_REFERENCE,
                                        SpreadsheetExpressionReferenceSimilarities.NO_LABEL,
                                        Sets.of(mapping)
                                )
                        ),
                        SpreadsheetExpressionReferenceSimilarities.class.getSimpleName()
                )
        );
    }

    @Test
    public void testLabelDelete() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L));

        // create a new spreadsheet.
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(), initial)
        );

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");
        final SpreadsheetLabelName label = SpreadsheetLabelName.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.mapping(reference);

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/label/",
                NO_HEADERS_TRANSACTION_ID,
                toJson(mapping),
                this.response(
                        HttpStatusCode.OK.status(),
                        this.toJson(mapping),
                        SpreadsheetLabelMapping.class.getSimpleName()
                )
        );

        server.handleAndCheck(
                HttpMethod.DELETE,
                "/api/spreadsheet/1/label/" + label,
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.NO_CONTENT.status()
                )
        );
    }

    // row...........................................................................................................

    @Test
    public void testClearRow() {
        final TestHttpServer server = this.startServer();

        // create spreadsheet
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata().set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        // save cell B2
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("B2")
                                                .setFormula(
                                                        formula("'Hello")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello\",\n" +
                                "                \"text\": \"Hello\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // clear B
        server.handleAndCheck(HttpMethod.POST,
                "/api/spreadsheet/1/row/2/clear",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"deletedCells\": \"B2\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // GET deleted cell should return nothing.
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{}",
                        DELTA
                )
        );
    }

    @Test
    public void testClearRowRange() {
        final TestHttpServer server = this.startServer();

        // create spreadsheet
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata().set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        // save cell B2
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("B2")
                                                .setFormula(
                                                        formula("'Hello")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello\",\n" +
                                "                \"text\": \"Hello\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // clear A:C
        server.handleAndCheck(HttpMethod.POST,
                "/api/spreadsheet/1/row/1:3/clear",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"deletedCells\": \"B2\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // GET deleted cell should return nothing.
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{}",
                        DELTA
                )
        );
    }

    @Test
    public void testRowInsertAfter() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L));

        // create a new spreadsheet.
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(), initial)
        );

        // save a cell at C3
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/C3",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("C3")
                                                        .setFormula(
                                                                formula("=123")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"C3\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=123\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"123\",\n" +
                                "                    \"text\": \"123\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"123\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=123\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"123\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"123\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 123.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"C\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"3\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // save a cell at D4
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/D4",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("D4")
                                                        .setFormula(
                                                                formula("=456")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"D4\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=456\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"456\",\n" +
                                "                    \"text\": \"456\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"456\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"456\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 456.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"D\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"4\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/row/3/after?count=1",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"D5\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=456\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"456\",\n" +
                                "                    \"text\": \"456\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"456\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"456\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 456.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"deletedCells\": \"D4\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"D\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"4\": 30,\n" +
                                "    \"5\": 30\n" +
                                "  }\n" +
                                "}",
                        SpreadsheetDelta.class.getSimpleName()
                )
        );
    }

    @Test
    public void testRowInsertBefore() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L));

        // create a new spreadsheet.
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(), initial)
        );

        // save a cell at C3
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/C3",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("C3")
                                                        .setFormula(
                                                                formula("=123")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"C3\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=123\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"123\",\n" +
                                "                    \"text\": \"123\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"123\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=123\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"123\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"123\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 123.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"C\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"3\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // save a cell at D4
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/D4",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("D4")
                                                        .setFormula(
                                                                formula("=456")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"D4\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=456\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"456\",\n" +
                                "                    \"text\": \"456\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"456\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"456\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 456.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"D\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"4\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/row/4/before?count=1",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"D5\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=456\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"456\",\n" +
                                "                    \"text\": \"456\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"456\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"456\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"456\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 456.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"deletedCells\": \"D4\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"D\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"4\": 30,\n" +
                                "    \"5\": 30\n" +
                                "  }\n" +
                                "}",
                        SpreadsheetDelta.class.getSimpleName()
                )
        );
    }

    @Test
    public void testRowDeleteShiftsCell() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L));

        // create a new spreadsheet.
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(), initial)
        );

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/C3",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("C3")
                                                        .setFormula(
                                                                formula("1.25")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"C3\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"1.25\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"1\",\n" +
                                "                \"text\": \"1\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-decimal-separator-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \".\",\n" +
                                "                \"text\": \".\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"25\",\n" +
                                "                \"text\": \"25\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"1.25\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"1.25\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"1.25\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 001.250\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"C\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"3\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA)
        );

        server.handleAndCheck(
                HttpMethod.DELETE,
                "/api/spreadsheet/1/row/2",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"C2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"1.25\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"1\",\n" +
                                "                \"text\": \"1\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-decimal-separator-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \".\",\n" +
                                "                \"text\": \".\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"25\",\n" +
                                "                \"text\": \"25\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"1.25\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"1.25\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"1.25\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 001.250\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"deletedCells\": \"C3\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"C\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30,\n" +
                                "    \"3\": 30\n" +
                                "  }\n" +
                                "}",
                        SpreadsheetDelta.class.getSimpleName()
                )
        );
    }

    // clear...........................................................................................................


    @Test
    public void testClearCell() {
        final TestHttpServer server = this.startServer();

        // create spreadsheet
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata().set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        // save cell B2
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("B2")
                                                .setFormula(
                                                        formula("'Hello")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello\",\n" +
                                "                \"text\": \"Hello\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // clear A1:B2
        server.handleAndCheck(HttpMethod.POST,
                "/api/spreadsheet/1/cell/B2/clear",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"deletedCells\": \"B2\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // GET deleted cell should return nothing.
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{}",
                        DELTA
                )
        );
    }

    @Test
    public void testClearCellRange() {
        final TestHttpServer server = this.startServer();

        // create spreadsheet
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata().set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        // save cell B2
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("B2")
                                                .setFormula(
                                                        formula("'Hello")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello\",\n" +
                                "                \"text\": \"Hello\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // clear A1:B2
        server.handleAndCheck(HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1:C3/clear",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"deletedCells\": \"B2\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // GET deleted cell should return nothing.
        server.handleAndCheck(
                HttpMethod.GET,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        "{}",
                        DELTA
                )
        );
    }

    // fillCell........................................................................................................

    @Test
    public void testFillCell() {
        final TestHttpServer server = this.startServer();

        // create spreadsheet
        server.handleAndCheck(HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))));

        // save cell B2
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("B2")
                                                .setFormula(
                                                        formula("'Hello")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello\",\n" +
                                "                \"text\": \"Hello\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // fill A1:B2
        server.handleAndCheck(HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1:B2/fill",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("A1")
                                                .setFormula(
                                                        formula("=1")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=1\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"1\",\n" +
                                "                    \"text\": \"1\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"1\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"1\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 001.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"deletedCells\": \"B2\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100,\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30,\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testFillCellRepeatsFromRange() {
        final TestHttpServer server = this.startServer();

        // create spreadsheet
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        // fill A1:B2 from A1
        server.handleAndCheck(HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1:B2/fill?from=A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("A1")
                                                        .setFormula(
                                                                formula("=1")
                                                        )
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=1\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"1\",\n" +
                                "                    \"text\": \"1\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"1\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"1\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 001.000\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    \"A2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=1\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"1\",\n" +
                                "                    \"text\": \"1\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"1\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"1\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 001.000\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    \"B1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=1\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"1\",\n" +
                                "                    \"text\": \"1\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"1\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"1\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 001.000\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=1\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"1\",\n" +
                                "                    \"text\": \"1\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"1\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"1\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 001.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100,\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30,\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testFillThatClears() {
        final TestHttpServer server = this.startServer();

        // create spreadsheet
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata().set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))
                )
        );

        // save cell B2
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/B2",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("B2")
                                                .setFormula(
                                                        formula("'Hello")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello\",\n" +
                                "                \"text\": \"Hello\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // fill A1:B2
        server.handleAndCheck(HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1:B2/fill",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"deletedCells\": \"B2\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testFillCellWithSelectionQueryParameter() {
        final TestHttpServer server = this.startServer();

        // create spreadsheet
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1L))));

        // save cell B2
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/B2?selectionType=cell&selection=C3",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("B2")
                                                .setFormula(
                                                        formula("'Hello")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"selection\": {\n" +
                                "    \"selection\": {\n" +
                                "      \"type\": \"spreadsheet-cell-reference\",\n" +
                                "      \"value\": \"C3\"\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"cells\": {\n" +
                                "    \"B2\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello\",\n" +
                                "                \"text\": \"Hello\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        // fill A1:B2
        server.handleAndCheck(HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1:B2/fill",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("A1")
                                                .setFormula(
                                                        formula("=1")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"=1\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-expression-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-equals-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"=\",\n" +
                                "                \"text\": \"=\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-number-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": [{\n" +
                                "                  \"type\": \"spreadsheet-digits-parser-token\",\n" +
                                "                  \"value\": {\n" +
                                "                    \"value\": \"1\",\n" +
                                "                    \"text\": \"1\"\n" +
                                "                  }\n" +
                                "                }],\n" +
                                "                \"text\": \"1\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"=1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": {\n" +
                                "            \"type\": \"expression-number\",\n" +
                                "            \"value\": \"1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"value\": {\n" +
                                "          \"type\": \"expression-number\",\n" +
                                "          \"value\": \"1\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Number 001.000\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"deletedCells\": \"B2\",\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100,\n" +
                                "    \"B\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30,\n" +
                                "    \"2\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );
    }

    // column...........................................................................................................

    @Test
    public void testPatchColumn() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        server.handleAndCheck(
                HttpMethod.PATCH,
                "/api/spreadsheet/1/column/A",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setColumns(
                                        Sets.of(
                                                SpreadsheetSelection.parseColumn("A")
                                                        .column()
                                                        .setHidden(true)
                                        )
                                ).setWindow(
                                        SpreadsheetSelection.parseWindow("A1:B2")
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"columns\": {\n" +
                                "    \"A\": {\n" +
                                "      \"hidden\": true\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"window\": \"A1:B2\"\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testSaveCellPatchColumn() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        // save cell A1
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("A1")
                                                .setFormula(
                                                        formula("'Hello A1")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello A1\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello A1\",\n" +
                                "                \"text\": \"Hello A1\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello A1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello A1\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello A1\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello A1\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        server.handleAndCheck(
                HttpMethod.PATCH,
                "/api/spreadsheet/1/column/A?window=A1:B2",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setColumns(
                                        Sets.of(
                                                SpreadsheetSelection.parseColumn("A")
                                                        .column()
                                                        .setHidden(true)
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"columns\": {\n" +
                                "    \"A\": {\n" +
                                "      \"hidden\": true\n" +
                                "    }\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        server.handleAndCheck(
                HttpMethod.PATCH,
                "/api/spreadsheet/1/column/A?window=A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setColumns(
                                        Sets.of(
                                                SpreadsheetSelection.parseColumn("A")
                                                        .column()
                                                        .setHidden(false)
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"columns\": {\n" +
                                "    \"A\": {\n" +
                                "      \"hidden\": false\n" +
                                "    }\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );
    }

    // row...........................................................................................................

    @Test
    public void testPatchRow() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        server.handleAndCheck(
                HttpMethod.PATCH,
                "/api/spreadsheet/1/row/1?window=A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setRows(
                                        Sets.of(
                                                SpreadsheetSelection.parseRow("1")
                                                        .row()
                                                        .setHidden(true)
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"rows\": {\n" +
                                "    \"1\": {\n" +
                                "      \"hidden\": true\n" +
                                "    }\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );
    }

    @Test
    public void testSaveCellPatchRow() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/",
                NO_HEADERS_TRANSACTION_ID,
                "",
                this.response(
                        HttpStatusCode.OK.status(),
                        this.createMetadata()
                                .set(
                                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                        SpreadsheetId.with(1L)
                                )
                )
        );

        // save cell A1
        server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/cell/A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        SpreadsheetSelection.parseCell("A1")
                                                .setFormula(
                                                        formula("'Hello A1")
                                                )
                                )
                        )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"cells\": {\n" +
                                "    \"A1\": {\n" +
                                "      \"formula\": {\n" +
                                "        \"text\": \"'Hello A1\",\n" +
                                "        \"token\": {\n" +
                                "          \"type\": \"spreadsheet-text-parser-token\",\n" +
                                "          \"value\": {\n" +
                                "            \"value\": [{\n" +
                                "              \"type\": \"spreadsheet-apostrophe-symbol-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"'\",\n" +
                                "                \"text\": \"'\"\n" +
                                "              }\n" +
                                "            }, {\n" +
                                "              \"type\": \"spreadsheet-text-literal-parser-token\",\n" +
                                "              \"value\": {\n" +
                                "                \"value\": \"Hello A1\",\n" +
                                "                \"text\": \"Hello A1\"\n" +
                                "              }\n" +
                                "            }],\n" +
                                "            \"text\": \"'Hello A1\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"expression\": {\n" +
                                "          \"type\": \"value-expression\",\n" +
                                "          \"value\": \"Hello A1\"\n" +
                                "        },\n" +
                                "        \"value\": \"Hello A1\"\n" +
                                "      },\n" +
                                "      \"formatted\": {\n" +
                                "        \"type\": \"text\",\n" +
                                "        \"value\": \"Text Hello A1\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"columnWidths\": {\n" +
                                "    \"A\": 100\n" +
                                "  },\n" +
                                "  \"rowHeights\": {\n" +
                                "    \"1\": 30\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        server.handleAndCheck(
                HttpMethod.PATCH,
                "/api/spreadsheet/1/row/1?window=A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setRows(
                                        Sets.of(
                                                SpreadsheetSelection.parseRow("1")
                                                        .row()
                                                        .setHidden(true)
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"rows\": {\n" +
                                "    \"1\": {\n" +
                                "      \"hidden\": true\n" +
                                "    }\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );

        server.handleAndCheck(
                HttpMethod.PATCH,
                "/api/spreadsheet/1/row/1?window=A1",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                        SpreadsheetDelta.EMPTY
                                .setRows(
                                        Sets.of(
                                                SpreadsheetSelection.parseRow("1")
                                                        .row()
                                                        .setHidden(false)
                                        )
                                )
                ),
                this.response(
                        HttpStatusCode.OK.status(),
                        "{\n" +
                                "  \"rows\": {\n" +
                                "    \"1\": {\n" +
                                "      \"hidden\": false\n" +
                                "    }\n" +
                                "  }\n" +
                                "}",
                        DELTA
                )
        );
    }

    // file server......................................................................................................

    @Test
    public void testFileFound() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(HttpMethod.POST,
                FILE.value(),
                Maps.of(
                        HttpHeaderName.ACCEPT,
                        Lists.of(MediaType.ALL.accept())
                ),
                "",
                this.response(HttpStatusCode.OK.status(),
                        HttpEntity.EMPTY
                                .addHeader(HttpHeaderName.CONTENT_TYPE, FILE_CONTENT_TYPE)
                                .addHeader(HttpHeaderName.CONTENT_LENGTH, 6L)
                                .addHeader(HttpHeaderName.LAST_MODIFIED, FILE_LAST_MODIFIED)
                                .setBody(FILE_BINARY)));
    }

    @Test
    public void testFileNotFound() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
                HttpMethod.POST,
                "/file/not/found.txt",
                HttpRequest.NO_HEADERS,
                "",
                FILE_NOT_FOUND,
                ""
        );
    }

    // helpers..........................................................................................................

    private TestHttpServer startServer() {
        SpreadsheetHttpServer.with(
                UrlScheme.HTTP,
                HostAddress.with("example.com"),
                IpPort.HTTP,
                Indentation.with("  "),
                LineEnding.NL,
                createMetadata(this.createMetadata(), this.metadataStore),
                fractioner(),
                idToFunctions(),
                this.idToRepository,
                this::fileServer,
                this::server,
                this::spreadsheetMetadataStamper,
                this::contentTypeFactory,
                LocalDateTime::now
        );
        this.httpServer.start();
        return this.httpServer;
    }

    /**
     * Creates a function which merges the given {@link Locale} with the given {@link SpreadsheetMetadata} and then saves it to the {@link SpreadsheetMetadataStore}.
     */
    private static Function<Optional<Locale>, SpreadsheetMetadata> createMetadata(final SpreadsheetMetadata metadataWithDefaults,
                                                                                  final SpreadsheetMetadataStore store) {
        return (locale) ->
                store.save(locale.map(l -> metadataWithDefaults.set(SpreadsheetMetadataPropertyName.LOCALE, l))
                        .orElse(metadataWithDefaults));
    }

    private SpreadsheetMetadata createMetadata() {
        final EmailAddress creator = EmailAddress.parse("user123@exaple.com");
        final LocalDateTime createDateTime = LocalDateTime.of(1999, 12, 31, 12, 58, 59);

        final EmailAddress modified = EmailAddress.parse("user123@exaple.com");
        final LocalDateTime modifiedDateTime = LocalDateTime.of(2000, 1, 2, 12, 58, 59);

        return SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.CREATOR, creator)
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, createDateTime)
                .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, modified)
                .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, modifiedDateTime)
                .set(SpreadsheetMetadataPropertyName.DATETIME_OFFSET, Converters.JAVA_EPOCH_OFFSET)
                .set(SpreadsheetMetadataPropertyName.EXPONENT_SYMBOL, "E")
                .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, EXPRESSION_NUMBER_KIND)
                .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.ENGLISH)
                .loadFromLocale()
                .set(SpreadsheetMetadataPropertyName.CELL_CHARACTER_WIDTH, 1)
                .set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, 1900)
                .set(SpreadsheetMetadataPropertyName.PRECISION, 10)
                .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                .set(SpreadsheetMetadataPropertyName.STYLE, SpreadsheetMetadata.NON_LOCALE_DEFAULTS.getOrFail(SpreadsheetMetadataPropertyName.STYLE))
                .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 20)
                .set(SpreadsheetMetadataPropertyName.DATE_FORMAT_PATTERN, SpreadsheetPattern.parseDateFormatPattern("\"Date\" yyyy/mm/dd"))
                .set(SpreadsheetMetadataPropertyName.DATE_PARSE_PATTERNS, SpreadsheetPattern.parseDateParsePatterns("yyyy/mm/dd"))
                .set(SpreadsheetMetadataPropertyName.DATETIME_FORMAT_PATTERN, SpreadsheetPattern.parseDateTimeFormatPattern("\"DateTime\" yyyy/mm/dd hh:mm"))
                .set(SpreadsheetMetadataPropertyName.DATETIME_PARSE_PATTERNS, SpreadsheetPattern.parseDateTimeParsePatterns("yyyy/mm/dd hh:mm"))
                .set(SpreadsheetMetadataPropertyName.NUMBER_FORMAT_PATTERN, SpreadsheetPattern.parseNumberFormatPattern("\"Number\" 000.000"))
                .set(SpreadsheetMetadataPropertyName.NUMBER_PARSE_PATTERNS, SpreadsheetPattern.parseNumberParsePatterns("000.000"))
                .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetPattern.parseTextFormatPattern("\"Text\" @"))
                .set(SpreadsheetMetadataPropertyName.TIME_FORMAT_PATTERN, SpreadsheetPattern.parseTimeFormatPattern("\"Time\" hh:mm"))
                .set(SpreadsheetMetadataPropertyName.TIME_PARSE_PATTERNS, SpreadsheetPattern.parseTimeParsePatterns("hh:mm"));
    }

    private static Function<BigDecimal, Fraction> fractioner() {
        return (n) -> {
            throw new UnsupportedOperationException();
        };
    }

    private static Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionEvaluationContext>>> idToFunctions() {
        return SpreadsheetHttpServerTest::functions;
    }

    /**
     * TODO Implement a real function lookup, that only exposes functions that are enabled for a single spreadsheet.
     */
    private static Function<FunctionExpressionName, ExpressionFunction<?, ExpressionEvaluationContext>> functions(final SpreadsheetId id) {
        return (n) -> {
            throw new UnknownExpressionFunctionException(n);
        };
    }

    private final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();
    private final Function<SpreadsheetId, SpreadsheetStoreRepository> idToRepository = idToRepository(Maps.concurrent(),
            storeRepositorySupplier(this.metadataStore));

    /**
     * Retrieves from the cache or lazily creates a {@link SpreadsheetStoreRepository} for the given {@link SpreadsheetId}.
     */
    static Function<SpreadsheetId, SpreadsheetStoreRepository> idToRepository(final Map<SpreadsheetId, SpreadsheetStoreRepository> idToRepository,
                                                                              final Supplier<SpreadsheetStoreRepository> repositoryFactory) {
        return (id) -> {
            SpreadsheetStoreRepository repository = idToRepository.get(id);
            if (null == repository) {
                repository = repositoryFactory.get();
                idToRepository.put(id, repository); // TODO add locks etc.
            }
            return repository;
        };
    }

    /**
     * Creates a new {@link SpreadsheetStoreRepository} on demand
     */
    private static Supplier<SpreadsheetStoreRepository> storeRepositorySupplier(final SpreadsheetMetadataStore metadataStore) {
        return () -> SpreadsheetStoreRepositories.basic(
                SpreadsheetCellStores.treeMap(),
                SpreadsheetExpressionReferenceStores.treeMap(),
                SpreadsheetColumnStores.treeMap(),
                SpreadsheetGroupStores.treeMap(),
                SpreadsheetLabelStores.treeMap(),
                SpreadsheetExpressionReferenceStores.treeMap(),
                metadataStore,
                SpreadsheetCellRangeStores.treeMap(),
                SpreadsheetCellRangeStores.treeMap(),
                SpreadsheetRowStores.treeMap(),
                SpreadsheetUserStores.treeMap()
        );
    }

    private Either<WebFile, HttpStatus> fileServer(final UrlPath path) {
        return path.equals(FILE) ?
                Either.left(new WebFile() {
                    @Override
                    public LocalDateTime lastModified() {
                        return FILE_LAST_MODIFIED;
                    }

                    @Override
                    public MediaType contentType() {
                        return FILE_CONTENT_TYPE;
                    }

                    @Override
                    public long contentSize() {
                        return FILE_BINARY.size();
                    }

                    @Override
                    public InputStream content() {
                        return FILE_BINARY.inputStream();
                    }

                    @Override
                    public Optional<ETag> etag() {
                        return Optional.empty();
                    }

                    @Override
                    public String toString() {
                        return path.toString();
                    }
                }) :
                Either.right(FILE_NOT_FOUND);
    }

    private static SpreadsheetFormula formula(final String text) {
        return SpreadsheetFormula.EMPTY
                .setText(text);
    }

    /**
     * Initializes the test {@link HttpServer}.
     */
    private HttpServer server(final BiConsumer<HttpRequest, HttpResponse> handler) {
        this.checkNotEquals(null, handler, "handler");
        this.httpServer.setHandler(handler);
        return this.httpServer;
    }

    private final TestHttpServer httpServer = new TestHttpServer();

    private SpreadsheetMetadata spreadsheetMetadataStamper(final SpreadsheetMetadata metadata) {
        return metadata.set(
                SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME,
                MODIFIED_DATE_TIME
        );
    }

    private HateosContentType contentTypeFactory(final SpreadsheetMetadata metadata,
                                                 final SpreadsheetLabelStore labelStore) {
        return SpreadsheetContexts.jsonHateosContentType(metadata, labelStore);
    }

    /**
     * A {@link HttpServer} that allows direct invocation of the main handler skipping the HTTP transport layer
     */
    private class TestHttpServer implements HttpServer {

        private TestHttpServer() {
            super();
        }

        void setHandler(final BiConsumer<HttpRequest, HttpResponse> handler) {
            this.handler = handler;
        }

        @Override
        public void start() {
            this.started = true;
        }

        @Override
        public void stop() {
            this.started = false;
        }

        void handleAndCheck(final HttpMethod method,
                            final String url,
                            final Map<HttpHeaderName<?>, List<?>> headers,
                            final String body,
                            final HttpStatus status,
                            final String bodyTextContains) {
            this.handleAndCheck(
                    request(method, url, headers, body),
                    status,
                    bodyTextContains
            );
        }

        void handleAndCheck(final HttpRequest request,
                            final HttpStatus status,
                            final String bodyTextContains) {
            final HttpResponse response = this.handle(request);
            checkEquals(status, response.status().orElse(null), "status");

            final List<HttpEntity> entities = response.entities();
            checkEquals(1, entities.size(), () -> "" + request + "\n" + response);

            final HttpEntity first = entities.get(0);
            final String body = first.bodyText();

            assertTrue(body.contains(bodyTextContains), () -> "" + request + "\n" + response);
        }

        void handleAndCheck(final HttpMethod method,
                            final String url,
                            final Map<HttpHeaderName<?>, List<?>> headers,
                            final String body,
                            final HttpResponse expected) {
            this.handleAndCheck(request(method, url, headers, body), expected);
        }

        void handleAndCheck(final HttpRequest request,
                            final HttpResponse expected) {
            final HttpResponse response = this.handle(request);

            checkEquals(expected, response, () -> "" + request);
        }

        HttpResponse handle(final HttpRequest request) {
            if (!this.started) {
                Assertions.fail("Server not running");
            }
            final HttpResponse response = HttpResponses.recording();
            this.handler.accept(request, response);
            checkNotEquals(null, response.status(), "status not set");
            return response;
        }

        private boolean started;
        private BiConsumer<HttpRequest, HttpResponse> handler;

        @Override
        public String toString() {
            return this.handler.toString();
        }
    }

    private static Binary binary(final String body,
                                 final MediaType contentType) {
        return Binary.with(bytes(body, contentType));
    }

    /**
     * Turns the text to bytes using the {@link MediaType content type}.
     */
    private static byte[] bytes(final String body,
                                final MediaType contentType) {
        final Charset charset = MediaTypeParameterName.CHARSET.parameterValue(contentType)
                .orElseThrow(() -> new IllegalStateException("Charset missing from " + contentType))
                .charset()
                .orElseThrow(() -> new IllegalStateException("Content type missing charset " + contentType));
        return body.getBytes(charset);
    }

    private static HttpRequest request(final HttpMethod method,
                                       final String url,
                                       final Map<HttpHeaderName<?>, List<?>> headers,
                                       final String body) {
        final Map<HttpHeaderName<?>, List<?>> headers2 = Maps.sorted();
        headers2.put(HttpHeaderName.ACCEPT, list(CONTENT_TYPE_UTF8.accept()));
        headers2.put(HttpHeaderName.ACCEPT_CHARSET, list(AcceptCharset.parse(CHARSET.toHeaderText())));
        headers2.put(HttpHeaderName.CONTENT_TYPE, list(CONTENT_TYPE_UTF8));
        headers2.putAll(headers);

        final byte[] bodyBytes = bytes(body, CONTENT_TYPE_UTF8);
        if (null != bodyBytes) {
            headers2.put(HttpHeaderName.CONTENT_LENGTH, list((long) bodyBytes.length));
        }
        return new HttpRequest() {
            @Override
            public HttpTransport transport() {
                return HttpTransport.SECURED;
            }

            @Override
            public HttpProtocolVersion protocolVersion() {
                return HttpProtocolVersion.VERSION_1_0;
            }

            @Override
            public HttpMethod method() {
                return method;
            }

            @Override
            public RelativeUrl url() {
                return Url.parseRelative(url);
            }

            @Override
            public Map<HttpHeaderName<?>, List<?>> headers() {
                return headers2;
            }

            @Override
            public byte[] body() {
                return bodyBytes;
            }

            @Override
            public Map<HttpRequestParameterName, List<String>> parameters() {
                final Map<HttpRequestParameterName, List<String>> parameters = Maps.ordered();

                this.url()
                        .query()
                        .parameters()
                        .forEach((key, value) -> parameters.put(HttpRequestParameterName.with(key.value()), value));

                return Maps.immutable(parameters);
            }

            @Override
            public List<String> parameterValues(final HttpRequestParameterName parameterName) {
                return Optional.ofNullable(this.parameters().get(parameterName)).orElse(Lists.empty());
            }

            @Override
            public String toString() {
                return method + " " + url + "\n" + headers.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue() + "\n").collect(Collectors.joining()) + "\n" + body;
            }
        };
    }

    private static <T> List<T> list(final T... values) {
        return Lists.of(values);
    }

    private HttpResponse response(final HttpStatus status) {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(status);
        return response;
    }

    private HttpResponse response(final HttpStatus status,
                                  final SpreadsheetMetadata body) {
        return this.response(status,
                NO_TRANSACTION_ID,
                toJson(body),
                SpreadsheetMetadata.class.getSimpleName());
    }

    private HttpResponse response(final HttpStatus status,
                                  final String body,
                                  final String bodyTypeName) {
        return this.response(status,
                NO_TRANSACTION_ID,
                binary(body, CONTENT_TYPE_UTF8),
                bodyTypeName);
    }

    private HttpResponse response(final HttpStatus status,
                                  final Optional<String> transactionId,
                                  final String body,
                                  final String bodyTypeName) {
        return this.response(status,
                transactionId,
                binary(body, CONTENT_TYPE_UTF8),
                bodyTypeName);
    }

    private HttpResponse response(final HttpStatus status,
                                  final Optional<String> transactionId,
                                  final Binary body,
                                  final String bodyTypeName) {
        return this.response(status,
                HttpEntity.EMPTY
                        .setHeader(SpreadsheetHttpServer.TRANSACTION_ID, transactionId.map(Lists::of).orElse(Lists.empty()))
                        .addHeader(HttpHeaderName.CONTENT_TYPE, CONTENT_TYPE_UTF8)
                        .addHeader(HateosResourceMapping.X_CONTENT_TYPE_NAME, bodyTypeName)
                        .addHeader(HttpHeaderName.CONTENT_LENGTH, (long) body.value().length)
                        .setBody(body));
    }

    private HttpResponse response(final HttpStatus status,
                                  final HttpEntity body) {
        final HttpResponse response = this.response(status);
        response.addEntity(body);
        return response;
    }

    private String toJson(final Object body) {
        return JsonNodeMarshallContexts.basic().marshall(body).toString();
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetHttpServer> type() {
        return SpreadsheetHttpServer.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }

    // TypeNameTesting..................................................................................................

    @Override
    public String typeNamePrefix() {
        return "Spreadsheet";
    }

    @Override
    public String typeNameSuffix() {
        return "Server";
    }
}
