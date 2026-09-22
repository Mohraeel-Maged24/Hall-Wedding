package hall_wedding;

import controllers.*;
import javafx.scene.Scene;
import views.AdminDashboardView;
import views.BookingView;
import views.CustomerBookingsView;
import views.CustomerView;
import views.HallView;
import views.LoginView;

import views.ServiceView;
import views.WelcomeView;

public class SceneManager {

    public static void switchTo(String page) {
        Scene scene = switch (page) {
            case "welcome"   -> WelcomeView.build();
            case "login"     -> LoginView.build();
            case "admin"     -> AdminDashboardView.build();
            case "halls"     -> HallView.build();
            case "services"  -> ServiceView.build();
            case "customers" -> CustomerView.build();
            case "bookings"  -> BookingView.build();
            case "my-bookings" -> CustomerBookingsView.build();
            default          -> WelcomeView.build();
        };
        Hall_Wedding.primaryStage.setScene(scene);
    }
}