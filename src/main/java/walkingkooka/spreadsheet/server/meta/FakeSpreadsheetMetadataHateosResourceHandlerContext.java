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

import walkingkooka.datetime.DateTimeSymbols;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.math.DecimalNumberSymbols;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.FakeHateosResourceHandlerContext;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class FakeSpreadsheetMetadataHateosResourceHandlerContext extends FakeHateosResourceHandlerContext
    implements SpreadsheetMetadataHateosResourceHandlerContext {

    // SpreadsheetMetadataHateosResourceHandlerContext..................................................................

    @Override
    public SpreadsheetProvider spreadsheetProvider(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Router<HttpRequestAttribute<?>, HttpHandler> httpRouter(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    // HateosResourceHandlerContext.....................................................................................

    @Override
    public MediaType contentType() {
        throw new UnsupportedOperationException();
    }

    // JsonNodeUnmarshallContext........................................................................................

    @Override
    public SpreadsheetMetadataHateosResourceHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadataHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unmarshall(final JsonNode json,
                            final Class<T> type) {
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
    public <K, V> Map<K, V> unmarshallMap(final JsonNode json, Class<K> type,
                                          final Class<V> type1) {
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

    // EnvironmentContext...............................................................................................

    @Override
    public SpreadsheetMetadataHateosResourceHandlerContext cloneEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale locale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadataHateosResourceHandlerContext setLocale(final Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadataHateosResourceHandlerContext setUser(final Optional<EmailAddress> user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<T> environmentValue(final EnvironmentValueName<T> environmentValueName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> SpreadsheetMetadataHateosResourceHandlerContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                                   final T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadataHateosResourceHandlerContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<EnvironmentValueName<?>> environmentValueNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<EmailAddress> user() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDateTime now() {
        throw new UnsupportedOperationException();
    }

    // LocaleContext....................................................................................................

    @Override
    public Set<Locale> availableLocales() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<DateTimeSymbols> dateTimeSymbolsForLocale(final Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<DecimalNumberSymbols> decimalNumberSymbolsForLocale(final Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Locale> findByLocaleText(final String text,
                                        final int offset,
                                        final int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> localeText(final Locale locale) {
        throw new UnsupportedOperationException();
    }

    // ProviderContext..................................................................................................

    @Override
    public ProviderContext providerContext() {
        throw new UnsupportedOperationException();
    }

    // SpreadsheetGlobalContext.........................................................................................

    @Override
    public SpreadsheetMetadata createMetadata(final EmailAddress user,
                                              final Optional<Locale> locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetMetadata> loadMetadata(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteMetadata(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SpreadsheetMetadata> findMetadataBySpreadsheetName(final String name,
                                                                   final int offset,
                                                                   final int count) {
        throw new UnsupportedOperationException();
    }
}
