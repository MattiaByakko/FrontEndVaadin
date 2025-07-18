package poc.insurance.base.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import poc.insurance.KeycloakAdminClient;

import java.util.concurrent.CompletableFuture;

@Route("")
@PermitAll
public class MainView extends Div {

    private static final Logger logger = LoggerFactory.getLogger(MainView.class);

    private final KeycloakAdminClient keycloakAdminClient;

    @Autowired
    public MainView(KeycloakAdminClient keycloakAdminClient) {
        this.keycloakAdminClient = keycloakAdminClient;

        addClassName(LumoUtility.Padding.MEDIUM);
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) auth.getPrincipal();
            String username = user.getPreferredUsername();

            layout.add(new Span("Benvenuto, " + username + "!"));

            Button logout = new Button("Logout", e -> {
                // Termina sessione Keycloak in background (non blocca UI)
                //CompletableFuture.runAsync(() -> {
                try {
                    String userId = keycloakAdminClient.getUserIdByUsername(username);
                    keycloakAdminClient.terminateUserSessions(userId);
                    // Invalidate Spring Security context and HTTP session
                    HttpServletRequest request = ((VaadinServletRequest) VaadinSession.getCurrent().getRequest()).getHttpServletRequest();
                    HttpSession httpSession = request.getSession(false);
                    if (httpSession != null) {
                        httpSession.invalidate();  // invalidates both Spring Security session and HTTP session
                    }

                    // Clear Spring Security context
                    SecurityContextHolder.clearContext();

                    // Invalidate Vaadin session
                    VaadinSession.getCurrent().close();
                    logger.info("Sessione Keycloak terminata per utente: " + username);
                } catch (Exception ex) {
                    logger.error("Errore nel logout Keycloak", ex);
                }
                //  });

                // Redirect semplice a /logout gestito da Spring Security
                UI.getCurrent().getPage().setLocation("/logout");
            });

            layout.add(logout);

        } else {
            layout.add(new Span("Non sei autenticato."));
            Button login = new Button("Login", e ->
                    UI.getCurrent().getPage().setLocation("/oauth2/authorization/myclient")
            );
            layout.add(login);
        }

        layout.add(new Div("Please select a view from the menu on the left."));
        add(layout);
    }
}
