package com.garretwilson.faces.component.renderkit.xhtml;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.*;
import javax.faces.context.*;

import static com.garretwilson.faces.component.ComponentUtilities.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**Renders a sequence of objects contained in a <code>UIData</code> component.
This class can function on its own or provide base functionality for more
	sophisticated data rendering
This version creates a span of bare objects.
@author Garret Wilson
<p>Inspired by com.sun.faces.renderkit.html_basic.TableRenderer.java,v 1.23 2004/05/12 18:30:41 ofung</p>
*/
public class DataRenderer extends AbstractXHTMLRenderer
{

	/**@return The name of the XML element for the component.*/
//G***del	protected String getComponentElementName() {return ELEMENT_SPAN;}

	/**Determines the name of the XML element for an item in a column.
	This version defaults to not providing an element for item, writing the
		bare contents.
	@param column The column for which to return the list item name.
	@return The name of the XML element for an item in a column, or
		<code>null</code> if no element should be rendered for items in this column.
	*/
	protected String getItemElementName(final UIColumn column)
	{
		return null;
	}

	/**@return <code>true</code>, as a list renderer renders its children.*/
	public boolean getRendersChildren()
	{
		return true;
	}

	/**Default constructor.*/
	public DataRenderer()
	{
		this(ELEMENT_SPAN);	//create a span element by default
	}

	/**XML element name constructor.
	@param componentElementName The name of the XML element to render.
	*/
	public DataRenderer(final String componentElementName)
	{
		super(componentElementName);	//construct the parent class
		getPassthroughAttributeSet().remove("rows");	//don't pass through the rows attribute TODO use a constant
	}

	/**Begins encoding the list.
	@param context The JSF context.
	@param component The component being rendered.
	@exception IOException Thrown if there is an error writing the output.
	@exception NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void encodeBegin(final FacesContext context, final UIComponent component) throws IOException
	{
		super.encodeBegin(context, component);	//do the default encoding
		if(component.isRendered())	//if the component should be rendered
		{
			final UIData data=(UIData) component;	//get the component as a data component
			data.setRowIndex(-1);	//start before the beginning of the data
			final ResponseWriter writer=context.getResponseWriter();	//get the response writer
//TODO fix        Util.renderPassThruAttributes(writer, component, new String[]{"rows"});
			writer.write('\n');	//create a newline after the starting tag
		}
	}


	/**Encodes the children of the list.
	@param context The JSF context.
	@param component The component being rendered.
	@exception IOException Thrown if there is an error writing the output.
	@exception NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void encodeChildren(final FacesContext context, final UIComponent component) throws IOException
	{
		if(component.isRendered())	//if the component should be rendered
		{
			final UIData data=(UIData)component;	//get the component as a data component
			final ResponseWriter writer=context.getResponseWriter();	//get the response writer
			int rowsProcessed=0;	//the number of rows processed
			int rowIndex=data.getFirst()-1;	//the current row index
			int rowCount=data.getRows();	//the number of rows
			while(rowCount==0 || rowsProcessed<rowCount)	//if we should process all rows, or we should only process some rows and we haven't processed them all
			{
				data.setRowIndex(++rowIndex);	//go to the next row and select it
				if(data.isRowAvailable())	//if this row is available
				{
					final Iterator childIterator=component.getChildren().iterator();	//get an iterator to the children
					while(childIterator.hasNext())	//while there are more children
					{
						final UIComponent child=(UIComponent)childIterator.next();	//get the next child
						if(child instanceof UIColumn)	//if the child is a column
						{
							encodeColumn(context, (UIColumn)child);	//encode this column child
						}
					}
				}
				else	//if this row isn't available
				{
					break;	//stop processing rows
				}
				++rowsProcessed;	//show that we processed another row
			}
			data.setRowIndex(-1);	//show that we're not using the data anymore
    }
	}

	/**Encodes an item in a single column.
	@param context The JSF context.
	@param column The column being rendered.
	@exception IOException Thrown if there is an error writing the output.
	@exception NullPointerException Thrown if <var>context</var> or
		<var>column</var> is <code>null</code>.
	*/
	protected void encodeColumn(final FacesContext context, final UIColumn column) throws IOException
	{
		final ResponseWriter writer=context.getResponseWriter();	//get the response writer
		if(column.isRendered())	//if the column is rendered
		{
			writer.write('\t');	//write a tab before the list
			final String itemElementName=getItemElementName(column);	//get the element name for this item
			if(itemElementName!=null)	//if there is an element name for an item in this column
			{
				writer.startElement(itemElementName, column);	//start the element for the item
			}
			encodeTree(column, context);	//encode this column and all its descendants 
			if(itemElementName!=null)	//if there is an element name for an item in this column
			{
				writer.endElement(itemElementName);	//end the element for the item
			}
			writer.writeText("\n", null);	//write a newline after the column
		}		
	}

	/**Encodes the end of the list.
	@param context The JSF context.
	@param component The component being rendered.
	@exception IOException Thrown if there is an error writing the output.
	@exception NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void encodeEnd(final FacesContext context, final UIComponent component) throws IOException
	{
		if(component.isRendered())	//if the component should be rendered
		{
			final UIData data=(UIData)component;	//get the component as a data component
			data.setRowIndex(-1);	//make sure we note that we're not using the data anymore
    }
		super.encodeEnd(context, component);	//do the default encoding
	}

}
