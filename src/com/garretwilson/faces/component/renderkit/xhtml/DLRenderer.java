package com.garretwilson.faces.component.renderkit.xhtml;

import com.garretwilson.faces.component.UIDefinition;
import com.garretwilson.faces.component.UIDefinitionList;
import com.garretwilson.faces.component.UITerm;
import com.garretwilson.util.Debug;
import com.globalmentor.marmot.faces.UIBurrowTable;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Iterator;

/**
 * <p>Render a {@link UIData} component as a two-dimensional table.</p>
<p>Inspired by com.sun.faces.renderkit.html_basic.TableRenderer.java,v 1.23 2004/05/12 18:30:41 ofung</p>
 */

public class DLRenderer extends AbstractXHTMLRenderer
{

	/**The type of renderer for XHTML &lt;dl&gt;.*/
	public static final String RENDERER_TYPE=UIDefinitionList.COMPONENT_TYPE;

    public boolean getRendersChildren()
    {
        return true;
    }


    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {

/*G***replace with assertions
        if ((context == null) || (component == null)) {
            throw new NullPointerException(Util.getExceptionMessageString(
                Util.NULL_PARAMETERS_ERROR_MESSAGE_ID));
        }
        if (log.isTraceEnabled()) {
            log.trace("Begin encoding component " + component.getId());
        }
*/

        // suppress rendering if "rendered" property on the component is
        // false.
        if (!component.isRendered()) {
            return;
        }
        UIData data = (UIData) component;
        data.setRowIndex(-1);

        // Render the beginning of the table
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("dl", data);
        writeIDAttribute(context, writer, component);
        String styleClass = (String) data.getAttributes().get("styleClass");	//TODO use a constant
        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }
//TODO fix        Util.renderPassThruAttributes(writer, component, new String[]{"rows"});
        writer.writeText("\n", null);

    }


    public void encodeChildren(FacesContext context, UIComponent component)
        throws IOException {
/*G**use assertions
        if ((context == null) || (component == null)) {
            throw new NullPointerException(Util.getExceptionMessageString(
                Util.NULL_PARAMETERS_ERROR_MESSAGE_ID));
        }
*/
        if (!component.isRendered()) {
            return;
        }
        UIData data = (UIData) component;

        // Set up variables we will need
/*
        String columnClasses[] = getColumnClasses(data);
        int columnStyle = 0;
        int columnStyles = columnClasses.length;
        String rowClasses[] = getRowClasses(data);
        int rowStyles = rowClasses.length;
*/
        ResponseWriter writer = context.getResponseWriter();
        Iterator kids = null;
        Iterator grandkids = null;

        // Iterate over the rows of data that are provided
        int processed = 0;
        int rowIndex = data.getFirst() - 1;
        int rows = data.getRows();
        int rowStyle = 0;

        while (true) {

            // Have we displayed the requested number of rows?
            if ((rows > 0) && (++processed > rows)) {
                break;
            }
            // Select the current row
            data.setRowIndex(++rowIndex);
            if (!data.isRowAvailable()) {
                break; // Scrolled past the last row
            }
/*G***del if not needed
            // Render the beginning of this row
            writer.startElement("tr", data);
            if (rowStyles > 0) {
                writer.writeAttribute("class", rowClasses[rowStyle++],
                                      "rowClasses");
                if (rowStyle >= rowStyles) {
                    rowStyle = 0;
                }
            }
            writer.writeText("\n", null);

            // Iterate over the child UIColumn components for each row
            columnStyle = 0;
*/

	   		Iterator children = component.getChildren().iterator();
	      while (children.hasNext())
	      {
	         UIComponent child = (UIComponent) children.next();
	            if (child.isRendered())
	            {
	            	final String definitionTag;
	            	if(child instanceof UITerm)
	            	{
	            		definitionTag=ELEMENT_DT;
	            	}
	            	else if(child instanceof UIDefinition)
	            	{
	            		definitionTag=ELEMENT_DD;
	            	}
	            	else
	            	{
	            		definitionTag=null;
	            	}
	            	if(definitionTag!=null)
	            	{
	            		writer.startElement(definitionTag, child);
	            	}
	            	encodeTree(context, child);	//encode this child and all its descendants 
	            	if(definitionTag!=null)
	            	{
	            		writer.endElement(definitionTag);
	            	}
	            }
	      	}
        }
/*G***fix or del            
            kids = getColumns(data);
            while (kids.hasNext()) {

                // Identify the next renderable column
                UIColumn column = (UIColumn) kids.next();

                // Render the beginning of this cell
                writer.startElement("td", column);
                if (columnStyles > 0) {
                    writer.writeAttribute("class", columnClasses[columnStyle++],
                                          "columnClasses");
                    if (columnStyle >= columnStyles) {
                        columnStyle = 0;
                    }
                }

                // Render the contents of this cell by iterating over
                // the kids of our kids
                grandkids = getChildren(column);
                while (grandkids.hasNext()) {
                    encodeRecursive(context, (UIComponent) grandkids.next());
                }

                // Render the ending of this cell
                writer.endElement("td");
                writer.writeText("\n", null);

            }

            // Render the ending of this row
            writer.endElement("tr");
            writer.writeText("\n", null);

        }
        writer.endElement("tbody");
*/
        writer.writeText("\n", null);

        // Clean up after ourselves
        data.setRowIndex(-1);
    }


    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {
/*G***replace with assertions
        if ((context == null) || (component == null)) {
            throw new NullPointerException(Util.getExceptionMessageString(
                Util.NULL_PARAMETERS_ERROR_MESSAGE_ID));
        }
        if (!component.isRendered()) {
            if (log.isTraceEnabled()) {
                log.trace("No encoding necessary " +
                          component.getId() + " since " +
                          "rendered attribute is set to false ");
            }
            return;
        }
*/
        UIData data = (UIData) component;
        data.setRowIndex(-1);
        ResponseWriter writer = context.getResponseWriter();

        // Render the ending of this table
        writer.endElement("dl");
        writer.writeText("\n", null);

    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Return an array of stylesheet classes to be applied to
     * each column in the table in the order specified. Every column may or
     * may not have a stylesheet.</p>
     *
     * @param data {@link UIData} component being rendered
     */
/*G***fix if needed
    private String[] getColumnClasses(UIData data) {

        String values = (String) data.getAttributes().get("columnClasses");
        if (values == null) {
            return (new String[0]);
        }
        values = values.trim();
        ArrayList list = new ArrayList();
        while (values.length() > 0) {
            int comma = values.indexOf(",");
            if (comma >= 0) {
                list.add(values.substring(0, comma).trim());
                values = values.substring(comma + 1);
            } else {
                list.add(values.trim());
                values = "";
            }
        }
        String results[] = new String[list.size()];
        return ((String[]) list.toArray(results));

    }
    */


    /**
     * <p>Return the number of child <code>UIColumn</code> components
     * that are nested in the specified {@link UIData}.</p>
     *
     * @param data {@link UIData} component being analyzed
     */
/*G***del if not needed
    private int getColumnCount(UIData data) {

        int columns = 0;
        Iterator kids = getColumns(data);
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            columns++;
        }
        return (columns);

    }


    /**
     * <p>Return an Iterator over the <code>UIColumn</code> children
     * of the specified <code>UIData</code> that have a
     * <code>rendered</code> property of <code>true</code>.</p>
     *
     * @param data <code>UIData</code> for which to extract children
     */
/*G***del if not needed
    private Iterator getColumns(UIData data) {

        List results = new ArrayList();
        Iterator kids = data.getChildren().iterator();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            if ((kid instanceof UIColumn) && kid.isRendered()) {
                results.add(kid);
            }
        }
        return (results.iterator());

    }
*/


    /**
     * <p>Return an array of stylesheet classes to be applied to
     * each row in the table, in the order specified.  Every row may or
     * may not have a stylesheet.</p>
     *
     * @param data {@link UIData} component being rendered
     */
/*G***fix if needed
    private String[] getRowClasses(UIData data) {

        String values = (String) data.getAttributes().get("rowClasses");
        if (values == null) {
            return (new String[0]);
        }
        values = values.trim();
        ArrayList list = new ArrayList();
        while (values.length() > 0) {
            int comma = values.indexOf(",");
            if (comma >= 0) {
                list.add(values.substring(0, comma).trim());
                values = values.substring(comma + 1);
            } else {
                list.add(values.trim());
                values = "";
            }
        }
        String results[] = new String[list.size()];
        return ((String[]) list.toArray(results));

    }
*/


}
