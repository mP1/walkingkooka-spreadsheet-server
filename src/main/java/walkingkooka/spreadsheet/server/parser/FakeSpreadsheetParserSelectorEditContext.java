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

package walkingkooka.spreadsheet.server.parser;

import walkingkooka.InvalidCharacterException;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.format.FakeSpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviderSamplesContext;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelectorToken;
import walkingkooka.spreadsheet.parser.SpreadsheetParser;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserInfoSet;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserName;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserSelector;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserSelectorToken;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.parser.Parser;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public
class FakeSpreadsheetParserSelectorEditContext extends FakeSpreadsheetFormatterContext implements SpreadsheetParserSelectorEditContext {

    public FakeSpreadsheetParserSelectorEditContext() {
        super();
    }

    @Override
    public SpreadsheetParserSelectorEditContext setLocale(final Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override 
    public char valueSeparator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canNumbersHaveGroupSeparator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InvalidCharacterException invalidCharacterException(final Parser<?> parser,
                                                               final TextCursor textCursor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProviderContext providerContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetParserSelectorEditContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterSelector formatter,
                                                     final ProviderContext providerContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterName name,
                                                     final List<?> values, 
                                                     final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetFormatterSelectorToken> spreadsheetFormatterNextToken(final SpreadsheetFormatterSelector formatter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SpreadsheetFormatterSample> spreadsheetFormatterSamples(final SpreadsheetFormatterSelector selector,
                                                                        final boolean includeSamples,
                                                                        final SpreadsheetFormatterProviderSamplesContext context) {
        throw new UnsupportedOperationException();
    }

    @Override public SpreadsheetFormatterInfoSet spreadsheetFormatterInfos() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetParser spreadsheetParser(final SpreadsheetParserSelector parser,
                                               final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetParser spreadsheetParser(final SpreadsheetParserName name,
                                               final List<?> values,
                                               final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetParserSelectorToken> spreadsheetParserNextToken(final SpreadsheetParserSelector parser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetFormatterSelector> spreadsheetFormatterSelector(final SpreadsheetParserSelector parser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetParserInfoSet spreadsheetParserInfos() {
        throw new UnsupportedOperationException();
    }
}
