package com.garretwilson.faces.component.renderkit.xhtml;

import javax.faces.component.*;

import com.garretwilson.faces.component.*;

import static com.globalmentor.text.xml.xhtml.XHTML.*;

/**Renders a <code>UIInput</code> as an XHTML input element of type <code>file</code>.
@author Garret Wilson
*/
public class InputFileRenderer extends AbstractInputRenderer
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
