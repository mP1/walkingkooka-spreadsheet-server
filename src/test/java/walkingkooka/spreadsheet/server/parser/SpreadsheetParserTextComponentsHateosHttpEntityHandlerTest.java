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

package walkingkooka.spreadsheet.server.parser;

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
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.parser.SpreadsheetParser;
import walkingkooka.spreadsheet.parser.SpreadsheetParserName;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelector;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelectorTextComponent;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelectorTextComponentAlternative;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelectorTextComponentList;
import walkingkooka.spreadsheet.server.engine.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SpreadsheetParserTextComponentsHateosHttpEntityHandlerTest implements HateosHttpEntityHandlerTesting<SpreadsheetParserTextComponentsHateosHttpEntityHandler, SpreadsheetParserName, SpreadsheetEngineHateosResourceHandlerContext>,
        ToStringTesting<SpreadsheetParserTextComponentsHateosHttpEntityHandler>,
        SpreadsheetMetadataTesting {

    private final static SpreadsheetParserName PARSER_NAME = SpreadsheetParserName.DATE_PARSER_PATTERN;

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
                SpreadsheetParserName.DATE_PARSER_PATTERN,
                this.entity()
                        .setContentType(MediaType.TEXT_PLAIN),
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
    public void testHandleAllBadAccept() {
        final IllegalArgumentException thrown = this.handleOneFails(
                SpreadsheetParserName.DATE_PARSER_PATTERN,
                this.entity()
                        .setContentType(MediaType.APPLICATION_JSON)
                        .addHeader(
                                HttpHeaderName.ACCEPT,
                                Accept.with(
                                        Lists.of(MediaType.IMAGE_BMP)
                                )
                        ),
                this.parameters(),
                this.context(),
                IllegalArgumentException.class
        );
        this.checkEquals(
                "Accept: Got image/bmp require application/json",
                thrown.getMessage()
        );
    }

    @Test
    public void testHandleOne() {
        final SpreadsheetParserSelector selector = SpreadsheetPattern.parseDateParsePattern("yyyy")
                .spreadsheetParserSelector();

        this.handleOneAndCheck(
                selector.name(), // resource id
                this.httpEntity(
                        JsonNode.string(selector.text())
                ).addHeader(
                        HttpHeaderName.ACCEPT,
                        Accept.with(
                                Lists.of(
                                        MediaType.APPLICATION_JSON
                                )
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
                        return JSON_NODE_UNMARSHALL_CONTEXT.unmarshall(json, type);
                    }

                    @Override
                    public SpreadsheetMetadata spreadsheetMetadata() {
                        return SpreadsheetMetadataTesting.METADATA_EN_AU;
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
                    public SpreadsheetParser spreadsheetParser(final SpreadsheetParserSelector selector,
                                                               final ProviderContext context) {
                        return SPREADSHEET_PARSER_PROVIDER.spreadsheetParser(
                                selector,
                                context
                        );
                    }

                    @Override
                    public JsonNode marshall(final Object value) {
                        return JSON_NODE_MARSHALL_CONTEXT.marshall(value);
                    }
                },
                this.httpEntity(
                        SpreadsheetParserSelectorTextComponentList.with(
                                Lists.of(
                                        SpreadsheetParserSelectorTextComponent.with(
                                                "yyyy",
                                                "yyyy",
                                                Lists.of(
                                                        SpreadsheetParserSelectorTextComponentAlternative.with(
                                                                "yy",
                                                                "yy"
                                                        )
                                                )
                                        )
                                )
                        )
                ).addHeader(
                        HateosResourceMapping.X_CONTENT_TYPE_NAME,
                        SpreadsheetParserSelectorTextComponentList.class.getSimpleName()
                )
        );
    }

    @Override
    public SpreadsheetParserTextComponentsHateosHttpEntityHandler createHandler() {
        return SpreadsheetParserTextComponentsHateosHttpEntityHandler.instance();
    }

    @Override
    public SpreadsheetParserName id() {
        return PARSER_NAME;
    }

    @Override
    public Set<SpreadsheetParserName> manyIds() {
        return Sets.of(
                PARSER_NAME
        );
    }

    @Override
    public Range<SpreadsheetParserName> range() {
        return Range.singleton(PARSER_NAME);
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
        return HttpEntity.EMPTY.setContentType(MediaType.APPLICATION_JSON.setCharset(CharsetName.UTF_8))
                .setBodyText(
                        JSON_NODE_MARSHALL_CONTEXT.marshall(value)
                                .toString()
        ).setContentLength();
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(),
                SpreadsheetParserTextComponentsHateosHttpEntityHandler.class.getSimpleName()
        );
    }

    // type name.........................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetParser.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return HateosHttpEntityHandler.class.getSimpleName();
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetParserTextComponentsHateosHttpEntityHandler> type() {
        return SpreadsheetParserTextComponentsHateosHttpEntityHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
