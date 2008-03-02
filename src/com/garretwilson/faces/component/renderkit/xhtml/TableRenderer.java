package com.garretwilson.faces.component.renderkit.xhtml;

import java.io.IOException;

import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.garretwilson.faces.component.UIColumnFacet;
import static com.garretwilson.faces.component.ComponentUtilities.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;

/**The renderer for an XHTML table based upon a <code>UIData</code> component.
@author Garret Wilson
*/
public class TableRenderer extends DataRenderer
{

	/**Default constructor.*/
	public TableRenderer()
	{
		super(ELEMENT_TABLE);	//default to the <table> element for the table
	}

	/**Determines the name of the XML element for a row in a column.
	This version returns <code>tr</code>.
	@param rowIndex The zero-based index of the row.
	@return The name of the XML element for a row in a column, or
		<code>null</code> if no element should be rendered for a row in this column.
	*/
	protected String getRowElementName(final int rowIndex)
	{
		return ELEMENT_TR;
	}

	/**Determines the name of the XML element for an item in a column.
	This version returns <code>ELEMENT_TD</code>.
	@param column The column for which to return the list item name.
	@return The name of the XML element for an item in a column, or
		<code>null</code> if no element should be rendered for items in this column.
	*/
	protected String getItemElementName(final UIColumn column)
	{
		return ELEMENT_TD;
	}

	/**We'll keep track of whether we've started the header.*/
	private boolean isHeaderStarted=false;

	/**Begins encoding the data.
	This version additionally renders any headers or footers.
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
			final ResponseWriter writer=context.getResponseWriter();	//get the response writer
			int columnCount=0;	//we'll determine how many columns there are so that we can create a table header and/or footer
			int columnHeaderCount=0;	//we'll determine how many column headers there are
			int columnMultipleHeaderCount=0;	//we'll determine how many column headers there are containing multiple headers
			int columnFooterCount=0;	//we'll determine how many column footers there are
			int columnMultipleFooterCount=0;	//we'll determine how many column headers there are containing multiple footers
			for(final Object child:component.getChildren())	//look at each child
			{
				if(child instanceof UIColumn)	//if this is a column
				{
					final UIColumn column=(UIColumn)child;	//cast the column to a child
					if(column.isRendered())	//if the column is rendered
					{
						++columnCount;	//show that we found another rendered column
						final UIComponent header=column.getHeader();	//get the column header
						if(header!=null && header.isRendered())	//if the header is rendered
						{
							++columnHeaderCount;	//show that we found another rendered column header
							if(header instanceof UIColumnFacet)	//if this is a special header containing multiple headers
							{
								++columnMultipleHeaderCount;	//show that we've found another multiple header holder
							}
						}
						final UIComponent footer=column.getHeader();	//get the column footer
						if(footer!=null && footer.isRendered())	//if the footer is rendered
						{
							++columnFooterCount;	//show that we found another rendered column footer
							if(footer instanceof UIColumnFacet && footer.getChildCount()>0)	//if this is a special footer containing multiple footers
							{
								++columnMultipleFooterCount;	//show that we've found another multiple header holder
							}
						}
					}
				}
			}
//G***del			boolean isHeaderStarted=false;	//show that we haven't yet started the header
			final UIComponent tableHeader=data.getHeader();	//get the table header, if there is one
				//if we have a table header or at least one column header
			if((tableHeader!=null && tableHeader.isRendered()) || columnHeaderCount>0)
			{
				writer.startElement(ELEMENT_THEAD, component);	//start the element for the header
					//table header
				if(tableHeader!=null && tableHeader.isRendered())	//if we have a rendered header
				{
					writer.startElement(ELEMENT_TR, tableHeader);	//<tr>
					encodeHeaderCell(context, writer, tableHeader, columnCount);	//encode the table header
					writer.endElement(ELEMENT_TR);	//<tr>
					writer.write('\n');	//create a newline after the row
				}
					//column headers
				if(columnHeaderCount>0)	//if we have any column headers to render
				{
					writer.startElement(ELEMENT_TR, component);	//<tr>
					for(final Object child:component.getChildren())	//look at each child
					{
						if(child instanceof UIColumn)	//if this is a column
						{
							final UIColumn column=(UIColumn)child;	//cast the column to a child
							if(column.isRendered())	//if the column is rendered
							{
								final UIComponent header=column.getHeader();	//get the column header
								if(header!=null)	//if there is a header
								{
									encodeHeaderCell(context, writer, header, 1);	//encode the column header
								}
							}
						}
					}
					writer.endElement(ELEMENT_TR);	//</tr>
					writer.write('\n');	//create a newline after the row
				}
				writer.endElement(ELEMENT_THEAD);	//end the element for the header
			}
			final UIComponent tableFooter=data.getFooter();	//get the table footer, if there is one
				//if we have a table footer or at least one column footer
			if((tableFooter!=null && tableFooter.isRendered()) || columnFooterCount>0)
			{
				writer.startElement(ELEMENT_TFOOT, component);	//start the element for the footer
					//column footers
				if(columnFooterCount>0)	//if we have any column footers to render
				{
					int row=0;	//keep count of what row we're on, with -1 representing the normal footer row
					boolean multipleFootersRemain=columnMultipleFooterCount>0;	//see if there are multiple footers as we're starting
					do
					{
						if(!multipleFootersRemain)	//if we don't have multiple footers to encode
						{
							row=-1;	//encode the normal footers
						}
						multipleFootersRemain=false;	//see if there are multiple footers remainining on this row
						writer.startElement(ELEMENT_TR, component);	//<tr>
						for(final Object child:component.getChildren())	//look at each child
						{
							if(child instanceof UIColumn)	//if this is a column
							{
								final UIColumn column=(UIColumn)child;	//cast the column to a child
								if(column.isRendered())	//if the column is rendered
								{
									final UIComponent footer=column.getFooter();	//get the column footer
									if(footer!=null)	//if there is a footer
									{
										if(row>=0)	//if we're encoding the special footers
										{
											if(footer instanceof UIColumnFacet && footer.getChildCount()>row)	//if this is a special footer with enough children
											{
												encodeFooterCell(context, writer, (UIComponent)footer.getChildren().get(row), 1);	//encode the appropriate footer child
												if(footer.getChildCount()>row+1)	//if there are enough footers for next time
												{
													multipleFootersRemain=true;	//show that we still have multiple footers to be encoded
												}
											}
											else	//if this isn't a special footer, or it doesn't have enough children, just write a placeholder
											{
												writer.startElement(ELEMENT_TD, column);	//<td>
												writer.endElement(ELEMENT_TD);	//</td>												
											}
										}
										else	//if we're encoding the normal footers
										{
											encodeFooterCell(context, writer, footer, 1);	//encode the column footer
										}
									}
									else	//if there is no footer, write a placeholder
									{
										writer.startElement(ELEMENT_TD, column);	//<td>
										writer.endElement(ELEMENT_TD);	//</td>												
									}
								}
							}
						}
						writer.endElement(ELEMENT_TR);	//</tr>
						writer.write('\n');	//create a newline after the row
						if(row>=0)	//if we were encoding the special rows
						{
							++row;	//go to the next row
						}
					}
					while(row>=0);	//keep going until we render the normal footer row 
				}
					//table footer
				if(tableFooter!=null && tableFooter.isRendered())	//if we have a rendered header
				{
					writer.startElement(ELEMENT_TR, tableFooter);	//<tr>
					encodeFooterCell(context, writer, tableFooter, columnCount);	//encode the table footer
					writer.endElement(ELEMENT_TR);	//<tr>
				}
				writer.endElement(ELEMENT_TFOOT);	//end the element for the header
			}
		}
	}

	/**Encodes the children of the data.
	This version wraps the table in a <code>tbody</code> element.
	@param context The JSF context.
	@param component The component being rendered.
	@exception IOException Thrown if there is an error writing the output.
	@exception NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void encodeChildren(final FacesContext context, final UIComponent component) throws IOException
	{
		final ResponseWriter writer=context.getResponseWriter();	//get the response writer
		if(component.isRendered())	//if the component should be rendered
		{
			writer.startElement(ELEMENT_TBODY, component);	//start the body of the table
		}
		super.encodeChildren(context, component);	//do the default child encoding
		if(component.isRendered())	//if the component should be rendered
		{
			writer.endElement(ELEMENT_TBODY);	//end the body of the table
		}
	}
	
	/**Encodes a header cell.
	@param context The JSF context.
	@param writer The response writer used to write output.
	@param facet The header or footer being rendered.
	@param columnCount The number of rendered columns for the column to cover.
		If <code>1</code> is specified, no attribute is given.
	@exception IOException Thrown if there is an error writing the output.
	*/
	protected void encodeHeaderCell(final FacesContext context, final ResponseWriter writer, final UIComponent facet, final int columnCount) throws IOException
	{
		encodeCell(context, writer, facet, ELEMENT_TH, columnCount);	//encode a header cell
	}

	/**Encodes a footer cell.
	@param context The JSF context.
	@param writer The response writer used to write output.
	@param facet The header or footer being rendered.
	@param columnCount The number of rendered columns for the column to cover.
		If <code>1</code> is specified, no attribute is given.
	@exception IOException Thrown if there is an error writing the output.
	*/
	protected void encodeFooterCell(final FacesContext context, final ResponseWriter writer, final UIComponent facet, final int columnCount) throws IOException
	{
		encodeCell(context, writer, facet, ELEMENT_TD, columnCount);	//encode a footer cell
	}

	/**Encodes a header or footer cell.
	@param context The JSF context.
	@param writer The response writer used to write output.
	@param facet The header or footer being rendered.
	@param elementName The XML element name to use for the cell. 
	@param columnCount The number of rendered columns for the column to cover.
		If <code>1</code> is specified, no attribute is given.
	@exception IOException Thrown if there is an error writing the output.
	*/
	protected void encodeCell(final FacesContext context, final ResponseWriter writer, final UIComponent facet, final String elementName, final int columnCount) throws IOException
	{
		if(facet.isRendered())	//if this facet is rendered
		{
			writer.startElement(elementName, facet);	//<th>
			if(columnCount>1)	//if more than one column is called for
			{
		    writer.writeAttribute(ELEMENT_TD_ATTRIBUTE_COLSPAN, String.valueOf(columnCount), null);	//colspan="columnCount"
		    writer.writeAttribute(ELEMENT_TD_ATTRIBUTE_SCOPE, TD_SCOPE_COLGROUP, null);	//scope="colgroup" G***do we always want this, even for multicolumn headers that aren't table headers (if there is such a thing)?
			}
			//TODO render the style class
			encodeTree(facet, context);	//encode this facet and all its descendants 
			writer.endElement(elementName);	//</th>
		}
	}

}
