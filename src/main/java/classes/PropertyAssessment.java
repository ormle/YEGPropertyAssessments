package classes;

import java.util.Objects;

public class PropertyAssessment implements Comparable<PropertyAssessment> {
    //variables
    private int account;
    private int value;
    private String garage;
    private Location location;
    private AssessmentClasses assessmentClasses;


    public PropertyAssessment(){
    }

    public PropertyAssessment(int account, int value, String garage, Location location, AssessmentClasses aC) {
        this.account = account;
        this.value = value;
        this.garage = garage;
        this.location = new Location(location.getLat(), location.getLon(), location.getAddress(), location.getNeighbourhood());
        this.assessmentClasses = new AssessmentClasses(aC.getClassPer1(), aC.getClassPer2(), aC.getClassPer3(), aC.getClass1(),
                aC.getClass2(), aC.getClass3());
    }


    //setters
    public void setAccount(int account){ this.account = account;}
    public void setGarage(String garage){this.garage = garage;}
    public void setValue(int value){this.value = value;}

    public void setLocation(Location location){
        this.location = new Location(location.getLat(), location.getLon(), location.getAddress(), location.getNeighbourhood());
    }

    public void setAssessmentClasses(AssessmentClasses aC){
        this.assessmentClasses = new AssessmentClasses(aC.getClassPer1(), aC.getClassPer2(), aC.getClassPer3(), aC.getClass1(),
                aC.getClass2(), aC.getClass3());
    }

    //getters
    public int getAccount(){ return this.account;}
    public String getGarage(){return this.garage;}
    public int getValue(){return this.value;}

    public Location getLocation(){
        return new Location(location.getLat(), location.getLon(), location.getAddress(), location.getNeighbourhood());
    }

    public AssessmentClasses getAssessmentClasses(){
       return new AssessmentClasses(assessmentClasses.getClassPer1(), assessmentClasses.getClassPer2(), assessmentClasses.getClassPer3(),
                    assessmentClasses.getClass1(), assessmentClasses.getClass2(),assessmentClasses.getClass3());
    }

    @Override
    public int compareTo(PropertyAssessment o){
        return value-o.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyAssessment that = (PropertyAssessment) o;
        return account == that.account && value == that.value && Objects.equals(garage, that.garage) &&
                Objects.equals(location, that.location) && Objects.equals(assessmentClasses, that.assessmentClasses);
    }

    public boolean emptyProperty(){
        PropertyAssessment empty = new PropertyAssessment();
        return this.equals(empty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, value, garage, location, assessmentClasses);
    }

    @Override
    public String toString(){
        String val = String.format("%,d", value);
        if (location != null) {
            return "Account = " + account + "\nAddress = " + location.getAddress() + "\nAssessed value = $" + val
                    + "\nAssessment class = " + assessmentClasses + "\nNeighbourhood = " + location.getNeighbourhood()
                    + "\nLocation = (" + location.getLat() + " " + location.getLon() + ")";
        }else {
            return "Account = " + account + "\nAddress = \nAssessed value = $" + val
                    + "\nAssessment class = " + assessmentClasses + "\nNeighbourhood = "
                    + "\nLocation = ";
        }
    }
}
