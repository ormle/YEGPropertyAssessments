package DAO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import classes.*;
import csvUtil.*;

public class ApiPropertyAssessmentDAO implements PropertyAssessmentDAO {
    private final String endpoint = "https://data.edmonton.ca/resource/q7d6-ambg.csv";
    public int offset;
    private String paging;
    private final int limit = 500;

    public ApiPropertyAssessmentDAO(int offset) {
        this.offset = offset;
        setPaging();
    }

    public ApiPropertyAssessmentDAO() {
        this.offset = 0;
        setPaging();
    }

    private void setPaging() {
        paging = "&$limit=" + limit + "&$offset=" + (offset * limit) + "&$order=account_number";
    }

    public String makeGenericUrl(String searchParamUrl) {
        return endpoint + "?$where=" + searchParamUrl + paging;
    }

    private List<PropertyAssessment> getProperties(String url) {
        List<PropertyAssessment> properties = null;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .GET().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            BufferedReader reader = new BufferedReader(new StringReader(response.body()));
            PropertiesCSVUtil csvUtil = new PropertiesCSVUtil();

            properties = csvUtil.loadPropertyData(reader);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Error accessing https://data.edmonton.ca/");
        }

        return properties;
    }

    @Override
    public PropertyAssessment getByAccountNumber(int accountNumber) {
        PropertyAssessment p = new PropertyAssessment();

        String account = URLEncoder.encode(String.valueOf(accountNumber), StandardCharsets.UTF_8);

        String url = endpoint + "?account_number=" + account + paging;

        List<PropertyAssessment> properties = getProperties(url);
        if (properties.size() > 0) {
            p = properties.get(0);
        }
        return p;
    }

    //----//
    @Override
    public List<PropertyAssessment> getByAddress(String suite, int housenumber, String streetName) {
        return getProperties(makeGenericUrl(makeAddressUrl(suite, housenumber, streetName)));
    }

    private String makeAddressUrl(String suite, int housenumber, String streetName) {
        String url;

        streetName = URLEncoder.encode(streetName, StandardCharsets.UTF_8);
        suite = URLEncoder.encode(suite, StandardCharsets.UTF_8);

        if ((suite == null || suite.isEmpty()) && housenumber == 0) {
            url = URLEncoder.encode("street_name like '%", StandardCharsets.UTF_8) + streetName
                    + URLEncoder.encode("%'", StandardCharsets.UTF_8);
        } else if (suite == null || suite.isEmpty()) { // and there is a houseNumber
            url = URLEncoder.encode("street_name like '%", StandardCharsets.UTF_8) + streetName
                    + URLEncoder.encode("%' AND house_number='", StandardCharsets.UTF_8) + housenumber + "'";
        } else {
            url = URLEncoder.encode("street_name like '%", StandardCharsets.UTF_8) + streetName
                    + URLEncoder.encode("%' AND house_number='", StandardCharsets.UTF_8) + housenumber
                    + URLEncoder.encode("' AND suite='", StandardCharsets.UTF_8) + suite + "'";
        }

        return url;
    }

    //----//
    @Override
    public List<PropertyAssessment> getByNeighbourhood(String neighbourhood) {
        return getProperties(makeGenericUrl(makeNeighbourhoodUrl(neighbourhood)));
    }

    public String makeNeighbourhoodUrl(String neighbourhood) {
        return "neighbourhood" + URLEncoder.encode(" like '%" + neighbourhood.toUpperCase() + "%", StandardCharsets.UTF_8) + "'";
    }

    @Override
    public List<PropertyAssessment> getByWard(String ward) {
        return getProperties(makeGenericUrl(makeWardUrl(ward)));
    }

    public String makeWardUrl(String ward) {
        return "ward" + URLEncoder.encode(" like '%" + ward + "%", StandardCharsets.UTF_8) + "'";
    }

    //----//
    @Override
    public List<PropertyAssessment> getByAssessmentClass(String assessmentClass) {
        return getProperties(makeGenericUrl(makeAssessmentClassUrl(assessmentClass)));
    }

    private String makeAssessmentClassUrl(String assessmentClass) {
        assessmentClass = URLEncoder.encode(assessmentClass.toUpperCase(), StandardCharsets.UTF_8);

        return "mill_class_1='" + assessmentClass
                + URLEncoder.encode("' OR mill_class_2='", StandardCharsets.UTF_8) + assessmentClass
                + URLEncoder.encode("' OR mill_class_3='", StandardCharsets.UTF_8) + assessmentClass + "'";
    }

    //----//
    @Override
    public List<PropertyAssessment> getBetweenValues(Integer min, Integer max) {
        if (min == null && max == null) //no search params
            return new ArrayList<>();

        return getProperties(makeGenericUrl(makeBetweenValuesUrl(min, max)));
    }


    private String makeBetweenValuesUrl(Integer min, Integer max) {
        String url;

        if (min == null) { //max != null
            url = "assessed_value" + URLEncoder.encode("<='", StandardCharsets.UTF_8) + max + "'";

        } else if (max == null) { //min != null
            url = "assessed_value" + URLEncoder.encode(">='", StandardCharsets.UTF_8) + min + "'";
        } else {
            //here when both min & max != null
            url = "assessed_value>='" + min
                    + URLEncoder.encode("' AND assessed_value<='", StandardCharsets.UTF_8) + max + "'";
        }
        return url;
    }

    private String makeBetweenValuesUrl(String min, String max) {
        String url;

        if (min == null) { //max != null
            url = "assessed_value" + URLEncoder.encode("<='", StandardCharsets.UTF_8) + max + "'";

        } else if (max == null) { //min != null
            url = "assessed_value" + URLEncoder.encode(">='", StandardCharsets.UTF_8) + min + "'";
        } else {
            //here when both min & max != null
            url = "assessed_value" + URLEncoder.encode(">='", StandardCharsets.UTF_8) + min
                    + URLEncoder.encode("' AND assessed_value<='", StandardCharsets.UTF_8) + max + "'";
        }
        return url;
    }


    /**
     * Returns first 1000 properties from API website
     */
    @Override
    public List<PropertyAssessment> getAllProperties() {
        return getProperties(endpoint);
    }

    @Override
    public List<PropertyAssessment> multipleParamaters(Map<String, String> params) {
        //no params so return empty list
        if (params.isEmpty()) {
            return List.of(new PropertyAssessment());
        }

        //return property if account number is requested; only ever one property per account
        String account = "accountNumber";
        if (params.containsKey(account)) {
            return List.of(getByAccountNumber(Integer.parseInt(params.get(account))));
        }

        //determine which search params are present, create URLs
        String house = "houseNumber";
        String suite = "suite";
        String street = "streetName";
        String neighbourhood = "neighbourhood";
        String ward = "ward";
        String aC = "assessmentClass";
        String min = "minValue";
        String max = "maxValue";

        List<String> urls = new ArrayList<>();
        if (params.containsKey(suite) || params.containsKey(house) || params.containsKey(street)) {
            if (!params.containsKey((house))) {
                urls.add(makeAddressUrl(params.getOrDefault(suite, ""), 0, params.get(street).toUpperCase()));
            } else {
                urls.add(makeAddressUrl(params.get(suite), Integer.parseInt(params.get(house)), params.get(street).toUpperCase()));
            }
        }

        if (params.containsKey(neighbourhood)) {
            urls.add(makeNeighbourhoodUrl(params.get(neighbourhood).toUpperCase()));
        }

        if (params.containsKey(ward)) {
            urls.add(makeWardUrl(params.get(ward)));
        }

        if (params.containsKey(aC)) {
            urls.add(makeAssessmentClassUrl(params.get(aC)));
        }

        if (params.containsKey(min) || params.containsKey(max)) {
            urls.add(makeBetweenValuesUrl(params.getOrDefault(min, null), params.getOrDefault(max, null)));
        }

        //construct searchURL
        StringBuilder searchURL = new StringBuilder();

        int urlSize = urls.size();

        for (int i =0; i<urlSize; i++)
            if (i==0){
                searchURL.append(urls.get(i));

                if (urlSize != 1) {
                    searchURL.append(URLEncoder.encode(" AND (", StandardCharsets.UTF_8));
                }
            } else if (i==urlSize-1){
                searchURL.append(urls.get(i));
                searchURL.append(URLEncoder.encode(")", StandardCharsets.UTF_8));
            } else {
                searchURL.append(urls.get(i));
                searchURL.append(URLEncoder.encode(") AND (", StandardCharsets.UTF_8));
            }

        String URL = makeGenericUrl(String.valueOf(searchURL));

        return getProperties(URL);
    }
}

