package com.garretwilson.faces.component;

import java.io.*;
import java.util.*;

import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import com.garretwilson.io.*;
import com.garretwilson.faces.context.RequestParametersFacesContextDecorator;
import com.garretwilson.util.*;

import org.apache.commons.fileupload.*;

/**Basic form component with enhanced functionality to process multipart form
	submissions.
@author Garret Wilson
*/
public class UIBasicForm extends UIForm
{

	/**Default constructor.*/
	public UIBasicForm()
	{
		super();	//construct the parent class
	}

	/**Performs the component tree processing required by the
		<em>Apply Request Values</em> phase of the request processing
		lifecycle for all facets of this component, all children of this
    component, and this component itself.
	This version checks to see if multipart content has been sent for this form
		and if so, processes decodes with a special <code>FacesContext</code> with
	 the parameters from that multipart form submission.
	@param context The JSF context for the request being processed.
	@exception NullPointerException if <code>context</code> is <code>null</code>.
	*/
	public void processDecodes(final FacesContext context)
	{
/*G***del
Debug.setDebug(true);
Debug.trace("processing decodes in UIBasicForm");
*/
		FacesContext decodeContext=context;	//we'll either keep the existing context, or wrap it with one that reports extra parameters
		if(context.getExternalContext().getRequest() instanceof HttpServletRequest)	//if this is an HTTP request
		{
			final HttpServletRequest request=(HttpServletRequest)context.getExternalContext().getRequest();	//get the HTTP request
			if(FileUpload.isMultipartContent(request))	//if this is multipart content
			{			
//G***del Debug.trace("is multipart content");
/*G***fix or delete
				final DiskFileUpload diskFileUpload=new DiskFileUpload();	//create a file upload handler
					//create a factory to populate a map with form fields
				final MultipartFormFieldFactory formFieldFactory=new MultipartFormFieldFactory(diskFileUpload.getFileItemFactory());
				diskFileUpload.setFileItemFactory(formFieldFactory);	//use our special factory for creating file items
//G***del				diskFileUpload.setSizeThreshold(Integer.MAX_VALUE);	//don't write anything to the disk
				diskFileUpload.setSizeMax(-1);	//don't reject anything
				try	//try to parse the file items submitted in the request
				{
					diskFileUpload.parseRequest(request);	//parse the request, ignoring the returned list of file items
					final Map<String, String> formFieldMap=formFieldFactory.getFormFieldMap();	//get the populated map of form fields and values
					final String clientID=component.getClientId(context);	//get the ID of the form
					if(formFieldMap.containsKey(clientID))	//if this request was posted from our form
					{
						final Map requestParameterMap=context.getExternalContext().getRequestParameterMap();	//get the request parameter map
						requestParameterMap.putAll(formFieldMap);	//put all the form fields into our request parameter map						
					}
					boolean foundClientID=false;	//start by assuming this is not a submission for our form
*/
				final DiskFileUpload diskFileUpload=new DiskFileUpload();	//create a file upload handler
//G***del				diskFileUpload.setSizeThreshold(Integer.MAX_VALUE);	//don't write anything to the disk
				diskFileUpload.setSizeMax(-1);	//don't reject anything
				try	//try to parse the file items submitted in the request
				{
					final String clientID=getClientId(context);	//get the form ID
					boolean foundClientID=false;	//start by assuming this is not a submission for our form
					final List fileItems=diskFileUpload.parseRequest(request);	//parse the request
						//see if this was a multipart form request for our form
					for(final Object object:fileItems)	//look at each file item
					{
						final FileItem fileItem=(FileItem)object;	//cast the object to a file item
/*G***del
	Debug.trace("looking at a file item", fileItem.getFieldName(), "with name", fileItem.getName(), "searching for our client ID");
	if(fileItem.isFormField())	//G***del
	{
		Debug.trace("looking at form field", fileItem.getFieldName(), "with value", fileItem.getString());
	}
*/
						if(fileItem.isFormField() && clientID.equals(fileItem.getFieldName()))	//if this is a form field for our client ID
						{
//G***del Debug.trace("found our client ID!");
							foundClientID=true;	//show that we found the client ID
							break;	//stop searching for the client ID
						}
					}
					if(foundClientID)	//if this form submission was for us, setup the request parameters
					{
							//create a decorator that allows us to add request parameters
						final RequestParametersFacesContextDecorator requestParametersFacesContextDecorator=new RequestParametersFacesContextDecorator(context);
						for(final Object object:fileItems)	//look at each file item
						{
							final FileItem fileItem=(FileItem)object;	//cast the object to a file item
							if(fileItem.isFormField())	//if this is a form field
							{
									//use this form field as a request item so that the default form renderer can process it
								requestParametersFacesContextDecorator.putRequestParameter(fileItem.getFieldName(), fileItem.getString());
							}
							else	//if this is not a form field, store the file item itself
							{
								requestParametersFacesContextDecorator.putRequestParameter(fileItem.getFieldName(), fileItem);								
							}
						}
						decodeContext=requestParametersFacesContextDecorator;	//use our JSF context decorator when doing the rest of the decoding
					}
				}
				catch(final FileUploadException fileUploadException)	//if there was an error parsing the files
				{
					Debug.warn(fileUploadException);	//just warn about the problem
				}
			}			
		}
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
//G***del 	Debug.trace("ready to create a file item");
			final FileItem fileItem;	//we'll store a file item here to return
			if(isFormField)	//if this is a form field
			{
					//delegate to the default factory
				fileItem=getDefaultFileItemFactory().createItem(fieldName, contentType, isFormField, fileName);
//G***del		Debug.trace("looking at form field", fileItem.getFieldName(), "with value", fileItem.getString());
				getFormFieldMap().put(fileItem.getFieldName(), fileItem.getString());	//store this field in this map
			}
			else	//if this is not a form field
			{
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
