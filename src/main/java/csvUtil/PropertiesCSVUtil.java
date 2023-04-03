package csvUtil;

import classes.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PropertiesCSVUtil extends CSVUtil {
    public List<PropertyAssessment> loadPropertyData(String filename){
        List<PropertyAssessment> propertyList;
        List<List<String>> data = super.importData(filename);

        propertyList = data.stream()
                .map(PropertiesCSVUtil::loadInfoIntoProperty)
                .collect(Collectors.toList());

        return propertyList;
    }

    /**
     * needed for API DAO
     */
    public List<PropertyAssessment> loadPropertyData(BufferedReader reader) throws IOException {
        List<PropertyAssessment> propertyList;
        List<List<String>> data = super.parseCSVString(reader);

        propertyList = data.stream()
                .map(PropertiesCSVUtil::loadInfoIntoProperty)
                .collect(Collectors.toList());

        return propertyList;
    }

    /**
     * loads PropertyAssessment information from a string into a PropertyAssessment Object
     * @param info - String of information on property
     * @return PropertyAssessment Object
     */
    private static PropertyAssessment loadInfoIntoProperty(List<String> info){
        ArrayList<String> scrubbedInfo = new ArrayList<>();

        //remove all " if present
        for (String str:info) {
            scrubbedInfo.add(str.replaceAll("\"", ""));
        }

        int acc = intFromString(scrubbedInfo.get(0));
        String suite = scrubbedInfo.get(1);
        int hNum = intFromString(scrubbedInfo.get(2));
        String sName = scrubbedInfo.get(3);
        String gar = scrubbedInfo.get(4);
        String nName = scrubbedInfo.get(6);
        String ward = scrubbedInfo.get(7);
        int val = intFromString(scrubbedInfo.get(8));
        double lat = Double.parseDouble(scrubbedInfo.get(9));
        double lon = Double.parseDouble(scrubbedInfo.get(10));
        int per1 = intFromString(scrubbedInfo.get(12));
        int per2 = intFromString(scrubbedInfo.get(13));
        int per3 = intFromString(scrubbedInfo.get(14));

        //load assessment classes
        AssessmentClasses aC = null;

        if(scrubbedInfo.size() == 16){
            String cl1 = scrubbedInfo.get(15);
            aC = new AssessmentClasses(per1, cl1);
        }else if (scrubbedInfo.size() == 17) {
            String cl1 = scrubbedInfo.get(15);
            String cl2 = scrubbedInfo.get(16);
            aC = new AssessmentClasses(per1, per2, cl1, cl2);
        }else if (scrubbedInfo.size() == 18) {
            String cl1 = scrubbedInfo.get(15);
            String cl2 = scrubbedInfo.get(16);
            String cl3 = scrubbedInfo.get(17);
            aC = new AssessmentClasses(per1, per2, per3, cl1, cl2, cl3);
        }

        return new PropertyAssessment(acc, val, gar, new Location(lat, lon, new Address(suite, hNum, sName),
                new Neighbourhood(nName, ward)), aC);
    }
}
