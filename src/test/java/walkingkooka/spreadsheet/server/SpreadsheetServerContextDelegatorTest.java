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

import walkingkooka.Binary;
import walkingkooka.currency.CurrencyCode;
import walkingkooka.currency.CurrencyLocaleContext;
import walkingkooka.currency.CurrencyLocaleContextDelegator;
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.locale.LocaleLanguageTag;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.ETag;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContextDelegator;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContextDelegator;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.provider.FakeSpreadsheetProvider;
import walkingkooka.spreadsheet.server.SpreadsheetServerContextDelegatorTest.TestSpreadsheetServerContextDelegator;
import walkingkooka.storage.StoragePath;
import walkingkooka.store.Store;
import walkingkooka.store.StoreWatcher;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class SpreadsheetServerContextDelegatorTest implements SpreadsheetServerContextTesting<TestSpreadsheetServerContextDelegator>,
    SpreadsheetMetadataTesting {

    private final static SpreadsheetContext SPREADSHEET_CONTEXT = SpreadsheetContexts.fake();

    @Override
    public void testTypeNaming() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestSpreadsheetServerContextDelegator createContext() {
        return new TestSpreadsheetServerContextDelegator();
    }

    // class............................................................................................................

    @Override
    public void testTestNaming() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<TestSpreadsheetServerContextDelegator> type() {
        return TestSpreadsheetServerContextDelegator.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    final static class TestSpreadsheetServerContextDelegator extends FakeSpreadsheetProvider implements SpreadsheetServerContext,
        SpreadsheetEnvironmentContextDelegator,
        JsonNodeMarshallUnmarshallContextDelegator,
        CurrencyLocaleContextDelegator,
        ProviderContextDelegator {

        // SpreadsheetServerContext.....................................................................................

        @Override
        public AbsoluteUrl serverUrl() {
            return SpreadsheetServerContextDelegatorTest.SERVER_URL;
        }

        @Override
        public SpreadsheetContext createEmptySpreadsheet(final Optional<Locale> locale) {
            Objects.requireNonNull(locale, "locale");

            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetContext createSpreadsheetContext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<SpreadsheetContext> spreadsheetContext(final SpreadsheetId id) {
            Objects.requireNonNull(id, "id");

            return Optional.ofNullable(
                id.equals(SpreadsheetServerContextDelegatorTest.SPREADSHEET_ID) ?
                    SPREADSHEET_CONTEXT :
                    null
            );
        }

        // SpreadsheetMetadataContext...................................................................................

        @Override
        public SpreadsheetMetadata createMetadata(final EmailAddress user,
                                                  final Optional<Locale> locale) {
            Objects.requireNonNull(user, "user");
            Objects.requireNonNull(locale, "locale");

            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<SpreadsheetMetadata> loadMetadata(final SpreadsheetId id) {
            Objects.requireNonNull(id, "id");

            throw new UnsupportedOperationException();
        }

        @Override
        public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
            Objects.requireNonNull(metadata, "metadata");

            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteMetadata(final SpreadsheetId id) {
            Objects.requireNonNull(id, "id");

            throw new UnsupportedOperationException();
        }

        @Override
        public List<SpreadsheetMetadata> findMetadataBySpreadsheetName(final String name,
                                                                       final int offset,
                                                                       final int count) {
            Objects.requireNonNull(name, "name");
            Store.checkOffsetAndCount(offset, count);

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

        // EnvironmentContextDelegator..................................................................................

        @Override
        public TestSpreadsheetServerContextDelegator cloneEnvironment() {
            return new TestSpreadsheetServerContextDelegator();
        }

        @Override
        public TestSpreadsheetServerContextDelegator setEnvironmentContext(final EnvironmentContext environmentContext) {
            Objects.requireNonNull(environmentContext, "environmentContext");

            return SPREADSHEET_ENVIRONMENT_CONTEXT.equals(environmentContext) ?
                this :
                new TestSpreadsheetServerContextDelegator();
        }

        @Override
        public <T> void setEnvironmentValue(final EnvironmentValueName<T> name,
                                            final T value) {
            this.spreadsheetEnvironmentContext.setEnvironmentValue(
                name,
                value
            );
        }

        @Override
        public void removeEnvironmentValue(final EnvironmentValueName<?> name) {
            this.spreadsheetEnvironmentContext.removeEnvironmentValue(name);
        }

        @Override
        public Currency currency() {
            return this.spreadsheetEnvironmentContext.currency();
        }

        @Override
        public void setCurrency(final Currency currency) {
            this.spreadsheetEnvironmentContext.setCurrency(currency);
        }
        
        @Override
        public Optional<StoragePath> currentWorkingDirectory() {
            return this.spreadsheetEnvironmentContext.currentWorkingDirectory();
        }
        
        @Override
        public void setCurrentWorkingDirectory(final Optional<StoragePath> path) {
            this.spreadsheetEnvironmentContext.setCurrentWorkingDirectory(path);
        }

        @Override
        public Optional<StoragePath> homeDirectory() {
            return this.spreadsheetEnvironmentContext.homeDirectory();
        }

        @Override
        public void setHomeDirectory(final Optional<StoragePath> path) {
            this.spreadsheetEnvironmentContext.setHomeDirectory(path);
        }

        @Override
        public LineEnding lineEnding() {
            return this.spreadsheetEnvironmentContext.lineEnding();
        }

        @Override
        public void setLineEnding(final LineEnding lineEnding) {
            this.spreadsheetEnvironmentContext.setLineEnding(lineEnding);
        }

        @Override
        public Locale locale() {
            return this.spreadsheetEnvironmentContext.locale();
        }

        @Override
        public void setLocale(final Locale locale) {
            this.spreadsheetEnvironmentContext.setLocale(locale);
        }

        @Override
        public void setUser(final Optional<EmailAddress> user) {
            this.spreadsheetEnvironmentContext.setUser(user);
        }

        @Override
        public EnvironmentContext environmentContext() {
            return this.spreadsheetEnvironmentContext();
        }

        @Override
        public SpreadsheetEnvironmentContext spreadsheetEnvironmentContext() {
            return this.spreadsheetEnvironmentContext;
        }

        private final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext = SPREADSHEET_ENVIRONMENT_CONTEXT.cloneEnvironment();

        // HateosHandlerContext.........................................................................................

        @Override
        public Optional<ETag> computeETag(final Binary binary) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Indentation indentation() {
            return this.spreadsheetEnvironmentContext.indentation();
        }

        // JsonNodeMarshallUnmarshallContextDelegator...................................................................

        @Override
        public SpreadsheetServerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
            return new TestSpreadsheetServerContextDelegator();
        }

        @Override
        public SpreadsheetServerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
            return new TestSpreadsheetServerContextDelegator();
        }

        @Override
        public JsonNodeMarshallUnmarshallContext jsonNodeMarshallUnmarshallContext() {
            return JSON_NODE_MARSHALL_UNMARSHALL_CONTEXT;
        }

        // CurrencyLocaleContextDelegator...............................................................................

        @Override
        public Optional<Currency> currencyForCurrencyCode(final CurrencyCode currencyCode) {
            return CURRENCY_LOCALE_CONTEXT.currencyForCurrencyCode(currencyCode);
        }

        @Override
        public Optional<Locale> localeForLanguageTag(final LocaleLanguageTag languageTag) {
            return CURRENCY_LOCALE_CONTEXT.localeForLanguageTag(languageTag);
        }

        @Override
        public CurrencyLocaleContext currencyLocaleContext() {
            return CURRENCY_LOCALE_CONTEXT;
        }

        // ProviderContextDelegator.....................................................................................

        @Override
        public ProviderContext providerContext() {
            return PROVIDER_CONTEXT;
        }

        // Object.......................................................................................................

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
        }
    }
}
