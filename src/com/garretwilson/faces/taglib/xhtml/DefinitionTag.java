package com.garretwilson.faces.taglib.xhtml;

import javax.faces.webapp.UIComponentTag;

import com.garretwilson.faces.component.UIDefinition;
import com.garretwilson.faces.component.UIDefinitionList;

//TODO comment
public class DefinitionTag extends AbstractXHTMLTag {

    public String getRendererType() {
        return null;
    }

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UIDefinition.COMPONENT_TYPE;}

}

