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

package walkingkooka.spreadsheet.server.plugin;

import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextDelegator;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Objects;

final class BasicPluginHateosResourceHandlerContext implements PluginHateosResourceHandlerContext,
    HateosResourceHandlerContextDelegator,
    ProviderContextDelegator {

    static BasicPluginHateosResourceHandlerContext with(final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                        final ProviderContext providerContext) {
        return new BasicPluginHateosResourceHandlerContext(
            Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext"),
            Objects.requireNonNull(providerContext, "providerContext")
        );
    }

    private BasicPluginHateosResourceHandlerContext(final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                    final ProviderContext providerContext) {
        this.hateosResourceHandlerContext = hateosResourceHandlerContext;
        this.providerContext = providerContext;
    }

    @Override
    public PluginHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        final HateosResourceHandlerContext before = this.hateosResourceHandlerContext;
        final HateosResourceHandlerContext after = before.setPreProcessor(processor);

        return before.equals(after) ?
            this :
            new BasicPluginHateosResourceHandlerContext(
                after,
                this.providerContext
            );
    }

    @Override
    public HateosResourceHandlerContext hateosResourceHandlerContext() {
        return this.hateosResourceHandlerContext;
    }

    private final HateosResourceHandlerContext hateosResourceHandlerContext;

    @Override
    public ProviderContext providerContext() {
        return this.providerContext;
    }

    private final ProviderContext providerContext;

    @Override
    public String toString() {
        return this.hateosResourceHandlerContext + " " + this.providerContext;
    }
}
