package com.garretwilson.faces.component.renderkit.xhtml;

import java.io.IOException;
import java.util.Iterator;
import javax.faces.component.*;
import javax.faces.context.*;
import com.garretwilson.faces.component.UIList;

import static com.garretwilson.faces.taglib.xhtml.XHTMLTagConstants.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**The default renderer for a XHTML ordered list
@author Garret Wilson
*/
public class ListRenderer extends AbstractListRenderer
{

	/**The type of renderer for XHTML &lt;ol&gt;.*/
	public static final String RENDERER_TYPE=UIList.COMPONENT_TYPE;

	/**The name of the XML element for the list.*/
	private final String listElementName;

		/**@return The name of the XML element for the list.*/
		protected String getListElementName() {return listElementName;}

	/**The name of the XML element for an item in a column.*/
	private final String itemElementName;

		/**@return The name of the XML element for an item in a column.*/
		protected String getItemElementName() {return itemElementName;}

	/**Default constructor.*/
	public ListRenderer()
	{
		this(ELEMENT_OL);	//default to the <ol> element for the list
	}

	/**List element name constructor with a default list item name.
	@param listElementName The name of the XML element for the list.
	*/
	public ListRenderer(final String listElementName)
	{
		this(listElementName, ELEMENT_LI);	//default to the <li> element for each list item
	}

	/**List element name and item element name constructor.
	@param listElementName The name of the XML element for the list.
	@param itemElementName The name of the XML element for an item in a column.
	*/
	public ListRenderer(final String listElementName, final String itemElementName)
	{
		this.listElementName=listElementName;
		this.itemElementName=itemElementName;
	}

	/**@return The name of the XML element for an item in a column.
	@param column The column for which to return the list item name.
	*/
	protected String getItemElementName(final UIColumn column)
	{
		return itemElementName;
	}
}
