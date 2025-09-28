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
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentContextDelegator;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.locale.LocaleContext;
import walkingkooka.locale.LocaleContextDelegator;
import walkingkooka.math.DecimalNumberContext;
import walkingkooka.math.DecimalNumberContextDelegator;
import walkingkooka.net.Url;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContextDelegator;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviderDelegator;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;

import java.math.MathContext;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicSpreadsheetEngineHateosResourceHandlerContextTest implements SpreadsheetEngineHateosResourceHandlerContextTesting<BasicSpreadsheetEngineHateosResourceHandlerContext>,
    SpreadsheetMetadataTesting,
    DecimalNumberContextDelegator {

    private final static SpreadsheetEngine SPREADSHEET_ENGINE = SpreadsheetEngines.fake();

    private final static HateosResourceHandlerContext HATEOS_RESOURCE_HANDLER_CONTEXT = HateosResourceHandlerContexts.basic(
        INDENTATION,
        EOL,
        JSON_NODE_MARSHALL_UNMARSHALL_CONTEXT
    );

    private final static SpreadsheetEngineContext SPREADSHEET_ENGINE_CONTEXT = SpreadsheetEngineContexts.basic(
        Url.parseAbsolute("https://example.com"),
        METADATA_EN_AU,
        SpreadsheetMetadataPropertyName.FORMULA_FUNCTIONS,
        new TestSpreadsheetContext(),
        TERMINAL_CONTEXT
    );

    final static class TestSpreadsheetContext implements SpreadsheetContext,
        EnvironmentContextDelegator,
        LocaleContextDelegator,
        SpreadsheetProviderDelegator,
        SpreadsheetMetadataContextDelegator {

        @Override
        public SpreadsheetId spreadsheetId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetContext setUser(final Optional<EmailAddress> user) {
            Objects.requireNonNull(user, "user");
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetContext cloneEnvironment() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> SpreadsheetContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                          final T value) {
            this.environmentContext.setEnvironmentValue(
                name,
                value
            );
            return this;
        }

        @Override
        public SpreadsheetContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
            this.environmentContext.removeEnvironmentValue(name);
            return this;
        }

        @Override
        public EnvironmentContext environmentContext() {
            return this.environmentContext;
        }

        private final EnvironmentContext environmentContext = EnvironmentContexts.map(ENVIRONMENT_CONTEXT);

        @Override
        public Locale locale() {
            return this.environmentContext.locale();
        }

        @Override
        public SpreadsheetContext setLocale(final Locale locale) {
            this.environmentContext.setLocale(locale);
            return this;
        }

        @Override
        public LocaleContext localeContext() {
            return LOCALE_CONTEXT;
        }

        @Override
        public SpreadsheetMetadata createMetadata(final EmailAddress user,
                                                  final Optional<Locale> locale) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetMetadataContext spreadsheetMetadataContext() {
            return SpreadsheetMetadataContexts.fake();
        }

        @Override
        public SpreadsheetProvider spreadsheetProvider() {
            return SPREADSHEET_PROVIDER;
        }

        @Override
        public SpreadsheetStoreRepository storeRepository() {
            return this.storeRepository;
        }

        private final SpreadsheetStoreRepository storeRepository = new FakeSpreadsheetStoreRepository() {
            @Override
            public SpreadsheetLabelStore labels() {
                return SpreadsheetLabelStores.fake();
            }
        };

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
            () -> BasicSpreadsheetEngineHateosResourceHandlerContext.with(
                null,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                SPREADSHEET_ENGINE_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullHateosResourceHandlerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetEngineHateosResourceHandlerContext.with(
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
            () -> BasicSpreadsheetEngineHateosResourceHandlerContext.with(
                SPREADSHEET_ENGINE,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                null
            )
        );
    }

    // SpreadsheetEngineHateosResourceHandlerContextTesting.............................................................

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
    public BasicSpreadsheetEngineHateosResourceHandlerContext createContext() {
        return BasicSpreadsheetEngineHateosResourceHandlerContext.with(
            SPREADSHEET_ENGINE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ENGINE_CONTEXT
        );
    }

    @Override
    public MathContext mathContext() {
        return HATEOS_RESOURCE_HANDLER_CONTEXT.mathContext();
    }

    // DecimalNumberContext.............................................................................................

    @Override
    public DecimalNumberContext decimalNumberContext() {
        return SPREADSHEET_FORMATTER_CONTEXT;
    }

    // class............................................................................................................

    @Override
    public Class<BasicSpreadsheetEngineHateosResourceHandlerContext> type() {
        return BasicSpreadsheetEngineHateosResourceHandlerContext.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
