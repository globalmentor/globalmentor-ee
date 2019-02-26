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

package com.globalmentor.faces.beans;

/**
 * A generic JSF backing bean that allows editing of object information.
 * @author Garret Wilson
 */
public abstract class EditObjectBean<T> extends EditBean {
	/**
	 * Constructor.
	 * @param navigationOutcome The outcome for navigating to the edit page.
	 */
	public EditObjectBean(final String navigationOutcome) {
		super(navigationOutcome); //construct the base class
	}

	/** @return A new object from the bean property settings. */
	protected abstract T create();

	/**
	 * Accepts the information entered.
	 * @return <code>true</code> if successful.
	 * @see #accept(T)
	 */
	public boolean accept() {
		return accept(create()); //create a user object from the provided information
	}

	/**
	 * Accepts the information from the given object.
	 * @param object The object to accept. The method can use <code>isNew()</code> to determine whether the object is new or an existing object was being edited.
	 * @return <code>true</code> if successful.
	 */
	public abstract boolean accept(final T object);

}
