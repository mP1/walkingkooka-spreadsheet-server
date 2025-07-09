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
import walkingkooka.Either;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.Converter;
import walkingkooka.convert.ConverterContext;
import walkingkooka.convert.provider.ConverterSelector;
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
import walkingkooka.spreadsheet.convert.SpreadsheetConverterContext;
import walkingkooka.spreadsheet.convert.SpreadsheetConverters;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.JsonNode;

import java.math.MathContext;
import java.util.Locale;
import java.util.Map;
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
            public ExpressionNumberKind expressionNumberKind() {
                return EXPRESSION_NUMBER_KIND;
            }

            @Override
            public String currencySymbol() {
                return DECIMAL_NUMBER_SYMBOLS.currencySymbol();
            }

            @Override
            public char decimalSeparator() {
                return DECIMAL_NUMBER_SYMBOLS.decimalSeparator();
            }

            @Override
            public String exponentSymbol() {
                return DECIMAL_NUMBER_SYMBOLS.exponentSymbol();
            }

            @Override
            public char groupSeparator() {
                return DECIMAL_NUMBER_SYMBOLS.groupSeparator();
            }

            @Override
            public String infinitySymbol() {
                return DECIMAL_NUMBER_SYMBOLS.infinitySymbol();
            }

            @Override
            public char monetaryDecimalSeparator() {
                return DECIMAL_NUMBER_SYMBOLS.monetaryDecimalSeparator();
            }

            @Override
            public String nanSymbol() {
                return DECIMAL_NUMBER_SYMBOLS.nanSymbol();
            }

            @Override
            public char negativeSign() {
                return DECIMAL_NUMBER_SYMBOLS.negativeSign();
            }

            @Override
            public char percentSymbol() {
                return DECIMAL_NUMBER_SYMBOLS.percentSymbol();
            }

            @Override
            public char permillSymbol() {
                return DECIMAL_NUMBER_SYMBOLS.permillSymbol();
            }

            @Override
            public char positiveSign() {
                return DECIMAL_NUMBER_SYMBOLS.positiveSign();
            }

            @Override
            public char zeroDigit() {
                return DECIMAL_NUMBER_SYMBOLS.zeroDigit();
            }

            @Override
            public DecimalNumberSymbols decimalNumberSymbols() {
                return DECIMAL_NUMBER_SYMBOLS;
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
            public Converter<SpreadsheetConverterContext> converter() {
                return this.converter(
                    METADATA_EN_AU.getOrFail(PROPERTY),
                    this
                );
            }

            @Override
            public boolean canConvert(final Object value,
                                      final Class<?> type) {
                return SpreadsheetConverters.simple()
                    .canConvert(
                        value,
                        type,
                        this
                    );
            }

            @Override
            public <T> Either<T, String> convert(final Object value,
                                                 final Class<T> type) {
                return this.converter()
                    .convert(
                        value,
                        type,
                        this
                    );
            }

            @Override
            public <C extends ConverterContext> Converter<C> converter(final ConverterSelector selector,
                                                                       final ProviderContext context) {
                return SpreadsheetMetadataTesting.CONVERTER_PROVIDER.converter(
                    selector,
                    SpreadsheetMetadataTesting.PROVIDER_CONTEXT
                );
            }

            @Override
            public SpreadsheetMetadata spreadsheetMetadata() {
                return METADATA_EN_AU;
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
