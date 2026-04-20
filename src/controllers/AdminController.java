package controllers;

public class AdminController {

    public static long hallCount()     { return HallController.count();     }
    public static long serviceCount()  { return ServiceController.count();  }
    public static long bookingCount()  { return BookingController.count();  }
    public static long customerCount() { return CustomerController.count(); }
}