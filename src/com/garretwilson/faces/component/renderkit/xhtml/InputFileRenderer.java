package com.garretwilson.faces.component.renderkit.xhtml;

import java.io.IOException;
import java.util.*;

import javax.faces.component.*;
import javax.faces.context.*;
import javax.faces.convert.*;
import javax.faces.event.*;

import com.garretwilson.faces.component.*;

import org.apache.commons.fileupload.*;

import static com.garretwilson.faces.component.ComponentUtilities.*;
import static com.garretwilson.faces.taglib.xhtml.XHTMLTagConstants.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**Renders a <code>UIInput</code> as an XHTML input element of type <code>file</code>.
@author Garret Wilson
*/
public class InputFileRenderer extends InputTextRenderer
{

	/**The type of renderer.*/
	public static final String RENDERER_TYPE=UIInputFile.COMPONENT_TYPE;

	/**Default constructor.*/
	public InputFileRenderer()
	{
		super(INPUT_TYPE_FILE);	//create an input file element
	}

	/**Attempts to convert previously stored state information into an
		object of the type required for this component (optionally using the
		registered {@link javax.faces.convert.Converter} for this component,
		if there is one). If conversion is successful, the new value
		should be returned from this method;  if not, a
		{@link ConverterException} should be thrown.
	This version expects an Apache Jakarta Commons FileUpload
		<code>FileItem</code>, which if there is no directory specified, will
		be converted to its physical bytes or, if the file is text-based, a string.		
	@param context JSF context for the request being processed.
	@param component The component to be decoded.
	@param submittedValue A value stored on the component during
		<code>decode</code>.
	@exception ConverterException if the submitted value
		cannot be converted successfully.
	@exception NullPointerException if <code>context</code>
		or <code>component</code> is <code>null</code>
	*/
/*G***del; moved to UIInputFile
	public Object getConvertedValue(final FacesContext context, final UIComponent component, final Object submittedValue) throws ConverterException
	{
		if(submittedValue instanceof FileItem)	//if the value is a file item
		{
			
		}
		else	//if the value is not a file item
		{
			
		}
		return submittedValue;	//TODO finish
	}
*/

}
