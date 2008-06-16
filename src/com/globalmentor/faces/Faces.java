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

package com.globalmentor.faces;

/**Constant values for JSF.
@author Garret Wilson
*/
public class Faces
{

			//common navigation outcomes, some from Mann, <cite>JavaServer Faces in Action</cite>, 130.
	/**Navigation outcome string indicating the operation completed successfully. Move to the next logical page.*/
	public final static String SUCCESS_OUTCOME="success";
	/**Navigation outcome string indicating the operation did not complete successfully. Show a page that tells users why and what they can do about it.*/
	public final static String FAILURE_OUTCOME="failure";
	/**Navigation outcome string indicating there was a system error of some sort. Show a system error page.*/
	public final static String ERROR_OUTCOME="error";
	/**The user needs to log in first. Show the login page.*/
	public final static String LOGIN_OUTCOME="login";
	/**Log out of the application. Show the logout page.*/
	public final static String LOGOUT_OUTCOME="logout";
	/**Cancel current action and move the logical cancel page.*/
	public final static String CANCEL_OUTCOME="cancel";

		//component families
	public final static String DATA_COMPONENT_FAMILY="javax.faces.Data";
		//render types
	public final static String BUTTON_RENDER_TYPE="javax.faces.Button";
	public final static String CHECKBOX_RENDER_TYPE="javax.faces.Checkbox";
	public final static String FORM_RENDER_TYPE="javax.faces.Form";
	public final static String FORMAT_RENDER_TYPE="javax.faces.Format";
	public final static String GRID_RENDER_TYPE="javax.faces.Grid";
	public final static String GROUP_RENDER_TYPE="javax.faces.Group";
	public final static String HIDDEN_RENDER_TYPE="javax.faces.Hidden";
	public final static String IMAGE_RENDER_TYPE="javax.faces.Image";
	public final static String LABEL_RENDER_TYPE="javax.faces.Label";
	public final static String LINK_RENDER_TYPE="javax.faces.Link";
	public final static String LISTBOX_RENDER_TYPE="javax.faces.Listbox";
	public final static String MENU_RENDER_TYPE="javax.faces.Menu";
	public final static String MESSAGE_RENDER_TYPE="javax.faces.Message";
	public final static String MESSAGES_RENDER_TYPE="javax.faces.Messages";
	public final static String RADIO_RENDER_TYPE="javax.faces.Radio";
	public final static String SECRET_RENDER_TYPE="javax.faces.Secret";
	public final static String TABLE_RENDER_TYPE="javax.faces.Table";
	public final static String TEXT_RENDER_TYPE="javax.faces.Text";
	public final static String TEXTAREA_RENDER_TYPE="javax.faces.Textarea";

}
