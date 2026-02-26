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
import walkingkooka.convert.ConverterContexts;
import walkingkooka.environment.AuditInfo;
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.FakeHateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.plugin.FakeProviderContext;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.environment.FakeSpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContexts;
import walkingkooka.spreadsheet.meta.FakeSpreadsheetMetadataContext;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
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
import walkingkooka.terminal.TerminalContext;
import walkingkooka.terminal.TerminalId;
import walkingkooka.terminal.server.FakeTerminalServerContext;
import walkingkooka.terminal.server.TerminalServerContext;
import walkingkooka.text.LineEnding;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicSpreadsheetServerContextTest implements SpreadsheetServerContextTesting<BasicSpreadsheetServerContext>,
    ToStringTesting<BasicSpreadsheetServerContext>,
    SpreadsheetMetadataTesting,
    TreePrintableTesting {

    private final static SpreadsheetEngine SPREADSHEET_ENGINE = SpreadsheetEngines.fake();

    private final static Function<SpreadsheetId, Optional<SpreadsheetStoreRepository>> SPREADSHEET_ID_TO_SPREADSHEET_STORE_REPOSITORY = (i) -> Optional.of(SpreadsheetStoreRepositories.fake());

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

    private final static SpreadsheetId SPREADSHEET_ID = SpreadsheetId.with(123);

    // with.............................................................................................................

    @Test
    public void testWithNullSpreadsheetEngineFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                null,
                SPREADSHEET_ID_TO_SPREADSHEET_STORE_REPOSITORY,
                SPREADSHEET_PROVIDER,
                CURRENCY_LOCALE_CONTEXT,
                SPREADSHEET_ENVIRONMENT_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT,
                TERMINAL_SERVER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetIdToSpreadsheetServerRepositoryFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SPREADSHEET_ENGINE,
                null,
                SPREADSHEET_PROVIDER,
                CURRENCY_LOCALE_CONTEXT,
                SPREADSHEET_ENVIRONMENT_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT,
                TERMINAL_SERVER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetProviderFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SPREADSHEET_ENGINE,
                SPREADSHEET_ID_TO_SPREADSHEET_STORE_REPOSITORY,
                null,
                CURRENCY_LOCALE_CONTEXT,
                SPREADSHEET_ENVIRONMENT_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT,
                TERMINAL_SERVER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullCurrencyLocaleContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SPREADSHEET_ENGINE,
                SPREADSHEET_ID_TO_SPREADSHEET_STORE_REPOSITORY,
                SPREADSHEET_PROVIDER,
                null,
                SPREADSHEET_ENVIRONMENT_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT,
                TERMINAL_SERVER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullEnvironmentContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SPREADSHEET_ENGINE,
                SPREADSHEET_ID_TO_SPREADSHEET_STORE_REPOSITORY,
                SPREADSHEET_PROVIDER,
                CURRENCY_LOCALE_CONTEXT,
                null,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT,
                TERMINAL_SERVER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetMetadataContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SPREADSHEET_ENGINE,
                SPREADSHEET_ID_TO_SPREADSHEET_STORE_REPOSITORY,
                SPREADSHEET_PROVIDER,
                CURRENCY_LOCALE_CONTEXT,
                SPREADSHEET_ENVIRONMENT_CONTEXT,
                null,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT,
                TERMINAL_SERVER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullHateosResourceHandlerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SPREADSHEET_ENGINE,
                SPREADSHEET_ID_TO_SPREADSHEET_STORE_REPOSITORY,
                SPREADSHEET_PROVIDER,
                CURRENCY_LOCALE_CONTEXT,
                SPREADSHEET_ENVIRONMENT_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                null,
                PROVIDER_CONTEXT,
                TERMINAL_SERVER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullProviderContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SPREADSHEET_ENGINE,
                SPREADSHEET_ID_TO_SPREADSHEET_STORE_REPOSITORY,
                SPREADSHEET_PROVIDER,
                CURRENCY_LOCALE_CONTEXT,
                SPREADSHEET_ENVIRONMENT_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                null,
                TERMINAL_SERVER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullTerminalServerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetServerContext.with(
                SPREADSHEET_ENGINE,
                SPREADSHEET_ID_TO_SPREADSHEET_STORE_REPOSITORY,
                SPREADSHEET_PROVIDER,
                CURRENCY_LOCALE_CONTEXT,
                SPREADSHEET_ENVIRONMENT_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT,
                null
            )
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
    public void testCreateEmptySpreadsheet() {
        final Locale locale = Locale.forLanguageTag("en-AU");

        final SpreadsheetServerContext spreadsheetServerContext = this.createContext();
        final SpreadsheetContext spreadsheetContext = spreadsheetServerContext.createEmptySpreadsheet(
            Optional.of(locale)
            );
        this.checkNotEquals(
            null,
            spreadsheetContext
        );

        this.userAndCheck(
            spreadsheetContext,
            USER
        );

        this.localeAndCheck(
            spreadsheetContext,
            locale
        );
    }

    @Test
    public void testCreateEmptySpreadsheetEnvironmentValueSpreadsheetIdReplaced() {
        final SpreadsheetId spreadsheetId = SpreadsheetId.with(0x9999);

        final EnvironmentContext environmentContext = ENVIRONMENT_CONTEXT.cloneEnvironment();
        environmentContext.setEnvironmentValue(
            SpreadsheetEnvironmentContext.SPREADSHEET_ID,
            spreadsheetId
        );

        final SpreadsheetServerContext spreadsheetServerContext = this.createContext(
            SpreadsheetEnvironmentContexts.basic(
                STORAGE,
                environmentContext
            )
        );

        final Locale locale = Locale.forLanguageTag("en-AU");
        final SpreadsheetContext spreadsheetContext = spreadsheetServerContext.createEmptySpreadsheet(
            Optional.of(locale)
        );
        this.checkNotEquals(
            null,
            spreadsheetContext
        );

        this.spreadsheetIdAndCheck(
            spreadsheetContext,
            SpreadsheetId.with(1)
        );

        // original EnvironmentContext and SpreadsheetServerContext not changed.
        this.environmentValueAndCheck(
            environmentContext,
            SpreadsheetEnvironmentContext.SPREADSHEET_ID,
            spreadsheetId
        );

        this.spreadsheetIdAndCheck(
            spreadsheetServerContext,
            spreadsheetId
        );

        this.userAndCheck(
            spreadsheetContext,
            USER
        );

        this.localeAndCheck(
            spreadsheetContext,
            locale
        );
    }

    @Test
    public void testCreateEmptySpreadsheetTwice() {
        final EmailAddress user1 = EmailAddress.parse("spreadsheet-user1@example.com");
        final SpreadsheetServerContext spreadsheetServerContext = this.createContext(user1);

        final Locale locale1 = Locale.forLanguageTag("en-AU");
        
        final SpreadsheetContext spreadsheetContext1 = spreadsheetServerContext.createEmptySpreadsheet(
            Optional.of(locale1)
        );
        this.checkNotEquals(
            null,
            spreadsheetContext1
        );

        final EmailAddress user2 = EmailAddress.parse("spreadsheet-user2@example.com");
        spreadsheetServerContext.setUser(
            Optional.of(user2)
        );
        final Locale locale2 = Locale.forLanguageTag("en-AU");

        final SpreadsheetContext spreadsheetContext2 = spreadsheetServerContext.createEmptySpreadsheet(
            Optional.of(locale2)
        );
        this.checkNotEquals(
            null,
            spreadsheetContext2
        );

        this.userAndCheck(
            spreadsheetContext1,
            user1
        );

        this.localeAndCheck(
            spreadsheetContext1,
            locale1
        );

        this.userAndCheck(
            spreadsheetContext2,
            user2
        );

        this.localeAndCheck(
            spreadsheetContext2,
            locale2
        );
    }

    // createSpreadsheetContext.........................................................................................

    @Test
    public void testCreateSpreadsheetContext() {
        final BasicSpreadsheetServerContext context = this.createContext();

        final SpreadsheetContext spreadsheetContext1 = context.createSpreadsheetContext();

        this.spreadsheetIdAndCheck(
            spreadsheetContext1,
            SPREADSHEET_ID
        );

        final SpreadsheetContext spreadsheetContext2 = context.createSpreadsheetContext();

        this.spreadsheetIdAndCheck(
            spreadsheetContext2,
            SPREADSHEET_ID
        );

        assertNotSame(
            spreadsheetContext1,
            spreadsheetContext2
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

        final Locale locale1 = Locale.forLanguageTag("en-AU");

        final SpreadsheetContext spreadsheetContext = spreadsheetServerContext.createEmptySpreadsheet(
            Optional.of(locale1)
        );
        this.checkNotEquals(
            null,
            spreadsheetContext
        );

        this.spreadsheetContextAndCheck(
            spreadsheetServerContext,
            spreadsheetContext.spreadsheetIdOrFail(),
            spreadsheetContext
        );
    }

    // EnvironmentContext...............................................................................................

    @Test
    public void testSetEnvironmentContextWithDifferent() {
        final EnvironmentContext environmentContext = EnvironmentContexts.empty(
            CURRENCY,
            INDENTATION,
            LineEnding.NL,
            LOCALE,
            HAS_NOW,
            EnvironmentContext.ANONYMOUS
        );

        final PluginStore pluginStore = PluginStores.fake();

        final SpreadsheetServerContext before = BasicSpreadsheetServerContext.with(
            SPREADSHEET_ENGINE,
            SPREADSHEET_ID_TO_SPREADSHEET_STORE_REPOSITORY,
            SPREADSHEET_PROVIDER,
            CURRENCY_LOCALE_CONTEXT,
            SPREADSHEET_ENVIRONMENT_CONTEXT,
            SPREADSHEET_METADATA_CONTEXT,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            ProviderContexts.basic(
                ConverterContexts.fake(),
                environmentContext,
                pluginStore
            ),
            TERMINAL_SERVER_CONTEXT
        );

        final EnvironmentContext differentEnvironmentContext = environmentContext.cloneEnvironment();
        differentEnvironmentContext.setLineEnding(LineEnding.CRNL);

        this.checkNotEquals(
            environmentContext,
            differentEnvironmentContext
        );

        final SpreadsheetServerContext after = before.setEnvironmentContext(differentEnvironmentContext);
        this.checkNotEquals(
            before,
            after
        );
    }

    // locale...........................................................................................................

    @Test
    public void testLocale() {
        final Locale locale = Locale.FRANCE;

        this.localeAndCheck(
            this.createContext(
                new FakeSpreadsheetEnvironmentContext() {
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
        context.setLocale(locale);

        this.localeAndCheck(
            context,
            locale
        );
    }

    // user...........................................................................................................

    @Test
    public void testUser() {
        final EmailAddress user = EmailAddress.parse("different@example.com");
        final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext = SpreadsheetEnvironmentContexts.basic(
            STORAGE,
            ENVIRONMENT_CONTEXT.cloneEnvironment()
        );
        spreadsheetEnvironmentContext.setUser(
            Optional.of(user)
        );

        this.userAndCheck(
            this.createContext(spreadsheetEnvironmentContext),
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

        this.setUserAndCheck(
            context,
            user
        );
    }

    @Override
    public BasicSpreadsheetServerContext createContext() {
        return this.createContext(USER);
    }

    private BasicSpreadsheetServerContext createContext(final EmailAddress user) {
        final EnvironmentContext environmentContext = ENVIRONMENT_CONTEXT.cloneEnvironment();
        environmentContext.setUser(
            Optional.of(user)
        );
        environmentContext.setEnvironmentValue(
            SpreadsheetEnvironmentContext.SPREADSHEET_ID,
            SPREADSHEET_ID
        );

        return this.createContext(
            SpreadsheetEnvironmentContexts.basic(
                STORAGE,
                environmentContext
            )
        );
    }

    private BasicSpreadsheetServerContext createContext(final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext) {
        final SpreadsheetMetadataStore spreadsheetMetadataStore = SpreadsheetMetadataStores.treeMap();

        return BasicSpreadsheetServerContext.with(
            SPREADSHEET_ENGINE,
            (id) -> Optional.of(
                new FakeSpreadsheetStoreRepository() {

                    @Override
                    public SpreadsheetLabelStore labels() {
                        return SpreadsheetLabelStores.fake();
                    }

                    @Override
                    public SpreadsheetMetadataStore metadatas() {
                        return spreadsheetMetadataStore;
                    }
                }
            ),
            SPREADSHEET_PROVIDER,
            CURRENCY_LOCALE_CONTEXT,
            spreadsheetEnvironmentContext,
            SpreadsheetMetadataContexts.basic(
                (u, l) -> {
                    final SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY.set(
                        SpreadsheetMetadataPropertyName.LOCALE,
                        l.get()
                    ).set(
                        SpreadsheetMetadataPropertyName.AUDIT_INFO,
                        AuditInfo.create(
                            u,
                            LocalDateTime.MIN
                        )
                    );
                    return spreadsheetMetadataStore.save(metadata);
                },
                spreadsheetMetadataStore
            ),
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SpreadsheetProviderContexts.spreadsheet(
                PluginStores.fake(),
                CURRENCY_LOCALE_CONTEXT,
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
                ).spreadsheetEnvironmentContext(
                    SPREADSHEET_ENVIRONMENT_CONTEXT.cloneEnvironment()
                ),
                JSON_NODE_MARSHALL_UNMARSHALL_CONTEXT
            ),
            new FakeTerminalServerContext() {

                @Override
                public Optional<TerminalContext> terminalContext(final TerminalId id) {
                    Objects.requireNonNull(id, "id");
                    throw new UnsupportedOperationException();
                }

                @Override
                public TerminalServerContext removeTerminalContext(final TerminalId id) {
                    Objects.requireNonNull(id, "id");
                    throw new UnsupportedOperationException();
                }
            }
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            BasicSpreadsheetServerContext.with(
                SPREADSHEET_ENGINE,
                SPREADSHEET_ID_TO_SPREADSHEET_STORE_REPOSITORY,
                SPREADSHEET_PROVIDER,
                CURRENCY_LOCALE_CONTEXT,
                SPREADSHEET_ENVIRONMENT_CONTEXT,
                SPREADSHEET_METADATA_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT,
                TERMINAL_SERVER_CONTEXT
            ),
            "{currency=\"AUD\", indentation=\"  \", lineEnding=\"\\n\", locale=en_AU, serverUrl=https://example.com, timeOffset=Z, user=user@example.com} JRE ReadOnly JRE en-AU [https://github.com/mP1/walkingkooka-spreadsheet/Converter/basic basic, https://github.com/mP1/walkingkooka-spreadsheet/Converter/boolean boolean, https://github.com/mP1/walkingkooka-spreadsheet/Converter/boolean-to-text boolean-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/collection collection, https://github.com/mP1/walkingkooka-spreadsheet/Converter/collection-to collection-to, https://github.com/mP1/walkingkooka-spreadsheet/Converter/collection-to-list collection-to-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/color color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/color-to-color color-to-color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/color-to-number color-to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/date-time date-time, https://github.com/mP1/walkingkooka-spreadsheet/Converter/date-time-symbols date-time-symbols, https://github.com/mP1/walkingkooka-spreadsheet/Converter/decimal-number-symbols decimal-number-symbols, https://github.com/mP1/walkingkooka-spreadsheet/Converter/environment environment, https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-throwing error-throwing, https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-to-error error-to-error, https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-to-number error-to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/expression expression, https://github.com/mP1/walkingkooka-spreadsheet/Converter/form-and-validation form-and-validation, https://github.com/mP1/walkingkooka-spreadsheet/Converter/format-pattern-to-string format-pattern-to-string, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-formatter-selector has-formatter-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-host-address has-host-address, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-parser-selector has-parser-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-properties has-properties, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-spreadsheet-selection has-spreadsheet-selection, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-style has-style, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-text-node has-text-node, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-validator-selector has-validator-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/json json, https://github.com/mP1/walkingkooka-spreadsheet/Converter/json-to json-to, https://github.com/mP1/walkingkooka-spreadsheet/Converter/locale locale, https://github.com/mP1/walkingkooka-spreadsheet/Converter/locale-to-text locale-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/net net, https://github.com/mP1/walkingkooka-spreadsheet/Converter/null-to-number null-to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/number number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/number-to-color number-to-color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/number-to-number number-to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/number-to-text number-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/optional-to optional-to, https://github.com/mP1/walkingkooka-spreadsheet/Converter/plugins plugins, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-cell-set spreadsheet-cell-set, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-metadata spreadsheet-metadata, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-selection-to-spreadsheet-selection spreadsheet-selection-to-spreadsheet-selection, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-selection-to-text spreadsheet-selection-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-value spreadsheet-value, https://github.com/mP1/walkingkooka-spreadsheet/Converter/storage storage, https://github.com/mP1/walkingkooka-spreadsheet/Converter/storage-path-json-to-class storage-path-json-to-class, https://github.com/mP1/walkingkooka-spreadsheet/Converter/storage-path-properties-to-class storage-path-properties-to-class, https://github.com/mP1/walkingkooka-spreadsheet/Converter/storage-path-txt-to-class storage-path-txt-to-class, https://github.com/mP1/walkingkooka-spreadsheet/Converter/storage-value-info-list-to-text storage-value-info-list-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/style style, https://github.com/mP1/walkingkooka-spreadsheet/Converter/system system, https://github.com/mP1/walkingkooka-spreadsheet/Converter/template template, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-node text-node, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-boolean-list text-to-boolean-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-color text-to-color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-csv-string-list text-to-csv-string-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-date-list text-to-date-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-date-time-list text-to-date-time-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-email-address text-to-email-address, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-environment-value-name text-to-environment-value-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-error text-to-error, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-expression text-to-expression, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-flag text-to-flag, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-form-name text-to-form-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-has-host-address text-to-has-host-address, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-host-address text-to-host-address, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-json text-to-json, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-line-ending text-to-line-ending, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-locale text-to-locale, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-number-list text-to-number-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-object text-to-object, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-color-name text-to-spreadsheet-color-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-formatter-selector text-to-spreadsheet-formatter-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-id text-to-spreadsheet-id, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-metadata text-to-spreadsheet-metadata, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-metadata-color text-to-spreadsheet-metadata-color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-metadata-property-name text-to-spreadsheet-metadata-property-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-name text-to-spreadsheet-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-selection text-to-spreadsheet-selection, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-text text-to-spreadsheet-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-storage-path text-to-storage-path, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-string-list text-to-string-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-template-value-name text-to-template-value-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text text-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text-node text-to-text-node, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text-style text-to-text-style, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text-style-property-name text-to-text-style-property-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-time-list text-to-time-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-url text-to-url, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-url-fragment text-to-url-fragment, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-url-query-string text-to-url-query-string, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-validation-error text-to-validation-error, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-validator-selector text-to-validator-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-value-type text-to-value-type, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-zone-offset text-to-zone-offset, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-boolean to-boolean, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-json-node to-json-node, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-json-text to-json-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-number to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-string to-string, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-styleable to-styleable, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-validation-checkbox to-validation-checkbox, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-validation-choice to-validation-choice, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-validation-choice-list to-validation-choice-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-validation-error-list to-validation-error-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/url url, https://github.com/mP1/walkingkooka-spreadsheet/Converter/url-to-hyperlink url-to-hyperlink, https://github.com/mP1/walkingkooka-spreadsheet/Converter/url-to-image url-to-image] [] [https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/date date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/date-time date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/day-of-month day-of-month, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/day-of-week day-of-week, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/hour-of-am-pm hour-of-am-pm, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/hour-of-day hour-of-day, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/minute-of-hour minute-of-hour, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/month-of-year month-of-year, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/nano-of-second nano-of-second, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/number number, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/seconds-of-minute seconds-of-minute, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/text text, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/text-case-insensitive text-case-insensitive, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/time time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/year year] [https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/accounting accounting, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/automatic automatic, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/badge-error badge-error, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/collection collection, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/currency currency, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/date date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/date-time date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/default-text default-text, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/expression expression, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/full-date full-date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/full-date-time full-date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/full-time full-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/general general, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/hyperlinking hyperlinking, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/long-date long-date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/long-date-time long-date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/long-time long-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/medium-date medium-date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/medium-date-time medium-date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/medium-time medium-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/number number, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/percent percent, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/scientific scientific, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/short-date short-date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/short-date-time short-date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/short-time short-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/text text, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/time time] [] [https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetImporter/collection collection, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetImporter/empty empty, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetImporter/json json] [https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/date date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/date-time date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/general general, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/number number, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/time time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/whole-number whole-number] [https://github.com/mP1/walkingkooka-validation/Validator/absolute-url absolute-url, https://github.com/mP1/walkingkooka-validation/Validator/checkbox checkbox, https://github.com/mP1/walkingkooka-validation/Validator/choice-list choice-list, https://github.com/mP1/walkingkooka-validation/Validator/collection collection, https://github.com/mP1/walkingkooka-validation/Validator/email-address email-address, https://github.com/mP1/walkingkooka-validation/Validator/expression expression, https://github.com/mP1/walkingkooka-validation/Validator/non-null non-null, https://github.com/mP1/walkingkooka-validation/Validator/text-length text-length, https://github.com/mP1/walkingkooka-validation/Validator/text-mask text-mask]"
        );
    }

    // TreePrintable....................................................................................................

    @Test
    public void testPrintTree() {
        this.treePrintAndCheck(
            BasicSpreadsheetServerContext.with(
                SPREADSHEET_ENGINE,
                SPREADSHEET_ID_TO_SPREADSHEET_STORE_REPOSITORY,
                SPREADSHEET_PROVIDER,
                CURRENCY_LOCALE_CONTEXT,
                SPREADSHEET_ENVIRONMENT_CONTEXT,
                new TestSpreadsheetMetadataContext(),
                new TestHateosResourceHandlerContext(),
                new TestProviderContext(),
                new TestTerminalServerContext()
            ),
            "BasicSpreadsheetServerContext\n" +
                "  currencyLocaleContext\n" +
                "    JRE ReadOnly JRE en-AU (walkingkooka.currency.ReadOnlyCurrencyLocaleContext)\n" +
                "  spreadsheetEnvironmentContext\n" +
                "    BasicSpreadsheetEnvironmentContext\n" +
                "      environment\n" +
                "        EnvironmentContextSharedReadOnly\n" +
                "          environmentContext\n" +
                "            EnvironmentContextSharedMap\n" +
                "              currency\n" +
                "                AUD (java.util.Currency)\n" +
                "              indentation\n" +
                "                \"  \" (walkingkooka.text.Indentation)\n" +
                "              lineEnding\n" +
                "                \"\\n\"\n" +
                "              locale\n" +
                "                en_AU (java.util.Locale)\n" +
                "              now\n" +
                "                1999-12-31T12:58 (java.time.LocalDateTime)\n" +
                "              serverUrl\n" +
                "                https://example.com (walkingkooka.net.AbsoluteUrl)\n" +
                "              timeOffset\n" +
                "                Z (java.time.ZoneOffset)\n" +
                "              user\n" +
                "                user@example.com (walkingkooka.net.email.EmailAddress)\n" +
                "          readOnlyNames\n" +
                "            * (walkingkooka.predicate.AlwaysPredicate)\n" +
                "      storage\n" +
                "         (walkingkooka.storage.StorageSharedEmpty)\n" +
                "    spreadsheetMetadataContext\n" +
                "      TestSpreadsheetMetadataContext (walkingkooka.spreadsheet.server.BasicSpreadsheetServerContextTest$TestSpreadsheetMetadataContext)\n" +
                "    hateosResourceHandlerContext\n" +
                "      TestHateosResourceHandlerContext (walkingkooka.spreadsheet.server.BasicSpreadsheetServerContextTest$TestHateosResourceHandlerContext)\n" +
                "    spreadsheetProvider\n" +
                "      [https://github.com/mP1/walkingkooka-spreadsheet/Converter/basic basic, https://github.com/mP1/walkingkooka-spreadsheet/Converter/boolean boolean, https://github.com/mP1/walkingkooka-spreadsheet/Converter/boolean-to-text boolean-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/collection collection, https://github.com/mP1/walkingkooka-spreadsheet/Converter/collection-to collection-to, https://github.com/mP1/walkingkooka-spreadsheet/Converter/collection-to-list collection-to-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/color color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/color-to-color color-to-color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/color-to-number color-to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/date-time date-time, https://github.com/mP1/walkingkooka-spreadsheet/Converter/date-time-symbols date-time-symbols, https://github.com/mP1/walkingkooka-spreadsheet/Converter/decimal-number-symbols decimal-number-symbols, https://github.com/mP1/walkingkooka-spreadsheet/Converter/environment environment, https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-throwing error-throwing, https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-to-error error-to-error, https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-to-number error-to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/expression expression, https://github.com/mP1/walkingkooka-spreadsheet/Converter/form-and-validation form-and-validation, https://github.com/mP1/walkingkooka-spreadsheet/Converter/format-pattern-to-string format-pattern-to-string, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-formatter-selector has-formatter-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-host-address has-host-address, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-parser-selector has-parser-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-properties has-properties, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-spreadsheet-selection has-spreadsheet-selection, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-style has-style, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-text-node has-text-node, https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-validator-selector has-validator-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/json json, https://github.com/mP1/walkingkooka-spreadsheet/Converter/json-to json-to, https://github.com/mP1/walkingkooka-spreadsheet/Converter/locale locale, https://github.com/mP1/walkingkooka-spreadsheet/Converter/locale-to-text locale-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/net net, https://github.com/mP1/walkingkooka-spreadsheet/Converter/null-to-number null-to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/number number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/number-to-color number-to-color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/number-to-number number-to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/number-to-text number-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/optional-to optional-to, https://github.com/mP1/walkingkooka-spreadsheet/Converter/plugins plugins, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-cell-set spreadsheet-cell-set, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-metadata spreadsheet-metadata, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-selection-to-spreadsheet-selection spreadsheet-selection-to-spreadsheet-selection, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-selection-to-text spreadsheet-selection-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-value spreadsheet-value, https://github.com/mP1/walkingkooka-spreadsheet/Converter/storage storage, https://github.com/mP1/walkingkooka-spreadsheet/Converter/storage-path-json-to-class storage-path-json-to-class, https://github.com/mP1/walkingkooka-spreadsheet/Converter/storage-path-properties-to-class storage-path-properties-to-class, https://github.com/mP1/walkingkooka-spreadsheet/Converter/storage-path-txt-to-class storage-path-txt-to-class, https://github.com/mP1/walkingkooka-spreadsheet/Converter/storage-value-info-list-to-text storage-value-info-list-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/style style, https://github.com/mP1/walkingkooka-spreadsheet/Converter/system system, https://github.com/mP1/walkingkooka-spreadsheet/Converter/template template, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-node text-node, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-boolean-list text-to-boolean-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-color text-to-color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-csv-string-list text-to-csv-string-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-date-list text-to-date-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-date-time-list text-to-date-time-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-email-address text-to-email-address, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-environment-value-name text-to-environment-value-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-error text-to-error, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-expression text-to-expression, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-flag text-to-flag, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-form-name text-to-form-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-has-host-address text-to-has-host-address, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-host-address text-to-host-address, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-json text-to-json, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-line-ending text-to-line-ending, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-locale text-to-locale, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-number-list text-to-number-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-object text-to-object, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-color-name text-to-spreadsheet-color-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-formatter-selector text-to-spreadsheet-formatter-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-id text-to-spreadsheet-id, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-metadata text-to-spreadsheet-metadata, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-metadata-color text-to-spreadsheet-metadata-color, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-metadata-property-name text-to-spreadsheet-metadata-property-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-name text-to-spreadsheet-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-selection text-to-spreadsheet-selection, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-text text-to-spreadsheet-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-storage-path text-to-storage-path, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-string-list text-to-string-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-template-value-name text-to-template-value-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text text-to-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text-node text-to-text-node, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text-style text-to-text-style, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text-style-property-name text-to-text-style-property-name, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-time-list text-to-time-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-url text-to-url, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-url-fragment text-to-url-fragment, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-url-query-string text-to-url-query-string, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-validation-error text-to-validation-error, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-validator-selector text-to-validator-selector, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-value-type text-to-value-type, https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-zone-offset text-to-zone-offset, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-boolean to-boolean, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-json-node to-json-node, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-json-text to-json-text, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-number to-number, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-string to-string, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-styleable to-styleable, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-validation-checkbox to-validation-checkbox, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-validation-choice to-validation-choice, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-validation-choice-list to-validation-choice-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-validation-error-list to-validation-error-list, https://github.com/mP1/walkingkooka-spreadsheet/Converter/url url, https://github.com/mP1/walkingkooka-spreadsheet/Converter/url-to-hyperlink url-to-hyperlink, https://github.com/mP1/walkingkooka-spreadsheet/Converter/url-to-image url-to-image] [] [https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/date date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/date-time date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/day-of-month day-of-month, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/day-of-week day-of-week, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/hour-of-am-pm hour-of-am-pm, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/hour-of-day hour-of-day, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/minute-of-hour minute-of-hour, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/month-of-year month-of-year, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/nano-of-second nano-of-second, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/number number, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/seconds-of-minute seconds-of-minute, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/text text, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/text-case-insensitive text-case-insensitive, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/time time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/year year] [https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/accounting accounting, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/automatic automatic, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/badge-error badge-error, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/collection collection, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/currency currency, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/date date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/date-time date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/default-text default-text, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/expression expression, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/full-date full-date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/full-date-time full-date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/full-time full-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/general general, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/hyperlinking hyperlinking, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/long-date long-date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/long-date-time long-date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/long-time long-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/medium-date medium-date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/medium-date-time medium-date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/medium-time medium-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/number number, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/percent percent, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/scientific scientific, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/short-date short-date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/short-date-time short-date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/short-time short-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/text text, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/time time] [] [https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetImporter/collection collection, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetImporter/empty empty, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetImporter/json json] [https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/date date, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/date-time date-time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/general general, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/number number, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/time time, https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/whole-number whole-number] [https://github.com/mP1/walkingkooka-validation/Validator/absolute-url absolute-url, https://github.com/mP1/walkingkooka-validation/Validator/checkbox checkbox, https://github.com/mP1/walkingkooka-validation/Validator/choice-list choice-list, https://github.com/mP1/walkingkooka-validation/Validator/collection collection, https://github.com/mP1/walkingkooka-validation/Validator/email-address email-address, https://github.com/mP1/walkingkooka-validation/Validator/expression expression, https://github.com/mP1/walkingkooka-validation/Validator/non-null non-null, https://github.com/mP1/walkingkooka-validation/Validator/text-length text-length, https://github.com/mP1/walkingkooka-validation/Validator/text-mask text-mask] (walkingkooka.spreadsheet.provider.BasicSpreadsheetProvider)\n" +
                "    providerContext\n" +
                "      TestProviderContext (walkingkooka.spreadsheet.server.BasicSpreadsheetServerContextTest$TestProviderContext)\n" +
                "    terminalServerContext\n" +
                "      TestTerminalServerContext (walkingkooka.spreadsheet.server.BasicSpreadsheetServerContextTest$TestTerminalServerContext)\n"
        );
    }

    final static class TestSpreadsheetMetadataContext extends FakeSpreadsheetMetadataContext {
        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

    final static class TestHateosResourceHandlerContext extends FakeHateosResourceHandlerContext {

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

    final static class TestProviderContext extends FakeProviderContext {

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

    final static class TestTerminalServerContext extends FakeTerminalServerContext {

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

    // class............................................................................................................

    @Override
    public Class<BasicSpreadsheetServerContext> type() {
        return BasicSpreadsheetServerContext.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
