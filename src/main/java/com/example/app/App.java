package com.example.app;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private MapView mapView;

    public static void main(String[] args){
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // set the title and size of the stage and show it
        stage.setTitle("Display a map tutorial");
        stage.setWidth(800);
        stage.setHeight(700);
        stage.show();

        //Creates the tab pane that will hold all the
        TabPane tabPane = new TabPane();
        //makes it, so you can't close any of the tabs
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        //Creating the different tabs, additional tabs can be created here
        Tab propertyTab = new Tab("Properties");
        Tab businessTab = new Tab("Businesses");
        Tab mapTab = new Tab("Map");

        // Note: it is not best practice to store API keys in source code.
        // The API key is referenced here for the convenience of this tutorial.
        String yourApiKey = "AAPKb0e9a3e549174ef8b1ce1861e64e1a0eizDvf5tYBwfvTV8edXmx8nTtNihwNBr014H8zVeUJvSRI4Ct0WMMsdNuVcjOGfEW";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        StackPane stackPane = new StackPane();

        // create a map view to display the map and add it to the stack pane
        mapView = new MapView();
        stackPane.getChildren().add(mapView);

        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);

        // set the map on the map view
        mapView.setMap(map);             //Currently Macewan University
        mapView.setViewpoint(new Viewpoint(53.547, -113.51, 144447.638572));
        mapTab.setContent(stackPane);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/PropertyAsessmentsDataView.fxml"));
        propertyTab.setContent(fxmlLoader.load());
        fxmlLoader = new FXMLLoader(getClass().getResource("/BusinessesDataView.fxml"));
        businessTab.setContent(fxmlLoader.load());

        // create a JavaFX scene with a stack pane as the root node, and add it to the scene
        tabPane.getTabs().addAll(propertyTab, businessTab, mapTab);
        Scene scene = new Scene(tabPane, 720, 640);
        stage.setScene(scene);
    }
    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() {
        if (mapView != null) {
            mapView.dispose();
        }
    }
}