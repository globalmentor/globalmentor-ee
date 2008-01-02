package com.garretwilson.faces.component;

import java.io.File;
import java.net.URI;
import javax.faces.component.UIColumn;

import com.globalmentor.java.ClassUtilities;

/**A component representing an item in a list.
@author Garret Wilson
@see UIList
*/
public class UIItem extends UIColumn
{

	public static final String COMPONENT_TYPE=ClassUtilities.getFullName(UIItem.class, "Item");

	public UIItem()
	{
		super();
	}

}
