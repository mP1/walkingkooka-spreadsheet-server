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

import org.junit.jupiter.api.Test;
import walkingkooka.Cast;
import walkingkooka.ToStringTesting;
import walkingkooka.environment.AuditInfo;
import walkingkooka.environment.EnvironmentContextTesting;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.Url;
import walkingkooka.net.UrlCredentials;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.FakeHateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.plugin.FakeProviderContext;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.provider.SpreadsheetProviderContexts;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicSpreadsheetServerContextTest implements SpreadsheetServerContextTesting<BasicSpreadsheetServerContext>,
    EnvironmentContextTesting,
    ToStringTesting<BasicSpreadsheetServerContext>,
    SpreadsheetMetadataTesting {

    private final static AbsoluteUrl SERVER_URL = Url.parseAbsolute("https://example.com");

    private final static Function<SpreadsheetId, SpreadsheetStoreRepository> REPO = (i) -> SpreadsheetStoreRepositories.fake();

    private final static Function<SpreadsheetContext, SpreadsheetEngineContext> SPREADSHEET_ENGINE_CONTEXT_FUNCTION = (c) ->
        new FakeSpreadsheetEngineContext() {
            @Override
            public SpreadsheetId spreadsheetId() {
                return SpreadsheetId.with(1);
            }

            @Override
            public SpreadsheetStoreRepository storeRepository() {
                return new FakeSpreadsheetStoreRepository() {
                    @Override
                    public SpreadsheetLabelStore labels() {
                        return SpreadsheetLabelStores.fake();
                    }
                };
            }
        };

    private final static SpreadsheetMetadataContext SPREADSHEET_METADATA_CONTEXT = SpreadsheetMetadataContexts.fake();

    private final static HateosResourceHandlerContext HATEOS_RESOURCE_HANDLER_CONTEXT = new FakeHateosResourceHandlerContext() {
        @Override
        public HateosResourceHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
            return this;
        }

        @Override
        public HateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
            return this;
        }
    };

    private final static ProviderContext PROVIDER_CONTEXT = ProviderContexts.fake();

    // with.............................................................................................................

    @Test
    public void testWithNullServerUrlFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                null,
                REPO,
                SPREADSHEET_PROVIDER,
                SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
                ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
            )
        );
    }

    @Test
    public void testWithServerUrlWithCredentialsFails() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> BasicSpreadsheetServerContext.with(
                Url.parseAbsolute("https://example.com")
                    .setCredentials(
                        Optional.of(
                            UrlCredentials.with("user1", "pass1")
                        )
                    ),
                REPO,
                SPREADSHEET_PROVIDER,
                SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
                ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
            )
        );

        this.checkEquals(
            "Url must not have credentials got \"https://user1:pass1@example.com\"",
            thrown.getMessage(),
            "message"
        );
    }

    @Test
    public void testWithServerUrlWithPathFails() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> BasicSpreadsheetServerContext.with(
                Url.parseAbsolute("https://example.com/path123"),
                REPO,
                SPREADSHEET_PROVIDER,
                SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
                ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
            )
        );

        this.checkEquals(
            "Url must not have path got \"https://example.com/path123\"",
            thrown.getMessage(),
            "message"
        );
    }

    @Test
    public void testWithServerUrlWithQueryStringFails() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> BasicSpreadsheetServerContext.with(
                Url.parseAbsolute("https://example.com?k=v"),
                REPO,
                SPREADSHEET_PROVIDER,
                SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
                ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
            )
        );

        this.checkEquals(
            "Url must not have query string got \"https://example.com?k=v\"",
            thrown.getMessage(),
            "message"
        );
    }

    @Test
    public void testWithServerUrlWithFragmentFails() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> BasicSpreadsheetServerContext.with(
                Url.parseAbsolute("https://example.com#fragment123"),
                REPO,
                SPREADSHEET_PROVIDER,
                SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
                ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
            )
        );

        this.checkEquals(
            "Url must not have fragment got \"https://example.com#fragment123\"",
            thrown.getMessage(),
            "message"
        );
    }

    @Test
    public void testWithNullSpreadsheetIdToSpreadsheetServerRepositoryFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SERVER_URL,
                null,
                SPREADSHEET_PROVIDER,
                SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
                ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetProviderFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SERVER_URL,
                REPO,
                null,
                SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
                ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetEngineContextFactoryFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SERVER_URL,
                REPO,
                SPREADSHEET_PROVIDER,
                null,
                ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullEnvironmentContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SERVER_URL,
                REPO,
                SPREADSHEET_PROVIDER,
                SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
                null,
                LOCALE_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullLocaleContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SERVER_URL,
                REPO,
                SPREADSHEET_PROVIDER,
                SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
                ENVIRONMENT_CONTEXT,
                null,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetMetadataContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SERVER_URL,
                REPO,
                SPREADSHEET_PROVIDER,
                SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
                ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                null,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullHateosResourceHandlerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SERVER_URL,
                REPO,
                SPREADSHEET_PROVIDER,
                SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
                ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                null,
                PROVIDER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullProviderContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SERVER_URL,
                REPO,
                SPREADSHEET_PROVIDER,
                SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
                ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                null
            )
        );
    }

    @Override
    public BasicSpreadsheetServerContext createContext() {
        return this.createContext(
            SpreadsheetProviderContexts.basic(
                PluginStores.fake(),
                JSON_NODE_MARSHALL_UNMARSHALL_CONTEXT,
                SpreadsheetMetadata.EMPTY.set(
                    SpreadsheetMetadataPropertyName.LOCALE,
                    LOCALE
                ).set(
                    SpreadsheetMetadataPropertyName.DATE_FORMATTER,
                    METADATA_EN_AU.getOrFail(SpreadsheetMetadataPropertyName.DATE_FORMATTER)
                ).set(
                    SpreadsheetMetadataPropertyName.DATE_PARSER,
                    METADATA_EN_AU.getOrFail(SpreadsheetMetadataPropertyName.DATE_PARSER)
                ).set(
                    SpreadsheetMetadataPropertyName.DATE_TIME_FORMATTER,
                    METADATA_EN_AU.getOrFail(SpreadsheetMetadataPropertyName.DATE_TIME_FORMATTER)
                ).set(
                    SpreadsheetMetadataPropertyName.DATE_TIME_PARSER,
                    METADATA_EN_AU.getOrFail(SpreadsheetMetadataPropertyName.DATE_TIME_PARSER)
                ).set(
                    SpreadsheetMetadataPropertyName.ERROR_FORMATTER,
                    METADATA_EN_AU.getOrFail(SpreadsheetMetadataPropertyName.ERROR_FORMATTER)
                ).set(
                    SpreadsheetMetadataPropertyName.NUMBER_FORMATTER,
                    METADATA_EN_AU.getOrFail(SpreadsheetMetadataPropertyName.NUMBER_FORMATTER)
                ).set(
                    SpreadsheetMetadataPropertyName.NUMBER_PARSER,
                    METADATA_EN_AU.getOrFail(SpreadsheetMetadataPropertyName.NUMBER_PARSER)
                ).set(
                    SpreadsheetMetadataPropertyName.TEXT_FORMATTER,
                    METADATA_EN_AU.getOrFail(SpreadsheetMetadataPropertyName.TEXT_FORMATTER)
                ).set(
                    SpreadsheetMetadataPropertyName.TIME_FORMATTER,
                    METADATA_EN_AU.getOrFail(SpreadsheetMetadataPropertyName.TIME_FORMATTER)
                ).set(
                    SpreadsheetMetadataPropertyName.TIME_PARSER,
                    METADATA_EN_AU.getOrFail(SpreadsheetMetadataPropertyName.TIME_PARSER)
                ).environmentContext(
                    EnvironmentContexts.map(
                        ENVIRONMENT_CONTEXT
                    )
                ),
                LOCALE_CONTEXT
            )
        );
    }

    private BasicSpreadsheetServerContext createContext(final ProviderContext providerContext) {
        final SpreadsheetMetadataStore spreadsheetMetadataStore = SpreadsheetMetadataStores.treeMap();

        return BasicSpreadsheetServerContext.with(
            SERVER_URL,
            (id) -> new FakeSpreadsheetStoreRepository() {

                @Override
                public SpreadsheetLabelStore labels() {
                    return SpreadsheetLabelStores.fake();
                }

                @Override
                public SpreadsheetMetadataStore metadatas() {
                    return spreadsheetMetadataStore;
                }
            },
            SPREADSHEET_PROVIDER,
            SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
            EnvironmentContexts.readOnly(
                EnvironmentContexts.map(ENVIRONMENT_CONTEXT)
            ),
            LOCALE_CONTEXT,
            SpreadsheetMetadataContexts.basic(
                (u, l) -> {
                    final SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY.set(
                        SpreadsheetMetadataPropertyName.LOCALE,
                        l.get()
                    ).set(
                        SpreadsheetMetadataPropertyName.AUDIT_INFO,
                        AuditInfo.with(
                            u,
                            LocalDateTime.MIN,
                            u,
                            LocalDateTime.MIN
                        )
                    );
                    spreadsheetMetadataStore.save(metadata);
                    return metadata;
                },
                SpreadsheetMetadataStores.treeMap()
            ),
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            providerContext
        );
    }

    // serverUrl........................................................................................................

    @Test
    public void testServerUrl() {
        this.serverUrlAndCheck(
            this.createContext(),
            SERVER_URL
        );
    }

    // createSpreadsheetContext.........................................................................................

    @Test
    public void testCreateSpreadsheetContext() {
        final EmailAddress user = EmailAddress.parse("spreadsheet-user1@example.com");
        final Locale locale = Locale.forLanguageTag("en-AU");

        final SpreadsheetServerContext spreadsheetServerContext = this.createContext();
        final SpreadsheetContext spreadsheetContext = spreadsheetServerContext.createSpreadsheetContext(
                user,
                Optional.of(locale)
            );
        this.checkNotEquals(
            null,
            spreadsheetContext
        );

        this.checkEquals(
            Optional.of(user),
            spreadsheetContext.user()
        );

        this.localeAndCheck(
            spreadsheetContext,
            locale
        );
    }

    @Test
    public void testCreateSpreadsheetContextTwice() {
        final SpreadsheetServerContext spreadsheetServerContext = this.createContext();

        final EmailAddress user1 = EmailAddress.parse("spreadsheet-user1@example.com");
        final Locale locale1 = Locale.forLanguageTag("en-AU");
        
        final SpreadsheetContext spreadsheetContext1 = spreadsheetServerContext.createSpreadsheetContext(
            user1,
            Optional.of(locale1)
        );
        this.checkNotEquals(
            null,
            spreadsheetContext1
        );

        final EmailAddress user2 = EmailAddress.parse("spreadsheet-user2@example.com");
        final Locale locale2 = Locale.forLanguageTag("en-AU");

        final SpreadsheetContext spreadsheetContext2 = spreadsheetServerContext.createSpreadsheetContext(
            user2,
            Optional.of(locale2)
        );
        this.checkNotEquals(
            null,
            spreadsheetContext2
        );

        this.checkEquals(
            Optional.of(user1),
            spreadsheetContext1.user()
        );

        this.localeAndCheck(
            spreadsheetContext1,
            locale1
        );

        this.checkEquals(
            Optional.of(user2),
            spreadsheetContext2.user()
        );

        this.localeAndCheck(
            spreadsheetContext2,
            locale2
        );
    }

    // spreadsheetContext...............................................................................................

    @Test
    public void testSpreadsheetContextWithMissingSpreadsheetId() {
        final SpreadsheetServerContext context = this.createContext();

        this.checkNotEquals(
            null,
            context.spreadsheetContext(
                SpreadsheetId.with(404)
            )
        );
    }

    @Test
    public void testSpreadsheetContext() {
        final BasicSpreadsheetServerContext spreadsheetServerContext = this.createContext();

        final EmailAddress user1 = EmailAddress.parse("spreadsheet-user1@example.com");
        final Locale locale1 = Locale.forLanguageTag("en-AU");

        final SpreadsheetContext spreadsheetContext = spreadsheetServerContext.createSpreadsheetContext(
            user1,
            Optional.of(locale1)
        );
        this.checkNotEquals(
            null,
            spreadsheetContext
        );

        this.spreadsheetContextAndCheck(
            spreadsheetServerContext,
            spreadsheetContext.spreadsheetId(),
            spreadsheetContext
        );
    }

    // locale...........................................................................................................

    @Test
    public void testLocale() {
        final Locale locale = Locale.FRANCE;

        this.localeAndCheck(
            this.createContext(
                new FakeProviderContext() {
                    @Override
                    public Locale locale() {
                        return locale;
                    }

                    @Override
                    public <T> Optional<T> environmentValue(final EnvironmentValueName<T> name) {
                        checkEquals(LOCALE, name);
                        return Optional.of(
                            Cast.to(locale)
                        );
                    }
                }
            ),
            locale
        );
    }

    @Test
    public void testSetLocale() {
        final BasicSpreadsheetServerContext context = this.createContext();
        this.localeAndCheck(
            context,
            LOCALE
        );

        final Locale locale = Locale.FRANCE;

        this.localeAndCheck(
            context.setLocale(locale),
            locale
        );
    }

    // user...........................................................................................................

    @Test
    public void testUser() {
        final EmailAddress user = EmailAddress.parse("different@example.com");

        this.userAndCheck(
            this.createContext(
                new FakeProviderContext() {
                    @Override
                    public Optional<EmailAddress> user() {
                        return Optional.of(user);
                    }
                }
            ),
            user
        );
    }

    @Test
    public void testSetUser() {
        final BasicSpreadsheetServerContext context = this.createContext();
        this.userAndCheck(
            context,
            USER
        );

        final EmailAddress user = EmailAddress.parse("different@example.com");

        this.userAndCheck(
            context.setUser(
                Optional.of(user)
            ),
            user
        );
    }
    
    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            BasicSpreadsheetServerContext.with(
                SERVER_URL,
                REPO,
                SPREADSHEET_PROVIDER,
                SPREADSHEET_ENGINE_CONTEXT_FUNCTION,
                ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
            ),
            "https://example.com {} JRE en-AU [https://github.com/mP1/walkingkooka-spreadsheet/Converter/basic basic, https://github.com/mP1/walkingkooka-spreadsheet/Converter/boolean boolean, https://github.com/mP1/walkingkooka-spreadsheet/Converter/boolean-to-text boolean-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/collection collection, https://github.com/mP1/walkingkooka-spreadsheet/Converter/collection-to collection-to, https://github.com/mP1/walkingkooka-spreadsheet/Converter/collection-to-list collection-to-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/color color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/color-to-color color-to-color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/color-to-number color-to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/date-time date-time, https://github.com/mP1/walkingkooka-spreadsheet/Converter/date-time-symbols date-time-symbols, https://github.com/mP1/walkingkooka-spreadsheet/Converter/decimal-number-symbols decimal-number-symbols, https://github.com/mP1/walkingkooka-spreadsheet/Converter/environment environment, https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-throwing error-throwing, https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-to-error error-to-error, https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-to-number error-to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/expression expression, https://github.com/mP1/walkingkooka-spreadsheet/Converter/form-and-validation form-and-validation, https://github.com/mP1/walkingkooka-spreadsheet/Converter/format-pattern-to-string format-pattern-to-string, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-formatter-selector has-formatter-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-host-address has-host-address, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-parser-selector has-parser-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-spreadsheet-selection has-spreadsheet-selection, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-style has-style, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-text-node has-text-node, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-validator-selector has-validator-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/json json, https://github.com/mP1/walkingkooka-spreadsheet/Converter/json-to json-to, https://github.com/mP1/walkingkooka-spreadsheet/Converter/locale locale, https://github.com/mP1/walkingkooka-spreadsheet/Converter/locale-to-text locale-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/net net, https://github.com/mP1/walkingkooka-spreadsheet/Converter/null-to-number null-to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/number number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/number-to-color number-to-color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/number-to-number number-to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/number-to-text number-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/optional-to optional-to, https://github.com/mP1/walkingkooka-spreadsheet/Converter/plugins plugins, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-cell-set spreadsheet-cell-set, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-metadata spreadsheet-metadata, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-selection-to-spreadsheet-selection spreadsheet-selection-to-spreadsheet-selection, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-selection-to-text spreadsheet-selection-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-value spreadsheet-value, https://github.com/mP1/walkingkooka-spreadsheet/Converter/style style, https://github.com/mP1/walkingkooka-spreadsheet/Converter/system system, https://github.com/mP1/walkingkooka-spreadsheet/Converter/template template, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-node text-node, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-boolean-list text-to-boolean-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-color text-to-color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-csv-string-list text-to-csv-string-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-date-list text-to-date-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-date-time-list text-to-date-time-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-email-address text-to-email-address, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-environment-value-name text-to-environment-value-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-error text-to-error, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-expression text-to-expression, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-form-name text-to-form-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-has-host-address text-to-has-host-address, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-host-address text-to-host-address, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-json text-to-json, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-locale text-to-locale, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-number-list text-to-number-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-object text-to-object, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-color-name text-to-spreadsheet-color-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-formatter-selector text-to-spreadsheet-formatter-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-id text-to-spreadsheet-id, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-metadata text-to-spreadsheet-metadata, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-metadata-color text-to-spreadsheet-metadata-color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-metadata-property-name text-to-spreadsheet-metadata-property-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-name text-to-spreadsheet-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-selection text-to-spreadsheet-selection, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-text text-to-spreadsheet-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-string-list text-to-string-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-template-value-name text-to-template-value-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text text-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text-node text-to-text-node, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text-style text-to-text-style, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text-style-property-name text-to-text-style-property-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-time-list text-to-time-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-url text-to-url, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-url-fragment text-to-url-fragment, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-url-query-string text-to-url-query-string, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-validation-error text-to-validation-error, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-validator-selector text-to-validator-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-value-type text-to-value-type, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-boolean to-boolean, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-json to-json, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-number to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-styleable to-styleable, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-validation-checkbox to-validation-checkbox, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-validation-choice to-validation-choice, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-validation-choice-list to-validation-choice-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-validation-error-list to-validation-error-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/url url, https://github.com/mP1/walkingkooka-spreadsheet/Converter/url-to-hyperlink url-to-hyperlink, https://github.com/mP1/walkingkooka-spreadsheet/Converter/url-to-image url-to-image] [] [https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/date date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/date-time date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/day-of-month day-of-month, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/day-of-week day-of-week, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/hour-of-am-pm hour-of-am-pm, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/hour-of-day hour-of-day, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/minute-of-hour minute-of-hour, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/month-of-year month-of-year, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/nano-of-second nano-of-second, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/number number, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/seconds-of-minute seconds-of-minute, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/text text, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/text-case-insensitive text-case-insensitive, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/time time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/year year] [https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/accounting accounting, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/automatic automatic, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/badge-error badge-error, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/collection collection, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/currency currency, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/date date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/date-time date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/default-text default-text, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/expression expression, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/full-date full-date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/full-date-time full-date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/full-time full-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/general general, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/long-date long-date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/long-date-time long-date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/long-time long-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/medium-date medium-date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/medium-date-time medium-date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/medium-time medium-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/number number, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/percent percent, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/scientific scientific, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/short-date short-date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/short-date-time short-date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/short-time short-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/text text, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/time time] [] [https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetImporter/collection collection, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetImporter/empty empty, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetImporter/json json] [https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/date date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/date-time date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/general general, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/number number, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/time time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/whole-number whole-number] [https://github.com/mP1/walkingkooka-validation/Validator/absolute-url absolute-url, https://github.com/mP1/walkingkooka-validation/Validator/checkbox checkbox, https://github.com/mP1/walkingkooka-validation/Validator/choice-list choice-list, https://github.com/mP1/walkingkooka-validation/Validator/collection collection, https://github.com/mP1/walkingkooka-validation/Validator/email-address email-address, https://github.com/mP1/walkingkooka-validation/Validator/expression expression, https://github.com/mP1/walkingkooka-validation/Validator/non-null non-null, https://github.com/mP1/walkingkooka-validation/Validator/text-length text-length, https://github.com/mP1/walkingkooka-validation/Validator/text-mask text-mask]"
        );
    }

    // class............................................................................................................

    @Override
    public Class<BasicSpreadsheetServerContext> type() {
        return BasicSpreadsheetServerContext.class;
    }
}
