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

import walkingkooka.net.UrlPath;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosHttpHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Objects;

/**
 * A handler that accepts a request with a possible {@link walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector} and returns a {@link SpreadsheetFormatterSelectorEdit}
 */
final class SpreadsheetFormatterSelectorEditHateosHttpHandler implements HateosHttpHandler<SpreadsheetEngineHateosResourceHandlerContext> {

    static {
        try {
            SpreadsheetFormatterSelectorEdit.parse(
                null,
                null
            ); // force json registry
        } catch (final NullPointerException ignore) {
            // dont care just want to force json registry
        }
    }

    /**
     * Singleton
     */
    final static SpreadsheetFormatterSelectorEditHateosHttpHandler INSTANCE = new SpreadsheetFormatterSelectorEditHateosHttpHandler();

    private SpreadsheetFormatterSelectorEditHateosHttpHandler() {
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

        // /api/spreadsheet/1/formatter/*/edit/SpreadsheetFormatterSelector
        // 01   2           3 4         5 6
        final UrlPath selector = request.url()
            .path()
            .pathAfter(6);

        final SpreadsheetFormatterSelectorEdit edit = SpreadsheetFormatterSelectorEdit.parse(
            selector.isRoot() ?
                "" :
                selector.value()
                    .substring(1),
            SpreadsheetFormatterSelectorEditContexts.basic(
                context.spreadsheetMetadata()
                    .spreadsheetFormatterContext(
                        SpreadsheetMetadata.NO_CELL,
                        context::spreadsheetExpressionEvaluationContext,
                        SpreadsheetLabelNameResolvers.fake(),
                        context, // ConverterProvider
                        context, // // SpreadsheetFormatterProvider
                        context, // LocaleContext
                        context // ProviderContext
                    ),
                context, // SpreadsheetLabelNameResolver
                context // ProviderContext
            )
        );

        response.setStatus(HttpStatusCode.OK.status());

        // write TextNodes as JSON response
        response.setEntity(
            HttpEntity.EMPTY.setContentType(
                requiredContentType.setCharset(CharsetName.UTF_8)
            ).addHeader(
                HateosResourceMappings.X_CONTENT_TYPE_NAME,
                edit.getClass().getSimpleName()
            ).setBodyText(
                context.marshall(edit)
                    .toString()
            ).setContentLength()
        );
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
