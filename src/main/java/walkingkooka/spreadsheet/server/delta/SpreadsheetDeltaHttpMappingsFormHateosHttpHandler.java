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

package walkingkooka.spreadsheet.server.delta;

import walkingkooka.collect.set.Sets;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosHttpHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.validation.form.SpreadsheetForms;
import walkingkooka.validation.form.Form;
import walkingkooka.validation.form.FormName;

import java.util.Objects;

/**
 * Glue that handles dispatching to {@link walkingkooka.net.http.server.hateos.HateosResourceHandler} for forms.
 */
final class SpreadsheetDeltaHttpMappingsFormHateosHttpHandler implements HateosHttpHandler<SpreadsheetEngineHateosResourceHandlerContext> {

    final static SpreadsheetDeltaHttpMappingsFormHateosHttpHandler INSTANCE = new SpreadsheetDeltaHttpMappingsFormHateosHttpHandler();

    private SpreadsheetDeltaHttpMappingsFormHateosHttpHandler() {
    }

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response,
                       final SpreadsheetEngineHateosResourceHandlerContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(context, "context");

        // extract cell from path
        final UrlPath path = request.url()
            .path();

        int i = 0;
        UrlPath formPath = UrlPath.EMPTY;

        ForLoop:
        //
        for (final UrlPathName name : path) {
            formPath = formPath.append(name);

            switch (i) {
                // /api/spreadsheet/SpreadsheetId/cell/SpreadsheetExpressionReference/form
                case 5:
                    final SpreadsheetExpressionReference reference = SpreadsheetSelection.parseExpressionReference(name.value());

                    final HateosResourceMappings<FormName,
                        SpreadsheetDelta,
                        SpreadsheetDelta,
                        Form<SpreadsheetExpressionReference>,
                        SpreadsheetEngineHateosResourceHandlerContext> mappings = HateosResourceMappings.with(
                        FormName.HATEOS_RESOURCE_NAME,
                        SpreadsheetDeltaHttpMappings::parseForm,
                        SpreadsheetDelta.class,
                        SpreadsheetDelta.class,
                        SpreadsheetForms.FORM_CLASS,
                        SpreadsheetEngineHateosResourceHandlerContext.class
                    ).setHateosResourceHandler(
                        LinkRelation.SELF,
                        HttpMethod.GET,
                        SpreadsheetDeltaHateosResourceHandlerPrepareForm.with(
                            reference
                        )
                    ).setHateosResourceHandler(
                        LinkRelation.SELF,
                        HttpMethod.POST,
                        SpreadsheetDeltaHateosResourceHandlerSubmitForm.with(
                            reference
                        )
                    );

                    HateosResourceMappings.router(
                        formPath,
                        Sets.of(
                            mappings
                        ),
                        context
                    ).route(
                        request.routerParameters()
                    ).ifPresent(
                        (HttpHandler handler) -> handler.handle(
                            request,
                            response
                        )
                    );
                    break ForLoop;
            }

            i++;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
