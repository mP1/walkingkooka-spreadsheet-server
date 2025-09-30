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

import walkingkooka.collect.map.Maps;
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentContextDelegator;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.locale.LocaleContext;
import walkingkooka.locale.LocaleContextDelegator;
import walkingkooka.locale.LocaleContexts;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.UrlFragment;
import walkingkooka.net.UrlQueryString;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextDelegator;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviderDelegator;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.CharSequences;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A basic fully functional {@link SpreadsheetServerContext}
 */
final class BasicSpreadsheetServerContext implements SpreadsheetServerContext,
    EnvironmentContextDelegator,
    LocaleContextDelegator,
    HateosResourceHandlerContextDelegator,
    SpreadsheetProviderDelegator {

    static BasicSpreadsheetServerContext with(final AbsoluteUrl serverUrl,
                                              final Supplier<SpreadsheetStoreRepository> spreadsheetStoreRepository,
                                              final SpreadsheetProvider spreadsheetProvider,
                                              final EnvironmentContext environmentContext,
                                              final LocaleContext localeContext,
                                              final SpreadsheetMetadataContext spreadsheetMetadataContext,
                                              final HateosResourceHandlerContext hateosResourceHandlerContext,
                                              final ProviderContext providerContext) {
        Objects.requireNonNull(serverUrl, "serverUrl");

        if (false == serverUrl.path().equals(walkingkooka.net.UrlPath.EMPTY)) {
            throw reportServerUrlFail("path", serverUrl);
        }
        if (false == serverUrl.query().equals(UrlQueryString.EMPTY)) {
            throw reportServerUrlFail("query string", serverUrl);
        }
        if (false == serverUrl.urlFragment().equals(UrlFragment.EMPTY)) {
            throw reportServerUrlFail("fragment", serverUrl);
        }

        return new BasicSpreadsheetServerContext(
            serverUrl,
            Objects.requireNonNull(spreadsheetStoreRepository, "spreadsheetStoreRepository"),
            Objects.requireNonNull(spreadsheetProvider, "spreadsheetProvider"),
            Objects.requireNonNull(environmentContext, "environmentContext"),
            Objects.requireNonNull(localeContext, "localeContext"),
            Objects.requireNonNull(spreadsheetMetadataContext, "spreadsheetMetadataContext"),
            Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext"),
            Objects.requireNonNull(providerContext, "providerContext")
        );
    }

    private static IllegalArgumentException reportServerUrlFail(final String property,
                                                                final AbsoluteUrl serverUrl) {
        return new IllegalArgumentException(
            "Url must not have " +
                property +
                " got " +
                CharSequences.quoteAndEscape(
                    serverUrl.toString()
                )
        );
    }
    
    private BasicSpreadsheetServerContext(final AbsoluteUrl serverUrl,
                                          final Supplier<SpreadsheetStoreRepository> spreadsheetStoreRepository,
                                          final SpreadsheetProvider spreadsheetProvider,
                                          final EnvironmentContext environmentContext,
                                          final LocaleContext localeContext,
                                          final SpreadsheetMetadataContext spreadsheetMetadataContext,
                                          final HateosResourceHandlerContext hateosResourceHandlerContext,
                                          final ProviderContext providerContext) {
        this.serverUrl = serverUrl;
        this.spreadsheetStoreRepository = spreadsheetStoreRepository;
        this.spreadsheetProvider = spreadsheetProvider;
        this.environmentContext = EnvironmentContexts.readOnly(environmentContext); // safety
        this.localeContext = localeContext;
        this.spreadsheetMetadataContext = spreadsheetMetadataContext;
        this.hateosResourceHandlerContext = hateosResourceHandlerContext;
        this.providerContext = providerContext;
    }

    // SpreadsheetServerContext.........................................................................................

    @Override
    public AbsoluteUrl serverUrl() {
        return this.serverUrl;
    }

    private final AbsoluteUrl serverUrl;

    @Override
    public SpreadsheetContext createSpreadsheetContext(final EmailAddress user,
                                                       final Optional<Locale> locale) {
        final SpreadsheetMetadata metadata = this.spreadsheetMetadataContext.createMetadata(
            user,
            locale
        );

        final SpreadsheetId id = metadata.getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID);

        final Optional<EmailAddress> user2 = Optional.of(user);

        final SpreadsheetContext context = SpreadsheetContexts.basic(
            this.serverUrl,
            id,
            this.spreadsheetStoreRepository.get(), // SpreadsheetStoreRepository
            this.spreadsheetProvider,
            metadata.environmentContext(
                this.environmentContext.cloneEnvironment()
                    .setUser(user2)
            ),
            LocaleContexts.readOnly(this.localeContext),
            ProviderContexts.readOnly(
                this.providerContext.cloneEnvironment()
                    .setUser(user2)
            )
        );

        this.spreadsheetIdToSpreadsheetContext.put(
            id,
            context
        );

        return context;
    }

    private final Supplier<SpreadsheetStoreRepository> spreadsheetStoreRepository;

    /**
     * The default or starting {@link EnvironmentContext} for each new spreadsheet.
     */
    private final EnvironmentContext environmentContext;

    @Override
    public Optional<SpreadsheetContext> spreadsheetContext(final SpreadsheetId id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(
            this.spreadsheetIdToSpreadsheetContext.get(id)
        );
    }

    private final Map<SpreadsheetId, SpreadsheetContext> spreadsheetIdToSpreadsheetContext = Maps.concurrent();

    // EnvironmentContextDelegator......................................................................................

    @Override
    public EnvironmentContext cloneEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> EnvironmentContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                      final T value) {
        this.environmentContext()
            .setEnvironmentValue(
                name,
                value
            );
        return this;
    }

    @Override
    public EnvironmentContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
        this.environmentContext()
            .removeEnvironmentValue(name);
        return this;
    }

    @Override
    public Locale locale() {
        return this.environmentContext()
            .locale();
    }

    @Override
    public SpreadsheetServerContext setLocale(final Locale locale) {
        this.environmentContext()
            .setLocale(locale);
        return this;
    }

    @Override
    public Optional<EmailAddress> user() {
        return this.environmentContext()
            .user();
    }

    @Override
    public EnvironmentContext setUser(final Optional<EmailAddress> user) {
        this.environmentContext()
            .setUser(user);
        return this;
    }

    /**
     * The {@link ProviderContext} holds the system or global environment.
     */
    @Override
    public EnvironmentContext environmentContext() {
        return this.providerContext;
    }

    // LocaleContextDelegator...........................................................................................

    @Override
    public LocaleContext localeContext() {
        return this.localeContext;
    }

    private final LocaleContext localeContext;

    // SpreadsheetMetadataContext.......................................................................................

    @Override
    public SpreadsheetMetadata createMetadata(final EmailAddress user,
                                              final Optional<Locale> locale) {
        return this.spreadsheetMetadataContext.createMetadata(
            user,
            locale
        );
    }

    @Override
    public Optional<SpreadsheetMetadata> loadMetadata(final SpreadsheetId spreadsheetId) {
        return this.spreadsheetMetadataContext.loadMetadata(spreadsheetId);
    }

    @Override
    public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
        return this.spreadsheetMetadataContext.saveMetadata(metadata);
    }

    @Override
    public void deleteMetadata(final SpreadsheetId spreadsheetId) {
        this.spreadsheetMetadataContext.deleteMetadata(spreadsheetId);

        this.spreadsheetIdToSpreadsheetContext.remove(spreadsheetId);
    }

    @Override
    public List<SpreadsheetMetadata> findMetadataBySpreadsheetName(final String name,
                                                                   final int offset,
                                                                   final int count) {
        return this.spreadsheetMetadataContext.findMetadataBySpreadsheetName(
            name,
            offset,
            count
        );
    }

    private final SpreadsheetMetadataContext spreadsheetMetadataContext;

    // HateosResourceHandlerContextDelegator............................................................................

    @Override
    public HateosResourceHandlerContext hateosResourceHandlerContext() {
        return this.hateosResourceHandlerContext;
    }

    private final HateosResourceHandlerContext hateosResourceHandlerContext;

    @Override
    public SpreadsheetServerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        return this.setHateosResourceHandlerContext(
            this.hateosResourceHandlerContext.setObjectPostProcessor(processor)
        );
    }

    @Override
    public SpreadsheetServerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.setHateosResourceHandlerContext(
            this.hateosResourceHandlerContext.setPreProcessor(processor)
        );
    }

    private SpreadsheetServerContext setHateosResourceHandlerContext(final HateosResourceHandlerContext context) {
        return this.hateosResourceHandlerContext.equals(context) ?
            this :
            new BasicSpreadsheetServerContext(
                this.serverUrl,
                this.spreadsheetStoreRepository,
                this.spreadsheetProvider,
                this.environmentContext,
                this.localeContext,
                this.spreadsheetMetadataContext,
                Objects.requireNonNull(context, "hateosResourceHandlerContext"),
                this.providerContext
            );
    }

    // SpreadsheetProviderDelegator.....................................................................................

    @Override
    public SpreadsheetProvider spreadsheetProvider() {
        return this.spreadsheetProvider;
    }

    private final SpreadsheetProvider spreadsheetProvider;

    // ProviderContext..................................................................................................

    @Override
    public ProviderContext providerContext() {
        return this.providerContext;
    }

    private final ProviderContext providerContext;

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.serverUrl + " " + this.environmentContext + " " + this.localeContext + " " + this.spreadsheetProvider;
    }
}
