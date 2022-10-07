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

package com.globalmentor.faces.component;

import java.io.IOException;

import javax.faces.context.*;
import javax.faces.component.*;
import javax.faces.event.*;

/**
 * Basic data component with enhanced functionality. This class also creates a work-around for the JSF RI bug that erroneously keeps a cached data model when
 * broadcasting commands in nested UIData.
 * @author Garret Wilson
 */
public class UIBasicData extends UIData {

	/** The attribute for storing the variable name. */
	public static final String VAR_ATTRIBUTE = "var";

	/** Default constructor. */
	public UIBasicData() {
		super(); //construct the parent class
	}

	/*TODO del
	  public void setRowIndex(int rowIndex)
			{
				getLogger().trace("basic data setting row index: {}", rowIndex);
				super.setRowIndex(rowIndex);
	getLogger().trace("row index set, var: {}", getVar());
	getLogger().trace("row data available: {}", isRowAvailable());
	if(isRowAvailable())
	{
		getLogger().trace("row data is: {}", getRowData());
	}
	else
	{
		getLogger().trace("row data not available; let's find out why.");
		final Object value=getValue();
		getLogger().trace("value is: {}", value);
	//TODO del	getLogger().trace("data model is: {}", getDataModel());
		if(value instanceof Object[])
		{
			final Object[] objects=(Object[])value;
			getLogger().trace("This is an array of length: {}", objects.length);
		}
	Map requestMap =getFacesContext().getExternalContext().getRequestMap();
	getLogger().trace("variable is now: {}", requestMap.get(getVar()));
	
			}
	*/

	/**
	 * Updates values based upon the current settings.
	 * @param context The JSF context.
	 */
	protected void updateValue(final FacesContext context) {
	}

	/**
	 * Prepares the component for encoding.
	 * @param context The JSF context.
	 * @throws IOException if there is an error encoding the information.
	 */
	public void encodeBegin(final FacesContext context) throws IOException {
		updateValue(context); //update our value before further processing
		super.encodeBegin(context); //do the default encoding
	}

	/**
	 * Finishes component for encoding.
	 * @param context The JSF context.
	 * @throws IOException if there is an error encoding the information.
	 */
	public void encodeEnd(final FacesContext context) throws IOException {
		super.encodeEnd(context); //do the default end encoding
	}

	/**
	 * Processes the decoding for this component and children.
	 * @param context The JSF context.
	 */
	public void processDecodes(final FacesContext context) {
		updateValue(context); //update our value before further processing
		super.processDecodes(context);
	}

	/**
	 * Broadcasts an event to interested components. This version fixes a bug in JSF RI that keeps a cached data data model in nested UIData.
	 * @param event The event to broadcast.
	 * @throws AbortProcessingException if processing should not continue.
	 */
	public void broadcast(final FacesEvent event) throws AbortProcessingException {
		if(FacesComponents.getParent(this, UIData.class) != null) { //if we're nested within another UIData
			if(getValueBinding("value") != null) { //if our value depends on a value binding (this won't work if we have a local value, too) TODO use a constant
				setValue(null); //remove our local value, uncaching the data model in the process
			}
		}
		super.broadcast(event); //do the default broadcasting
	}

}
