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

	/**Determines the current value to be rendered.
	This implementation always returns <code>null</code>, because the sumitted
		and/or current value should never be rendered.
	@return <code>null</code> indicating the current value should not be displayed.
	*/
	protected Object getRenderValue(final UIInput input)
	{
		return null;
	}
}
