package csvUtil;

import classes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BusinessesCSVUtil extends CSVUtil {
    public List<Business> loadBusinessData(String filename){
        List<Business> businessList;
        List<List<String>> data = super.importData(filename);

        businessList = data.stream()
                .map(BusinessesCSVUtil::loadInfoIntoBusiness)
                .collect(Collectors.toList());

        return businessList;
    }

    /**
     * loads Business information from a string into a Business Object
     * @param info - String of information on Business
     * @return Business Object
     */
    private static Business loadInfoIntoBusiness(List<String> info){
        ArrayList<String> scrubbedInfo = new ArrayList<>();

        //remove all " if present
        for (String str:info) {
            scrubbedInfo.add(str.replaceAll("\"", ""));
        }

        String category = scrubbedInfo.get(0);
        String name = scrubbedInfo.get(1);

        String licence = scrubbedInfo.get(3);

        String nName = scrubbedInfo.get(9);
        String ward = scrubbedInfo.get(10);

        double lat = doubleFromString(scrubbedInfo.get(11));
        double lon = doubleFromString(scrubbedInfo.get(12));

        return new Business(licence, name, category, new Location(lat, lon, parseAddress(scrubbedInfo.get(2)),
                new Neighbourhood(nName, ward)));
    }

    /**
     * change string into double or 0 if string is empty
     * @param str - String
     * @return int
     */
    public static double doubleFromString(String str){
        if (str.equals("")){
            return 0;
        }
        return Double.parseDouble(str.replaceAll(" ", ""));
    }

    private static Address parseAddress(String str){

        if (str.isEmpty()){
            return new Address();
        } else{
            str = str.replaceAll("\"", "");
            List<String> address = List.of(str.split("[,-]"));
            switch (address.size()) {
                case 1 -> { //only street name
                    return new Address(address.get(0));
                }
                case 2 -> { //houseNum & streetName
                    return new Address(intFromString(address.get(0)), address.get(1));
                }
                case 3 -> { //suiteNum, HouseNum, StreetName
                    return new Address(address.get(0), intFromString(address.get(1)), address.get(2));
                }
            }
        }
        return new Address();
    }
}
