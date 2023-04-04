package DataControllers;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.*;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.*;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import DAO.BusinessDAO;
import classes.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BusinessesDataController implements Initializable {
    //region FXMLvariables
    @FXML
    private TableView<Business> businessesDataTable;
    @FXML
    private TableColumn<Business, String> nameTableColumn;
    @FXML
    private TableColumn<Business, Address> addressTableColumn;
    @FXML
    private TableColumn<Business, String> wardTableColumn;
    @FXML
    private TableColumn<Business, String> neighbourhoodTableColumn;
    @FXML
    private TableColumn<Business, String> locationTableColumn;
    @FXML
    private TableColumn<Business, String> categoriesTableColumn;
    @FXML
    private ComboBox<String> wardsComboBox;
    @FXML
    private ComboBox<String> businessTypeComboBox;
    @FXML
    private Button loadBusinessesButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button resetButton;
    @FXML
    private TextField suiteInput;
    @FXML
    private TextField houseNumberInput;
    @FXML
    private TextField streetInput;
    @FXML
    private TextField neighbourhoodInput;
    @FXML
    private Label propertyInfo;
    @FXML
    private AnchorPane propertyPane;
    @FXML
    private CheckBox includePropertyBox;
    @FXML
    private TextField nameInput;

    @FXML
    private Label titleText;

    @FXML
    private Tab businessMap;
    //endregion

    private TextField radiusInput;

    private TextField propertySearch;

    private Button searchProperty;
    private MapView mapView;
    private GraphicsOverlay graphicsOverlay = new GraphicsOverlay();

    private ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphics;
    BusinessDAO dao;

    Map<String, String> params;
    ObservableList<String> wards;

    private LocatorTask locatorTask;

    ObservableList<String> neighbourhoods;

    private static final String CAMPSITE_SYMBOL = "http://maps.google.com/mapfiles/ms/icons/blue.png";

    private static final String REDMAP_SYMBOL = "http://maps.google.com/mapfiles/ms/icons/red.png";

    private StackPane stackPane = new StackPane();

    ObservableList<String> businessTypes = FXCollections.observableArrayList(Arrays.asList("Bars", "Cannabis Stores",
            "Liquor Stores", "Restaurants"));

    ObservableList<Business> businesses;
    private ServiceFeatureTable featureTable;
    private VBox controlsVBox;

    private ComboBox<String> parcelsComboBox;
    private Label parcelsComboBoxLabel;
    private Label color1Label;
    private Label color2Label;
    private Label color3Label;
    //https://www.arcgis.com/home/item.html?id=3b64fab6e28f43b69e49ce7aadd5c353
    private final String edmontonNeighPortalId = "3b64fab6e28f43b69e49ce7aadd5c353";
    private final Portal portal = new Portal("https://www.arcgis.com");

    //filename here so it is easy to find and change
    private final String filename = "businesses_trimmed.csv";

    // temp property to search within radius
    PropertyAssessment property = new PropertyAssessment(1124304, 380000, "Y", new Location(53.517970704620545,
            -113.58016177749678, new Address(15006, "85 AVENUE NW"), new Neighbourhood(
                    "LYNNWOOD", "sipiwiyiniwak Ward")), new AssessmentClasses(100, "RESIDENTIAL"));

    public BusinessesDataController() {
    }
    //1124304,,15006,85 AVENUE NW,Y,4280,LYNNWOOD,sipiwiyiniwak Ward,380000,53.517970704620545,-113.58016177749678,POINT (-113.58016177749678 53.517970704620545),100,,,RESIDENTIAL,,

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dao = new BusinessDAO(filename);

        businessTypeComboBox.setItems(businessTypes);

        loadBusinessesButton.setOnAction(event -> loadBusinesses(businessTypeComboBox.getSelectionModel().getSelectedIndex()));

        searchButton.setOnAction(event -> search());
        resetButton.setOnAction(event -> resetSearchFilters());

        includePropertyBox.setOnAction(event -> setPropertyPane());

        //Initialises business map
        // Note: it is not best practice to store API keys in source code.
        // The API key is referenced here for the convenience of this tutorial.
        String yourApiKey = "AAPKb0e9a3e549174ef8b1ce1861e64e1a0eizDvf5tYBwfvTV8edXmx8nTtNihwNBr014H8zVeUJvSRI4Ct0WMMsdNuVcjOGfEW";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        // create a map view to display the map and add it to the stack pane
        mapView = new MapView();
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);

        // set the map on the map view
        mapView.setMap(map);             //Currently Macewan University
        mapView.setViewpoint(new Viewpoint(53.547, -113.51, 72000));

        // set the callout's default style
        Callout callout = mapView.getCallout();
        callout.setLeaderPosition(Callout.LeaderPosition.BOTTOM);

        // create new graphics overlay and add it to the map view
        mapView.getGraphicsOverlays().add(graphicsOverlay);

        PortalItem portalItem = new PortalItem(portal, edmontonNeighPortalId);
        long layerId = 4;
        featureTable = new ServiceFeatureTable(portalItem, layerId);
        portalItem.addDoneLoadingListener(() -> {
            if (portalItem.getLoadStatus() != LoadStatus.LOADED) {
                new Alert(Alert.AlertType.ERROR, "Portal item not loaded. " + portalItem.getLoadError().getCause().getMessage()).show();
            }
        });

        setUpUI();

        //Create text input for property search address and radius distance
        propertySearch = new TextField();
        propertySearch.setPromptText("Property search bar");
        propertySearch.setMaxWidth(260);
        radiusInput = new TextField();
        radiusInput.setPromptText("Radius box");
        radiusInput.setMaxWidth(260);
        searchProperty = new Button("Search");
        searchProperty.setMaxWidth(260);

        // create feature layer
        createFeatureLayer(map);

        controlsVBox.getChildren().addAll(parcelsComboBoxLabel, parcelsComboBox, color1Label, color2Label, color3Label, propertySearch, searchProperty);
        stackPane.getChildren().addAll(mapView);
        stackPane.getChildren().add(controlsVBox);
        stackPane.setAlignment(controlsVBox, Pos.TOP_LEFT);
        stackPane.setMargin(controlsVBox, new Insets(10, 0, 0, 10));
        businessMap.setContent(stackPane);

        // create a locatorTask
        locatorTask = new LocatorTask("https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer");

        // create geocode task parameters
        GeocodeParameters geocodeParameters = new GeocodeParameters();
        //geocodeParameters.setCountryCode("CANADA");
        // return all attributes
        geocodeParameters.getResultAttributeNames().add("*");
        geocodeParameters.setMaxResults(1); // get closest match
        geocodeParameters.setOutputSpatialReference(mapView.getSpatialReference());

        searchProperty.setOnAction(event -> {
            String query;
            if (!searchProperty.getText().equals("")){
                query = propertySearch.getText();
                mapView.getCallout().dismiss();

                // run the locatorTask geocode task
                ListenableFuture<List<GeocodeResult>> results = locatorTask.geocodeAsync(query, geocodeParameters);

                // add a listener to display the result when loaded
                results.addDoneListener(new ResultsLoadedListener(results));
            }
        });

        //listen into click events on the map view
        mapView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        // Respond to primary (left) button only
                        if (event.isStillSincePress() && event.getButton() == MouseButton.PRIMARY) {
                            //make a screen coordinate from the clicked location
                            Point2D clickedPoint = new Point2D(event
                                    .getX(), event.getY());

                            // create a map point from a point
                            Point mapPoint = mapView.screenToLocation(clickedPoint);

                            // identify graphics on the graphics overlay
                            identifyGraphics = mapView.identifyGraphicsOverlayAsync(graphicsOverlay, clickedPoint, 1, false);
                            identifyGraphics.addDoneListener(() -> Platform.runLater(() -> createGraphicDialog(mapPoint)));
                        }
                    }

                    private void createGraphicDialog(Point clickedPoint) {
                        try {
                            // get the list of graphics returned by identify
                            IdentifyGraphicsOverlayResult result = identifyGraphics.get();
                            List<Graphic> graphics = result.getGraphics();

                            if (!graphics.isEmpty()) {
                                // display the callout

                                Callout callout = mapView.getCallout();
                                callout.setTitle(graphics.get(0).getAttributes().get("Name").toString());
                                callout.setDetail(graphics.get(0).getAttributes().get("Location").toString());
                                callout.showCalloutAt(clickedPoint, Duration.ZERO);
                                //listen into click events on the map view
                                mapView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                                        new EventHandler<MouseEvent>() {
                                            @Override
                                            public void handle(MouseEvent event) {
                                callout.dismiss();}});
                            }
                        } catch (Exception e) {
                            // on any error, display the stack trace
                            e.printStackTrace();
                        }
                    }

                });
    }

    /**
     * Runnable listener to update marker and callout when new results are loaded.
     */
    private class ResultsLoadedListener implements Runnable {

        private final ListenableFuture<List<GeocodeResult>> results;

        /**
         * Constructs a runnable listener for the geocode results.
         *
         * @param results results from a {@link LocatorTask#geocodeAsync} task
         */
        ResultsLoadedListener(ListenableFuture<List<GeocodeResult>> results) {
            this.results = results;
        }

        @Override
        public void run() {

            try {
                List<GeocodeResult> geocodes = results.get();
                if (geocodes.size() > 0) {
                    // get the top result
                    GeocodeResult geocode = geocodes.get(0);

                    // get attributes from the result for the callout
                    String addrType = geocode.getAttributes().get("Addr_type").toString();
                    String placeName = geocode.getAttributes().get("PlaceName").toString();
                    String placeAddr = geocode.getAttributes().get("Place_addr").toString();
                    String matchAddr = geocode.getAttributes().get("Match_addr").toString();
                    String locType = geocode.getAttributes().get("Type").toString();

                    // format callout details
                    String title;
                    String detail;
                    switch (addrType) {
                        case "POI":
                            title = placeName.equals("") ? "" : placeName;
                            if (!placeAddr.equals("")) {
                                detail = placeAddr;
                            } else if (!matchAddr.equals("") && !locType.equals("")) {
                                detail = !matchAddr.contains(",") ? locType : matchAddr.substring(matchAddr.indexOf(", ") + 2);
                            } else {
                                detail = "";
                            }
                            break;
                        case "StreetName":
                        case "PointAddress":
                        case "Postal":
                            if (matchAddr.contains(",")) {
                                title = matchAddr.equals("") ? "" : matchAddr.split(",")[0];
                                detail = matchAddr.equals("") ? "" : matchAddr.substring(matchAddr.indexOf(", ") + 2);
                                break;
                            }
                        default:
                            title = "";
                            detail = matchAddr.equals("") ? "" : matchAddr;
                    }

                    HashMap<String, Object> attributes = new HashMap<>();
                    attributes.put("title", title);
                    attributes.put("detail", detail);

                    // create the marker
                    PictureMarkerSymbol redsymbol = new PictureMarkerSymbol(REDMAP_SYMBOL);
                    Graphic marker = new Graphic(geocode.getDisplayLocation(), attributes, redsymbol);

                    // set the viewpoint to the marker
                    Point location = geocodes.get(0).getDisplayLocation();
                    mapView.setViewpointCenterAsync(location, 10000);

                    // update the marker
                    Platform.runLater(() -> {
                        // clear out previous results
                        //graphicsOverlay.getGraphics().clear();
                        //searchBox.hide();

                        // add the marker to the graphics overlay
                        graphicsOverlay.getGraphics().add(marker);

                        // display the callout
                        Callout callout = mapView.getCallout();
                        callout.setTitle(marker.getAttributes().get("title").toString());
                        callout.setDetail(marker.getAttributes().get("detail").toString());
                        callout.showCalloutAt(location, new Point2D(0, -24), Duration.ZERO);
                    });
                }

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpUI(){
        // create a control panel
        controlsVBox = new VBox(6);
        controlsVBox.setBackground(new Background(new BackgroundFill(Paint.valueOf("rgba(0,0,0,0.5)"), CornerRadii.EMPTY,
                Insets.EMPTY)));
        controlsVBox.setPadding(new Insets(10.0));
        controlsVBox.setMaxSize(250, 50);
        controlsVBox.getStyleClass().add("panel-region");
        //Combobox
        parcelsComboBoxLabel = new Label("WHERE EXPRESSION: ");
        parcelsComboBoxLabel.setTextFill(Color.WHITE);

        color1Label = new Label("Orange -> 3 Businesses or less");
        color1Label.setStyle("-fx-text-fill: orange;");
        color2Label = new Label("Red -> 4 to 10 Businesses");
        color2Label.setStyle("-fx-text-fill: red;");
        color3Label = new Label("Pink -> 10 or more Businesses");
        color3Label.setStyle("-fx-text-fill: pink;");


        // create the list of expressions for the combo box and then create the combo box
        setNeighbourhoods();
        parcelsComboBox = new ComboBox<>(neighbourhoods);
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
        AtomicInteger noOfBusinesses = new AtomicInteger();
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
                    String currentChoice = selectionModel.getSelectedItem();
                    noOfBusinesses.set(Math.toIntExact(getNoOfBusinesses(currentChoice.substring(6, currentChoice.length() - 1),
                            businessTypeComboBox.getSelectionModel().getSelectedItem())));
                    //parcelsComboBoxLabel.setText(noOfBusinesses.toString());
                    Envelope currentExtent =
                            (Envelope) (mapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).getTargetGeometry());

                    queryFeatureLayer(layer.getId(), currentChoice, currentExtent);
                    //Set selection colour based on number of businesses in that neighbourhood
                    if (noOfBusinesses.get() <= 3){
                        mapView.getSelectionProperties().setColor(Color.ORANGE);
                    }else if (noOfBusinesses.get() > 3 && noOfBusinesses.get() <= 10){
                        mapView.getSelectionProperties().setColor(Color.RED);
                    }else{//noOfBusinesses > 10
                        mapView.getSelectionProperties().setColor(Color.PINK);
                    }

                });
            }
        });
        //  add the layer to the map
        map.getOperationalLayers().add(layer);
        }

    private long getNoOfBusinesses(String Neighbourhood, String BusinessType){
        String neigh = Neighbourhood.charAt(0) + Neighbourhood.substring(1).toLowerCase();
        int businessesInNeighbourhood = 0;
        switch (BusinessType){
            case "Bars":
                businessesInNeighbourhood = Math.toIntExact(dao.getBars().stream()
                        .map(Business::getLocation)
                        .map(Location::getNeighbourhood)
                        .filter(neighbourhood -> neighbourhood.getName().equals(neigh))
                        .count());
                break;
            case "Cannabis Stores":
                businessesInNeighbourhood = Math.toIntExact(dao.getCannabisRetail().stream()
                        .map(Business::getLocation)
                        .map(Location::getNeighbourhood)
                        .filter(neighbourhood -> neighbourhood.getName().equals(neigh))
                        .count());
                break;
            case "Liquor Stores":
                businessesInNeighbourhood = Math.toIntExact(dao.getAlcoholRetail().stream()
                        .map(Business::getLocation)
                        .map(Location::getNeighbourhood)
                        .filter(neighbourhood -> neighbourhood.getName().equals(neigh))
                        .count());
                break;
            case "Restaurants":
                businessesInNeighbourhood = Math.toIntExact(dao.getRestaurants().stream()
                        .map(Business::getLocation)
                        .map(Location::getNeighbourhood)
                        .filter(neighbourhood -> neighbourhood.getName().equals(neigh))
                        .count());
                break;
        }
        //parcelsComboBoxLabel.setText(String.valueOf(businessesInNeighbourhood));
        return businessesInNeighbourhood;
    }

    /**
     * Adds a Graphic to the Graphics Overlay using a Point and a Picture Marker
     * Symbol.
     *
     * @param markerSymbol PictureMarkerSymbol to be used
     * @param graphicPoint where the Graphic is going to be placed
     */
    private void placePictureMarkerSymbol(PictureMarkerSymbol markerSymbol, Viewpoint graphicPoint, GraphicsOverlay graphicsOverlay, String name, String location) {

        // set size of the image
        markerSymbol.setHeight(40);
        markerSymbol.setWidth(40);

        // load symbol asynchronously
        markerSymbol.loadAsync();

        // add to the graphic overlay once done loading
        markerSymbol.addDoneLoadingListener(() -> {
            if (markerSymbol.getLoadStatus() == LoadStatus.LOADED) {
                Graphic symbolGraphic = new Graphic(graphicPoint.getTargetGeometry(), markerSymbol);
                symbolGraphic.getAttributes().put("Name", name);
                symbolGraphic.getAttributes().put("Location", location);
                graphicsOverlay.getGraphics().add(symbolGraphic);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Picture Marker Symbol Failed to Load!");
                alert.show();
            }
        });

    }

    private void setPropertyPane(){
        propertyPane.setVisible(includePropertyBox.isSelected());
        propertyInfo.setText("Account: " + property.getAccount() + "\n" + property.getLocation().getAddress() + "\n"
                + property.getLocation().getNeighbourhood());
    }

    private void loadBusinesses(Integer businessType) {
        if (businessType == 0){
            businesses = FXCollections.observableArrayList(dao.getBars());
            titleText.setText("Edmonton Bars (2022)");
        } else if (businessType==1){
            businesses = FXCollections.observableArrayList(dao.getCannabisRetail());
            titleText.setText("Edmonton Cannabis Stores (2022)");
        } else if (businessType==2){
            businesses = FXCollections.observableArrayList(dao.getAlcoholRetail());
            titleText.setText("Edmonton Liquor Stores (2022)");
        } else if (businessType==3){
            businesses = FXCollections.observableArrayList(dao.getRestaurants());
            titleText.setText("Edmonton Restaurants (2022)");
        }

        loadDataTable();
        enableSearchResetButtons();
        setWards();
    }

    /**
     * creates set of wards based on dao.allBusinesses and sets respective combobox to list
     */
    private void setWards() {
        Set<String> wardNames = dao.getAllBusinesses().stream()
                .map(Business::getLocation)
                .map(Location::getNeighbourhood)
                .map(Neighbourhood::getWard)
                .filter(ward -> !ward.isEmpty())
                .collect(Collectors.toSet());
        wards = FXCollections.observableArrayList(wardNames);
        wardsComboBox.setItems(wards);
    }

    /**
     * creates set of neighbourhoods based on dao.allBusinesses and sets respective combobox to list
     */
    private void setNeighbourhoods() {
        Set<String> neighbourhoodNames = dao.getAllBusinesses().stream()
                .map(Business::getLocation)
                .map(Location::getNeighbourhood)
                .filter(neighbourhood -> !neighbourhood.isEmpty())
                .map(Neighbourhood::getName)
                .collect(Collectors.toSet());
        String q = "name=";
        ArrayList<String> queryNeigh = new ArrayList<>();
        queryNeigh.add("Select an expression");
        for (String n: neighbourhoodNames) {
            String query = q + "'" + n.toUpperCase() +"'";
            queryNeigh.add(query);
        }
        neighbourhoods = FXCollections.observableArrayList(queryNeigh);
    }

    private void enableSearchResetButtons() {
        searchButton.setDisable(false);
        resetButton.setDisable(false);
    }


    private void loadDataTable() {
        businessesDataTable.setItems(businesses);

        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressTableColumn.setCellValueFactory(property ->
                new SimpleObjectProperty<>(property.getValue().getLocation().getAddress()));
        wardTableColumn.setCellValueFactory(property ->
                new SimpleObjectProperty<>(property.getValue().getLocation().getNeighbourhood().getWard()));
        neighbourhoodTableColumn.setCellValueFactory(property ->
                new SimpleObjectProperty<>(property.getValue().getLocation().getNeighbourhood().getName()));
        locationTableColumn.setCellValueFactory(property ->
                new SimpleStringProperty(property.getValue().getLocation().getLatLon()));
        categoriesTableColumn.setCellValueFactory(property ->
                new SimpleStringProperty(property.getValue().getCategories()));

        if (businesses!=null) {
            mapView.getGraphicsOverlays().clear();
            graphicsOverlay.getGraphics().clear();
            mapView.getGraphicsOverlays().add(graphicsOverlay);
            for (Business business : businesses) {
                Viewpoint point = new Viewpoint(business.getLocation().getLat(),business.getLocation().getLon(), 144447.638572);
                PictureMarkerSymbol symbol = new PictureMarkerSymbol(CAMPSITE_SYMBOL);
                placePictureMarkerSymbol(symbol, point, graphicsOverlay, business.getName(), business.getLocation().getAddress().toString());
            }
            //listen into click events on the map view
            mapView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            // Respond to primary (left) button only
                            if (event.isStillSincePress() && event.getButton() == MouseButton.PRIMARY) {
                                //make a screen coordinate from the clicked location
                                Point2D clickedPoint = new Point2D(event
                                        .getX(), event.getY());

                                // create a map point from a point
                                Point mapPoint = mapView.screenToLocation(clickedPoint);

                                // identify graphics on the graphics overlay
                                identifyGraphics = mapView.identifyGraphicsOverlayAsync(graphicsOverlay, clickedPoint, 1, false);
                                identifyGraphics.addDoneListener(() -> Platform.runLater(() -> createGraphicDialog(mapPoint)));


                            }
                        }

                        private void createGraphicDialog(Point clickedPoint) {
                            try {
                                // get the list of graphics returned by identify
                                IdentifyGraphicsOverlayResult result = identifyGraphics.get();
                                List<Graphic> graphics = result.getGraphics();

                                if (!graphics.isEmpty()) {
                                    // display the callout
                                    Callout callout = mapView.getCallout();
                                    callout.setTitle(graphics.get(0).getAttributes().get("Name").toString());
                                    //callout.setDetail(graphics.get(0).getAttributes().get("detail").toString());
                                    callout.showCalloutAt(clickedPoint, Duration.ZERO);
                                    //listen into click events on the map view
                                    mapView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                                            new EventHandler<MouseEvent>() {
                                                @Override
                                                public void handle(MouseEvent event) {
                                                    callout.dismiss();}});
                                }
                            } catch (Exception e) {
                                // on any error, display the stack trace
                                e.printStackTrace();
                            }
                        }

                    });

            //stackPane.getChildren().add(mapView);
            businessMap.setContent(stackPane);
        }
    }


    private void search() {
        //build search param map
        params = new HashMap<>();

        addTextFieldToParamMap(nameInput, "name");
        addTextFieldToParamMap(suiteInput, "suite");
        addTextFieldToParamMap(houseNumberInput, "houseNumber");
        addTextFieldToParamMap(streetInput, "streetName");
        addTextFieldToParamMap(neighbourhoodInput, "neighbourhood");

        if (wardsComboBox.getSelectionModel().getSelectedItem()!=null){
            params.put("ward", wardsComboBox.getSelectionModel().getSelectedItem());
        }

        params.put("businessType", businessTypeComboBox.getSelectionModel().getSelectedItem());

        //do search
        try{
            businesses = FXCollections.observableArrayList(dao.multipleParamaters(params));
        } catch (NumberFormatException e){
            throwAlert("Number Format Error", """
                        The following fields must consist only of digits 0-9:
                        Account Number
                        House Number""");
            e.printStackTrace();
            return;
        }

        //check if any properties returned
        if (businesses.stream().allMatch(Business::emptyBusiness)){
            throwAlert("Search Results", "No properties found");
        }
        else {
            loadDataTable();
        }

        if (businesses!=null) {

            for (Business business : businesses) {
                Viewpoint point = new Viewpoint(business.getLocation().getLat(),business.getLocation().getLon(), 144447.638572);
                PictureMarkerSymbol symbol = new PictureMarkerSymbol(CAMPSITE_SYMBOL);
                placePictureMarkerSymbol(symbol, point, graphicsOverlay, business.getName(), business.getLocation().toString());
            }
            //listen into click events on the map view
            mapView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            // Respond to primary (left) button only
                            if (event.isStillSincePress() && event.getButton() == MouseButton.PRIMARY) {
                                //make a screen coordinate from the clicked location
                                Point2D clickedPoint = new Point2D(event
                                        .getX(), event.getY());

                                // create a map point from a point
                                Point mapPoint = mapView.screenToLocation(clickedPoint);

                                // identify graphics on the graphics overlay
                                identifyGraphics = mapView.identifyGraphicsOverlayAsync(graphicsOverlay, clickedPoint, 1, false);
                                identifyGraphics.addDoneListener(() -> Platform.runLater(() -> createGraphicDialog(mapPoint)));


                            }
                        }

                        private void createGraphicDialog(Point clickedPoint) {
                            try {
                                // get the list of graphics returned by identify
                                IdentifyGraphicsOverlayResult result = identifyGraphics.get();
                                List<Graphic> graphics = result.getGraphics();

                                if (!graphics.isEmpty()) {
                                    // display the callout
                                    Callout callout = mapView.getCallout();
                                    callout.setTitle(graphics.get(0).getAttributes().get("Name").toString());
                                    //callout.setDetail(graphics.get(0).getAttributes().get("detail").toString());
                                    callout.showCalloutAt(clickedPoint, Duration.ZERO);
                                    //listen into click events on the map view
                                    mapView.addEventHandler(MouseEvent.MOUSE_CLICKED,
                                            new EventHandler<MouseEvent>() {
                                                @Override
                                                public void handle(MouseEvent event) {
                                                    callout.dismiss();}});
                                }
                            } catch (Exception e) {
                                // on any error, display the stack trace
                                e.printStackTrace();
                            }
                        }

                    });

            //stackPane.getChildren().add(mapView);
            businessMap.setContent(stackPane);
        }
    }

    private void addTextFieldToParamMap(TextField textField, String key) {
        String text = textField.getText();
        if (text == null || text.isEmpty()){
            return;
        }
        params.put(key, textField.getText());
    }

    private void throwAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private void resetSearchFilters() {
        nameInput.clear();
        suiteInput.clear();
        houseNumberInput.clear();
        streetInput.clear();
        neighbourhoodInput.clear();
        wardsComboBox.setValue(null);
    }
}
