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

package walkingkooka.spreadsheet.server.formatter;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.color.Color;
import walkingkooka.net.UrlPath;
import walkingkooka.net.header.Accept;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandlerTesting;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.text.TextNode;
import walkingkooka.tree.text.TextNodeList;
import walkingkooka.tree.text.TextStylePropertyName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetFormatterFormatHateosHttpEntityHandlerTest implements HateosHttpEntityHandlerTesting<SpreadsheetFormatterFormatHateosHttpEntityHandler, SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
    ToStringTesting<SpreadsheetFormatterFormatHateosHttpEntityHandler>,
    SpreadsheetMetadataTesting {

    private final static SpreadsheetFormatterName FORMATTER_NAME = SpreadsheetFormatterName.DATE_FORMAT_PATTERN;

    @Test
    public void testHandleOneFails() {
        this.handleOneFails(
            this.id(),
            this.entity(),
            this.parameters(),
            this.path(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleManyFails() {
        this.handleManyFails(
            this.manyIds(),
            this.entity(),
            this.parameters(),
            this.path(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleNoneFails() {
        this.handleNoneFails(
            this.entity(),
            this.parameters(),
            this.path(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleRangeFails() {
        this.handleRangeFails(
            this.range(),
            this.entity(),
            this.parameters(),
            this.path(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleAllContentTypeBadContentType() {
        final IllegalArgumentException thrown = this.handleAllFails(
            this.entity()
                .setContentType(MediaType.TEXT_PLAIN),
            this.parameters(),
            this.path(),
            this.context(),
            IllegalArgumentException.class
        );
        this.checkEquals(
            "Content-Type: Got text/plain require application/json",
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleAllBadAccept() {
        final IllegalArgumentException thrown = this.handleAllFails(
            this.entity()
                .setContentType(MediaType.APPLICATION_JSON)
                .setAccept(
                    Accept.with(
                        Lists.of(
                            MediaType.ANY_IMAGE
                        )
                    )
                ),
            this.parameters(),
            this.path(),
            this.context(),
            IllegalArgumentException.class
        );
        this.checkEquals(
            "Accept: Got application/json require image/*",
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleAll() {
        this.handleAllAndCheck(
            // two format requests
            this.httpEntity(
                SpreadsheetFormatterFormatRequestList.with(
                    Lists.of(
                        SpreadsheetFormatterFormatRequest.with(
                            SpreadsheetPattern.parseDateFormatPattern("[black]yyyy/mm/dd")
                                .spreadsheetFormatterSelector(),
                            LocalDate.of(1999, 12, 31)
                        ),
                        SpreadsheetFormatterFormatRequest.with(
                            SpreadsheetPattern.parseDateTimeFormatPattern("yyyy/mm/dd hh:mm")
                                .spreadsheetFormatterSelector(),
                            LocalDateTime.of(1999, 12, 31, 12, 58, 59)
                        ),
                        SpreadsheetFormatterFormatRequest.with(
                            SpreadsheetPattern.parseTextFormatPattern("@")
                                .spreadsheetFormatterSelector(),
                            "Hello111"
                        ),
                        SpreadsheetFormatterFormatRequest.with(
                            SpreadsheetPattern.parseTextFormatPattern("@@")
                                .spreadsheetFormatterSelector(),
                            "Hello222"
                        )
                    )
                )
            ).setAccept(
                Accept.with(
                    Lists.of(
                        MediaType.APPLICATION_JSON
                    )
                )
            ),
            this.parameters(),
            this.path(),
            new FakeSpreadsheetEngineHateosResourceHandlerContext() {
                @Override
                public MediaType contentType() {
                    return MediaType.APPLICATION_JSON;
                }

                @Override
                public <T> T unmarshall(final JsonNode json,
                                        final Class<T> type) {
                    return fromJson(json, type);
                }

                @Override
                public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterSelector selector,
                                                                 final ProviderContext context) {
                    return SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatter(
                        selector,
                        context
                    );
                }

                @Override
                public Optional<TextNode> formatValue(final SpreadsheetCell cell,
                                                      final Optional<Object> value,
                                                      final SpreadsheetFormatter formatter) {
                    return formatter.format(
                        value,
                        SPREADSHEET_FORMATTER_CONTEXT
                    );
                }

                @Override
                public JsonNode marshall(final Object value) {
                    return JSON_NODE_MARSHALL_CONTEXT.marshall(value);
                }
            },
            this.httpEntity(
                TextNodeList.with(
                    Lists.of(
                        TextNode.text("1999/12/31")
                            .setAttributes(
                                Maps.of(
                                    TextStylePropertyName.COLOR,
                                    Color.BLACK
                                )
                            ),
                        TextNode.text("1999/12/31 12:58"),
                        TextNode.text("Hello111"),
                        TextNode.text("Hello222Hello222")
                    )
                )
            ).addHeader(
                HateosResourceMappings.X_CONTENT_TYPE_NAME,
                TextNodeList.class.getSimpleName()
            )
        );
    }

    @Override
    public SpreadsheetFormatterFormatHateosHttpEntityHandler createHandler() {
        return SpreadsheetFormatterFormatHateosHttpEntityHandler.instance();
    }

    @Override
    public SpreadsheetFormatterName id() {
        return FORMATTER_NAME;
    }

    @Override
    public Set<SpreadsheetFormatterName> manyIds() {
        return Sets.of(
            FORMATTER_NAME
        );
    }

    @Override
    public Range<SpreadsheetFormatterName> range() {
        return Range.singleton(FORMATTER_NAME);
    }

    @Override
    public HttpEntity entity() {
        return HttpEntity.EMPTY;
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosHttpEntityHandler.NO_PARAMETERS;
    }

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return new FakeSpreadsheetEngineHateosResourceHandlerContext() {
            @Override
            public MediaType contentType() {
                return MediaType.APPLICATION_JSON;
            }
        };
    }

    private HttpEntity httpEntity(final Object value) {
        return HttpEntity.EMPTY.setContentType(
            MediaType.APPLICATION_JSON.setCharset(CharsetName.UTF_8)
        ).setBodyText(
            JSON_NODE_MARSHALL_CONTEXT.marshall(value)
                .toString()
        ).setContentLength();
    }

    private <T> T fromJson(final JsonNode json,
                           final Class<T> type) {
        return JSON_NODE_UNMARSHALL_CONTEXT.unmarshall(
            json,
            type
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createHandler(),
            SpreadsheetFormatterFormatHateosHttpEntityHandler.class.getSimpleName()
        );
    }

    // type name.........................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetFormatter.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return HateosHttpEntityHandler.class.getSimpleName();
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetFormatterFormatHateosHttpEntityHandler> type() {
        return SpreadsheetFormatterFormatHateosHttpEntityHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
