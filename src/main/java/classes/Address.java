package classes;

import java.util.Objects;

public class Address{
    private String suite; //some suites have letters
    private int houseNumber;
    private String streetName;

    public Address() {
        this ("", 0, "");
    }

    public Address(String streetName) {
        this ("", 0, streetName);
    }

    public Address(int houseNumber, String streetName) {
        this ("", houseNumber, streetName);
    }

    public Address(String suite, int houseNumber, String streetName) {
        this.suite = suite;
        this.houseNumber = houseNumber;
        this.streetName = streetName;
    }

    public void setSuite(String suite) {
        this.suite = suite;
    }

    public void setHouseNumber(int houseNumber) {
        this.houseNumber = houseNumber;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getSuite() {
        return suite;
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    public boolean isEmpty(){
        return this.suite.isEmpty() && this.houseNumber == 0 && this.streetName.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(suite,address.suite) && houseNumber == address.houseNumber && Objects.equals(streetName, address.streetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suite, houseNumber, streetName);
    }

    @Override
    public String toString() {
        if (this.isEmpty()){
            return "";
        } else if (suite.isEmpty() && houseNumber == 0 && !streetName.isEmpty()) {
            return streetName;
        } else if (suite.isEmpty() && houseNumber != 0 && !streetName.isEmpty()) {
            return houseNumber + " " + streetName;
        } else {
            return suite + " " + houseNumber + " " + streetName;
        }
    }
}
