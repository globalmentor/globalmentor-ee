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

package com.globalmentor.faces.taglib;

import java.io.Serializable;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.webapp.UIComponentBodyTag;
import javax.faces.webapp.UIComponentTag;

public class ForEachTag extends UIComponentBodyTag
{
   public String getRendererType() {
      return ForEachTag.class.getPackage().getName()+".ForEach";
   }
   public String getComponentType() {
      return ForEachTag.class.getPackage().getName()+".ForEach";
   }

   private String items, var;
   public void setItems(String newValue) { items = newValue; }
   public void setVar(String newValue) { var = newValue; }


   protected void setProperties(UIComponent component) {
      // make sure you always call the superclass
      super.setProperties(component);
      setString(component, "items", items);
      setString(component, "var", var);
   }

   public void release() {
      // always call the superclass method
      super.release();
      items=null;
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

   public static void setInteger(UIComponent component, 
         String attributeName, String attributeValue) {
      if (attributeValue == null) return;
      if (UIComponentTag.isValueReference(attributeValue))
         setValueBinding(component, attributeName, attributeValue);
      else 
         component.getAttributes().put(attributeName, 
               new Integer(attributeValue));
   }

   public static void setBoolean(UIComponent component, 
         String attributeName, String attributeValue) {
      if (attributeValue == null) return;
      if (UIComponentTag.isValueReference(attributeValue))
         setValueBinding(component, attributeName, attributeValue);
      else 
         component.getAttributes().put(attributeName, 
               new Boolean(attributeValue));
   }
   
   public static void setValueBinding(UIComponent component, String attributeName,
         String attributeValue) {
      FacesContext context = FacesContext.getCurrentInstance();
      Application app = context.getApplication();
      ValueBinding vb = app.createValueBinding(attributeValue);
      component.setValueBinding(attributeName, vb);
   }

   public static void setActionListener(UIComponent component, String attributeValue) {
      setMethodBinding(component, "actionListener", attributeValue,
            new Class[] { ActionEvent.class });      
   }

   public static void setValueChangeListener(UIComponent component, 
         String attributeValue) {
      setMethodBinding(component, "valueChangeListener", attributeValue,
            new Class[] { ValueChangeEvent.class });      
   }

   public static void setValidator(UIComponent component, 
         String attributeValue) {
      setMethodBinding(component, "validator", attributeValue,
            new Class[] { FacesContext.class, UIComponent.class, Object.class });      
   }

   public static void setAction(UIComponent component, String attributeValue) {
      if (attributeValue == null) return;
      if (UIComponentTag.isValueReference(attributeValue)) 
         setMethodBinding(component, "action", attributeValue,
               new Class[] {});
      else {
         FacesContext context = FacesContext.getCurrentInstance();
         Application app = context.getApplication();
         MethodBinding mb = new ActionMethodBinding(attributeValue);
         component.getAttributes().put("action", mb);         
      }
   }
      
   public static void setMethodBinding(UIComponent component, String attributeName,
         String attributeValue, Class[] paramTypes) {
      if (attributeValue == null)
         return;
      if (UIComponentTag.isValueReference(attributeValue)) {
         FacesContext context = FacesContext.getCurrentInstance();
         Application app = context.getApplication();
         MethodBinding mb = app.createMethodBinding(attributeValue, paramTypes);
         component.getAttributes().put(attributeName, mb);
      }
   }     
   
   private static class ActionMethodBinding 
         extends MethodBinding implements Serializable {      
      private String result;
   
      public ActionMethodBinding(String result) { this.result = result; }      
      public Object invoke(FacesContext context, Object params[]) {
         return result;
      }
      public String getExpressionString() { return result; }
      public Class getType(FacesContext context) { return String.class; }
   }

}