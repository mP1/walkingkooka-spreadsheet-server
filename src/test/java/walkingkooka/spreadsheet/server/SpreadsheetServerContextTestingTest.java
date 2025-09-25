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

import walkingkooka.net.email.EmailAddress;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.server.SpreadsheetServerContextTestingTest.TestSpreadsheetServerContext;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class SpreadsheetServerContextTestingTest implements SpreadsheetServerContextTesting<TestSpreadsheetServerContext> {

    static class TestSpreadsheetServerContext extends FakeSpreadsheetServerContext {

        @Override
        public SpreadsheetContext createSpreadsheetContext(final EmailAddress user,
                                                           final Optional<Locale> locale) {
            Objects.requireNonNull(user, "user");
            Objects.requireNonNull(locale, "locale");

            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<SpreadsheetContext> spreadsheetContext(final SpreadsheetId id) {
            Objects.requireNonNull(id, "id");

            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

    @Override
    public TestSpreadsheetServerContext createContext() {
        return new TestSpreadsheetServerContext();
    }

    @Override
    public Class<TestSpreadsheetServerContext> type() {
        return TestSpreadsheetServerContext.class;
    }
}
