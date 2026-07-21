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

package walkingkooka.spreadsheet.server.storage;

import walkingkooka.ToStringBuilder;
import walkingkooka.ToStringBuilderOption;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributes;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosHandlerContext;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link Router} that matches the {@link SpreadsheetStorageGetHeadPostOrDeleteHttpHandler} if the path matches the given base.
 */
public final class SpreadsheetServerStorageRouter implements Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetEngineHateosHandlerContext>> {

    public static SpreadsheetServerStorageRouter with(final UrlPath base) {
        Objects.requireNonNull(base, "base");

        return new SpreadsheetServerStorageRouter(base);
    }

    private SpreadsheetServerStorageRouter(final UrlPath base) {
        super();
        this.base = base.normalize();
    }

    // Router...........................................................................................................

    @Override
    public Optional<HttpHandler<SpreadsheetEngineHateosHandlerContext>> route(final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(parameters, "parameters");

        // /api/spreadsheet/1/storage
        return Optional.ofNullable(
            -1 == this.consumeBasePath(parameters) ?
                null :
                SpreadsheetStorageGetHeadPostOrDeleteHttpHandler.INSTANCE
        );
    }

    int consumeBasePath(final Map<HttpRequestAttribute<?>, Object> parameters) {
        int pathIndex = 0;
        for (final UrlPathName name : this.base) {
            if (false == name.equals(parameters.get(HttpRequestAttributes.pathComponent(pathIndex)))) {
                pathIndex = -1;
                break;
            }
            pathIndex++;
        }
        return pathIndex;
    }

    private final UrlPath base;

    // toString.........................................................................................................

    @Override
    public String toString() {
        return ToStringBuilder.empty()
            .enable(ToStringBuilderOption.SKIP_IF_DEFAULT_VALUE)
            .value(this.base)
            .build();
    }
}
