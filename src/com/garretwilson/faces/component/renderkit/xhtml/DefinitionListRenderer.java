package com.garretwilson.faces.component.renderkit.xhtml;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.*;
import javax.faces.context.*;

import com.garretwilson.faces.component.*;

import static com.garretwilson.faces.taglib.xhtml.XHTMLTagConstants.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;

/**Renders XHTML definition lists.
@author Garret Wilson
*/
public class DefinitionListRenderer extends ListRenderer
{

	/**The type of renderer for an XHTML definition list.*/
	public static final String RENDERER_TYPE=UIDefinitionList.COMPONENT_TYPE;

	/**Default constructor.*/
	public DefinitionListRenderer()
	{
		super(ELEMENT_DL);	//use the <dl> element for definition list rendering
	}

	/**@return The name of the XML element for an item in a column.
	@param column The column for which to return the list item name.
	*/
	protected String getItemElementName(final UIColumn column)
	{
		if(column instanceof UITerm)	//if this is a term
		{
			return ELEMENT_DT;	//return the term element
		}
		else if(column instanceof UIDefinition)	//if this is a definition
		{
			return ELEMENT_DD;	//return the definition element
		}
		else	//if we don't recognize the type of column
		{
			return super.getItemElementName(column);	//return the default item element name for this column
		}
	}
}
