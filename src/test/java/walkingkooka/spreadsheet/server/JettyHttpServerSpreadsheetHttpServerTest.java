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
import walkingkooka.net.email.EmailAddress;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.PublicStaticHelperTesting;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

public final class JettyHttpServerSpreadsheetHttpServerTest implements PublicStaticHelperTesting<JettyHttpServerSpreadsheetHttpServer> {

    @Test
    public void testPrepareInitialMetadataWithoutUserLocale() {
        this.prepareInitialMetadataAndConverter(null);
    }

    @Test
    public void testPrepareInitialMetadataWithUserLocale() {
        this.prepareInitialMetadataAndConverter(Locale.FRENCH);
    }

    private void prepareInitialMetadataAndConverter(final Locale userLocale) {
        final SpreadsheetMetadata metadata = JettyHttpServerSpreadsheetHttpServer.prepareInitialMetadata(
                EmailAddress.parse("user@example.com"),
                LocalDateTime.now(),
                Optional.ofNullable(userLocale),
                Locale.ENGLISH
        );
        metadata.converter();
    }

    // Class............................................................................................................

    @Override
    public Class<JettyHttpServerSpreadsheetHttpServer> type() {
        return JettyHttpServerSpreadsheetHttpServer.class;
    }

    @Override
    public final JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }

    @Override
    public boolean canHavePublicTypes(final Method method) {
        return false;
    }
}
