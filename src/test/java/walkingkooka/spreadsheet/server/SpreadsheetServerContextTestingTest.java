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
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.locale.LocaleContext;
import walkingkooka.locale.LocaleContextDelegator;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.MediaType;
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
import walkingkooka.spreadsheet.server.SpreadsheetServerContextTestingTest.TestSpreadsheetServerContext;
import walkingkooka.store.Store;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class SpreadsheetServerContextTestingTest implements SpreadsheetServerContextTesting<TestSpreadsheetServerContext>,
    SpreadsheetMetadataTesting {

    private final static SpreadsheetId SPREADSHEET_ID = SpreadsheetId.with(1);

    private final static SpreadsheetContext SPREADSHEET_CONTEXT = SpreadsheetContexts.fake();

    // spreadsheetContextAndCheck.......................................................................................

    @Test
    public void testSpreadsheetContextAndCheckMissing() {
        this.spreadsheetContextAndCheck(
            new TestSpreadsheetServerContext(),
            SpreadsheetId.with(404)
        );
    }

    @Test
    public void testSpreadsheetContextAndCheck() {
        this.spreadsheetContextAndCheck(
            new TestSpreadsheetServerContext(),
            SPREADSHEET_ID,
            SPREADSHEET_CONTEXT
        );
    }

    @Override
    public void testRemoveEnvironmentValueWithNowFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetEnvironmentValueWithNowFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetCurrencyWithDifferentAndWatcher() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetIndentationWithDifferentAndWatcher() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetLineEndingWithDifferentAndWatcher() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetLocaleWithDifferent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetLocaleWithDifferentAndWatcher() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetSpreadsheetIdWithSame() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetTimeOffsetWithDifferentAndWatcher() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetUserWithDifferentAndWatcher() {
        throw new UnsupportedOperationException();
    }

    final static class TestSpreadsheetServerContext extends FakeSpreadsheetProvider implements SpreadsheetServerContext,
        SpreadsheetEnvironmentContextDelegator,
        JsonNodeMarshallUnmarshallContextDelegator,
        LocaleContextDelegator,
        ProviderContextDelegator {

        // SpreadsheetServerContext.....................................................................................

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
                id.equals(SpreadsheetServerContextTestingTest.SPREADSHEET_ID) ?
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

        // EnvironmentContextDelegator..................................................................................

        @Override
        public TestSpreadsheetServerContext cloneEnvironment() {
            return new TestSpreadsheetServerContext();
        }

        @Override
        public TestSpreadsheetServerContext setEnvironmentContext(final EnvironmentContext environmentContext) {
            Objects.requireNonNull(environmentContext, "environmentContext");

            return SPREADSHEET_ENVIRONMENT_CONTEXT.equals(environmentContext) ?
                this :
                new TestSpreadsheetServerContext();
        }

        @Override
        public <T> void setEnvironmentValue(final EnvironmentValueName<T> name,
                                            final T value) {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(value, "value");

            throw new UnsupportedOperationException();
        }

        @Override
        public void removeEnvironmentValue(final EnvironmentValueName<?> name) {
            Objects.requireNonNull(name, "name");

            throw new UnsupportedOperationException();
        }

        @Override
        public LineEnding lineEnding() {
            return SpreadsheetServerContextTestingTest.LINE_ENDING;
        }

        @Override
        public void setLineEnding(final LineEnding lineEnding) {
            Objects.requireNonNull(lineEnding, "lineEnding");
        }

        @Override
        public Locale locale() {
            return SpreadsheetServerContextTestingTest.LOCALE;
        }

        @Override
        public void setLocale(final Locale locale) {
            Objects.requireNonNull(locale, "locale");
        }

        @Override
        public AbsoluteUrl serverUrl() {
            return SpreadsheetServerContextTestingTest.SERVER_URL;
        }

        @Override
        public void setUser(final Optional<EmailAddress> user) {
            Objects.requireNonNull(user, "user");
        }

        @Override
        public EnvironmentContext environmentContext() {
            return this.spreadsheetEnvironmentContext();
        }

        @Override
        public SpreadsheetEnvironmentContext spreadsheetEnvironmentContext() {
            return SPREADSHEET_ENVIRONMENT_CONTEXT.cloneEnvironment();
        }

        // HateosResourceHandlerContext.................................................................................

        @Override
        public MediaType contentType() {
            return MediaType.APPLICATION_JSON;
        }

        @Override
        public Indentation indentation() {
            return Indentation.SPACES2;
        }

        // JsonNodeMarshallUnmarshallContextDelegator...................................................................

        @Override
        public SpreadsheetServerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
            return new TestSpreadsheetServerContext();
        }

        @Override
        public SpreadsheetServerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
            return new TestSpreadsheetServerContext();
        }

        @Override
        public JsonNodeMarshallUnmarshallContext jsonNodeMarshallUnmarshallContext() {
            return JSON_NODE_MARSHALL_UNMARSHALL_CONTEXT;
        }

        // LocaleContextDelegator.......................................................................................

        @Override
        public LocaleContext localeContext() {
            return LOCALE_CONTEXT;
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

    @Override
    public TestSpreadsheetServerContext createContext() {
        return new TestSpreadsheetServerContext();
    }

    // class............................................................................................................

    @Override
    public void testTestNaming() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<TestSpreadsheetServerContext> type() {
        return TestSpreadsheetServerContext.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
