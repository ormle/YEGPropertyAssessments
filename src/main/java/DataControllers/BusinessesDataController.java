package DataControllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import DAO.BusinessDAO;
import classes.*;

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
    //endregion

    BusinessDAO dao;

    Map<String, String> params;
    ObservableList<String> wards;

    ObservableList<String> businessTypes = FXCollections.observableArrayList(Arrays.asList("Bars", "Cannabis Stores",
            "Liquor Stores", "Restaurants"));

    ObservableList<Business> businesses;

    //filename here so it is easy to find and change
    private final String filename = "businesses_trimmed.csv";

    // temp property to search within radius
    PropertyAssessment property = new PropertyAssessment(1124304, 380000, "Y", new Location(53.517970704620545,
            -113.58016177749678, new Address(15006, "85 AVENUE NW"), new Neighbourhood(
                    "LYNNWOOD", "sipiwiyiniwak Ward")), new AssessmentClasses(100, "RESIDENTIAL"));
    //1124304,,15006,85 AVENUE NW,Y,4280,LYNNWOOD,sipiwiyiniwak Ward,380000,53.517970704620545,-113.58016177749678,POINT (-113.58016177749678 53.517970704620545),100,,,RESIDENTIAL,,

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dao = new BusinessDAO(filename);

        businessTypeComboBox.setItems(businessTypes);

        loadBusinessesButton.setOnAction(event -> loadBusinesses(businessTypeComboBox.getSelectionModel().getSelectedIndex()));

        searchButton.setOnAction(event -> search());
        resetButton.setOnAction(event -> resetSearchFilters());

        includePropertyBox.setOnAction(event -> setPropertyPane());

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
    }


    private void search() {
        //build search param map
        params = new HashMap<>();

        addTextFieldToParamMap(nameInput, "name");
        addTextFieldToParamMap(suiteInput, "suite");
        addTextFieldToParamMap(houseNumberInput, "houseNumber");
        addTextFieldToParamMap(streetInput, "streetName");
        addTextFieldToParamMap(neighbourhoodInput, "neighbourhood");

        params.put("ward", wardsComboBox.getSelectionModel().getSelectedItem());
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
