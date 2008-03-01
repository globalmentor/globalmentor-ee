package com.garretwilson.faces.component;

import java.io.IOException;
import java.net.URI;
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

import static com.garretwilson.faces.component.ComponentUtilities.*;
import com.garretwilson.faces.el.*;
import com.globalmentor.util.*;

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

	/**Updates values based upon the current settings.
	@param context The JSF context.
	*/
	protected void updateValue(final FacesContext context)
	{
	}
	
	/**Prepares the component for encoding.
	@param context The JSF context.
	@throws IOException if there is an error encoding the information.
	*/
	public void encodeBegin(final FacesContext context) throws IOException
	{
		updateValue(context);	//update our value before further processing
		super.encodeBegin(context);	//do the default encoding
	}

	/**Finishes component for encoding.
	@param context The JSF context.
	@throws IOException if there is an error encoding the information.
	*/
	public void encodeEnd(final FacesContext context) throws IOException
	{
		super.encodeEnd(context);	//do the default end encoding
	}

  /**Processes the decoding for this component and children.
	@param context The JSF context.
	*/	 
	public void processDecodes(final FacesContext context)
	{
		updateValue(context);	//update our value before further processing
		super.processDecodes(context);
	}

	/**Broadcasts an event to interested components.
	This version fixes a bug in JSF RI that keeps a cached data data model in nested UIData.
	@param event The event to broadcast.
	@exception AbortProcessingException if processing should not continue.
	*/
	public void broadcast(final FacesEvent event) throws AbortProcessingException
	{
		if(ComponentUtilities.getParent(this, UIData.class)!=null)	//if we're nested within another UIData
		{
			if(getValueBinding("value")!=null)	//if our value depends on a value binding (this won't work if we have a local value, too) TODO use a constant
			{
				setValue(null);	//remove our local value, uncaching the data model in the process
			}
		}
		super.broadcast(event);	//do the default broadcasting
	}
	
}
