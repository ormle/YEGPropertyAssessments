package classes;
import java.util.*;

public class PropertyAssessments{
    private List<PropertyAssessment> properties;
    private int number, min, max, range, median;
    private double mean;

    private TreeMap<Integer, Integer> medianMap; //<valueOfProperty, count>

    public PropertyAssessments(List<PropertyAssessment> propertyList) {
        this.properties = copyList(propertyList);
        this.medianMap = new TreeMap<>();

        if (propertyList.size()>0){
            runStats();
        }
    }

    private List<PropertyAssessment> copyList(List<PropertyAssessment> propertyList){
        List<PropertyAssessment> newList = new ArrayList<>();
        for (PropertyAssessment property:propertyList){
            PropertyAssessment p = new PropertyAssessment(property.getAccount(), property.getValue(), property.getGarage(),
                    property.getLocation(), property.getAssessmentClasses());
            newList.add(p);
        }
        return newList;
    }

    public List<PropertyAssessment> getAllProperties(){
        return copyList(this.properties);
    }

    /**
     * Find the property for a given account number if property is on list
     * @param account - int account number to find
     * @return PropertyAssessment object with the account number
     */
    public PropertyAssessment getPropertyInfo(int account){
        for (PropertyAssessment property:properties) {
            if (property.getAccount() == account){
                return property;
            }
        }
        return new PropertyAssessment();
    }

    private void runStats(){
        this.number = properties.size();
        int val;
        double sum = 0;

        //set min/max to first property value in list
        min = properties.get(0).getValue();
        max = properties.get(0).getValue();

        for (PropertyAssessment property:properties){
            val = property.getValue();
            sum += val;

            if (val<min){
                min = val;
            }
            if (val>max){
                max=val;
            }
            //create treemap to use for finding median
            medianMap.merge(val, 1, Integer::sum);
        }

        this.range = max - min;
        this.mean = Math.round(sum/number);

        findMedian();
    }

    private void findMedian(){
        int count = 0;

        //find median when odd number of properties
        if (number%2 > 0) {
            for (Map.Entry<Integer, Integer> entry : medianMap.entrySet()) {
                count += entry.getValue();
                if (count > (number/2)) {
                    this.median = entry.getKey();
                    return;
                }
            }
        }else{
            //set first price value
            int before = medianMap.firstKey();

            for (Map.Entry<Integer, Integer> entry : medianMap.entrySet()) {
                count += entry.getValue();

                if (count > (number/2)) { //median has been reached
                    if (entry.getValue() == 1){
                        //then we need to consider the previous entry when reporting median
                        this.median = (before + entry.getKey())/2;
                    }else{
                        this.median = entry.getKey();
                    }
                    return;
                }else{ //set before to current price value and continue loop
                    before = entry.getKey();
                }
            }
        }
    }

    /**
     * change property list; recalculates statistics for new list
     * @param newProperties - List of PropertyAsessment objects
     */
    public void setProperties(List<PropertyAssessment> newProperties){
        this.properties = List.copyOf(newProperties);
        this.medianMap = new TreeMap<>(); //because new list
        runStats();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyAssessments that = (PropertyAssessments) o;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(properties);
    }

    @Override
    public String toString() {
        String minS = String.format("%,d", min);
        String maxS = String.format("%,d", max);
        String rangeS = String.format("%,d", range);
        String meanS = String.format("%,.0f", mean);
        String medianS = String.format("%,d", median);

        return "n = " + number + "\nmin = $" + minS + "\nmax = $" + maxS + "\nrange = $" + rangeS + "\nmean = $" + meanS +
                "\nmedian = $" + medianS;
    }
}
