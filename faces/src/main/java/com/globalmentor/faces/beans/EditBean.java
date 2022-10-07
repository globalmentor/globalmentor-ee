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

package com.globalmentor.faces.beans;

import static com.globalmentor.faces.Faces.*;

import com.globalmentor.faces.Faces;

/**
 * A generic JSF backing bean that allows editing of information.
 * @author Garret Wilson
 */
public abstract class EditBean {

	/** Whether this bean represents a new object. */
	private boolean isNew = true;

	/** @return Whether this bean represents a new object. */
	public boolean isNew() {
		return isNew;
	}

	/**
	 * Sets whether this bean represents a new object.
	 * @param isNew <code>true</code> if a new object is being edited.
	 */
	protected void setNew(final boolean isNew) {
		this.isNew = isNew;
	}

	/** The outcome for navigating to the edit page. */
	private final String navigationOutcome;

	/** @return The outcome for navigating to the edit page. */
	protected final String getNavigationOutcome() {
		return navigationOutcome;
	}

	/**
	 * Constructor.
	 * @param navigationOutcome The outcome for navigating to the edit page.
	 */
	public EditBean(final String navigationOutcome) {
		this.navigationOutcome = navigationOutcome;
	}

	/** Initializes the values of the bean. */
	public abstract void clear();

	/**
	 * Loads information from an object into the bean.
	 * @return <code>true</code> if successful.
	 */
	public abstract boolean load();

	/**
	 * Accepts the information entered.
	 * @return <code>true</code> if successful.
	 */
	public abstract boolean accept();

	/**
	 * Prepares for a new information and navigates to the new object page.
	 * @return An outcome for the edit page.
	 * @see #clear()
	 * @see #getNavigationOutcome()
	 */
	public String actionNew() {
		setNew(true); //show that we are ready to edit a new object
		clear(); //initialize the values
		return getNavigationOutcome(); //go to the account page
	}

	/**
	 * Loads the current information and navigates to the edit page.
	 * @return The navigation outcome if successful, else <code>ERROR_OUTCOME</code>.
	 * @see #load()
	 * @see #getNavigationOutcome()
	 * @see Faces#ERROR_OUTCOME
	 */
	public String actionEdit() {
		if(load()) { //load the object; if we were successful
			setNew(false); //show that this information is not for a new object
			return getNavigationOutcome(); //navigate to the edit page
		}
		return ERROR_OUTCOME; //something went wrong
	}

	/**
	 * Creates a new object based upon the current state of the bean properties, and accepts the object..
	 * @return A string indicating the outcome of acceptance.
	 */
	public String actionAccept() {
		if(accept()) { //accept the information; if we were successful
			return SUCCESS_OUTCOME; //show that we were able to accept the object
		} else { //if something went wrong
			return ERROR_OUTCOME; //show that there was an error			
		}
	}

}
