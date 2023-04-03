package DAO;

import classes.*;
import csvUtil.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvPropertyAssessmentDAO implements PropertyAssessmentDAO{

    private final PropertyAssessments csvProperties;
    private final String neighbourhood = "neighbourhood";
    private final String aC = "assessmentClass";
    private final String ward = "ward";
    private final String min = "minValue";
    private final String max = "maxValue";


    public CsvPropertyAssessmentDAO(PropertyAssessments csvProperties){
        this.csvProperties = csvProperties;
    }

    public CsvPropertyAssessmentDAO(String fileName){
        PropertiesCSVUtil csvUtil = new PropertiesCSVUtil();
        this.csvProperties = new PropertyAssessments(csvUtil.loadPropertyData(fileName));
    }

    public List<PropertyAssessment> getAllProperties() {
        return csvProperties.getAllProperties();
    }

    @Override
    public List<PropertyAssessment> multipleParamaters(Map<String, String> params) throws NumberFormatException{
        List<PropertyAssessment> propertiesList = List.of(new PropertyAssessment());

        //no params so return empty list
        if (params.isEmpty()){
            return List.of(new PropertyAssessment());
        }

        //return property if account number is requested; only ever one property per account
        String account = "accountNumber";
        if (params.containsKey(account)) {
            return List.of(getByAccountNumber(Integer.parseInt(params.get(account))));
        }

        propertiesList = initializeSearchList(params, propertiesList);

        //filter initialized list
        return filterSearchList(params, propertiesList);
    }

    private List<PropertyAssessment> filterSearchList(Map<String, String> params, List<PropertyAssessment> propertiesList){
        if (params.containsKey(neighbourhood)){
            propertiesList = propertiesList.stream().
                    filter(p->p.getLocation().getNeighbourhood().getName().contains(params.get(neighbourhood).toUpperCase()))
                    .collect(Collectors.toList());
        }

        if (params.containsKey(ward)){
            propertiesList = propertiesList.stream().
                    filter(p->p.getLocation().getNeighbourhood().getWard().toUpperCase().contains(params.get(ward).toUpperCase()))
                    .collect(Collectors.toList());
        }

        if (params.containsKey(aC)){
            propertiesList = propertiesList.stream().
                    filter(p->p.getAssessmentClasses().hasClass(params.get(aC)))
                    .collect(Collectors.toList());
        }

        if (params.containsKey(min)) {
            propertiesList = propertiesList.stream().
                    filter(p->p.getValue() >= Integer.parseInt(params.get(min)))
                    .collect(Collectors.toList());
        }

        if (params.containsKey(max)) {
            propertiesList = propertiesList.stream().
                    filter(p->p.getValue() <= Integer.parseInt(params.get(max)))
                    .collect(Collectors.toList());
        }
        return propertiesList;
    }

    private List<PropertyAssessment> initializeSearchList(Map<String, String> params, List<PropertyAssessment> propertiesList){

        String suite = "suite";
        String house = "houseNumber";
        String street = "streetName";

        //initialize propertiesList with the most restrictive search param
        if (params.containsKey(suite) || params.containsKey(house) || params.containsKey(street)){
            if (!params.containsKey(house)){
                propertiesList = getByAddress(params.get(suite), 0, params.get(street));
            } else {
                propertiesList = getByAddress(params.get(suite), Integer.parseInt(params.get(house)), params.get(street));
            }
        } else if (params.containsKey(neighbourhood)) {
            propertiesList = getByNeighbourhood(params.get(neighbourhood));
        } else if (params.containsKey(ward)) {
            propertiesList = getByWard(params.get(ward));
        } else if (params.containsKey(aC)) {
            propertiesList = getByAssessmentClass(params.get(aC));
        } else if (params.containsKey(min) || params.containsKey(max)) {
            if (!params.containsKey(min)){ //no min param
                propertiesList = getBetweenValues(null, Integer.parseInt(params.get(max)));
            } else if (!params.containsKey(max)) { //no max param
                propertiesList = getBetweenValues(Integer.parseInt(params.get(min)), null);
            } else {//both params are present
                propertiesList = getBetweenValues(Integer.parseInt(params.get(min)), Integer.parseInt(params.get(max)));
            }
        }
        return propertiesList;
    }

    @Override
    public PropertyAssessment getByAccountNumber(int accountNumber) {
        return csvProperties.getPropertyInfo(accountNumber);
    }

    @Override
    public List<PropertyAssessment> getByNeighbourhood(String neighbourhood) {
        return csvProperties.getAllProperties().stream().
                filter(property -> property.getLocation().getNeighbourhood().getName().contains(neighbourhood.toUpperCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyAssessment> getByWard(String ward) {
        return csvProperties.getAllProperties().stream().
                filter(property -> property.getLocation().getNeighbourhood().getWard().toUpperCase().contains(ward.toUpperCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyAssessment> getByAssessmentClass(String assessmentClass) {
        return csvProperties.getAllProperties().stream().
                filter(property -> property.getAssessmentClasses().hasClass(assessmentClass.toUpperCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyAssessment> getByAddress(String suite, int housenumber, String streetName) {

        if ((suite == null || suite.isEmpty()) && housenumber == 0){
            return csvProperties.getAllProperties().stream()
                    .filter(property -> property.getLocation().getAddress().getStreetName().contains(streetName.toUpperCase()))
                    .collect(Collectors.toList());
        } else if (suite == null || suite.isEmpty()) { // and there is a houseNumber
            return csvProperties.getAllProperties().stream()
                    .filter(property -> property.getLocation().getAddress().getStreetName().contains(streetName.toUpperCase()))
                    .filter(property -> property.getLocation().getAddress().getHouseNumber() == housenumber)
                    .collect(Collectors.toList());
        }

        return csvProperties.getAllProperties().stream()
                .filter(property -> property.getLocation().getAddress().getSuite().equalsIgnoreCase(suite))
                .filter(property -> property.getLocation().getAddress().getStreetName().contains(streetName.toUpperCase()))
                .filter(property -> property.getLocation().getAddress().getHouseNumber() == housenumber)
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyAssessment> getBetweenValues(Integer min, Integer max) {
        if (min == null && max == null) //no search params
            return new ArrayList<>();

        else if (min == null) { //max != null
            return csvProperties.getAllProperties().stream()
                    .filter(property -> property.getValue() <= max)
                    .collect(Collectors.toList());

        } else if (max == null) { //min != null
            return csvProperties.getAllProperties().stream()
                    .filter(property -> property.getValue() >= min)
                    .collect(Collectors.toList());
        }
        //here when both min & max != null
        return csvProperties.getAllProperties().stream()
                .filter(property -> property.getValue() >= min)
                .filter(property -> property.getValue() <= max)
                .collect(Collectors.toList());
    }

}
