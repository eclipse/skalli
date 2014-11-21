/*******************************************************************************
 * Copyright (c) 2010-2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
function initForm(formId) {
   var form = document.getElementById(formId);
   var elements = form.elements;
   var defaultValues = new Object();
   for (var i = 0; i < elements.length; i++) {
	 var classname =  elements[i].className;
     if (elements[i].type == "text" && classname.search("inputhelp")>0) {
        defaultValues[i] = elements[i].value;
     }
   }
                  
   form.onsubmit = function(e) {
     for (var i = 0; i < elements.length; i++) {
       var classname =  elements[i].className;
       if (elements[i].type == "text" && classname.search("inputhelp")>0) {
         if (elements[i].value == defaultValues[i]) {
           elements[i].value = "";
         }
       }
     }
   }
}
        
function inputHelp(elementId, defaultValue) {
  var input = document.getElementById(elementId);
  if (input.value == "") {
    input.value = defaultValue;
  }
  if (input.value == defaultValue) {
    addClassName(input, "inputhelp");
  }
    
  input.onfocus = function(e) {
    if (input.value == defaultValue) {
      input.value = "";
      removeClassName(input, "inputhelp");
    }
  }
            
  input.onblur = function(e) {
    if (input.value == "") {
      input.value = defaultValue;
      addClassName(input, "inputhelp");
    }
  }
}
        
function addClassName(input, className) {
  var found = false;
  var newClassNames = '';
  var classNames = input.className.split(' ');

  for (var i = 0; i < classNames.length; i++) {
    if (classNames[i] == className) {
      found = true;
    } else {
      newClassNames += classNames[i] + " ";
    }
  }
  if (!found) {
    newClassNames += className
  }
  input.className = newClassNames;
}
  
function removeClassName(input, className) {
  var found = false;
  var newClassNames = '';
  var classNames = input.className.split(' ');

  for (var i = 0; i < classNames.length; i++) {
    if (classNames[i] == className) {
      found = true;
    } else {
      newClassNames += classNames[i] + " ";
    }
  }
  if (found) {
    input.className = newClassNames;
  }
}
  
function toggleBooleanValue(elementId) {
  var e = document.getElementById(elementId);
  if (e.value == "true") {
    e.value = "false";
  } else if (e.value == "false") {
    e.value = "true";
  }
}