package com.garretwilson.faces.taglib.xhtml;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import com.garretwilson.faces.component.UIDefinitionList;
import com.garretwilson.faces.component.renderkit.xhtml.DLRenderer;

/**A tag for displaying a definition list.
@author Garret Wilson
*/
public class DefinitionListTag extends UIComponentTag
{

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UIDefinitionList.COMPONENT_TYPE;}

	/**@return The default renderer for this component.*/
	public String getRendererType() {return DLRenderer.RENDERER_TYPE;}

   private String value, var;
   public void setValue(String newValue) { value = newValue; }
   public void setVar(String newValue) { var = newValue; }


   protected void setProperties(UIComponent component) {
      // make sure you always call the superclass
      super.setProperties(component);
/*G***fix
if (value != null)
{
if (isValueReference(value))
{
viewer.setValueBinding("rows", app.createValueBinding(rows));
}
else
{
viewer.setRows(Integer.parseInt(rows));
}
}      
*/
      setString(component, "value", value);
      setString(component, "var", var);
   }

   public void release() {
      // always call the superclass method
      super.release();
      value=null;
      var=null;
   }
   public static void setString(UIComponent component, String attributeName,
         String attributeValue) {
      if (attributeValue == null)
         return;
      if (UIComponentTag.isValueReference(attributeValue))
         setValueBinding(component, attributeName, attributeValue);
      else
         component.getAttributes().put(attributeName, attributeValue);
   }

    public static void setValueBinding(UIComponent component, String attributeName,
         String attributeValue) {
      FacesContext context = FacesContext.getCurrentInstance();
      Application app = context.getApplication();
      ValueBinding vb = app.createValueBinding(attributeValue);
      component.setValueBinding(attributeName, vb);
   }

}
