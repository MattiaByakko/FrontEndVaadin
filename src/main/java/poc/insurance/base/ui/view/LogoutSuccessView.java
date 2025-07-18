package poc.insurance.base.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.UI;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route("logout-success")
@PageTitle("Logout")
public class LogoutSuccessView extends Div {

    public LogoutSuccessView() {
        add(new Span("Logout avvenuto con successo!"));

        Button loginButton = new Button("Torna al login", e ->
                UI.getCurrent().getPage().setLocation("/oauth2/authorization/myclient")
        );

        add(loginButton);
    }
}
