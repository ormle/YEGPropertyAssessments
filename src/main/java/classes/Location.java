package classes;

import java.util.Objects;

public class Location{
    private double lat;
    private double lon;

    //TODO add point?

    private Address address;
    private Neighbourhood neighbourhood;

    public Location(){
        this(0,0,new Address(), new Neighbourhood());
    }

    public Location(double lat, double lon) {
        this(lat,lon,new Address(), new Neighbourhood());
    }

    public Location(double lat, double lon, Address address) {
        this(lat,lon,address, new Neighbourhood());
    }

    public Location(double lat, double lon, Address address, Neighbourhood neighbourhood) {
        this.lat = lat;
        this.lon = lon;

        if (address != null) {
            this.address = new Address(address.getSuite(), address.getHouseNumber(), address.getStreetName());
        }

        if (neighbourhood != null) {
            this.neighbourhood = new Neighbourhood(neighbourhood.getName(), neighbourhood.getWard());
        }
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public void setAddress(Address address){
        this.address = new Address(address.getSuite(), address.getHouseNumber(), address.getStreetName());
    }
    public void setNeighbourhood(Neighbourhood neighbourhood){
        this.neighbourhood = new Neighbourhood(neighbourhood.getName(), neighbourhood.getWard());
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public Neighbourhood getNeighbourhood(){
        if (neighbourhood!=null) {
            return new Neighbourhood(neighbourhood.getName(), neighbourhood.getWard());
        }
        return new Neighbourhood();
    }

    public Address getAddress(){
        if (address!=null) {
            return new Address(address.getSuite(), address.getHouseNumber(), address.getStreetName());
        }
        return new Address();
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon, address, neighbourhood);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;

        if (neighbourhood!=null){
            return lat == location.lat && lon == location.lon && address.equals(location.address) &&
                    neighbourhood.equals(location.neighbourhood);
        } else if (address!=null) {
            return lat == location.lat && lon == location.lon && address.equals(location.address);
        }
        return lat == location.lat && lon == location.lon;
    }

    @Override
    public String toString() {
        if (neighbourhood.isEmpty() && address.isEmpty()) {
            return "(" + lat + " " + lon + ")";
        } else if (neighbourhood.isEmpty() && !address.isEmpty()) {
            return address + "\n(" + lat + " " + lon + ")";
        } else {
            return address + "\n" + neighbourhood + "\n(" + lat + " " + lon + ")";
        }
    }

    public String getLatLon() {
        return "(" + lat + ", " + lon + ")";
    }
}

