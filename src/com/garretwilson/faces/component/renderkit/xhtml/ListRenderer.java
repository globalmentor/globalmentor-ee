package com.garretwilson.faces.component.renderkit.xhtml;

import javax.faces.component.*;
import com.garretwilson.faces.component.UIList;

import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**The default renderer for an XHTML ordered list
@author Garret Wilson
*/
public class ListRenderer extends DataRenderer
{

	/**The type of renderer for XHTML &lt;ol&gt;.*/
	public static final String RENDERER_TYPE=UIList.COMPONENT_TYPE;

	/**The name of the XML element for the component.*/
//G***del	private final String componentElementName;

		/**@return The name of the XML element for the component.*/
//G***del		protected String getComponentElementName() {return componentElementName;}

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
		super(listElementName);	//construct the parent with the name of the XML list element
//G***del		this.componentElementName=listElementName;
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
