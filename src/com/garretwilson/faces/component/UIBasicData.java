package com.garretwilson.faces.component;

import javax.faces.component.UIData;
import com.garretwilson.faces.el.*;

/**Basic data component with enhanced functionality.
@author Garret Wilson
*/
public class UIBasicData extends UIData
{

	/**The attribute for storing the variable name.*/
	public final static String VAR_ATTRIBUTE="var";
	
	/**Default constructor.*/
	public UIBasicData()
	{
		super();	//construct the parent class
		setVar(VAR_ATTRIBUTE);	//show which attribute we use for storing the variable name
	}

}
