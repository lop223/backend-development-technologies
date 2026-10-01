package net.lop223.helpdesksystem.controller.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeControllerTest {

    private final HomeController homeController = new HomeController();

    @Test
    @DisplayName("home: повертає ім'я шаблону index")
    void home_always_returnsIndexView() {
        String expectedView = "index";

        String view = homeController.home();

        assertEquals(expectedView, view);
    }
}
