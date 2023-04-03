module com.example.app {
    // require ArcGIS Runtime module
    requires com.esri.arcgisruntime;

    // requires JavaFX modules that the application uses
    requires javafx.graphics;
    requires java.net.http;
    requires javafx.fxml;

    opens DataControllers to javafx.fxml;
    opens classes to javafx.base;
    exports com.example.app;
}