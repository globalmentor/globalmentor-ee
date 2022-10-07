/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.faces;

/**
 * Constant values for JSF.
 * @author Garret Wilson
 */
public class Faces {

	//common navigation outcomes, some from Mann, <cite>JavaServer Faces in Action</cite>, 130.
	/** Navigation outcome string indicating the operation completed successfully. Move to the next logical page. */
	public static final String SUCCESS_OUTCOME = "success";
	/** Navigation outcome string indicating the operation did not complete successfully. Show a page that tells users why and what they can do about it. */
	public static final String FAILURE_OUTCOME = "failure";
	/** Navigation outcome string indicating there was a system error of some sort. Show a system error page. */
	public static final String ERROR_OUTCOME = "error";
	/** The user needs to log in first. Show the login page. */
	public static final String LOGIN_OUTCOME = "login";
	/** Log out of the application. Show the logout page. */
	public static final String LOGOUT_OUTCOME = "logout";
	/** Cancel current action and move the logical cancel page. */
	public static final String CANCEL_OUTCOME = "cancel";

	//component families
	/** <code>Data</code> */
	public static final String DATA_COMPONENT_FAMILY = "javax.faces.Data";
	//render types
	/** <code>Button</code> */
	public static final String BUTTON_RENDER_TYPE = "javax.faces.Button";
	/** <code>Checkbox</code> */
	public static final String CHECKBOX_RENDER_TYPE = "javax.faces.Checkbox";
	/** <code>Form</code> */
	public static final String FORM_RENDER_TYPE = "javax.faces.Form";
	/** <code>Format</code> */
	public static final String FORMAT_RENDER_TYPE = "javax.faces.Format";
	/** <code>Grid</code> */
	public static final String GRID_RENDER_TYPE = "javax.faces.Grid";
	/** <code>Group</code> */
	public static final String GROUP_RENDER_TYPE = "javax.faces.Group";
	/** <code>Hidden</code> */
	public static final String HIDDEN_RENDER_TYPE = "javax.faces.Hidden";
	/** <code>Image</code> */
	public static final String IMAGE_RENDER_TYPE = "javax.faces.Image";
	/** <code>Label</code> */
	public static final String LABEL_RENDER_TYPE = "javax.faces.Label";
	/** <code>Link</code> */
	public static final String LINK_RENDER_TYPE = "javax.faces.Link";
	/** <code>Listbox</code> */
	public static final String LISTBOX_RENDER_TYPE = "javax.faces.Listbox";
	/** <code>Menu</code> */
	public static final String MENU_RENDER_TYPE = "javax.faces.Menu";
	/** <code>Message</code> */
	public static final String MESSAGE_RENDER_TYPE = "javax.faces.Message";
	/** <code>Messages</code> */
	public static final String MESSAGES_RENDER_TYPE = "javax.faces.Messages";
	/** <code>Radio</code> */
	public static final String RADIO_RENDER_TYPE = "javax.faces.Radio";
	/** <code>Secret</code> */
	public static final String SECRET_RENDER_TYPE = "javax.faces.Secret";
	/** <code>Text</code> */
	public static final String TABLE_RENDER_TYPE = "javax.faces.Text";
	/** <code>Text</code> */
	public static final String TEXT_RENDER_TYPE = "javax.faces.Text";
	/** <code>Textarea</code> */
	public static final String TEXTAREA_RENDER_TYPE = "javax.faces.Textarea";

}
