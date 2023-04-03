package classes;

import java.util.Objects;

public class Business {
    private String licence; //unique number
    private String name;
    private String categories;
    private Location location;

    public Business() {
    }

    public Business(String licence, String name, String categories, Location location) {
        this.licence = licence;
        this.name = name;
        this.categories = categories;
        this.location = new Location(location.getLat(), location.getLon(), location.getAddress(), location.getNeighbourhood());
    }

    public Location getLocation(){
        return new Location(location.getLat(), location.getLon(), location.getAddress(), location.getNeighbourhood());
    }

    public String getName(){
        return this.name;
    }

    public String getCategories(){return this.categories;}

    public boolean hasCategory(String category){
        if (this.categories.isEmpty())
            return false;

        return this.categories.contains(category);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Business that = (Business) o;
        return Objects.equals(licence, that.licence)  && Objects.equals(name, that.name) && Objects.equals(categories, that.categories)
            && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licence, name, categories, location);
    }

    @Override
    public String toString(){
        if (location != null) {
            return "Licence = " + licence + "\nName = " + name + "\nCategories = " + categories
                    + "\nAddress = " + location.getAddress() + "\nNeighbourhood = " + location.getNeighbourhood()
                    + "\nLocation = (" + location.getLat() + " " + location.getLon() + ")";
        }else {
            return "Licence = " + licence + "\nName = " + name + "\nCategories = " + categories
                    + "\nAddress = \n \nNeighbourhood = \nLocation = ";
        }
    }


    public boolean emptyBusiness() {
        Business empty = new Business();
        return this.equals(empty);
    }

}
