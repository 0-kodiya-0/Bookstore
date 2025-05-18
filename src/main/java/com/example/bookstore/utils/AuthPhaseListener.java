package com.example.bookstore.utils;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class AuthPhaseListener implements PhaseListener, Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final List<String> PUBLIC_PAGES = Arrays.asList(
            "/index.xhtml",
            "/customers/login.xhtml",
            "/customers/register.xhtml",
            "/error.xhtml",
            "/books/list.xhtml",
            "/authors/list.xhtml",
            "/resources"
    );

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext facesContext = event.getFacesContext();
        String viewId = facesContext.getViewRoot().getViewId();
        
        // Allow public pages to be accessed without login
        for (String publicPage : PUBLIC_PAGES) {
            if (viewId.startsWith(publicPage)) {
                return;
            }
        }
        
        // Check if user is logged in
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        
        if (session == null || session.getAttribute("loggedInCustomer") == null) {
            // Redirect to login page
            try {
                facesContext.getExternalContext()
                    .redirect(((HttpServletRequest) facesContext.getExternalContext()
                    .getRequest()).getContextPath() + "/customers/login.xhtml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        // Not needed for authentication check
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
}
