package com.garretwilson.faces.component.renderkit.xhtml;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;

import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**Renders a <code>UIInput</code> as an XHTML input element of type <code>checkbox</code>.
@author Garret Wilson
*/
public class InputCheckboxRenderer extends AbstractInputRenderer
{

	/**Default constructor.*/
	public InputCheckboxRenderer()
	{
		super(INPUT_TYPE_CHECKBOX);	//create an input checkbox element
	}

	/**Determines the current value to be rendered.
	This implementation always returns <code>true</code>, because a checkbox
		uses its <code>value</code> attribute to indicate the value indicating
		checked status, not the current value of the component. For the latter, the
		<code>checked</code> attribute is used.
	@return <code>null</code> indicating the current value should not be displayed.
	*/
	protected Object getRenderValue(final UIInput input)
	{
		return Boolean.TRUE.toString();	//always show "true" as the checkbox value
//TODO bring back when we create our own response writer; the current RI response writer doesn't encode Boolean values correctly 		return Boolean.TRUE;	//always show "true" as the checkbox value
	}

  /**Converts the submitted value to a value usable by the component.
  @param context The JSF context.
  @param component The component for which the converted value is intended.
  @param submittedValue The value submitted to the component.
	*/
	public Object getConvertedValue(final FacesContext context, final UIComponent component, final Object submittedValue) throws ConverterException
	{
		return Boolean.valueOf(submittedValue.toString());	//return the value as a Boolean
	}

	/**Begins encoding the component.
	This version adds rendering of the <code>checked</code> attribute.
	@param context The JSF context.
	@param component The component being rendered.
	@exception IOException Thrown if there is an error writing the output.
	@exception NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void encodeBegin(final FacesContext context, final UIComponent component) throws IOException
	{
		super.encodeBegin(context, component);	//do the default encoding
		if(component.isRendered())	//if the component should be rendered
		{
			final UIInput input=(UIInput)component;	//get the component as an input component
			final ResponseWriter writer=context.getResponseWriter();	//get the response writer
			final Object intendedValue=getIntendedValue(input);	//get the intended render value 
			final boolean checked=Boolean.valueOf(intendedValue.toString()).booleanValue();	//convert the intended value to a boolean
			if(checked)	//if the checkbox is checked
			{
				writer.writeAttribute(ELEMENT_INPUT_ATTRIBUTE_CHECKED, INPUT_CHECKED_CHECKED, ATTRIBUTE_VALUE);	//checked="checked"				
			}
		}
	}

	/**Decodes the value information, doing any transformations necessary.
	This version changes the value to a <code>Boolean</code>, even if no value was present.
	@param value The value received as a request submission.
	@return <code>Boolean.TRUE</code> or <code>Boolean.FALSE</code>.
	*/
	protected Object decodeValue(final Object value)
	{
		return value!=null ? Boolean.valueOf(value.toString()) : Boolean.FALSE;	//return false if no value was submitted
	}

}
