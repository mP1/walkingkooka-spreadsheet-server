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
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.engine.SpreadsheetMetadataMode;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceLoaders;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.value.SpreadsheetCell;
import walkingkooka.text.CharSequences;

import java.util.Objects;
import java.util.Optional;

/**
 * A handler that accepts a request with a possible {@link SpreadsheetFormatterSelector} and returns a {@link SpreadsheetFormatterSelectorEdit}
 */
abstract class SpreadsheetFormatterSelectorEditHateosHttpHandler implements HateosHttpHandler<SpreadsheetEngineHateosResourceHandlerContext> {

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

    SpreadsheetFormatterSelectorEditHateosHttpHandler() {
        super();
    }

    @Override
    public final void handle(final HttpRequest request,
                             final HttpResponse response,
                             final SpreadsheetEngineHateosResourceHandlerContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(context, "context");


        final MediaType requiredContentType = context.contentType();

        HttpHeaderName.ACCEPT.headerOrFail(request)
            .testOrFail(requiredContentType);

        final SpreadsheetFormatterSelectorEdit edit = this.extractSelectorAndProduceEdit(
            request.url()
                .path(),
            context
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
                context.toJsonText(
                    context.marshall(edit)
                )
            ).setContentLength()
        );
    }

    abstract SpreadsheetFormatterSelectorEdit extractSelectorAndProduceEdit(final UrlPath path,
                                                                            final SpreadsheetEngineHateosResourceHandlerContext context);

    final SpreadsheetFormatterSelectorEdit produceEdit(final String formatterSelector,
                                                       final Optional<SpreadsheetCell> cell,
                                                       final SpreadsheetEngineHateosResourceHandlerContext context) {
        final ProviderContext providerContext = context.providerContext();

        return SpreadsheetFormatterSelectorEdit.parse(
            null != formatterSelector && formatterSelector.startsWith(UrlPath.SEPARATOR.string()) ?
                formatterSelector.substring(
                    UrlPath.SEPARATOR.string()
                        .length()
                ) :
                CharSequences.nullToEmpty(formatterSelector)
                    .toString(),
            SpreadsheetFormatterSelectorEditContexts.basic(
                context.spreadsheetMetadata()
                    .spreadsheetFormatterContext(
                        cell,
                        (final Optional<Object> v) -> context.setSpreadsheetMetadataMode(SpreadsheetMetadataMode.FORMATTING)
                            .spreadsheetExpressionEvaluationContext(
                                cell,
                                SpreadsheetExpressionReferenceLoaders.fake()
                            ).addLocalVariable(
                                SpreadsheetExpressionEvaluationContext.FORMAT_VALUE,
                                v
                            ),
                        context.indentation(),
                        SpreadsheetLabelNameResolvers.empty(),
                        context.lineEnding(),
                        context, // LocaleContext
                        context, // // SpreadsheetProvider
                        providerContext // ProviderContext
                    ),
                context, // SpreadsheetLabelNameResolver
                providerContext // ProviderContext
            )
        );
    }

    @Override
    public final String toString() {
        return this.getClass().getSimpleName();
    }
}
