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

package com.globalmentor.faces.convert;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * Converts an enum value to and from a string representation.
 * @author Garret Wilson
 * @param <T> The type of enum this converter supports.
 */
public class EnumConverter<T extends Enum<T>> implements Converter {

	/** The type of enumeration this converter converts. */
	private final Class<T> enumType;

	/**
	 * Creates an enum converter for a particular enumeration type.
	 * @param enumType The class indicating the type of enum to convert.
	 */
	public EnumConverter(final Class<T> enumType) {
		this.enumType = enumType; //save the enum type
	}

	/**
	 * Converts a string to an enum.
	 * @param context The Faces context for the request being processed.
	 * @param component The component with which this model object value is associated.
	 * @param value The string value to be converted (may be <code>null</code>).
	 * @return <code>null</code> if the value to convert is <code>null</code>, otherwise the result of the conversion.
	 * @throws ConverterException if conversion cannot be successfully performed.
	 * @throws NullPointerException if <code>context</code> or <code>component</code> is <code>null</code>.
	 */
	public Object getAsObject(final FacesContext context, final UIComponent component, final String value) {
		try {
			return value != null ? Enum.valueOf(enumType, value) : null; //if there is a value, convert it
		} catch(final IllegalArgumentException illegalArgumentException) { //if the value is not valid
			throw new ConverterException(illegalArgumentException);
		}
	}

	/**
	 * Converts an enum to a string.
	 * @param context The Faces context for the request being processed.
	 * @param component The component with which this model object value is associated.
	 * @param value The model object value to be converted (may be <code>null</code>).
	 * @return A zero-length string if value is <code>null</code>, otherwise the result of the conversion.
	 * @throws ConverterException if conversion cannot be successfully performed.
	 * @throws NullPointerException if <code>context</code> or <code>component</code> is <code>null</code>.
	 */
	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {
		try {
			return value != null ? enumType.cast(value).toString() : ""; //if there is a value, return its string form, casting it to make sure its of the correct type
		} catch(final ClassCastException classCastException) { //if the value is not valid
			throw new ConverterException(classCastException);
		}
	}
}
