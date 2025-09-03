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
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.Converter;
import walkingkooka.convert.ConverterContext;
import walkingkooka.convert.provider.ConverterName;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.datetime.DateTimeSymbols;
import walkingkooka.math.DecimalNumberSymbols;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandlerTesting;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.convert.MissingConverterSet;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.JsonNode;

import java.math.MathContext;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ConverterSelectorVerifyHateosHttpEntityHandlerTest implements HateosHttpEntityHandlerTesting<ConverterSelectorVerifyHateosHttpEntityHandler, SpreadsheetMetadataPropertyName<?>, SpreadsheetEngineHateosResourceHandlerContext>,
    SpreadsheetMetadataTesting {

    private final SpreadsheetMetadataPropertyName<ConverterSelector> PROPERTY = SpreadsheetMetadataPropertyName.FORMULA_CONVERTER;

    @Test
    public void testHandleOne() {
        this.handleOneAndCheck(
            PROPERTY,
            this.entity(),
            HateosHttpEntityHandler.NO_PARAMETERS,
            this.path(),
            this.context(),
            HttpEntity.EMPTY.setContentType(
                SpreadsheetServerMediaTypes.CONTENT_TYPE
            ).setHeader(
                HateosResourceMappings.X_CONTENT_TYPE_NAME,
                Lists.of(
                    MissingConverterSet.class.getSimpleName()
                )
            ).setBodyText(
                JSON_NODE_MARSHALL_CONTEXT.marshall(
                    MissingConverterSet.EMPTY
                ).toString()
            ).setContentLength()
        );
    }

    @Override
    public ConverterSelectorVerifyHateosHttpEntityHandler createHandler() {
        return ConverterSelectorVerifyHateosHttpEntityHandler.INSTANCE;
    }

    @Override
    public SpreadsheetMetadataPropertyName<?> id() {
        return SpreadsheetMetadataPropertyName.FORMULA_CONVERTER;
    }

    @Override
    public Set<SpreadsheetMetadataPropertyName<?>> manyIds() {
        return Sets.of(
            this.id()
        );
    }

    @Override
    public Range<SpreadsheetMetadataPropertyName<?>> range() {
        return Range.singleton(
            this.id()
        );
    }

    @Override
    public HttpEntity entity() {
        return HttpEntity.EMPTY.setBodyText(
            JSON_NODE_MARSHALL_CONTEXT.marshall(
                METADATA_EN_AU.getOrFail(PROPERTY)
            ).toString()
        );
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
            public Indentation indentation() {
                return INDENTATION;
            }

            @Override
            public LineEnding lineEnding() {
                return LineEnding.NL;
            }

            @Override
            public ExpressionNumberKind expressionNumberKind() {
                return EXPRESSION_NUMBER_KIND;
            }

            @Override
            public Locale locale() {
                return LOCALE;
            }

            @Override
            public MathContext mathContext() {
                return MathContext.DECIMAL32;
            }

            @Override
            public JsonNode marshall(final Object value) {
                return JSON_NODE_MARSHALL_CONTEXT.marshall(value);
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
            public <C extends ConverterContext> Converter<C> converter(final ConverterSelector selector,
                                                                       final ProviderContext context) {
                return SPREADSHEET_PROVIDER.converter(
                    selector,
                    context
                );
            }

            @Override
            public <C extends ConverterContext> Converter<C> converter(final ConverterName converterName,
                                                                       final List<?> values,
                                                                       final ProviderContext context) {
                return SPREADSHEET_PROVIDER.converter(
                    converterName,
                    values,
                    context
                );
            }

            @Override
            public SpreadsheetMetadata spreadsheetMetadata() {
                return METADATA_EN_AU;
            }

            @Override
            public Optional<DateTimeSymbols> dateTimeSymbolsForLocale(final Locale locale) {
                return LOCALE_CONTEXT.dateTimeSymbolsForLocale(locale);
            }

            @Override
            public Optional<DecimalNumberSymbols> decimalNumberSymbolsForLocale(final Locale locale) {
                return LOCALE_CONTEXT.decimalNumberSymbolsForLocale(locale);
            }

            @Override
            public ProviderContext providerContext() {
                return PROVIDER_CONTEXT;
            }
        };
    }

    // class............................................................................................................

    @Override
    public Class<ConverterSelectorVerifyHateosHttpEntityHandler> type() {
        return ConverterSelectorVerifyHateosHttpEntityHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    @Override
    public String typeNamePrefix() {
        return ConverterSelector.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return HateosHttpEntityHandler.class.getSimpleName();
    }
}
