/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.faces.component;

import java.io.*;
import java.util.*;

import javax.activation.MimeTypeParseException;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;

import com.globalmentor.faces.context.RequestParametersFacesContextDecorator;
import com.globalmentor.io.*;
import com.globalmentor.log.Log;
import com.globalmentor.text.xml.xhtml.XHTML;
import com.globalmentor.util.*;

import org.apache.commons.fileupload.*;

import static com.globalmentor.text.xml.xhtml.XHTML.*;

/**Basic form component with enhanced functionality to process multipart form
	submissions.
<p>This class has the following enhancements:</p>
<ul>
	<li>Automatic conversion of multipart form submissions.</li>
	<li>A map of render-scope variables.</li>
</ul>
<p>This component currently does not support two forms on a page with <code>enctype="multipart/form-data".</code></p>
@author Garret Wilson
*/
public class UIBasicForm extends UIForm
{

	/**The ID of the hidden field used to hold the button value.*/
	protected final static String HIDDEN_FIELD_ID="button";

	/**The map of encode-scope variables and values.*/
	private final Map<?, ?> encodeMap=new HashMap(); 
			
		/**@return A mutable <code>Map</code> representing the encode scope
		 	attributes for the current response rendering.
		 */
		public Map<?, ?> getEncodeMap() {return encodeMap;}

		/**Constructs a client ID for the hidden field associated with this button.
		@param context The JSF context.
		@param component The component being rendered.
		@return The client ID of the hidden field that holds this button's value.
		*/
		public String getHiddenFieldClientID(final FacesContext context)
		{
			final String clientID=getClientId(context);	//get the component's client ID
			return clientID+NamingContainer.SEPARATOR_CHAR+HIDDEN_FIELD_ID;	//construct a client ID for the hidden field
		}	
		
		
	/**Default constructor.*/
	public UIBasicForm()
	{
		super();	//construct the parent class
	}

  /**Performs encoding after first initializing the map of encode attributes.
  @param context The JSF context.
  @throws IOException if There is an error encoding the response.
  @throws NullPointerException {@inheritDoc}   
  */ 
	public void encodeBegin(final FacesContext context) throws IOException
	{
  	getEncodeMap().clear();	//make sure the encode map is clear
  	super.encodeBegin(context);	//do the default encoding
			//render the hidden field
		if(isRendered()) {	//if the component should be rendered
			final String hiddenFieldClientID=getHiddenFieldClientID(context);	//get the client ID of the hidden field
			final ResponseWriter writer=context.getResponseWriter();	//get the response writer
			writer.startElement(ELEMENT_INPUT, null);	//<input>
			writer.writeAttribute(ATTRIBUTE_NAME, hiddenFieldClientID, ATTRIBUTE_NAME);	//name="xxx:button"
			writer.writeAttribute(ELEMENT_INPUT_ATTRIBUTE_TYPE, INPUT_TYPE_HIDDEN, ELEMENT_INPUT_ATTRIBUTE_TYPE);	//type="hidden"
			writer.endElement(ELEMENT_INPUT);	//</input>
		}
  }

  /**Finishes encoding and then clears the map of encode attributes.
  @param context The JSF context.
  @throws IOException if There is an error encoding the response.
  @throws NullPointerException {@inheritDoc}   
  */ 
  public void encodeEnd(final FacesContext context) throws IOException
	{
  	super.encodeEnd(context);	//do the default end encoding
  	getEncodeMap().clear();	//reset the encode map
  }

	/**Performs the component tree processing required by the
		<em>Apply Request Values</em> phase of the request processing
		lifecycle for all facets of this component, all children of this
    component, and this component itself.
	This version checks to see if multipart content has been sent for this form
		and if so, processes decodes with a special <code>FacesContext</code> with
	 the parameters from that multipart form submission.
	@param context The JSF context for the request being processed.
	@throws NullPointerException if <code>context</code> is <code>null</code>.
	*/
	public void processDecodes(final FacesContext context)
	{
		FacesContext decodeContext=context;	//we'll either keep the existing context, or wrap it with one that reports extra parameters
		final String enctype=(String)getAttributes().get(ELEMENT_FORM_ATTRIBUTE_ENCTYPE);	//get the enctype
		try
		{
			if(enctype!=null && XHTML.MULTIPART_FORM_DATA_CONTENT_TYPE.match(enctype)) {	//if our form was multipart-encoded, see if this is a multipart submission
				if(context.getExternalContext().getRequest() instanceof HttpServletRequest) {	//if this is an HTTP request
					final HttpServletRequest request=(HttpServletRequest)context.getExternalContext().getRequest();	//get the HTTP request
					if(FileUpload.isMultipartContent(request)) {	//if this is multipart content			
		Log.trace("is multipart content");
		/*TODO fix or delete
						final DiskFileUpload diskFileUpload=new DiskFileUpload();	//create a file upload handler
							//create a factory to populate a map with form fields
						final MultipartFormFieldFactory formFieldFactory=new MultipartFormFieldFactory(diskFileUpload.getFileItemFactory());
						diskFileUpload.setFileItemFactory(formFieldFactory);	//use our special factory for creating file items
		//TODO del				diskFileUpload.setSizeThreshold(Integer.MAX_VALUE);	//don't write anything to the disk
						diskFileUpload.setSizeMax(-1);	//don't reject anything
						try {	//try to parse the file items submitted in the request
							diskFileUpload.parseRequest(request);	//parse the request, ignoring the returned list of file items
							final Map<String, String> formFieldMap=formFieldFactory.getFormFieldMap();	//get the populated map of form fields and values
							final String clientID=component.getClientId(context);	//get the ID of the form
							if(formFieldMap.containsKey(clientID)) {	//if this request was posted from our form
								final Map requestParameterMap=context.getExternalContext().getRequestParameterMap();	//get the request parameter map
								requestParameterMap.putAll(formFieldMap);	//put all the form fields into our request parameter map						
							}
							boolean foundClientID=false;	//start by assuming this is not a submission for our form
		*/
						final DiskFileUpload diskFileUpload=new DiskFileUpload();	//create a file upload handler
		//TODO del				diskFileUpload.setSizeThreshold(Integer.MAX_VALUE);	//don't write anything to the disk
						diskFileUpload.setSizeMax(-1);	//don't reject anything
						try {	//try to parse the file items submitted in the request
							final String clientID=getClientId(context);	//get the form ID
							boolean foundClientID=false;	//start by assuming this is not a submission for our form
							final List fileItems=diskFileUpload.parseRequest(request);	//parse the request
								//see if this was a multipart form request for our form
							for(final Object object:fileItems) {	//look at each file item
								final FileItem fileItem=(FileItem)object;	//cast the object to a file item
	/*TODO del
			Log.trace("looking at a file item", fileItem.getFieldName(), "with name", fileItem.getName(), "searching for our client ID");
			if(fileItem.isFormField()) {	//TODO del
				Log.trace("looking at form field", fileItem.getFieldName(), "with value", fileItem.getString());
			}
	*/
								if(fileItem.isFormField() && clientID.equals(fileItem.getFieldName())) {	//if this is a form field for our client ID
	//TODO del	Log.trace("found our client ID!", clientID);
									foundClientID=true;	//show that we found the client ID
									break;	//stop searching for the client ID
								}
							}
							if(foundClientID) {	//if this form submission was for us, setup the request parameters
									//create a decorator that allows us to add request parameters
								final RequestParametersFacesContextDecorator requestParametersFacesContextDecorator=new RequestParametersFacesContextDecorator(context);
								for(final Object object:fileItems) {	//look at each file item
									final FileItem fileItem=(FileItem)object;	//cast the object to a file item
									if(fileItem.isFormField()) {	//if this is a form field
											//use this form field as a request item so that the default form renderer can process it
										requestParametersFacesContextDecorator.putRequestParameter(fileItem.getFieldName(), fileItem.getString());
									}
									else {	//if this is not a form field, store the file item itself
										requestParametersFacesContextDecorator.putRequestParameter(fileItem.getFieldName(), fileItem);								
									}
								}
								decodeContext=requestParametersFacesContextDecorator;	//use our JSF context decorator when doing the rest of the decoding
							}
						}
						catch(final FileUploadException fileUploadException) {	//if there was an error parsing the files
							Log.warn(fileUploadException);	//just warn about the problem
						}
					}			
				}
			}
		}
		catch(final MimeTypeParseException mimeTypeParseException)
		{
			Log.warn(mimeTypeParseException);	//just warn about the incorrect MIME type format
		}
//TODO del Log.trace("ready to process default form decodes");
			//if this form submision was meant for us, we'll have set up the request parameters with the submitted values
		super.processDecodes(decodeContext);	//do the default decode processing with either the context we received, or the one we wrapped to return our extra parameters
	}

		//TODO delete this class if not needed
	/**A map of form fields that populates itself from a multipart request
		through its <code>FileItemFactory</code> interface.
	@author Garret Wilson
	*/
	private static class MultipartFormFieldFactory extends DefaultFileItemFactory	//TODO don't descend from DefaultFileItemFactory when DiskFileUpload is fixed to not require it
	{
		
		/**The map of form fields and values.*/
		private final Map<String, String> formFieldMap=new HashMap<String, String>();

			/**@return The map of form fields and values.*/
			public Map<String, String> getFormFieldMap() {return formFieldMap;}

		/**The default file item factory for creating form field file items.*/
		private final FileItemFactory defaultFileItemFactory;

			/**@return The default file item factory for creating form field file items.*/
			protected FileItemFactory getDefaultFileItemFactory() {return defaultFileItemFactory;}

		/**Constructs a form field factory that populates a map with form fields.
		@param defaultFileItemFactory The factory to which to delegate for form
			fields.
		*/
		public MultipartFormFieldFactory(final FileItemFactory defaultFileItemFactory)
		{
			this.defaultFileItemFactory=defaultFileItemFactory;	//save the default file item factory
		}

		/**Create a new <code>FileItem</code. instance from the supplied parameters
			and any local factory configuration.
		This version adds form fields to this map using the default file item
			factory, and returns a dummy file item for all other file items.
		@param fieldName The name of the form field.
		@param contentType The content type of the form field.
		@param isFormField <code>true</code> if this is a plain form field;
			<code>false</code> otherwise.
		@param fileName The name of the uploaded file, if any, as supplied by the
			browser or other client.		
		*/
		public FileItem createItem(final String fieldName, final String contentType, final boolean isFormField, final String fileName)
		{
//TODO del 	Log.trace("ready to create a file item");
			final FileItem fileItem;	//we'll store a file item here to return
			if(isFormField) {	//if this is a form field
					//delegate to the default factory
				fileItem=getDefaultFileItemFactory().createItem(fieldName, contentType, isFormField, fileName);
//TODO del		Log.trace("looking at form field", fileItem.getFieldName(), "with value", fileItem.getString());
				getFormFieldMap().put(fileItem.getFieldName(), fileItem.getString());	//store this field in this map
			}
			else {	//if this is not a form field
				fileItem=new DummyFileItem(fieldName, contentType, isFormField, fileName);	//create a dummy file item				
			}
			return fileItem;	//return this file item
		}

		/**A dummy file item that doesn't provide any real functionality.
		@author Garret Wilson
		*/
		private static class DummyFileItem implements FileItem
		{
			private String fieldName;
			private final String contentType;
			private boolean isFormField;
			private final String fileName;

			/**Creates a dummy file item that can only return its simple properties.
			@param fieldName The name of the form field.
			@param contentType The content type of the form field.
			@param isFormField <code>true</code> if this is a plain form field;
				<code>false</code> otherwise.
			@param fileName The name of the uploaded file, if any, as supplied by the
				browser or other client.		
			*/
			public DummyFileItem(final String fieldName, final String contentType, final boolean isFormField, final String fileName)
			{
				this.fieldName=fieldName;
				this.contentType=contentType;
				this.isFormField=isFormField;
				this.fileName=fileName;
			}
			
			public void delete() {throw new UnsupportedOperationException();}
			public byte[] get() {throw new UnsupportedOperationException();}

			public String getContentType() {return contentType;}
			public String getFieldName() {return fieldName;}
			public InputStream getInputStream() {throw new UnsupportedOperationException();}
			public String getName() {return fileName;}

			/**@return A null output stream that throws away all information.*/
			public OutputStream getOutputStream() {return new NullOutputStream();}

			public long getSize() {throw new UnsupportedOperationException();}
			public String getString() {throw new UnsupportedOperationException();}
			public String getString(java.lang.String encoding) {throw new UnsupportedOperationException();}
			public boolean isFormField() {return isFormField;}
			public boolean isInMemory() {return true;}
			public void setFieldName(final String name) {fieldName=name;}
			public void setFormField(final boolean state) {this.isFormField=state;}
			public void write(final File file) {}
		}
		
	}

}
