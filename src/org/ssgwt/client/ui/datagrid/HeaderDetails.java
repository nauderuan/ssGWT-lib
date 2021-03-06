/**
 * Copyright 2012 A24Group
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ssgwt.client.ui.datagrid;

import org.ssgwt.client.ui.datagrid.filter.AbstractHeaderFilter;

/**
 * Object holding info of the FilterSortHeader for the FilterSortCell
 * 
 * @author Johannes Gryffenberg
 * @since 29 June 2012
 */
public class HeaderDetails {

    /**
     * The label on the header
     */
    public String label;

    /**
     * The filter widget that should be displayed if the filte icon is clicked
     */
    public AbstractHeaderFilter filterWidget;
}
