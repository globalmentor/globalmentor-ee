package com.garretwilson.faces.component.renderkit.xhtml;

import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**Renders a <code>UIInput</code> as an XHTML input element of type <code>text</code>.
@author Garret Wilson
*/
public class InputTextRenderer extends AbstractXHTMLRenderer
{

	/**Default constructor.*/
	public InputTextRenderer()
	{
		super(INPUT_TYPE_TEXT);	//create an input text element
	}

}
