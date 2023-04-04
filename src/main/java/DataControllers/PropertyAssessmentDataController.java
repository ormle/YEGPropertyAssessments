package DataControllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import DAO.*;
import classes.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PropertyAssessmentDataController implements Initializable{
    //region FXMLvariables
    @FXML
    private TableView<PropertyAssessment> assessmentDataTable;
    @FXML
    private TableColumn<PropertyAssessment, Integer> accountTableColumn;
    @FXML
    private TableColumn<PropertyAssessment, Address> addressTableColumn;
    @FXML
    private TableColumn<PropertyAssessment, Integer> assessedValueTableColumn;
    @FXML
    private TableColumn<PropertyAssessment, AssessmentClasses> assessmentClassesTableColumn;
    @FXML
    private TableColumn<PropertyAssessment, String> neighbourhoodTableColumn;
    @FXML
    private TableColumn<PropertyAssessment, String> wardTableColumn;
    @FXML
    private TableColumn<PropertyAssessment, String> locationTableColumn;
    @FXML
    private ComboBox<String> dataSourceComboBox;
    @FXML
    private ComboBox<String> assessmentClassComboBox;
    @FXML
    private ComboBox<String> wardsComboBox;
    @FXML
    private Button readDataSourceButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button loadMoreApiDataButton;
    @FXML
    private TextField accountNumberInput;
    @FXML
    private TextField suiteInput;
    @FXML
    private TextField houseNumberInput;
    @FXML
    private TextField streetInput;
    @FXML
    private TextField neighbourhoodInput;
    @FXML
    private TextField minValueInput;
    @FXML
    private TextField maxValueInput;
    //endregion
    PropertyAssessmentDAO dao;
    ObservableList<PropertyAssessment> properties;
    ObservableList<String> assessmentClasses;
    ObservableList<String> wards;
    Map<String, String> params;
    int offset; //used for paging when APIDao is used
    int lastLoadFlag; //used for paging when APIDao is used

    //filename here so it is easy to find and change
    private final String filename = "Property_Assessment_Data__Current_Calendar_Year_.csv";


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> dataSources = FXCollections.observableArrayList(
                Arrays.asList("CSV File", "Edmonton's Open Data Portal"));

        dataSourceComboBox.setItems(dataSources);

        //When read data clicked, get selected index from combobox, use index to load source data
        readDataSourceButton.setOnAction(event ->
                loadSourceData(dataSourceComboBox.getSelectionModel().getSelectedIndex()));

        //When search button clicked, use dao to get account number
        searchButton.setOnAction(event -> search());

        //Reset search fields
        resetButton.setOnAction(event -> resetSearchFilters());

        //load more data
        loadMoreApiDataButton.setOnAction(event -> loadMoreData());

        assessmentDataTable.setRowFactory( tv -> {
            TableRow<PropertyAssessment> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    PropertyAssessment rowData = row.getItem();

                    final ClipboardContent content = new ClipboardContent();
                    content.putString(String.valueOf(rowData.getAccount()));
                    Clipboard.getSystemClipboard().setContent(content);

                    throwAlert("Copied to Clipboard", "Account Number Copied to Clipboard\nAccount number: "+ rowData.getAccount());
                }
            });
            return row ;
        });

        //for number currency format
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        currencyFormat.setMaximumFractionDigits(0);
        assessedValueTableColumn.setCellFactory(tc -> new TableCell<>() {

            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(value));
                }
            }
        });
    }

    /**
     * Only accessed when dao class is APIDao therefore load more button is visible
     */
    private void loadMoreData(){
        offset += 1;
        dao = new ApiPropertyAssessmentDAO(offset);

        List<PropertyAssessment> moreProperties = dao.multipleParamaters(params);

        if (!moreProperties.isEmpty()){
            properties.addAll(FXCollections.observableArrayList(moreProperties));
            loadDataTable();

            checkLoad(moreProperties);
            }
        }

    /**
     * Given 0 or 1, creates DAO and loads info appropriately
     * @param dataSource Integer
     */
    private void loadSourceData(Integer dataSource){
        if (dataSource == 0) { //dataSource is CSV
            dao = new CsvPropertyAssessmentDAO(filename);
            loadMoreApiDataButton.setVisible(false);
        } else if (dataSource == 1) { //datasource is API
            offset = 0;
            lastLoadFlag = 0;
            dao = new ApiPropertyAssessmentDAO(offset);
            loadMoreApiDataButton.setVisible(true);
        }
        properties = FXCollections.observableArrayList(dao.getAllProperties());

        loadDataTable();
        enableSearchResetButtons();
        setAssessmentClasses();
        setWards();
    }

    /**
     * creates set of assessment classes based on current properties observable list and sets respective combobox to list
     */
    private void setAssessmentClasses() {
        Set<String> assessmentClassNames = dao.getAllProperties().stream()
                .map(PropertyAssessment::getAssessmentClasses)
                .map(AssessmentClasses::getClassNames)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        assessmentClasses = FXCollections.observableArrayList(assessmentClassNames);
        assessmentClassComboBox.setItems(assessmentClasses);
    }

    /**
     * creates set of wards based on dao.allProperties and sets respective combobox to list
     */
    private void setWards() {
        Set<String> wardNames = dao.getAllProperties().stream()
                .map(PropertyAssessment::getLocation)
                .map(Location::getNeighbourhood)
                .map(Neighbourhood::getWard)
                .filter(ward -> !ward.isEmpty())
                .collect(Collectors.toSet());
        wards = FXCollections.observableArrayList(wardNames);
        wardsComboBox.setItems(wards);
    }

    private void enableSearchResetButtons(){
        searchButton.setDisable(false);
        resetButton.setDisable(false);
    }

    /**
     * Populates data table with contents of properties
     */
    private void loadDataTable(){
        assessmentDataTable.setItems(properties);

        accountTableColumn.setCellValueFactory(new PropertyValueFactory<>("account"));
        addressTableColumn.setCellValueFactory(property ->
                new SimpleObjectProperty<>(property.getValue().getLocation().getAddress()));
        assessedValueTableColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        assessmentClassesTableColumn.setCellValueFactory(property ->
                new SimpleObjectProperty<>(property.getValue().getAssessmentClasses()));
        neighbourhoodTableColumn.setCellValueFactory(property ->
                new SimpleObjectProperty<>(property.getValue().getLocation().getNeighbourhood().getName()));
        wardTableColumn.setCellValueFactory(property ->
                new SimpleObjectProperty<>(property.getValue().getLocation().getNeighbourhood().getWard()));
        locationTableColumn.setCellValueFactory(property ->
                new SimpleStringProperty(property.getValue().getLocation().getLatLon()));
    }

    private void search() {
        //build earch param map
        params = new HashMap<>();

        //reset offset & flag when search requested
        if (dao.getClass() == ApiPropertyAssessmentDAO.class){
            offset = 0;
            lastLoadFlag = 0;
            dao = new ApiPropertyAssessmentDAO(offset);
        }

        addTextFieldToParamMap(accountNumberInput, "accountNumber");
        addTextFieldToParamMap(suiteInput, "suite");
        addTextFieldToParamMap(houseNumberInput, "houseNumber");
        addTextFieldToParamMap(streetInput, "streetName");
        addTextFieldToParamMap(neighbourhoodInput, "neighbourhood");
        addTextFieldToParamMap(minValueInput, "minValue");
        addTextFieldToParamMap(maxValueInput, "maxValue");

       if (assessmentClassComboBox.getValue() != null) {
           String aC = "assessmentClass";
           params.put(aC, assessmentClassComboBox.getValue());
       }

        if (wardsComboBox.getValue() != null) {
            String ward = "ward";
            params.put(ward, wardsComboBox.getValue());
        }

       //do search
        try{
            properties = FXCollections.observableArrayList(dao.multipleParamaters(params));
        } catch (NumberFormatException e){
            throwAlert("Number Format Error", """
                        The following fields must consist only of digits 0-9:
                        Account Number
                        House Number
                        Assessed Values""");
            e.printStackTrace();
            return;
        }

       //check if any properties returned
        if (properties.stream().allMatch(PropertyAssessment::emptyProperty)){
            throwAlert("Search Results", "No properties found");
        }
        else {
            checkLoad(properties);
            loadDataTable();
        }
    }

    private void checkLoad(List<PropertyAssessment> propertyAssessmentList){
        if (propertyAssessmentList.size() < 500){
            lastLoadFlag = 1;
            loadMoreApiDataButton.setDisable(true);
        }
    }

    private void addTextFieldToParamMap(TextField textField, String key){
        String text = textField.getText();
        if (text == null || text.isEmpty()){
            return;
        }
        params.put(key, textField.getText());
    }

    private void throwAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private void resetSearchFilters(){
        accountNumberInput.clear();
        suiteInput.clear();
        houseNumberInput.clear();
        streetInput.clear();
        neighbourhoodInput.clear();
        minValueInput.clear();
        maxValueInput.clear();

        assessmentClassComboBox.setValue(null);
        wardsComboBox.setValue(null);
    }
}
