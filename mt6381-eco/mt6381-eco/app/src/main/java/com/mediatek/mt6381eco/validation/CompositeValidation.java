package com.mediatek.mt6381eco.validation;

import java.util.ArrayList;

public class CompositeValidation {
  private final ArrayList<ViewValidation> viewValidations = new ArrayList<>();
  private String errorMessage;

  public CompositeValidation addValidation(ViewValidation viewValidation) {
    viewValidations.add(viewValidation);
    return this;
  }

  public boolean isValid(){
    this.errorMessage = "";
    for(ViewValidation viewValidation: viewValidations){
      if(!viewValidation.isValid()){
        errorMessage = viewValidation.getErrorMessage();
        return false;
      }
    }
    return true;
  }

  public void clear(){
    viewValidations.clear();
  }

}
