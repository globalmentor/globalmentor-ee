package com.garretwilson.faces.taglib.xhtml;

import javax.faces.webapp.UIComponentTag;

import com.garretwilson.faces.component.UIDefinition;
import com.garretwilson.faces.component.UITerm;

//TODO comment
public class TermTag extends UIComponentTag {

    public String getRendererType() {
        return null;
    }

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UITerm.COMPONENT_TYPE;}

}

