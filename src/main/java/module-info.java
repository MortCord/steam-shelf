module com.mort.shelflauncher.steamshelflauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;


    opens com.mort.shelflauncher.steamshelflauncher to javafx.fxml;
    exports com.mort.shelflauncher.steamshelflauncher;
}