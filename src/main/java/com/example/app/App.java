package com.example.app;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.esri.arcgisruntime.data.*;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.mapping.Viewpoint;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
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
    private ComboBox<String> parcelsComboBox;
    private Label parcelsComboBoxLabel;
    //https://www.arcgis.com/home/item.html?id=3b64fab6e28f43b69e49ce7aadd5c353
    private String edmontonNeighPortalId = "3b64fab6e28f43b69e49ce7aadd5c353";
    private Portal portal = new Portal("https://www.arcgis.com");

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

        // create parcels feature layer
        createFeatureLayer(map);

//        // listen for when a radio button within the toggle group is selected
//        toggleGroup.selectedToggleProperty().addListener(e -> {
//
//            // check if the feature layer has already been added to the map's operational layers, and if not, add it
//            if (map.getOperationalLayers().size() == 0){
//                map.getOperationalLayers().add(layer);
//            }
//
//            // check the feature layer has loaded before setting the request mode of the feature table, selected from
//            // the radio button's user data
//            layer.addDoneLoadingListener(() -> {
//                if (layer.getLoadStatus() == LoadStatus.LOADED) {
//                    // if the manual radio button isn't selected, display a blank label, otherwise display instruction to user
//                    if (!neighbourhoodButton.isSelected()) {
//                        map.getOperationalLayers().remove(0);
//                    }
//                    // set request mode of service feature table to selected toggle option
//                    featureTable.setFeatureRequestMode((ServiceFeatureTable.FeatureRequestMode)
//                            toggleGroup.getSelectedToggle().getUserData());
//
//                } else {
//                    new Alert(Alert.AlertType.ERROR, "Feature Layer Failed to Load!").show();
//                }
//            });
//        });

        controlsVBox.getChildren().addAll(neighbourhoodButton, noneButton, parcelsComboBoxLabel, parcelsComboBox);
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
        controlsVBox.setMaxSize(150, 80);
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
        //Combobox
        parcelsComboBoxLabel = new Label("WHERE EXPRESSION: ");
        parcelsComboBoxLabel.setTextFill(Color.WHITE);

        // create the list of expressions for the combo box and then create the combo box
        ObservableList<String> parcelsComboBoxList = FXCollections.observableArrayList("Select an expression",
                 "name = 'QUEEN MARY PARK'", "name = 'DUNLUCE'");
        parcelsComboBox = new ComboBox<>(parcelsComboBoxList);
        parcelsComboBox.setMaxWidth(Double.MAX_VALUE);

        // set the default combo box value
        parcelsComboBox.getSelectionModel().select(0);
    }

    // Query the feature layer, providing a Where expression and the current extent.
    // Then select the features returned by the query.
    private void queryFeatureLayer(String featureLayerId, String whereExpression, Envelope queryExtent) {
        try {
            FeatureLayer featureLayerToQuery = null;

            // get the layer based on its Id
            for (Layer layer : mapView.getMap().getOperationalLayers()) {
                if (layer.getId().equals(featureLayerId)) {
                    featureLayerToQuery = (FeatureLayer) layer;
                    break;
                }
            }

            // check if feature layer retrieved based on layer Id
            if (featureLayerToQuery == null) {
                String msg = "Specified Id did not match any feature layer in this map";
                new Alert(Alert.AlertType.ERROR, msg).show();
                return;
            }

            // get the feature table from the feature layer
            FeatureTable featureTableToQuery = featureLayerToQuery.getFeatureTable();
            // clear any previous selections
            featureLayerToQuery.clearSelection();

            // create a query for the state that was entered
            QueryParameters query = new QueryParameters();
            query.setWhereClause(whereExpression);
            query.setReturnGeometry(true);
            query.setGeometry(queryExtent);

            // call query features
            ListenableFuture<FeatureQueryResult> future = featureTableToQuery.queryFeaturesAsync(query);

            // create an effectively final variable for access from add done listener
            FeatureLayer finalFeatureLayerToQuery = featureLayerToQuery;

            // add done listener to fire when the query returns
            future.addDoneListener(() -> {
                try {
                    // check if there are some results
                    FeatureQueryResult featureQueryResult = future.get();
                    if (featureQueryResult.iterator().hasNext()) {
                        for (Feature feature : featureQueryResult) {
                            finalFeatureLayerToQuery.selectFeature(feature);
                        }
                    } else {
                        String msg = "No neighbourhood found in the current extent, using Where expression: " + whereExpression + ".";
                        new Alert(Alert.AlertType.INFORMATION, msg).show();
                    }
                }
                catch (Exception e) {
                    String msg = "Feature search failed for: " + whereExpression + ". Error: " + e.getMessage();
                    new Alert(Alert.AlertType.ERROR, msg).show();
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            // on any error, display the stack trace
            e.printStackTrace();
        }
    }

    // Create and load the parcels feature layer from the feature service. When the layer is loaded, query it based on
    // current extent and current selection in parcelsComboBox
    private void createFeatureLayer(ArcGISMap map){
        PortalItem portalItem = new PortalItem(portal, edmontonNeighPortalId);
        long layerId = 4;
        FeatureTable serviceFeatureTable =
                new ServiceFeatureTable(portalItem, layerId);

        FeatureLayer layer = new FeatureLayer(serviceFeatureTable);

        // give the layer an ID, so we can easily find it later
        //layer.setId("Parcels");
        // Load the layer and add a done loading listener, which runs only when the layer is completely loaded.
        layer.loadAsync();
        layer.addDoneLoadingListener( () -> {
            if (layer.getLoadStatus() == LoadStatus.LOADED) {
                // get the selected item property from the combo box, and add a listener that runs
                // when the property value is changed by the user.
                SingleSelectionModel<String> selectionModel = parcelsComboBox.getSelectionModel();
                selectionModel.selectedItemProperty().addListener(observable -> {
                    if (selectionModel.getSelectedIndex() == 0) {
                        layer.clearSelection();
                        return;
                    }
                    String currentParcelsChoice = selectionModel.getSelectedItem();

                    Envelope currentExtent =
                            (Envelope) (mapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).getTargetGeometry());

                    queryFeatureLayer(layer.getId(), currentParcelsChoice, currentExtent);

                });
            }
        });
        //  add the layer to the map
        map.getOperationalLayers().add(layer);
        mapView.getSelectionProperties().setColor(Color.YELLOW);
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