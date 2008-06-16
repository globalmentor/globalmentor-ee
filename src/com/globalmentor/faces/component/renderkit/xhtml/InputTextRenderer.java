package com.globalmentor.faces.component.renderkit.xhtml;

import static com.globalmentor.text.xml.xhtml.XHTML.*;

/**Renders a <code>UIInput</code> as an XHTML input element of type <code>text</code>.
@author Garret Wilson
*/
public class InputTextRenderer extends AbstractInputRenderer
{

	/**Default constructor.*/
	public InputTextRenderer()
	{
		super(INPUT_TYPE_TEXT);	//create an input text element
	}

}
