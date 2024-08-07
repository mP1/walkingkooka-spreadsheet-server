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
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorTextComponent;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorTextComponentAlternative;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.engine.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandlerTest implements HateosHttpEntityHandlerTesting<SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandler, SpreadsheetFormatterName, SpreadsheetEngineHateosResourceHandlerContext>,
        ToStringTesting<SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandler>,
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
                        .setHeader(
                                HttpHeaderName.CONTENT_TYPE,
                                Lists.of(MediaType.TEXT_PLAIN)
                        ),
                this.parameters(),
                this.context(),
                IllegalArgumentException.class
        );
        this.checkEquals(
                "Got text/plain requested application/json",
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
                        JsonNode.string(selector.text())
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
                    public Optional<SpreadsheetFormatterSelectorTextComponent> spreadsheetFormatterNextTextComponent(final SpreadsheetFormatterSelector selector) {
                        return SPREADSHEET_FORMATTER_PROVIDER.spreadsheetFormatterNextTextComponent(selector);
                    }

                    @Override
                    public JsonNode marshall(final Object value) {
                        return JSON_NODE_MARSHALL_CONTEXT.marshall(value);
                    }
                },
                this.httpEntity(
                        SpreadsheetFormatterSelectorTextComponent.with(
                                "",
                                "",
                                Lists.of(
                                        SpreadsheetFormatterSelectorTextComponentAlternative.with(
                                                "d",
                                                "d"
                                        ),
                                        SpreadsheetFormatterSelectorTextComponentAlternative.with(
                                                "dd",
                                                "dd"
                                        ),
                                        SpreadsheetFormatterSelectorTextComponentAlternative.with(
                                                "ddd",
                                                "ddd"
                                        ),
                                        SpreadsheetFormatterSelectorTextComponentAlternative.with(
                                                "dddd",
                                                "dddd"
                                        ),
                                        SpreadsheetFormatterSelectorTextComponentAlternative.with(
                                                "m",
                                                "m"
                                        ),
                                        SpreadsheetFormatterSelectorTextComponentAlternative.with(
                                                "mm",
                                                "mm"
                                        ),
                                        SpreadsheetFormatterSelectorTextComponentAlternative.with(
                                                "mmm",
                                                "mmm"
                                        ),
                                        SpreadsheetFormatterSelectorTextComponentAlternative.with(
                                                "mmmm",
                                                "mmmm"
                                        ),
                                        SpreadsheetFormatterSelectorTextComponentAlternative.with(
                                                "mmmmm",
                                                "mmmmm"
                                        )
                                )
                        )
                ).addHeader(
                        HateosResourceMapping.X_CONTENT_TYPE_NAME,
                        SpreadsheetFormatterSelectorTextComponent.class.getSimpleName()
                )
        );
    }

    @Override
    public SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandler createHandler() {
        return SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandler.instance();
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
        return HttpEntity.EMPTY.addHeader(
                HttpHeaderName.CONTENT_TYPE,
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
                SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandler.class.getSimpleName()
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
    public Class<SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandler> type() {
        return SpreadsheetFormatterProviderNextTextComponentHateosHttpEntityHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
