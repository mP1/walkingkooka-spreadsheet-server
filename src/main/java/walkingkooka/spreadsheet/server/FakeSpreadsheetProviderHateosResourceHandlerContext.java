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

import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.spreadsheet.provider.FakeSpreadsheetProvider;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonString;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.math.MathContext;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class FakeSpreadsheetProviderHateosResourceHandlerContext extends FakeSpreadsheetProvider implements SpreadsheetProviderHateosResourceHandlerContext,
    HateosResourceHandlerContext {

    public FakeSpreadsheetProviderHateosResourceHandlerContext() {
        super();
    }

    @Override
    public MediaType contentType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public <T> T unmarshall(final JsonNode json,final Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Enum<T>> Set<T> unmarshallEnumSet(final JsonNode json, 
                                                        final Class<T> type,
                                                        final Function<String, T> function) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public <T> Optional<T> unmarshallOptional(final JsonNode json,
                                              final Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public <T> Optional<T> unmarshallOptionalWithType(final JsonNode json) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<T> unmarshallList(final JsonNode json,
                                                final Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public <T> Set<T> unmarshallSet(final JsonNode json,
                                              final Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public <K, V> Map<K, V> unmarshallMap(final JsonNode json, 
                                                    final Class<K> keyType, 
                                                    final Class<V> valueType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unmarshallWithType(final JsonNode json) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<T> unmarshallListWithType(final JsonNode json) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public <T> Set<T> unmarshallSetWithType(final JsonNode json) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public <K, V> Map<K, V> unmarshallMapWithType(final JsonNode json) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MathContext mathContext() {
        throw new UnsupportedOperationException();
    }

    @Override 
    public ExpressionNumberKind expressionNumberKind() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonNodeMarshallContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public JsonNode marshall(final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public JsonNode marshallEnumSet(final Set<? extends Enum<?>> set) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public JsonNode marshallWithType(final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonNode marshallOptional(final Optional<?> value) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public JsonNode marshallOptionalWithType(final Optional<?> value) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public JsonNode marshallCollection(final Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonNode marshallMap(final Map<?, ?> map) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public JsonNode marshallCollectionWithType(final Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonNode marshallMapWithType(final Map<?, ?> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Class<?>> registeredType(final JsonString string) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public Optional<JsonString> typeName(final Class<?> type) {
        throw new UnsupportedOperationException();
    }
}
