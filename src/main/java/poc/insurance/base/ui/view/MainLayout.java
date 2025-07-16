package poc.insurance.base.ui.view;

import poc.insurance.security.CurrentUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.button.Button;


import java.awt.*;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

@Layout
@PermitAll
public final class MainLayout extends AppLayout {

    private final CurrentUser currentUser;
    private final AuthenticationContext authenticationContext;

    public MainLayout(CurrentUser currentUser, AuthenticationContext authenticationContext) {
        this.currentUser = currentUser;
        this.authenticationContext = authenticationContext;
        setPrimarySection(Section.DRAWER);
        addToDrawer(createHeader(), new Scroller(createSideNav()), createUserMenu());
    }

    private Div createHeader() {
        var appLogo = VaadinIcon.CUBES.create();
        appLogo.addClassNames(TextColor.PRIMARY, IconSize.LARGE);

        var appName = new Span("Vaadin App");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);

        var header = new Div(appLogo, appName);
        header.addClassNames(Display.FLEX, Padding.MEDIUM, Gap.MEDIUM, AlignItems.CENTER);
        return header;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.addClassNames(Margin.Horizontal.MEDIUM);
        MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            return new SideNavItem(menuEntry.title(), menuEntry.path(), new Icon(menuEntry.icon()));
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.path());
        }
    }

    private Component createUserMenu() {
        var optionalUser = currentUser.get();

        if (optionalUser.isEmpty()) {
            // Utente non autenticato, mostra bottone Login
            Button loginButton = new Button("Login", event -> {
                UI.getCurrent().getPage().setLocation("/login");
            });
            loginButton.addClassNames(Margin.MEDIUM);
            return loginButton;
        } else {
            // Utente autenticato, mostra menu utente con avatar
            var user = optionalUser.get();

            var avatar = new Avatar(user.getFullName(), user.getPictureUrl());
            avatar.addThemeVariants(AvatarVariant.LUMO_XSMALL);
            avatar.addClassNames(Margin.Right.SMALL);
            avatar.setColorIndex(5);

            var userMenu = new MenuBar();
            userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
            userMenu.addClassNames(Margin.MEDIUM);

            var userMenuItem = userMenu.addItem(avatar);
            userMenuItem.add(user.getFullName());

            if (user.getProfileUrl() != null) {
                userMenuItem.getSubMenu().addItem("View Profile",
                        event -> UI.getCurrent().getPage().open(user.getProfileUrl()));
            }

            userMenuItem.getSubMenu().addItem("Logout", event -> authenticationContext.logout());

            return userMenu;
        }
    }
}
