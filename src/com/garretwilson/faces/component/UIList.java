package com.garretwilson.faces.component;

import javax.faces.component.UIData;

import com.garretwilson.faces.component.renderkit.xhtml.ListRenderer;
import com.garretwilson.lang.ClassUtilities;

/**A component containing an ordered list.
@author Garret Wilson
*/
public class UIList extends UIBasicData
{
	public static final String COMPONENT_TYPE=ClassUtilities.getFullName(UIList.class, "List");	//TODO we probably don't even need a special list type

	/**Constructor.*/
	public UIList()
	{
		super();	//construct the parent class
		setRendererType(ListRenderer.RENDERER_TYPE);	//default to an XHTML &lt;dl&/gt; renderer
	}
}
