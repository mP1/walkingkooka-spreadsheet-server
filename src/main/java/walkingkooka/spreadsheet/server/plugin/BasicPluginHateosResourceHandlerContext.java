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

import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextDelegator;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContextDelegator;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Locale;
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
    public PluginHateosResourceHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        return this.setHateosResourceHandlerContext(
            this.hateosResourceHandlerContext.setObjectPostProcessor(processor)
        );
    }


    @Override
    public PluginHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.setHateosResourceHandlerContext(
            this.hateosResourceHandlerContext.setPreProcessor(processor)
        );
    }

    private BasicPluginHateosResourceHandlerContext setHateosResourceHandlerContext(final HateosResourceHandlerContext context) {
        return this.hateosResourceHandlerContext.equals(context) ?
            this :
            new BasicPluginHateosResourceHandlerContext(
                context,
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

    @Override
    public LineEnding lineEnding() {
        return this.providerContext.lineEnding();
    }

    @Override
    public void setLineEnding(final LineEnding lineEnding) {
        this.providerContext.setLineEnding(lineEnding);
    }

    @Override
    public void setLocale(final Locale locale) {
        this.providerContext.setLocale(locale);
    }

    @Override
    public PluginHateosResourceHandlerContext cloneEnvironment() {
        return this; // ProviderContext.cloneEnvironment always returns this
    }

    @Override
    public PluginHateosResourceHandlerContext setEnvironmentContext(final EnvironmentContext environmentContext) {
        final ProviderContext before = this.providerContext;
        final ProviderContext after = before.setEnvironmentContext(environmentContext);

        return before == after ?
            this :
            new BasicPluginHateosResourceHandlerContext(
                this.hateosResourceHandlerContext,
                Objects.requireNonNull(after, "environmentContext")
            );
    }

    @Override
    public <T> void setEnvironmentValue(final EnvironmentValueName<T> name,
                                        final T value) {
        this.providerContext.setEnvironmentValue(
            name,
            value
        );
    }

    @Override
    public void removeEnvironmentValue(final EnvironmentValueName<?> name) {
        this.providerContext.removeEnvironmentValue(name);
    }

    private final ProviderContext providerContext;

    @Override
    public String toString() {
        return this.hateosResourceHandlerContext + " " + this.providerContext;
    }
}
