package com.garretwilson.faces.component.renderkit.xhtml;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.*;
import javax.faces.context.*;

import static com.garretwilson.faces.taglib.xhtml.XHTMLTagConstants.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**Abstract class providing basic rendering functionality for lists.
<p>Inspired by com.sun.faces.renderkit.html_basic.TableRenderer.java,v 1.23 2004/05/12 18:30:41 ofung</p>
*/
public abstract class AbstractListRenderer extends AbstractXHTMLRenderer
{
	/**@return The name of the XML element for the list.*/
	protected abstract String getListElementName();

	/**@return The name of the XML element for an item in a column.
	@param column The column for which to return the list item name.
	*/
	protected abstract String getItemElementName(final UIColumn column);

	/**@return <code>true</code>, as a list renderer renders its children.*/
	public boolean getRendersChildren()
	{
		return true;
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
		if(component.isRendered())	//if the component should be rendered
		{
			final UIData data=(UIData) component;	//get the component as a data component
			data.setRowIndex(-1);	//start before the beginning of the data
			final ResponseWriter writer=context.getResponseWriter();	//get the response writer
			writer.startElement(getListElementName(), data);	//write the starting tag
			writeIDAttribute(context, writer, data);	//write the ID attribute
			final String styleClass=(String)data.getAttributes().get(STYLE_CLASS_ATTRIBUTE);	//see if the component has a style class attribute
			if(styleClass!=null)	//if there is a style class attribute
			{
				writer.writeAttribute(ATTRIBUTE_CLASS, styleClass, STYLE_CLASS_ATTRIBUTE);	//write the style class attribute
			}
//TODO fix        Util.renderPassThruAttributes(writer, component, new String[]{"rows"});
			writer.write('\n');	//create a newline after the start of the list component
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
			final UIData data=(UIData) component;	//get the component as a data component
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
							final UIColumn column=(UIColumn)child;	//get the child as a column
							if(column.isRendered())	//if the column is rendered
							{
				        writer.write('\t');	//write a tab before the list
								final String itemElementName=getItemElementName(column);	//get the element name for this item
								assert itemElementName!=null : "Missing element name for list column item.";
								writer.startElement(itemElementName, column);	//start the element for the item
	            	encodeTree(context, column);	//encode this column and all its descendants 
								writer.endElement(itemElementName);	//end the element for the item
				        writer.write('\n');	//write a newline after the column
	            }
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
			final UIData data=(UIData) component;	//get the component as a data component
			final ResponseWriter writer=context.getResponseWriter();	//get the response writer
			data.setRowIndex(-1);	//make sure we note that we're not using the data anymore
			writer.endElement(getListElementName());	//write the ending tag
			writer.write('\n');	//write a newline after the list
    }
	}

}
