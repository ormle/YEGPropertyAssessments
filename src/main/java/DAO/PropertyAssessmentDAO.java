package DAO;

import java.util.List;
import java.util.Map;
import classes.PropertyAssessment;

public interface PropertyAssessmentDAO {
    PropertyAssessment getByAccountNumber(int accountNumber);
    List<PropertyAssessment> getByAddress(String suite, int housenumber, String streetName);
    List<PropertyAssessment> getByNeighbourhood(String neighbourhood);
    List<PropertyAssessment> getByWard(String ward);
    List<PropertyAssessment> getByAssessmentClass(String assessmentClass);
    List<PropertyAssessment> getBetweenValues(Integer min, Integer max);
    List<PropertyAssessment> getAllProperties();

    /**
     * Appropriate keys are: accountNumber, suite, houseNumber, streetName, neighbourhood, assessmentClass, minValue,
     *                      maxValue, ward
     */
    List<PropertyAssessment> multipleParamaters(Map<String, String> params);

}

