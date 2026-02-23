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

package walkingkooka.spreadsheet.server.formatter;

import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosHttpHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviderSamplesContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Objects;
import java.util.Optional;

/**
 * A handler that returns all available {@link SpreadsheetFormatterMenu}
 */
final class SpreadsheetFormatterMenuHateosHttpHandler implements HateosHttpHandler<SpreadsheetEngineHateosResourceHandlerContext> {

    /**
     * Singleton
     */
    final static SpreadsheetFormatterMenuHateosHttpHandler INSTANCE = new SpreadsheetFormatterMenuHateosHttpHandler();

    private SpreadsheetFormatterMenuHateosHttpHandler() {
    }

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response,
                       final SpreadsheetEngineHateosResourceHandlerContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(context, "context");

        final MediaType requiredContentType = context.contentType();
        HttpHeaderName.ACCEPT.headerOrFail(request)
            .testOrFail(requiredContentType);

        final ProviderContext providerContext = context.providerContext();

        final SpreadsheetFormatterMenuList menuList = SpreadsheetFormatterMenu.prepare(
            SpreadsheetFormatterMenuContexts.basic(
                context, // SpreadsheetFormatProvider
                SpreadsheetFormatterProviderSamplesContexts.basic(
                    context.spreadsheetMetadata()
                        .spreadsheetFormatterContext(
                            SpreadsheetMetadata.NO_CELL,
                            (final Optional<Object> v) -> {
                                throw new UnsupportedOperationException(); // SpreadsheetExpressionEvaluationContext not required
                            },
                            context, // HasUserDirectories
                            context.indentation(),
                            context, // SpreadsheetLabelNameResolver
                            context.lineEnding(),
                            context, // CurrencyLocaleContext
                            context, // SpreadsheetProvider
                            providerContext // ProviderContext
                        ), // SpreadsheetFormatterContext
                    providerContext // ProviderContext
                )
            )
        );

        // write TextNodes as JSON response
        response.setStatus(HttpStatusCode.OK.status());
        response.setEntity(
            HttpEntity.EMPTY.setContentType(
                requiredContentType.setCharset(CharsetName.UTF_8)
            ).addHeader(
                HateosResourceMappings.X_CONTENT_TYPE_NAME,
                menuList.getClass().getSimpleName()
            ).setBodyText(
                context.toJsonText(
                    context.marshall(menuList)
                )
            ).setContentLength()
        );
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
