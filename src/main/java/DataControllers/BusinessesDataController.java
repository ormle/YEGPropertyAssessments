package DataControllers;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.*;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.Symbol;
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
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import DAO.BusinessDAO;
import classes.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;
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
    private TextField radiusInput;
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

    private MapView mapView;
    private GraphicsOverlay graphicsOverlay = new GraphicsOverlay();

    private ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphics;
    BusinessDAO dao;

    Map<String, String> params;
    ObservableList<String> wards;

    private LocatorTask locatorTask;

    private ProgressIndicator progressIndicator;
    ObservableList<String> neighbourhoods;

    private static final String CAMPSITE_SYMBOL = "http://maps.google.com/mapfiles/ms/icons/blue.png";

    private StackPane stackPane = new StackPane();

    ObservableList<String> businessTypes = FXCollections.observableArrayList(Arrays.asList("Bars", "Cannabis Stores",
            "Liquor Stores", "Restaurants"));

    ObservableList<Business> businesses;

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

        Viewpoint point = new Viewpoint(53.547,-113.51, 144447.638572);

        // set the map on the map view
        mapView.setMap(map);             //Currently Macewan University
        mapView.setViewpoint(new Viewpoint(53.547, -113.51, 144447.638572));

        // set the callout's default style
        Callout callout = mapView.getCallout();
        callout.setLeaderPosition(Callout.LeaderPosition.BOTTOM);

        // create a locatorTask
        locatorTask = new LocatorTask("https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer");

        // create geocode task parameters
        GeocodeParameters geocodeParameters = new GeocodeParameters();
        // return all attributes
        geocodeParameters.getResultAttributeNames().add("*");
        geocodeParameters.setMaxResults(1); // get closest match
        geocodeParameters.setOutputSpatialReference(mapView.getSpatialReference());

        // create new graphics overlay and add it to the map view
        mapView.getGraphicsOverlays().add(graphicsOverlay);

        PictureMarkerSymbol symbol = new PictureMarkerSymbol(CAMPSITE_SYMBOL);
        placePictureMarkerSymbol(symbol, point, graphicsOverlay, "name", "location");

        // create a new graphic with a our point and symbol
        //Graphic graphic = new Graphic(point.getTargetGeometry(), symbol);
        //graphicsOverlay.getGraphics().add(graphic);

        stackPane.getChildren().add(mapView);
        businessMap.setContent(stackPane);

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
                                System.out.println("NOT EMPTY, IM THERE");
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
    }

    /**
     * Adds a Graphic to the Graphics Overlay using a Point and a Picture Marker
     * Symbol.
     *
     * @param markerSymbol PictureMarkerSymbol to be used
     * @param graphicPoint where the Graphic is going to be placed
     */
    private void placePictureMarkerSymbol(PictureMarkerSymbol markerSymbol, Viewpoint graphicPoint, GraphicsOverlay graphicsOverlay, String name, String Location) {

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
     * creates set of wards based on dao.allBusinesses and sets respective combobox to list
     */
    private void setNeighbourhoods() {
        Set<String> neighbourhoodNames = dao.getAllBusinesses().stream()
                .map(Business::getLocation)
                .map(Location::getNeighbourhood)
                .map(Neighbourhood::getWard)
                .filter(neighbourhood -> !neighbourhood.isEmpty())
                .collect(Collectors.toSet());
        neighbourhoods = FXCollections.observableArrayList(neighbourhoodNames);
        //YOUD PUT YOUR COMBOBOX HERE!!!
        //neighbourhoodComboBox.setItems(neighbourhoods);
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
