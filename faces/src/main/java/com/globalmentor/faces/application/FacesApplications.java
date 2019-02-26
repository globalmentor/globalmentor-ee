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

package com.globalmentor.faces.application;

import javax.faces.application.*;
import javax.faces.component.*;
import javax.faces.context.FacesContext;

import static com.globalmentor.faces.Faces.*;
import static com.globalmentor.faces.component.FacesComponentConstants.*;
import static com.globalmentor.faces.component.FacesComponents.*;
import static com.globalmentor.faces.el.FacesExpressions.*;

import com.globalmentor.faces.component.*;
import com.globalmentor.faces.el.ExpressionMethodBinding;

/**
 * Utilities for working with JSF applications.
 * @author Garret Wilson
 */
public class FacesApplications {
	/**
	 * Creates a <code>UIColumn</code> component.
	 * @param application The current JSF application
	 * @return A new <code>UIColumn</code> component.
	 */
	public static UIColumn createColumn(final Application application) { //TODO probably require a unique ID here, too
		final String id = FacesContext.getCurrentInstance().getViewRoot().createUniqueId(); //TODO testing
		final UIColumn column = (UIColumn)createComponent(application, UIColumn.COMPONENT_TYPE, id); //create a column component
		/*TODO del
		Debug.setDebug(true);
		Debug.setVisible(true);
		Log.trace("created column with ID", id);
		*/
		return column; //return the component
	}

	/**
	 * Creates a <code>UIColumnFacet</code> component.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @return A new <code>UIColumnFacet</code> component.
	 */
	public static UIColumnFacet createColumnFacet(final Application application, final String id) {
		final UIColumnFacet columnFacet = (UIColumnFacet)createComponent(application, UIColumnFacet.COMPONENT_TYPE, id); //create a column facet component
		return columnFacet; //return the component
	}

	/**
	 * Creates a <code>UICommand</code> component rendered as a link with the given action.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @param action The command action.
	 * @return A new <code>UICommand</code> component with the given values.
	 */
	public static UICommand createCommandLink(final Application application, final String id, final String action) {
		final UICommand command = createCommand(application, id, action); //create a command component
		command.setRendererType(LINK_RENDER_TYPE); //render the command as a link
		return command; //return the component
	}

	/**
	 * Creates a default <code>UICommand</code> component.
	 * @param id The unique ID of the new component.
	 * @param application The current JSF application
	 * @return A new <code>UICommand</code> component.
	 */
	public static UICommand createCommand(final Application application, final String id) {
		return createCommand(application, id, null); //create a command with no action
	}

	/**
	 * Creates a <code>UICommand</code> component with the given action.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @param action The command action.
	 * @return A new <code>UICommand</code> component with the given values.
	 */
	public static UICommand createCommand(final Application application, final String id, final String action) {
		final UICommand command = (UICommand)createComponent(application, UICommand.COMPONENT_TYPE, id); //create a command component
		if(action != null) { //if we have an action
			//create an expression from the action, and wrap it in a method-binding subclass so that UICommand will recognize it
			command.setAction(new ExpressionMethodBinding(createExpression(application, action)));
		}
		return command; //return the component
	}

	/**
	 * Creates a <code>UIData</code> component with the given value and var.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @param value The value of the data.
	 * @param var The name of the variable to use for iteration, with no value-binding allowed.
	 * @return A new <code>UIData</code> component with the given values.
	 */
	public static UIData createData(final Application application, final String id, final String value, final String var) {
		final UIData data = (UIData)createComponent(application, UIData.COMPONENT_TYPE, id); //create a data component
		setStringValue(data, VALUE_ATTRIBUTE, value); //store the value, creating a value binding if necessary
		data.setVar(var); //set the variable name
		return data; //return the component
	}

	/**
	 * Creates a <code>UIGraphic</code> component with the given URL and alternate text.
	 * @param application The current JSF application
	 * @param url The context-relative URL of the graphic.
	 * @param alt The alternate text of the graphic.
	 * @return A new <code>UIGraphic</code> component with the given values.
	 */
	public static UIGraphic createGraphic(final Application application, final String url, final String alt) {
		final UIGraphic graphic = (UIGraphic)createComponent(application, UIGraphic.COMPONENT_TYPE,
				FacesContext.getCurrentInstance().getViewRoot().createUniqueId()); //create a graphic component
		setStringValue(graphic, GRAPHIC_URL_ATTRIBUTE, url); //store the URL, creating a value binding if necessary
		setStringValue(graphic, GRAPHIC_ALT_ATTRIBUTE, alt); //store the alternate text, creating a value binding if necessary
		return graphic; //return the component
	}

	/**
	 * Creates a <code>UIInputFile</code> file upload component with the given id.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @return A new <code>UIInputFile</code> component with the given values.
	 */
	public static UIInputFile createInputFile(final Application application, final String id) {
		final UIInputFile inputFile = (UIInputFile)createComponent(application, UIInputFile.COMPONENT_TYPE, id); //create an input file component with the given ID
		return inputFile; //return the component
	}

	/**
	 * Creates a <code>UIInput</code> component rendered as hidden with the given id and value.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @param value The new value.
	 * @return A new <code>UIInput</code> component with the given values.
	 */
	public static UIInput createInputHidden(final Application application, final String id, final String value) {
		final UIInput input = createInput(application, id, value); //create an input component with the given ID and value
		input.setRendererType(HIDDEN_RENDER_TYPE); //render the input hidden
		return input; //return the component
	}

	/**
	 * Creates a <code>UIInput</code> component rendered as text with the given id and value.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @param value The new value.
	 * @return A new <code>UIInput</code> component with the given values.
	 */
	public static UIInput createInputText(final Application application, final String id, final String value) {
		final UIInput input = createInput(application, id, value); //create an input component with the given ID and value
		input.setRendererType(TEXT_RENDER_TYPE); //render the input as text
		return input; //return the component
	}

	/**
	 * Creates a <code>UIInput</code> component with the given id and value.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @param value The new value.
	 * @return A new <code>UIInput</code> component with the given values.
	 */
	public static UIInput createInput(final Application application, final String id, final String value) {
		final UIInput input = (UIInput)createComponent(application, UIInput.COMPONENT_TYPE, id); //create an input component with the given ID
		setStringValue(input, VALUE_ATTRIBUTE, value); //store the value, creating a value binding if necessary
		return input; //return the component
	}

	/**
	 * Creates a <code>UIOutput</code> component with the given value.
	 * @param application The current JSF application
	 * @param value The value of the output to create.
	 * @return A new <code>UIOutput</code> component with the given values.
	 */
	public static UIOutput createOutput(final Application application, final String value) {
		final UIOutput output = (UIOutput)createComponent(application, UIOutput.COMPONENT_TYPE, FacesContext.getCurrentInstance().getViewRoot().createUniqueId()); //create an output component
		setStringValue(output, VALUE_ATTRIBUTE, value); //store the value, creating a value binding if necessary
		return output; //return the component
	}

	/**
	 * Creates a <code>UIPanel</code> component rendered as a group.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @return A new <code>UIPanel</code> component.
	 */
	public static UIPanel createPanelGroup(final Application application, final String id) {
		final UIPanel panel = createPanel(application, id); //create a panel component
		panel.setRendererType(GROUP_RENDER_TYPE); //render the panel as a group
		return panel; //return the component
	}

	/**
	 * Creates a <code>UIPanel</code> component.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @return A new <code>UIPanel</code> component.
	 */
	public static UIPanel createPanel(final Application application, final String id) {
		final UIPanel panel = (UIPanel)createComponent(application, UIPanel.COMPONENT_TYPE, id); //create a panel component
		return panel; //return the component
	}

	/**
	 * Creates a <code>UIParameter</code> component with only a value.
	 * @param application The current JSF application
	 * @param value The parameter value.
	 * @return A new <code>UIParameter</code> component with the given value.
	 */
	public static UIParameter createParameter(final Application application, final String value) {
		return createParameter(application, null, value); //create a parameter with no name
	}

	/**
	 * Creates a <code>UIParameter</code> component with the given name and value.
	 * @param application The current JSF application
	 * @param name The parameter name, or <code>null</code> for no name.
	 * @param value The parameter value.
	 * @return A new <code>UIParameter</code> component with the given name and value.
	 */
	public static UIParameter createParameter(final Application application, final String name, final String value) {
		final UIParameter parameter = (UIParameter)createComponent(application, UIParameter.COMPONENT_TYPE,
				FacesContext.getCurrentInstance().getViewRoot().createUniqueId()); //create a parameter component
		setStringValue(parameter, NAME_ATTRIBUTE, name); //store the name, creating a value binding if necessary
		setStringValue(parameter, VALUE_ATTRIBUTE, value); //store the value, creating a value binding if necessary
		return parameter; //return the component
	}

	/**
	 * Creates a <code>UIParameterFormat</code> component with only a value.
	 * @param application The current JSF application
	 * @param value The parameter value.
	 * @return A new <code>UIParameterFormat</code> component with the given value.
	 */
	public static UIParameterFormat createParameterFormat(final Application application, final String value) {
		return createParameterFormat(application, null, value); //create a parameter with no name
	}

	/**
	 * Creates a <code>UIParameterFormat</code> component with the given name and value.
	 * @param application The current JSF application
	 * @param name The parameter name, or <code>null</code> for no name.
	 * @param value The parameter value.
	 * @return A new <code>UIParameterFormat</code> component with the given name and value.
	 */
	public static UIParameterFormat createParameterFormat(final Application application, final String name, final String value) {
		final UIParameterFormat parameter = (UIParameterFormat)createComponent(application, UIParameterFormat.COMPONENT_TYPE,
				FacesContext.getCurrentInstance().getViewRoot().createUniqueId()); //create a parameter component
		setStringValue(parameter, NAME_ATTRIBUTE, name); //store the name, creating a value binding if necessary
		setStringValue(parameter, VALUE_ATTRIBUTE, value); //store the value, creating a value binding if necessary
		return parameter; //return the component
	}

	/**
	 * Creates a <code>UISelectBoolean</code> component rendered as a checkbox with the given id and value.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @param value The new value.
	 * @return A new <code>UISelectBoolean</code> component with the given values.
	 */
	public static UISelectBoolean createSelectBooleanCheckbox(final Application application, final String id, final String value) {
		final UISelectBoolean selectBoolean = createSelectBoolean(application, id, value); //create a select boolean component with the given ID and value
		selectBoolean.setRendererType(CHECKBOX_RENDER_TYPE); //render the select boolean as a checkbox
		return selectBoolean; //return the component		
	}

	/**
	 * Creates a <code>UISelectBoolean</code> component with the given id and value.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @param value The new value.
	 * @return A new <code>UISelectBoolean</code> component with the given values.
	 */
	public static UISelectBoolean createSelectBoolean(final Application application, final String id, final String value) {
		final UISelectBoolean selectBoolean = (UISelectBoolean)createComponent(application, UISelectBoolean.COMPONENT_TYPE, id); //create a select boolean component with the given ID
		setBooleanValue(selectBoolean, VALUE_ATTRIBUTE, value); //store the value, creating a value binding if necessary
		return selectBoolean; //return the component
	}

	/**
	 * Creates a <code>UISelectItem</code> component with the given values.
	 * @param application The current JSF application
	 * @param itemValue The value of the item or a value-binding expression.
	 * @param itemLabel The item label or a value-binding expression.
	 * @param itemDescription The item description or a value-binding expression.
	 * @return A new <code>UISelecdtItems</code> component with the given value.
	 */
	public static UISelectItem createSelectItem(final Application application, final Object itemValue, final String itemLabel, final String itemDescription) {
		final UISelectItem selectItem = (UISelectItem)createComponent(application, UISelectItem.COMPONENT_TYPE,
				FacesContext.getCurrentInstance().getViewRoot().createUniqueId()); //create a parameter component
		setObjectValue(selectItem, SELECT_ITEM_ITEM_VALUE_ATTRIBUTE, itemValue); //store the item value, creating a value binding if necessary
		setStringValue(selectItem, SELECT_ITEM_ITEM_LABEL_ATTRIBUTE, itemLabel); //store the item label, creating a value binding if necessary
		setStringValue(selectItem, SELECT_ITEM_ITEM_DESCRIPTION_ATTRIBUTE, itemDescription); //store the item description, creating a value binding if necessary
		return selectItem; //return the component
	}

	/**
	 * Creates a <code>UISelectItems</code> component with the given value.
	 * @param application The current JSF application
	 * @param value The select items value.
	 * @return A new <code>UISelecdtItems</code> component with the given value.
	 */
	public static UISelectItems createSelectItems(final Application application, final String value) {
		final UISelectItems selectItems = (UISelectItems)createComponent(application, UISelectItems.COMPONENT_TYPE,
				FacesContext.getCurrentInstance().getViewRoot().createUniqueId()); //create a parameter component
		setValueBindingValue(selectItems, VALUE_ATTRIBUTE, value); //store the value, creating a value binding if necessary
		return selectItems; //return the component
	}

	/**
	 * Creates a <code>UISelectOne</code> component rendered as a menu with the given id and value.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @param value The new value.
	 * @return A new <code>UISelectOne</code> component with the given values.
	 */
	public static UISelectOne createSelectOneMenu(final Application application, final String id, final Object value) {
		final UISelectOne selectOne = createSelectOne(application, id, value); //create a select one component
		selectOne.setRendererType(MENU_RENDER_TYPE); //render the select one as a menu
		return selectOne; //return the component
	}

	/**
	 * Creates a <code>UISelectOne</code> component with the given id and value.
	 * @param application The current JSF application
	 * @param id The unique ID of the new component.
	 * @param value The new value.
	 * @return A new <code>UISelectOne</code> component with the given values.
	 */
	public static UISelectOne createSelectOne(final Application application, final String id, final Object value) {
		final UISelectOne selectOne = (UISelectOne)createComponent(application, UISelectOne.COMPONENT_TYPE, id); //create a select one component with the given ID
		setObjectValue(selectOne, VALUE_ATTRIBUTE, value); //store the value, creating a value binding if necessary
		return selectOne; //return the component
	}

	/**
	 * Creates a <code>UIComponent</code> with a unique ID.
	 * @param application The current JSF application
	 * @param componentType The type of component to create.
	 * @return A new component.
	 */
	/*TODO del; force all components to give an ID
		public static UIComponent createComponent(final Application application, final String componentType)
		{
			return createComponent(application, componentType, FacesContext.getCurrentInstance().getViewRoot().createUniqueId());	//TODO fix
		}
	*/

	/**
	 * Creates a <code>UIComponent</code> with the given ID.
	 * @param application The current JSF application
	 * @param componentType The type of component to create.
	 * @param id The unique ID of the new component.
	 * @return A new component with the given ID.
	 */
	public static UIComponent createComponent(final Application application, final String componentType, final String id) {
		final UIComponent component = application.createComponent(componentType); //create The component
		component.setId(id); //set the component ID
		return component; //return the component we created
	}

}
