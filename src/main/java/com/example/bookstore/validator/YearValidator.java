package com.example.bookstore.validator;

import java.util.Calendar;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("yearValidator")
public class YearValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return; // Let required=true handle empty values
        }
        
        try {
            int year = Integer.parseInt(value.toString());
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            
            if (year < 1000 || year > currentYear) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Publication year must be between 1000 and " + currentYear, null));
            }
        } catch (NumberFormatException e) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Please enter a valid year", null));
        }
    }
}
