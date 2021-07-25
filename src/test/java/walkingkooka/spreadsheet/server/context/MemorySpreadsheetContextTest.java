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

package walkingkooka.spreadsheet.server.context;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.Converters;
import walkingkooka.math.Fraction;
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
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestParameterName;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetRangeStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.store.Store;
import walkingkooka.tree.expression.ExpressionNumberContexts;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionContext;
import walkingkooka.tree.expression.function.UnknownExpressionFunctionException;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class MemorySpreadsheetContextTest implements SpreadsheetContextTesting<MemorySpreadsheetContext> {

    private final static ExpressionNumberKind EXPRESSION_NUMBER_KIND = ExpressionNumberKind.DEFAULT;
    private final static LocalDateTime MODIFIED_DATE_TIME = LocalDateTime.of(2021, 7, 15, 20, 20);

    @Test
    public void testWithNullBaseFails() {
        this.withFails(
                null,
                this.contentType(),
                this::fractioner,
                this::createMetadata,
                this::spreadsheetIdFunctions,
                this::spreadsheetIdToRepository,
                this::spreadsheetMetadataStamper
        );
    }

    @Test
    public void testWithNullContentTypeFails() {
        this.withFails(
                this.base(),
                null,
                this::fractioner,
                this::createMetadata,
                this::spreadsheetIdFunctions,
                this::spreadsheetIdToRepository,
                this::spreadsheetMetadataStamper
        );
    }

    @Test
    public void testWithNullFractionerFails() {
        this.withFails(
                this.base(),
                this.contentType(),
                null,
                this::createMetadata,
                this::spreadsheetIdFunctions,
                this::spreadsheetIdToRepository,
                this::spreadsheetMetadataStamper
        );
    }

    @Test
    public void testWithNullCreateMetadataFails() {
        this.withFails(
                this.base(),
                this.contentType(),
                this::fractioner,
                null,
                this::spreadsheetIdFunctions,
                this::spreadsheetIdToRepository,
                this::spreadsheetMetadataStamper
        );
    }

    @Test
    public void testWithNullSpreadsheetIdFunctionsFails() {
        this.withFails(
                this.base(),
                this.contentType(),
                this::fractioner,
                this::createMetadata,
                null,
                this::spreadsheetIdToRepository,
                this::spreadsheetMetadataStamper);
    }

    @Test
    public void testWithNullSpreadsheetIdRepositoryFails() {
        this.withFails(
                this.base(),
                this.contentType(),
                this::fractioner,
                this::createMetadata,
                this::spreadsheetIdFunctions,
                null,
                this::spreadsheetMetadataStamper);
    }

    @Test
    public void testWithNullSpreadsheetMetadataStamperFails() {
        this.withFails(
                this.base(),
                this.contentType(),
                this::fractioner,
                this::createMetadata,
                this::spreadsheetIdFunctions,
                this::spreadsheetIdToRepository,
                null
        );
    }

    private void withFails(final AbsoluteUrl base,
                           final HateosContentType contentType,
                           final Function<BigDecimal, Fraction> fractioner,
                           final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                           final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> spreadsheetIdFunctions,
                           final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository,
                           final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper) {
        assertThrows(
                NullPointerException.class,
                () -> MemorySpreadsheetContext.with(
                base,
                contentType,
                fractioner,
                createMetadata,
                spreadsheetIdFunctions,
                spreadsheetIdToRepository,
                spreadsheetMetadataStamper)
        );
    }

    @Test
    public void testFunctions() {
        assertNotEquals(null, this.createContext().functions(this.spreadsheetId()));
    }

    @Test
    public void testHateosRouter() {
        assertNotEquals(null, this.createContext().hateosRouter(this.spreadsheetId()));
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
                        "            \"type\": \"expression-number-expression\",\n" +
                        "            \"value\": \"1\"\n" +
                        "          }, {\n" +
                        "            \"type\": \"expression-number-expression\",\n" +
                        "            \"value\": \"2\"\n" +
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
                        "    \"B\": 100\n" +
                        "  },\n" +
                        "  \"maxRowHeights\": {\n" +
                        "    \"2\": 30\n" +
                        "  }\n" +
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
                        "            \"type\": \"expression-number-expression\",\n" +
                        "            \"value\": \"1\"\n" +
                        "          }, {\n" +
                        "            \"type\": \"expression-number-expression\",\n" +
                        "            \"value\": \"2\"\n" +
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
                        "    \"B\": 100\n" +
                        "  },\n" +
                        "  \"maxRowHeights\": {\n" +
                        "    \"2\": 30\n" +
                        "  }\n" +
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
                        "            \"type\": \"expression-number-expression\",\n" +
                        "            \"value\": \"1\"\n" +
                        "          }, {\n" +
                        "            \"type\": \"expression-number-expression\",\n" +
                        "            \"value\": \"2\"\n" +
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
                        "    \"B\": 100\n" +
                        "  },\n" +
                        "  \"maxRowHeights\": {\n" +
                        "    \"2\": 30\n" +
                        "  }\n" +
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
                        "            \"type\": \"expression-number-expression\",\n" +
                        "            \"value\": \"1\"\n" +
                        "          }, {\n" +
                        "            \"type\": \"expression-number-expression\",\n" +
                        "            \"value\": \"2\"\n" +
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
                        "    \"B\": 100\n" +
                        "  },\n" +
                        "  \"maxRowHeights\": {\n" +
                        "    \"2\": 30\n" +
                        "  }\n" +
                        "}"
        );
    }

    private void hateosRouterThenSaveThenLoadAndCheck(final SpreadsheetEngineEvaluation evaluation,
                                                      final String expectedBody) {
        final MemorySpreadsheetContext context = this.createContext();
        final SpreadsheetId id = this.spreadsheetId();
        final Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> router = context.hateosRouter(id);

        final SpreadsheetCellReference cellReference = SpreadsheetExpressionReference.parseCellReference("B2");
        final SpreadsheetCell cell = SpreadsheetCell.with(cellReference, SpreadsheetFormula.with("=1+2"));

        // save a cell
        {
            final HttpRequest request = new TestHttpRequest() {
                @Override
                public HttpMethod method() {
                    return HttpMethod.POST;
                }

                @Override
                public RelativeUrl url() {
                    return Url.parseRelative("/api987/123def/cell/B2/");
                }

                @Override
                public Map<HttpHeaderName<?>, List<?>> headers() {
                    final MediaType contentType = contentType().contentType();
                    return headersMap(
                            HttpHeaderName.ACCEPT, contentType.accept(),
                            HttpHeaderName.ACCEPT_CHARSET, AcceptCharset.parse("UTF-8"),
                            HttpHeaderName.CONTENT_LENGTH, Long.valueOf(this.bodyText().length()),
                            HttpHeaderName.CONTENT_TYPE, contentType
                    );
                }

                public Map<HttpRequestParameterName, List<String>> parameters() {
                    return HttpRequest.NO_PARAMETERS;
                }

                @Override
                public String bodyText() {
                    return marshallContext().marshall(SpreadsheetDelta.with(Sets.of(cell)))
                            .toString();
                }

                @Override
                public String toString() {
                    return this.method() + " " + this.url();
                }
            };

            final Optional<BiConsumer<HttpRequest, HttpResponse>> mapped = router.route(request.routerParameters());
            assertNotEquals(Optional.empty(), mapped, "request " + request.routerParameters());

            final HttpResponse response = HttpResponses.recording();
            @SuppressWarnings("OptionalGetWithoutIsPresent") final BiConsumer<HttpRequest, HttpResponse> consumer = mapped.get();
            consumer.accept(request, response);
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
                    final MediaType contentType = contentType().contentType();
                    return headersMap(
                            HttpHeaderName.ACCEPT, contentType.accept(),
                            HttpHeaderName.ACCEPT_CHARSET, AcceptCharset.parse("UTF-8"),
                            HttpHeaderName.CONTENT_LENGTH, Long.valueOf(this.bodyText().length()),
                            HttpHeaderName.CONTENT_TYPE, contentType
                    );
                }

                public Map<HttpRequestParameterName, List<String>> parameters() {
                    return HttpRequest.NO_PARAMETERS;
                }

                @Override
                public String bodyText() {
                    return "";
                }
            };

            final Optional<BiConsumer<HttpRequest, HttpResponse>> mapped = router.route(request.routerParameters());
            assertNotEquals(Optional.empty(), mapped, "request " + request.parameters());

            final HttpResponse response = HttpResponses.recording();
            //noinspection OptionalGetWithoutIsPresent
            final BiConsumer<HttpRequest, HttpResponse> consumer = mapped.get();
            consumer.accept(request, response);

            final HttpResponse expected = HttpResponses.recording();
            expected.setStatus(HttpStatusCode.OK.setMessage("GET SpreadsheetDelta OK"));

            expected.addEntity(HttpEntity.EMPTY
                    .addHeader(HttpHeaderName.CONTENT_TYPE, contentType().contentType().setCharset(CharsetName.UTF_8))
                    .addHeader(HateosResourceMapping.X_CONTENT_TYPE_NAME, SpreadsheetDelta.class.getSimpleName())
                    .setBodyText(expectedBody)
                    .setContentLength());

            assertEquals(expected, response, () -> "consumer: " + consumer + ", request: " + request);
        }
    }

    @Test
    public void testHateosRouterAndRouteInvalidRequest() {
        final MemorySpreadsheetContext context = this.createContext();
        final SpreadsheetId id = this.spreadsheetId();
        final Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> router = context.hateosRouter(id);

        final HttpRequest request = new TestHttpRequest() {
            @Override
            public HttpMethod method() {
                return HttpMethod.GET;
            }

            @Override
            public RelativeUrl url() {
                return Url.parseRelative("/INVALID");
            }

            @Override
            public Map<HttpHeaderName<?>, List<?>> headers() {
                return HttpRequest.NO_HEADERS;
            }

            public Map<HttpRequestParameterName, List<String>> parameters() {
                return HttpRequest.NO_PARAMETERS;
            }

            @Override
            public byte[] body() {
                return new byte[0];
            }
        };

        final Optional<BiConsumer<HttpRequest, HttpResponse>> mapped = router.route(request.routerParameters());
        assertEquals(Optional.empty(), mapped, "request " + request.parameters());
    }

    @Test
    public void testMetadataWithDefaultsWithLocale() {
        final Optional<Locale> locale = Optional.of(Locale.ENGLISH);
        assertEquals(this.createMetadata(locale),
                this.createContext().createMetadata(locale));
    }

    @Test
    public void testMetadataWithDefaultsWithoutLocale() {
        final Optional<Locale> locale = Optional.empty();
        assertEquals(this.createMetadata(locale),
                this.createContext().createMetadata(locale));
    }

    @Test
    public void testStoreRepositoryUnknownSpreadsheetId() {
        final MemorySpreadsheetContext context = this.createContext();
        final SpreadsheetId id = SpreadsheetId.with(123);

        final SpreadsheetStoreRepository repository = context.storeRepository(id);
        assertNotEquals(null, repository);

        this.countAndCheck(repository.cells(), 0);
        this.countAndCheck(repository.cellReferences(), 0);
        this.countAndCheck(repository.groups(), 0);
        this.countAndCheck(repository.labels(), 0);
        this.countAndCheck(repository.labelReferences(), 0);
        this.countAndCheck(repository.rangeToCells(), 0);
        this.countAndCheck(repository.rangeToConditionalFormattingRules(), 0);
        this.countAndCheck(repository.users(), 0);

        repository.cells().save(SpreadsheetCell.with(SpreadsheetExpressionReference.parseCellReference("A1"), SpreadsheetFormula.with("1+2")));
        this.countAndCheck(repository.cells(), 1);
    }

    @Test
    public void testStoreRepositoryDifferentSpreadsheetId() {
        final MemorySpreadsheetContext context = this.createContext();

        final SpreadsheetId id1 = SpreadsheetId.with(111);
        final SpreadsheetStoreRepository repository1 = context.storeRepository(id1);
        assertNotEquals(null, repository1);

        final SpreadsheetId id2 = SpreadsheetId.with(222);
        final SpreadsheetStoreRepository repository2 = context.storeRepository(id2);
        assertNotEquals(null, repository2);
    }

    @Test
    public void testStoreRepositorySameSpreadsheetId() {
        final MemorySpreadsheetContext context = this.createContext();

        final SpreadsheetId id1 = SpreadsheetId.with(111);
        final SpreadsheetStoreRepository repository1 = context.storeRepository(id1);
        assertSame(repository1, context.storeRepository(id1));
    }

    private void countAndCheck(final Store<?, ?> store, final int count) {
        assertEquals(count, store.count(), () -> "" + store.all());
    }

    @Test
    public void testToString() {
        final MemorySpreadsheetContext context = this.createContext();
        context.storeRepository(SpreadsheetId.with(111));

        this.toStringAndCheck(context, "base=http://example.com/api987 contentType=JSON");
    }

    // SpreadsheetContext...............................................................................................

    @Override
    public MemorySpreadsheetContext createContext() {
        return MemorySpreadsheetContext.with(
                this.base(),
                this.contentType(),
                this::fractioner,
                this::createMetadata,
                this::spreadsheetIdFunctions,
                this::spreadsheetIdToRepository,
                this::spreadsheetMetadataStamper
        );
    }

    private AbsoluteUrl base() {
        return Url.parseAbsolute("http://example.com/api987");
    }

    private HateosContentType contentType() {
        return HateosContentType.json(this.unmarshallContext(), this.marshallContext());
    }

    private JsonNodeUnmarshallContext unmarshallContext() {
        return JsonNodeUnmarshallContexts.basic(ExpressionNumberContexts.basic(ExpressionNumberKind.DEFAULT, MathContext.DECIMAL32));
    }

    final Fraction fractioner(final BigDecimal value) {
        throw new UnsupportedOperationException();
    }

    private SpreadsheetMetadata createMetadata(final Optional<Locale> locale) {
        final EmailAddress creatorEmail = EmailAddress.parse("creator@example.com");
        final LocalDateTime createDateTime = LocalDateTime.of(1999, 12, 31, 12, 58, 59);
        final EmailAddress modifiedEmail = EmailAddress.parse("modified@example.com");
        final LocalDateTime modifiedDateTime = LocalDateTime.of(2000, 1, 2, 12, 58, 59);

        SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(999))
                .set(SpreadsheetMetadataPropertyName.CREATOR, creatorEmail)
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, createDateTime)
                .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, modifiedEmail)
                .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, modifiedDateTime);
        if (locale.isPresent()) {
            metadata = metadata.set(SpreadsheetMetadataPropertyName.LOCALE, locale.get());
        }
        return metadata;
    }

    private Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>> spreadsheetIdFunctions(final SpreadsheetId spreadsheetId) {
        this.checkSpreadsheetId(spreadsheetId);

        return (f) -> {
            throw new UnknownExpressionFunctionException(f);
        };
    }

    private SpreadsheetMetadata spreadsheetMetadataStamper(final SpreadsheetMetadata metadata) {
        return metadata.set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, MODIFIED_DATE_TIME);
    }

    private SpreadsheetStoreRepository spreadsheetIdToRepository(final SpreadsheetId id) {
        Objects.requireNonNull(id, "id");

        SpreadsheetStoreRepository repository = this.idToRepositories.get(id);

        if (null == repository) {
            final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();

            final EmailAddress creator = EmailAddress.parse("user123@exaple.com");
            final LocalDateTime now = LocalDateTime.now();

            metadataStore.save(SpreadsheetMetadata.EMPTY
                    .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, id)
                    .set(SpreadsheetMetadataPropertyName.CREATOR, creator)
                    .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, now)
                    .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, creator)
                    .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, now)
                    .set(SpreadsheetMetadataPropertyName.DATETIME_OFFSET, Converters.JAVA_EPOCH_OFFSET)
                    .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.ENGLISH)
                    .loadFromLocale()
                    .set(SpreadsheetMetadataPropertyName.CELL_CHARACTER_WIDTH, 1)
                    .set(SpreadsheetMetadataPropertyName.CURRENCY_SYMBOL, "C")
                    .set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, 1900)
                    .set(SpreadsheetMetadataPropertyName.EXPONENT_SYMBOL, "E")
                    .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, EXPRESSION_NUMBER_KIND)
                    .set(SpreadsheetMetadataPropertyName.PRECISION, 10)
                    .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                    .set(SpreadsheetMetadataPropertyName.STYLE, SpreadsheetMetadata.NON_LOCALE_DEFAULTS.getOrFail(SpreadsheetMetadataPropertyName.STYLE))
                    .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 20)
                    .set(SpreadsheetMetadataPropertyName.DATE_FORMAT_PATTERN, SpreadsheetPattern.parseDateFormatPattern("\"Date\" yyyy mm dd"))
                    .set(SpreadsheetMetadataPropertyName.DATE_PARSE_PATTERNS, SpreadsheetPattern.parseDateParsePatterns("\"Date\" yyyy mm dd"))
                    .set(SpreadsheetMetadataPropertyName.DATETIME_FORMAT_PATTERN, SpreadsheetPattern.parseDateTimeFormatPattern("\"DateTime\" yyyy hh"))
                    .set(SpreadsheetMetadataPropertyName.DATETIME_PARSE_PATTERNS, SpreadsheetPattern.parseDateTimeParsePatterns("\"DateTime\" yyyy hh"))
                    .set(SpreadsheetMetadataPropertyName.NUMBER_FORMAT_PATTERN, SpreadsheetPattern.parseNumberFormatPattern("\"Number\" 000.000"))
                    .set(SpreadsheetMetadataPropertyName.NUMBER_PARSE_PATTERNS, SpreadsheetPattern.parseNumberParsePatterns("\"Number\" 000.000"))
                    .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetPattern.parseTextFormatPattern("\"Text\" @"))
                    .set(SpreadsheetMetadataPropertyName.TIME_FORMAT_PATTERN, SpreadsheetPattern.parseTimeFormatPattern("\"Time\" ss hh"))
                    .set(SpreadsheetMetadataPropertyName.TIME_PARSE_PATTERNS, SpreadsheetPattern.parseTimeParsePatterns("\"Time\" ss hh")));
            repository = SpreadsheetStoreRepositories.basic(
                    SpreadsheetCellStores.treeMap(),
                    SpreadsheetExpressionReferenceStores.treeMap(),
                    SpreadsheetGroupStores.treeMap(),
                    SpreadsheetLabelStores.treeMap(),
                    SpreadsheetExpressionReferenceStores.treeMap(),
                    metadataStore,
                    SpreadsheetRangeStores.treeMap(),
                    SpreadsheetRangeStores.treeMap(),
                    SpreadsheetUserStores.treeMap()
            );
            this.idToRepositories.put(id, repository);
        }

        return repository;
    }

    private final Map<SpreadsheetId, SpreadsheetStoreRepository> idToRepositories = Maps.sorted();

    private void checkSpreadsheetId(final SpreadsheetId id) {
        Objects.requireNonNull(id, "spreadsheetId");
        assertEquals(this.spreadsheetId(), id, "spreadsheetId");
    }

    private SpreadsheetId spreadsheetId() {
        return SpreadsheetId.with(0x123def);
    }

    private JsonNodeMarshallContext marshallContext() {
        return JsonNodeMarshallContexts.basic();
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
                header1, list(value1),
                header2, list(value2),
                header3, list(value3),
                header4, list(value4)
        );
    }

    private static <T> List<T> list(final T... values) {
        return Lists.of(values);
    }

    abstract class TestHttpRequest extends FakeHttpRequest {
        TestHttpRequest() {
            super();
        }

        @Override
        public final long bodyLength() {
            return this.bodyText().length();
        }
    }

    @Override
    public Class<MemorySpreadsheetContext> type() {
        return MemorySpreadsheetContext.class;
    }
}
