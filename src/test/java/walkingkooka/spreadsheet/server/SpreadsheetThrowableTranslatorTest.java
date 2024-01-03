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
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.store.LoadStoreException;
import walkingkooka.text.CharSequences;
import walkingkooka.util.FunctionTesting;

public final class SpreadsheetThrowableTranslatorTest implements FunctionTesting<SpreadsheetThrowableTranslator, Throwable, HttpStatus>,
        ClassTesting<SpreadsheetThrowableTranslator> {

    @Test
    public void testLoadStoreExceptionFileNotFound() {
        final String message = "Load failed 12345";

        this.applyAndCheck(
                new LoadStoreException(message),
                HttpStatusCode.NOT_FOUND.setMessage(message)
        );
    }

    @Test
    public void testLoadStoreExceptionFileNotFoundMultiline() {
        final String message = "Load failed 12345";

        this.applyAndCheck(
                new LoadStoreException(message + "\n678"),
                HttpStatusCode.NOT_FOUND.setMessage(message)
        );
    }

    @Test
    public void testRuntimeException() {
        final String message = "Expected boolean result but got " + CharSequences.quoteIfChars("=1");

        this.applyAndCheck(
                new RuntimeException(message),
                HttpStatusCode.INTERNAL_SERVER_ERROR.setMessage(message)
        );
    }

    public void testTypeNaming() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetThrowableTranslator createFunction() {
        return SpreadsheetThrowableTranslator.INSTANCE;
    }

    @Override
    public Class<SpreadsheetThrowableTranslator> type() {
        return SpreadsheetThrowableTranslator.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
