package com.example.app;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.mapping.Viewpoint;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;

import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.layout.*;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class App extends Application {
    private MapView mapView;

    private GeocodeParameters geocodeParameters;
    private GraphicsOverlay graphicsOverlay;
    private LocatorTask locatorTask;

    private TextField searchBox;
    private RadioButton neighbourhoodButton;
    private RadioButton noneButton;
    private ServiceFeatureTable featureTable;
    private ToggleGroup toggleGroup;
    private VBox controlsVBox;

    public static void main(String[] args){
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // set the title and size of the stage and show it
        stage.setTitle("YEG");
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
        // create a graphics overlay and add it to the map view
//        graphicsOverlay = new GraphicsOverlay();
//        mapView.getGraphicsOverlays().add(graphicsOverlay);

        //https://www.arcgis.com/home/item.html?id=3b64fab6e28f43b69e49ce7aadd5c353
        String edmontonNeighPortalId = "3b64fab6e28f43b69e49ce7aadd5c353";
        Portal portal = new Portal("https://www.arcgis.com");
        PortalItem portalItem = new PortalItem(portal, edmontonNeighPortalId);
        long layerId = 4;
        featureTable = new ServiceFeatureTable(portalItem, layerId);
        FeatureLayer layer = new FeatureLayer(featureTable);
        portalItem.addDoneLoadingListener(() -> {
            if (portalItem.getLoadStatus() != LoadStatus.LOADED) {
                new Alert(Alert.AlertType.ERROR, "Portal item not loaded. " + portalItem.getLoadError().getCause().getMessage()).show();
            }
        });

        setUpUI();

        // listen for when a radio button within the toggle group is selected
        toggleGroup.selectedToggleProperty().addListener(e -> {

            // check if the feature layer has already been added to the map's operational layers, and if not, add it
            if (map.getOperationalLayers().size() == 0){
                map.getOperationalLayers().add(layer);
            }

            // check the feature layer has loaded before setting the request mode of the feature table, selected from
            // the radio button's user data
            layer.addDoneLoadingListener(() -> {
                if (layer.getLoadStatus() == LoadStatus.LOADED) {
                    // if the manual radio button isn't selected, display a blank label, otherwise display instruction to user
                    if (!neighbourhoodButton.isSelected()) {
                        map.getOperationalLayers().remove(0);
                    }
                    // set request mode of service feature table to selected toggle option
                    featureTable.setFeatureRequestMode((ServiceFeatureTable.FeatureRequestMode)
                            toggleGroup.getSelectedToggle().getUserData());

                } else {
                    new Alert(Alert.AlertType.ERROR, "Feature Layer Failed to Load!").show();
                }
            });
        });

        controlsVBox.getChildren().addAll(neighbourhoodButton, noneButton);
        stackPane.getChildren().add(controlsVBox);
        stackPane.setAlignment(controlsVBox, Pos.TOP_LEFT);
        stackPane.setMargin(controlsVBox, new Insets(10, 0, 0, 10));


//        setupTextField();
//
//        createLocatorTaskAndDefaultParameters();
//
//        stackPane.getChildren().add(searchBox);
//        StackPane.setAlignment(searchBox, Pos.TOP_LEFT);
//        StackPane.setMargin(searchBox, new Insets(10, 0, 0, 10));
//
//        searchBox.setOnAction(event -> {
//            String address = searchBox.getText();
//            if (!address.isBlank()) {
//                performGeocode(address);
//            }
//        });

        //Set content of mapTab
        mapTab.setContent(stackPane);
        //Set content of property tab
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/PropertyAsessmentsDataView.fxml"));
        propertyTab.setContent(fxmlLoader.load());
        //Set content of business tab
        //fxmlLoader = new FXMLLoader(getClass().getResource("/BusinessesDataView.fxml"));
        fxmlLoader = new FXMLLoader(getClass().getResource("/test.fxml"));
        businessTab.setContent(fxmlLoader.load());

        // create a JavaFX scene with a stack pane as the root node, and add it to the scene
        tabPane.getTabs().addAll(propertyTab, businessTab, mapTab);
        Scene scene = new Scene(tabPane, 720, 640);
        stage.setScene(scene);
    }

    private void setUpUI(){
        // create a control panel
        controlsVBox = new VBox(6);
        controlsVBox.setBackground(new Background(new BackgroundFill(Paint.valueOf("rgba(0,0,0,0.5)"), CornerRadii.EMPTY,
                Insets.EMPTY)));
        controlsVBox.setPadding(new Insets(10.0));
        controlsVBox.setMaxSize(150, 60);
        controlsVBox.getStyleClass().add("panel-region");

        // create a label to display information on the UI
        //label = new Label("Choose a feature request mode.");

        // set the feature request mode as the user data on the equivalent radio button
        noneButton = new RadioButton("None");
        noneButton.setUserData(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_NO_CACHE);
        noneButton.setSelected(true);
        neighbourhoodButton = new RadioButton("Neighbourhoods");
        neighbourhoodButton.setUserData(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_NO_CACHE);
        // create a new toggle group and add the radio buttons to it
        toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(neighbourhoodButton, noneButton);
    }

    private void createLocatorTaskAndDefaultParameters() {
        locatorTask = new LocatorTask("https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer");

        geocodeParameters = new GeocodeParameters();
        geocodeParameters.getResultAttributeNames().add("*");
        geocodeParameters.setMaxResults(1);
        geocodeParameters.setOutputSpatialReference(mapView.getSpatialReference());
    }

    private void performGeocode(String address) {
        ListenableFuture<List<GeocodeResult>> geocodeResults = locatorTask.geocodeAsync(address, geocodeParameters);

        geocodeResults.addDoneListener(() -> {
            try {
                List<GeocodeResult> geocodes = geocodeResults.get();
                if (geocodes.size() > 0) {
                    GeocodeResult result = geocodes.get(0);
                    displayResult(result);
                } else {
                    new Alert(Alert.AlertType.INFORMATION, "No results found.").show();
                }
            } catch (InterruptedException | ExecutionException e) {
                new Alert(Alert.AlertType.ERROR, "Error getting result.").show();
                e.printStackTrace();
            }
        });
    }

    private void displayResult(GeocodeResult geocodeResult) {
        graphicsOverlay.getGraphics().clear(); // clears the overlay of any previous result

        // create a graphic to display the address text
        String label = geocodeResult.getLabel();
        TextSymbol textSymbol = new TextSymbol(18, label, Color.BLACK, TextSymbol.HorizontalAlignment.CENTER, TextSymbol.VerticalAlignment.BOTTOM);
        Graphic textGraphic = new Graphic(geocodeResult.getDisplayLocation(), textSymbol);
        graphicsOverlay.getGraphics().add(textGraphic);

        // create a graphic to display the location as a red square
        SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, Color.RED, 12.0f);
        Graphic markerGraphic = new Graphic(geocodeResult.getDisplayLocation(), geocodeResult.getAttributes(), markerSymbol);
        graphicsOverlay.getGraphics().add(markerGraphic);

        mapView.setViewpointCenterAsync(geocodeResult.getDisplayLocation());
    }

    private void setupTextField() {
        searchBox = new TextField();
        searchBox.setMaxWidth(400);
        searchBox.setPromptText("Search for an address");
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