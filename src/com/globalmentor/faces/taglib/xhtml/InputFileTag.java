package com.globalmentor.faces.taglib.xhtml;

import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.globalmentor.faces.component.UIInputFile;
import com.globalmentor.faces.component.renderkit.xhtml.InputFileRenderer;

import static com.globalmentor.faces.component.ComponentConstants.*;
import static com.globalmentor.faces.component.ComponentUtilities.*;
import static com.globalmentor.faces.el.ExpressionUtilities.*;

/**An abstract base class for input component and renderer tags.
@author Garret Wilson
*/
public class InputFileTag extends AbstractInputTag
{

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UIInputFile.COMPONENT_TYPE;}

	/**@return The renderer for this component.*/
	public String getRendererType() {return InputFileRenderer.RENDERER_TYPE;}

	/**The directory in which the file should be stored, if any.*/
	private String directory;

		/**@return The directory in which the file should be stored, if any.*/
		public String getDirectory() {return directory;}

		/**The directory in which the file should be stored, if any.
		@param directory The directory in which the file should be stored, if any.
		*/
		public void setDirectory(final String directory) {this.directory=directory;}

	/**The filename under which the file should be stored, if any.*/
	private String filename;

		/**@return The filename under which the file should be stored, if any.*/
		public String getFilename() {return filename;}

		/**The filename under which the file should be stored, if any.
		@param filename The filename under which the file should be stored, if any.
		*/
		public void setFilename (final String filename) {this.filename=filename;}

	/**Sets the component properties from the tag's attributes.
	@param component The component the properties of which should be set.
	*/
	protected void setProperties(final UIComponent component)
	{
		super.setProperties(component);	//set the default properties
		setFileValue(component, UIInputFile.DIRECTORY_VAR, getDirectory());	//set the directory
		setStringValue(component, UIInputFile.FILENAME_VAR, getFilename());	//set the filename
   }

	/**Release our resources.*/
	public void release()
	{
		super.release();	//release the default resources
		directory=null;
		filename=null;
	}
}
