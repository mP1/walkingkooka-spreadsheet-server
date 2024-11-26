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
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.Converter;
import walkingkooka.convert.ConverterContext;
import walkingkooka.convert.provider.ConverterName;
import walkingkooka.net.header.Accept;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandlerTesting;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorToken;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorTokenAlternative;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandlerTest implements HateosHttpEntityHandlerTesting<SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandler, SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        ToStringTesting<SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandler>,
        SpreadsheetMetadataTesting {

    private final static SpreadsheetFormatterName FORMATTER_NAME = SpreadsheetFormatterName.DATE_FORMAT_PATTERN;

    @Test
    public void testHandleAllFails() {
        this.handleAllFails(
                this.entity(),
                this.parameters(),
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
                this.context(),
                UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleNoneFails() {
        this.handleNoneFails(
                this.entity(),
                this.parameters(),
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
                this.context(),
                UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleAllContentTypeBadContentType() {
        final IllegalArgumentException thrown = this.handleOneFails(
                SpreadsheetFormatterName.TEXT_FORMAT_PATTERN,
                this.entity()
                        .setContentType(MediaType.TEXT_PLAIN)
                        .addHeader(
                                HttpHeaderName.ACCEPT,
                                Accept.parse(MediaType.APPLICATION_JSON.toHeaderText())
                        ),
                this.parameters(),
                this.context(),
                IllegalArgumentException.class
        );
        this.checkEquals(
                "Content-Type: Got text/plain require application/json",
                thrown.getMessage()
        );
    }

    @Test
    public void testHandleOne() {
        final SpreadsheetFormatterSelector selector = SpreadsheetPattern.parseDateFormatPattern("yyyy")
                .spreadsheetFormatterSelector();

        this.handleOneAndCheck(
                selector.name(), // resource id
                this.httpEntity(
                        JsonNode.string(selector.valueText())
                ).addHeader(
                        HttpHeaderName.ACCEPT,
                        Accept.with(
                                Lists.of(MediaType.APPLICATION_JSON)
                        )
                ),
                this.parameters(),
                new FakeSpreadsheetEngineHateosResourceHandlerContext() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.APPLICATION_JSON;
                    }

                    @Override
                    public <T> T unmarshall(final JsonNode json,
                                            final Class<T> type) {
                        return JSON_NODE_UNMARSHALL_CONTEXT.unmarshall(
                                json,
                                type
                        );
                    }

                    @Override
                    public <C extends ConverterContext> Converter<C> converter(final ConverterName name,
                                                                               final List<?> values,
                                                                               final ProviderContext context) {
                        return CONVERTER_PROVIDER.converter(
                                name,
                                values,
                                context
                        );
                    }

                    @Override
                    public Optional<SpreadsheetFormatterSelectorToken> spreadsheetFormatterNextToken(final SpreadsheetFormatterSelector selector) {
                        return SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatterNextToken(selector);
                    }

                    @Override
                    public JsonNode marshall(final Object value) {
                        return JSON_NODE_MARSHALL_CONTEXT.marshall(value);
                    }
                },
                this.httpEntity(
                        SpreadsheetFormatterSelectorToken.with(
                                "",
                                "",
                                Lists.of(
                                        SpreadsheetFormatterSelectorTokenAlternative.with(
                                                "d",
                                                "d"
                                        ),
                                        SpreadsheetFormatterSelectorTokenAlternative.with(
                                                "dd",
                                                "dd"
                                        ),
                                        SpreadsheetFormatterSelectorTokenAlternative.with(
                                                "ddd",
                                                "ddd"
                                        ),
                                        SpreadsheetFormatterSelectorTokenAlternative.with(
                                                "dddd",
                                                "dddd"
                                        ),
                                        SpreadsheetFormatterSelectorTokenAlternative.with(
                                                "m",
                                                "m"
                                        ),
                                        SpreadsheetFormatterSelectorTokenAlternative.with(
                                                "mm",
                                                "mm"
                                        ),
                                        SpreadsheetFormatterSelectorTokenAlternative.with(
                                                "mmm",
                                                "mmm"
                                        ),
                                        SpreadsheetFormatterSelectorTokenAlternative.with(
                                                "mmmm",
                                                "mmmm"
                                        ),
                                        SpreadsheetFormatterSelectorTokenAlternative.with(
                                                "mmmmm",
                                                "mmmmm"
                                        )
                                )
                        )
                ).addHeader(
                        HateosResourceMapping.X_CONTENT_TYPE_NAME,
                        SpreadsheetFormatterSelectorToken.class.getSimpleName()
                )
        );
    }

    @Override
    public SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandler createHandler() {
        return SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandler.instance();
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

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(),
                SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandler.class.getSimpleName()
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
    public Class<SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandler> type() {
        return SpreadsheetFormatterProviderNextTokenHateosHttpEntityHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
