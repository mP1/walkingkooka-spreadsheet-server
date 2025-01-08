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

import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpHandlers;
import walkingkooka.store.MissingStoreException;

import java.util.function.Function;

/**
 * Adds a few more Throwable type check and translate.
 */
final class SpreadsheetThrowableTranslator implements Function<Throwable, HttpStatus> {

    /**
     * Singleton
     */
    final static SpreadsheetThrowableTranslator INSTANCE = new SpreadsheetThrowableTranslator();

    /**
     * Private ctor use instance.
     */
    private SpreadsheetThrowableTranslator() {
    }

    @Override
    public HttpStatus apply(final Throwable throwable) {
        return throwable instanceof MissingStoreException ?
            HttpStatusCode.NOT_FOUND.setMessage(
                HttpStatus.firstLineOfText(throwable.getMessage())
            ) :
            HttpHandlers.throwableTranslator()
                .apply(throwable);
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }
}
