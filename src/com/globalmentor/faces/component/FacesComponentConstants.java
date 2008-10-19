/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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
 */

package com.globalmentor.faces.component;

/**Constant values used by components
@author Garret Wilson
*/
public class FacesComponentConstants
{
	/**The rendered attribute.*/
	public final static String RENDERED_ATTRIBUTE="rendered";
	/**The style class attribute.*/
	public final static String STYLE_CLASS_ATTRIBUTE="styleClass";

			//TODO are these XHTML attributes?
	/**The disabled attribute.*/
	public final static String DISABLED_ATTRIBUTE="disabled";
	/**The immediate attribute.*/
	public final static String IMMEDIATE_ATTRIBUTE="immediate";
	/**The read-only attribute.*/
	public final static String READONLY_ATTRIBUTE="readonly";
	/**The name attribute.*/
	public final static String NAME_ATTRIBUTE="name";
	/**The value attribute.*/
	public final static String VALUE_ATTRIBUTE="value";

		//UICommand attributes
	/**The UICommand.action attribute.*/
	public final static String COMMAND_ACTION_ATTRIBUTE="action";
	/**The UICommand.actionListener attribute.*/
	public final static String COMMAND_ACTION_LISTENER_ATTRIBUTE="actionListener";

		//UIGraphic attributes
	/**The UIGraphic.url attribute.*/
	public final static String GRAPHIC_URL_ATTRIBUTE="url";
	/**The UIGraphic.alt attribute.*/
	public final static String GRAPHIC_ALT_ATTRIBUTE="alt";

		//UIInput attributes
	/**The UIInput.validator attribute.*/
	public final static String INPUT_VALIDATOR_ATTRIBUTE="validator";
	/**The UInput.converter attribute.*/
	public final static String INPUT_CONVERTER_ATTRIBUTE="converter";
	/**The UInput.immediate attribute.*/
	public final static String INPUT_IMMEDIATE_ATTRIBUTE="immediate";
	/**The UInput.required attribute.*/
	public final static String INPUT_REQUIRED_ATTRIBUTE="required";
	/**The UInput.valueChangeListener attribute.*/
	public final static String INPUT_VALUE_CHANGE_LISTENER_ATTRIBUTE="valueChangeListener";

		//UISelectItem attributes
	/**The UISelectItem.itemValue attribute.*/
	public final static String SELECT_ITEM_ITEM_VALUE_ATTRIBUTE="itemValue";
	/**The UISelectItem.itemLabel attribute.*/
	public final static String SELECT_ITEM_ITEM_LABEL_ATTRIBUTE="itemLabel";
	/**The UISelectItem.itemDescription attribute.*/
	public final static String SELECT_ITEM_ITEM_DESCRIPTION_ATTRIBUTE="itemDescription";
}