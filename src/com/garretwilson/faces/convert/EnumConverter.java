package com.garretwilson.faces.convert;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**Converts an enum value to and from a string representation.
@author Garret Wilson
@param <T> The type of enum this converter supports.
*/
public class EnumConverter<T extends Enum<T>> implements Converter
{
	
	/**The type of enumeration this converter converts.*/
	private final Class<T> enumType;

	/**Creates an enum converter for a particular enumeration type.
	@param enumType
	*/
	public EnumConverter(final Class<T> enumType)
	{
		this.enumType=enumType;	//save the enum type
	}
	
	/**Converts a string to an enum.
	@param context The Faces context for the request being processed.
	@param component The component with which this model object value is associated.
	@param value The string value to be converted (may be <code>null</code>).
	@return <code>null</code> if the value to convert is <code>null</code>, otherwise the result of the conversion.
	@exception ConverterException if conversion cannot be successfully performed.
	@exception NullPointerException if <code>context</code> or <code>component</code> is <code>null</code>.
	*/
	public Object getAsObject(final FacesContext context, final UIComponent component, final String value)
	{
		try
		{
			return value!=null ? Enum.valueOf(enumType, value) : null;	//if there is a value, convert it
		}
		catch(final IllegalArgumentException illegalArgumentException)	//if the value is not valid
		{
			throw new ConverterException(illegalArgumentException);
		}
	}

	/**Converts an enum to a string.
	@param context The Faces context for the request being processed.
	@param component The component with which this model object value is associated.
	@param value The model object value to be converted (may be <code>null</code>).
	@return A zero-length string if value is <code>null</code>, otherwise the result of the conversion.
	@exception ConverterException if conversion cannot be successfully performed.
	@exception NullPointerException if <code>context</code> or <code>component</code> is <code>null</code>.
	*/
	public String getAsString(final FacesContext context, final UIComponent component, final Object value)
	{
		try
		{
			return value!=null ? enumType.cast(value).toString() : "";	//if there is a value, return its string form, casting it to make sure its of the correct type
		}
		catch(final ClassCastException classCastException)	//if the value is not valid
		{
			throw new ConverterException(classCastException);
		}
	}		
}
