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
import walkingkooka.Binary;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.BinaryNumberConverterFunction;
import walkingkooka.currency.CurrencyContext;
import walkingkooka.currency.CurrencyContextDelegator;
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentContextDelegator;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.locale.LocaleContext;
import walkingkooka.locale.LocaleContextDelegator;
import walkingkooka.math.DecimalNumberContext;
import walkingkooka.math.DecimalNumberContextDelegator;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlerContext;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.convert.SpreadsheetConverterContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.engine.SpreadsheetMetadataMode;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContextDelegator;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviderDelegator;
import walkingkooka.spreadsheet.provider.SpreadsheetProviders;
import walkingkooka.spreadsheet.storage.SpreadsheetStorageContext;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.storage.Storage;
import walkingkooka.storage.StoragePath;
import walkingkooka.storage.Storages;
import walkingkooka.store.StoreWatcher;
import walkingkooka.tree.expression.function.ExpressionFunctions;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProviders;

import java.math.MathContext;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicSpreadsheetEngineHateosHandlerContextTest implements SpreadsheetEngineHateosHandlerContextTesting<BasicSpreadsheetEngineHateosHandlerContext>,
    SpreadsheetMetadataTesting,
    DecimalNumberContextDelegator {

    private final static SpreadsheetId SPREADSHEET_ID = SpreadsheetId.with(1);

    private final static SpreadsheetEngine SPREADSHEET_ENGINE = SpreadsheetEngines.fake();

    private final static SpreadsheetEngineContext SPREADSHEET_ENGINE_CONTEXT = SpreadsheetEngineContexts.fake();

    final static class TestSpreadsheetContext implements SpreadsheetContext,
        EnvironmentContextDelegator,
        CurrencyContextDelegator,
        LocaleContextDelegator,
        SpreadsheetProviderDelegator,
        SpreadsheetMetadataContextDelegator {

        @Override
        public AbsoluteUrl serverUrl() {
            return Url.parseAbsolute("https://example.com");
        }

        @Override
        public Optional<SpreadsheetId> spreadsheetId() {
            return Optional.of(
                BasicSpreadsheetEngineHateosHandlerContextTest.SPREADSHEET_ID
            );
        }

        @Override
        public void setSpreadsheetId(final Optional<SpreadsheetId> spreadsheetId) {
            Objects.requireNonNull(spreadsheetId, "spreadsheetId");

            if (false == this.spreadsheetId().equals(spreadsheetId)) {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public Charset charset() {
            return BasicSpreadsheetEngineHateosHandlerContextTest.CHARSET;
        }

        @Override
        public MediaType detect(final String filename,
                                final Binary content) {
            return MEDIA_TYPE_DETECTOR.detect(
                filename,
                content
            );
        }

        @Override
        public SpreadsheetEngineContext spreadsheetEngineContext() {
            return SpreadsheetEngineContexts.spreadsheetContext(
                SpreadsheetMetadataMode.FORMULA,
                this,
                TERMINAL_CONTEXT
            );
        }

        @Override
        public Router<HttpRequestAttribute<?>, HttpHandler<HttpHandlerContext>> httpRouter() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BinaryNumberConverterFunction<SpreadsheetConverterContext> multiplier() {
            return MULTIPLIER;
        }

        @Override
        public SpreadsheetContext cloneEnvironment() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetContext setEnvironmentContext(final EnvironmentContext environmentContext) {
            Objects.requireNonNull(environmentContext, "environmentContext");

            return new TestSpreadsheetContext();
        }

        @Override
        public EnvironmentContext environmentContext() {
            return this.environmentContext;
        }

        {
            this.environmentContext = EnvironmentContexts.map(
                SPREADSHEET_ENVIRONMENT_CONTEXT.cloneEnvironment()
            );
            this.environmentContext.setEnvironmentValue(
                SPREADSHEET_ID,
                BasicSpreadsheetEngineHateosHandlerContextTest.SPREADSHEET_ID
            );
        }

        private final EnvironmentContext environmentContext;

        @Override
        public Currency currency() {
            return this.environmentContext.currency();
        }

        @Override
        public void setCurrency(final Currency currency) {
            this.environmentContext.setCurrency(currency);
        }

        @Override
        public Optional<StoragePath> currentWorkingDirectory() {
            return this.environmentContext.environmentValue(SpreadsheetEnvironmentContext.CURRENT_WORKING_DIRECTORY);
        }

        @Override
        public void setCurrentWorkingDirectory(final Optional<StoragePath> currentWorkingDirectory) {
            this.environmentContext.setOrRemoveEnvironmentValue(
                SpreadsheetEnvironmentContext.CURRENT_WORKING_DIRECTORY,
                currentWorkingDirectory
            );
        }

        @Override
        public Optional<StoragePath> homeDirectory() {
            return this.environmentContext.environmentValue(SpreadsheetEnvironmentContext.HOME_DIRECTORY);
        }

        @Override
        public void setHomeDirectory(final Optional<StoragePath> homeDirectory) {
            this.environmentContext.setOrRemoveEnvironmentValue(
                SpreadsheetEnvironmentContext.HOME_DIRECTORY,
                homeDirectory
            );
        }

        @Override
        public Locale locale() {
            return this.environmentContext.locale();
        }

        @Override
        public void setLocale(final Locale locale) {
            this.environmentContext.setLocale(locale);
        }

        @Override
        public Storage<SpreadsheetStorageContext> storage() {
            return Storages.empty();
        }

        @Override
        public CurrencyContext currencyContext() {
            return CURRENCY_CONTEXT;
        }

        @Override
        public LocaleContext localeContext() {
            return LOCALE_CONTEXT;
        }

        @Override
        public SpreadsheetMetadata spreadsheetMetadata() {
            return METADATA_EN_AU.setOrRemove(
                SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                this.spreadsheetId()
                    .orElse(null)
            );
        }

        @Override
        public SpreadsheetMetadata createMetadata(final EmailAddress user,
                                                  final Optional<Locale> locale) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Runnable addMetadataWatcher(final StoreWatcher<SpreadsheetMetadata> watcher) {
            Objects.requireNonNull(watcher, "watcher");
            throw new UnsupportedOperationException();
        }

        @Override
        public Runnable addMetadataWatcherOnce(final StoreWatcher<SpreadsheetMetadata> watcher) {
            Objects.requireNonNull(watcher, "watcher");
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetMetadataContext spreadsheetMetadataContext() {
            return SpreadsheetMetadataContexts.fake();
        }

        @Override
        public SpreadsheetProvider spreadsheetProvider() {
            return SpreadsheetProviders.basic(
                CONVERTER_PROVIDER,
                ExpressionFunctionProviders.basic(
                    Url.parseAbsolute("https://example.com/functions"),
                    SpreadsheetExpressionFunctions.NAME_CASE_SENSITIVITY,
                    Sets.of(
                        ExpressionFunctions.typeName()
                    )
                ),
                SPREADSHEET_COMPARATOR_PROVIDER,
                SPREADSHEET_EXPORTER_PROVIDER,
                SPREADSHEET_FORMATTER_PROVIDER,
                FORM_HANDLER_PROVIDER,
                SPREADSHEET_IMPORTER_PROVIDER,
                SPREADSHEET_PARSER_PROVIDER,
                VALIDATOR_PROVIDER
            );
        }

        @Override
        public SpreadsheetStoreRepository storeRepository() {
            return this.storeRepository;
        }

        private final SpreadsheetStoreRepository storeRepository = SpreadsheetStoreRepositories.treeMap(
            SpreadsheetMetadataStores.treeMap()
        );

        @Override
        public SpreadsheetEngine spreadsheetEngine() {
            return SpreadsheetEngines.basic();
        }

        @Override
        public ProviderContext providerContext() {
            return PROVIDER_CONTEXT;
        }
    }

    // with.............................................................................................................

    @Test
    public void testWithNullSpreadsheetEngineFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetEngineHateosHandlerContext.with(
                null,
                HATEOS_HANDLER_CONTEXT,
                SPREADSHEET_ENGINE_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullHateosHandlerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetEngineHateosHandlerContext.with(
                SPREADSHEET_ENGINE,
                null,
                SPREADSHEET_ENGINE_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetEngineContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetEngineHateosHandlerContext.with(
                SPREADSHEET_ENGINE,
                HATEOS_HANDLER_CONTEXT,
                null
            )
        );
    }

    // SpreadsheetMetadataHateosHandlerContextTesting.............................................................

    @Override
    public void testCreateMetadataWithNullUserFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testCreateMetadataWithNullLocaleFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testDeleteMetadataWithNullFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testFindMetadataBySpreadsheetNameWithNullNameFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testFindMetadataBySpreadsheetNameWithNegativeOffsetFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testFindMetadataBySpreadsheetNameWithNegativeCountFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testLoadMetadataWithNullIdFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testResolveLabelWithNullFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSaveMetadataWithNullFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testCheckToStringOverridden() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BasicSpreadsheetEngineHateosHandlerContext createContext() {
        return BasicSpreadsheetEngineHateosHandlerContext.with(
            SPREADSHEET_ENGINE,
            HATEOS_HANDLER_CONTEXT,
            new TestSpreadsheetContext()
                .spreadsheetEngineContext()
        );
    }

    @Override
    public MathContext mathContext() {
        return HATEOS_HANDLER_CONTEXT.mathContext();
    }

    // DecimalNumberContext.............................................................................................

    @Override
    public DecimalNumberContext decimalNumberContext() {
        return SPREADSHEET_FORMATTER_CONTEXT;
    }

    // class............................................................................................................

    @Override
    public Class<BasicSpreadsheetEngineHateosHandlerContext> type() {
        return BasicSpreadsheetEngineHateosHandlerContext.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
