package com.garretwilson.faces.component;

import javax.faces.context.*;
import javax.faces.component.*;
import javax.faces.event.*;
import com.garretwilson.faces.el.*;
import com.garretwilson.util.*;

/**Basic data component with enhanced functionality.
@author Garret Wilson
*/
public class UIBasicData extends UIData
{

	/**The attribute for storing the variable name.*/
	public final static String VAR_ATTRIBUTE="var";	//TODO delete this
	
	/**Default constructor.*/
	public UIBasicData()
	{
		super();	//construct the parent class
		setVar(VAR_ATTRIBUTE);	//show which attribute we use for storing the variable name
	}

}
