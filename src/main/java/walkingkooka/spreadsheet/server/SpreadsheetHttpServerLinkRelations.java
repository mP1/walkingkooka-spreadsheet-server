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

import walkingkooka.net.header.LinkRelation;
import walkingkooka.reflect.PublicStaticHelper;

public final class SpreadsheetHttpServerLinkRelations implements PublicStaticHelper {

    public final static LinkRelation<?> EDIT = LinkRelation.with("edit");

    public final static LinkRelation<?> FORMAT = LinkRelation.with("format");

    public final static LinkRelation<?> NEXT_TEXT_COMPONENT = LinkRelation.with("next-text-component");

    public final static LinkRelation<?> TEXT_COMPONENTS = LinkRelation.with("text-components");

    public final static LinkRelation<?> SAMPLES = LinkRelation.with("samples");

    /**
     * Stop creation
     */
    private SpreadsheetHttpServerLinkRelations() {
        throw new UnsupportedOperationException();
    }
}
