package com.garretwilson.faces.component;

import javax.faces.component.*;

import static com.globalmentor.java.ClassUtilities.*;

/**A component which signifies that the child components should be rendered as distinct header or footer cells.  
@author Garret Wilson
*/
public class UIColumnFacet extends UIComponentBase
{

	/**The component type.*/
	public static final String COMPONENT_TYPE=getFullName(UIColumnFacet.class, "ColumnFacet");

	/**The component family.*/
	public static final String COMPONENT_FAMILY=COMPONENT_TYPE;

  /**@return The family of the component.*/
	public String getFamily() {return COMPONENT_FAMILY;}

	/**Default constructor.*/
	public UIColumnFacet()
	{
		super();	//construct the parent class
    setRendererType(null);	//no renderer is used
	}

}
