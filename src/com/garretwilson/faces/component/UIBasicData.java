package com.garretwilson.faces.component;

import java.util.*;
import javax.faces.context.*;
import javax.faces.component.*;
import javax.faces.event.*;
import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.ResultDataModel;
import javax.faces.model.ResultSetDataModel;
import javax.faces.model.ScalarDataModel;
import com.garretwilson.faces.el.*;
import com.garretwilson.util.*;

/**Basic data component with enhanced functionality.
This class also creates a work-around for the JSF RI bug that erroneously
	keeps a cached data model when broadcasting commands in nested UIData.
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
	}

	/**Fixes a bug in JSF RI that keeps a cached data data model in nested UIData.*/
	public void broadcast(final FacesEvent event) throws AbortProcessingException
	{
//G***del Debug.trace("ready to broadcast an event: ", event, "component", event.getComponent());
		if(ComponentUtilities.getParent(this, UIData.class)!=null)	//if we're nested within another UIData
		{
			if(getValueBinding("value")!=null)	//if our value depends on a value binding (this won't work if we have a local value, too) TODO use a constant
			{
				setValue(null);	//remove our local value, uncaching the data model in the process
			}
		}
		super.broadcast(event);	//broadcast the event normally, now that we've uncached the data model if needed
	}
	

/*G***del	
	public void broadcast(FacesEvent event)
	{
		Debug.trace("basic data ready to broadcast event:", event);
		super.broadcast(event);
	}
*/

/*G***del
    public void setRowIndex(int rowIndex)
		{
			Debug.trace("basic data setting row index:", rowIndex);
			super.setRowIndex(rowIndex);
Debug.trace("row index set, var:", getVar());
Debug.trace("row data available:", isRowAvailable());
if(isRowAvailable())
{
	Debug.trace("row data is:", getRowData());
}
else
{
	Debug.trace("row data not available; let's find out why.");
	final Object value=getValue();
	Debug.trace("value is: ", value);
//G***del	Debug.trace("data model is:", getDataModel());
	if(value instanceof Object[])
	{
		final Object[] objects=(Object[])value;
		Debug.trace("This is an array of length: ", objects.length);
	}
Map requestMap =getFacesContext().getExternalContext().getRequestMap();
Debug.trace("variable is now:", requestMap.get(getVar()));

		}
*/

}
