package DAO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import csvUtil.*;
import classes.*;

public class BusinessDAO {
    public final List<Business> businesses;
    private final String neighbourhood = "neighbourhood";
    private final String ward = "ward";
    private final String name = "name";

    public BusinessDAO(String filename){
        BusinessesCSVUtil csvUtil = new BusinessesCSVUtil();
        this.businesses = csvUtil.loadBusinessData(filename);
    }

    public List<Business> getAllBusinesses() {
        return businesses;
    }

    public List<Business> getCannabisRetail(){
        return businesses.stream()
                .filter(business-> business.hasCategory("Cannabis Retail Sales"))
                .collect(Collectors.toList());
    }

    /**
     * Sells alcohol for off site consumption AND is not a restaurant
     * Restaurants are not considered liquor stores but some restaurants sell liquor via delivery for off site consumption
     */
    public List<Business> getAlcoholRetail(){
        return businesses.stream()
                .filter(business-> business.hasCategory("Alcohol Sales (Consumption Off-Premises)"))
                .filter(business-> !business.hasCategory("Restaurant or Food Service"))
                .collect(Collectors.toList());
    }

    public List<Business> getRestaurants(){
        return businesses.stream()
                .filter(business-> business.hasCategory("Restaurant or Food Service"))
                .collect(Collectors.toList());
    }

    public List<Business> getBars(){
        return businesses.stream()
                .filter(business-> business.hasCategory("Alcohol Sales (Consumption On-Premises / Minors Prohibited)"))
                .collect(Collectors.toList());
    }

    public List<Business> getByName(String name) {
        return businesses.stream()
                .filter(business -> business.getName().toUpperCase().contains(name.toUpperCase()))
                .collect(Collectors.toList());
    }

    public List<Business> getByAddress(String suite, int housenumber, String streetName) {
        if ((suite == null || suite.isEmpty()) && housenumber == 0){
            return businesses.stream()
                    .filter(business -> business.getLocation().getAddress().getStreetName().toUpperCase().contains(streetName.toUpperCase()))
                    .collect(Collectors.toList());
        } else if ((suite == null || suite.isEmpty()) && housenumber > 0) {
            return businesses.stream()
                    .filter(business -> business.getLocation().getAddress().getStreetName().toUpperCase().contains(streetName.toUpperCase()))
                    .filter(business -> business.getLocation().getAddress().getHouseNumber() == housenumber)
                    .collect(Collectors.toList());
        }
        return businesses.stream()
                .filter(business -> business.getLocation().getAddress().getSuite().equalsIgnoreCase(suite))
                .filter(business -> business.getLocation().getAddress().getStreetName().toUpperCase().contains(streetName.toUpperCase()))
                .filter(business -> business.getLocation().getAddress().getHouseNumber() == housenumber)
                .collect(Collectors.toList());
    }

    public List<Business> getByNeighbourhood(String neighbourhood) {
        return businesses.stream()
                .filter(business -> business.getLocation().getNeighbourhood().getName().toUpperCase().contains(neighbourhood.toUpperCase()))
                .collect(Collectors.toList());
    }

    public List<Business> getByWard(String ward) {
        return businesses.stream()
                .filter(business -> business.getLocation().getNeighbourhood().getWard().toUpperCase().contains(ward.toUpperCase()))
                .collect(Collectors.toList());
    }


    public List<Business> multipleParamaters(Map<String, String> params) throws NumberFormatException{

        //no params so return empty list
        if (params.isEmpty()){
            return List.of(new Business());
        }

        List<Business> businessList = initializeSearchList(params);

        //filter initialized list
        return filterSearchList(params, businessList);
    }

    private List<Business> filterSearchList(Map<String, String> params, List<Business> businessList) {

        String suite = "suite";
        String house = "houseNumber";
        String street = "streetName";

        if (params.containsKey(suite)){
            businessList = businessList.stream().
                    filter(b -> b.getLocation().getAddress().getSuite().toUpperCase().contains(params.get(suite).toUpperCase()))
                    .collect(Collectors.toList());
        }

        if (params.containsKey(house)){
            businessList = businessList.stream().
                    filter(b -> b.getLocation().getAddress().getHouseNumber() == Integer.parseInt(params.get(house)))
                    .collect(Collectors.toList());
        }

        if (params.containsKey(street)){
            businessList = businessList.stream().
                    filter(b -> b.getLocation().getAddress().getStreetName().toUpperCase().contains(params.get(street).toUpperCase()))
                    .collect(Collectors.toList());
        }

        if (params.containsKey(name)) {
            businessList = businessList.stream().
                    filter(b -> b.getName().toUpperCase().contains(params.get(name).toUpperCase()))
                    .collect(Collectors.toList());
        }
        if (params.containsKey(neighbourhood)) {
            businessList = businessList.stream().
                    filter(b -> b.getLocation().getNeighbourhood().getName().toUpperCase().contains(params.get(neighbourhood).toUpperCase()))
                    .collect(Collectors.toList());
        }

        if (params.containsKey(ward)) {
            businessList = businessList.stream().
                    filter(b -> b.getLocation().getNeighbourhood().getWard().toUpperCase().contains(params.get(ward).toUpperCase()))
                    .collect(Collectors.toList());

        }

        return businessList;

    }

    private List<Business> initializeSearchList(Map<String, String> params){
        String business = params.getOrDefault("businessType", "").toUpperCase();

        if (business.contains("BAR")){
            return getBars();
        } else if (business.contains("RESTAURANT")) {
            return getRestaurants();
        } else if (business.contains("LIQUOR") || business.contains("ALCOHOL")) {
            return getAlcoholRetail();
        } else if (business.contains("CANNABIS")) {
            return getCannabisRetail();
        } else{
            return getAllBusinesses();
        }
    }

}
