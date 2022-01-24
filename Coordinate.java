public class Coordinate {
    Double longitude = null;
    Double latitude = null;
    public Coordinate(double coordinateLongitude, double coordinateLatitude){
        longitude = coordinateLongitude;
        latitude = coordinateLatitude;
    }

    public Double getCoordinateLongitude() {
        return longitude;
    }

    public Double getCoordinateLatitude() {
        return latitude;
    }
}
