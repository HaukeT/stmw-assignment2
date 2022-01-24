import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Searcher {

    public Searcher() {
    }

    public static void main(String[] args) throws Exception {
        String usage = "java Searcher";
        boolean x = false;
        boolean y = false;
        boolean w = false;
        boolean coordinates = false;
        double latitude = 0;
        double longitude = 0;
        double radius = 0;
        for (String arg: args) {
            if (arg.equals("-x") || arg.equals("-y") || arg.equals("-w")){
                coordinates = true;
                switch (arg){
                    case "-x": x = true;
                        break;
                    case "-y": y = true;
                        break;
                    case "-w": w = true;
                        break;
                }
            } else {
                if (x)
                    longitude = Double.parseDouble(arg);
                if (y)
                    latitude = Double.parseDouble(arg);
                if (w)
                    radius = Double.parseDouble(arg);

                x = false;
                y = false;
                w = false;
            }
        }
        if (coordinates)
            search(args[0], "indexes", longitude, latitude, radius);
        else
            search(args[0], "indexes");
    }

    private static TopDocs search(String searchText, String p) {
        System.out.println("Running search(" + searchText + ")");
        try {
            Path path = Paths.get(p);
            Directory directory = FSDirectory.open(path);
            IndexReader indexReader = DirectoryReader.open(directory);
            final IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            QueryParser queryParser = new QueryParser("concatColumns", new SimpleAnalyzer());
            Query query = queryParser.parse(searchText);
            TopDocs topDocs = indexSearcher.search(query, 10000);

            ArrayList<ScoreDoc> topDocsList = new ArrayList<>(Arrays.asList(topDocs.scoreDocs));

            Collections.sort(topDocsList, new Comparator<ScoreDoc>() {
                @Override
                public int compare(ScoreDoc o1, ScoreDoc o2) {
                    int i = Double.compare(o2.score, o1.score);
                    if (i == 0){
                        try {
                            Document document1 = indexSearcher.doc(o1.doc);
                            Document document2 = indexSearcher.doc(o2.doc);
                            i = Double.compare(Double.parseDouble(ItemPrice(document1.get("item_id"))),
                                    Double.parseDouble(ItemPrice(document2.get("item_id"))));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return i;
                    }
                    return i;
                }
            });

            System.out.println("Number of Hits: " + topDocsList.size());
            for (ScoreDoc scoreDoc : topDocsList) {
                Document document = indexSearcher.doc(scoreDoc.doc);
                System.out.println("item_id: " + document.get("item_id") + ", "
                        + document.get("concatColumns").substring(0, 64) + ", score: " + scoreDoc.score +
                        ", price: " + ItemPrice(document.get("item_id")));
            }


            return topDocs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static TopDocs search(String searchText, String p, final Double longitude, final Double latitude, Double radius) {
        System.out.println("Running search(" + searchText + ", latitude: " + latitude + ", longitude: " + longitude + ", radius: " + radius + ")");
        try {
            Path path = Paths.get(p);
            Directory directory = FSDirectory.open(path);
            IndexReader indexReader = DirectoryReader.open(directory);
            final IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            QueryParser queryParser = new QueryParser("concatColumns", new SimpleAnalyzer());
            Query query = queryParser.parse(searchText);
            TopDocs topDocs = indexSearcher.search(query, 10000);

            ArrayList<ScoreDoc> topDocsList = new ArrayList<>(Arrays.asList(topDocs.scoreDocs));
            Coordinate coordinate = new Coordinate(longitude, latitude);
            HashMap<String, Coordinate> itemsWithinBoundingBox = ItemsWithinBoundingBox(BoundingBox(coordinate, radius));
            for (Iterator<ScoreDoc> iterator = topDocsList.iterator(); iterator.hasNext(); ) {
                ScoreDoc scoreDoc = iterator.next();
                Document document = indexSearcher.doc(scoreDoc.doc);
                if (!itemsWithinBoundingBox.containsKey(document.get("item_id"))) {
                    iterator.remove();
                }
            }

            for (Iterator<ScoreDoc> iterator = topDocsList.iterator(); iterator.hasNext(); ) {
                ScoreDoc scoreDoc = iterator.next();
                Document document = indexSearcher.doc(scoreDoc.doc);
                if (radius < CheckDistance(ItemCoordinate(document.get("item_id")), new Coordinate(longitude, latitude))) {
                    iterator.remove();
                }
            }

            Collections.sort(topDocsList, new Comparator<ScoreDoc>() {
                @Override
                public int compare(ScoreDoc o1, ScoreDoc o2) {
                    int i = Double.compare(o2.score, o1.score);
                    if (i == 0){
                        try {
                            Document document1 = indexSearcher.doc(o1.doc);
                            Document document2 = indexSearcher.doc(o2.doc);
                            i = Double.compare(CheckDistance(ItemCoordinate(document1.get("item_id")), new Coordinate(longitude, latitude)),
                                    CheckDistance(ItemCoordinate(document2.get("item_id")), new Coordinate(longitude, latitude)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (i == 0){
                            try {
                                Document document1 = indexSearcher.doc(o1.doc);
                                Document document2 = indexSearcher.doc(o2.doc);
                                i = Double.compare(Double.parseDouble(ItemPrice(document1.get("item_id"))),
                                        Double.parseDouble(ItemPrice(document2.get("item_id"))));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return i;
                    }
                    return i;
                }
            });

            System.out.println("Number of Hits: " + topDocsList.size());
            for (ScoreDoc scoreDoc : topDocsList) {
                Document document = indexSearcher.doc(scoreDoc.doc);
                System.out.printf(Locale.US, "item_id: %s, %s, score: %.9f, distance: %f, price: %s %n", document.get("item_id"),
                        ItemTitle(document.get("item_id")), scoreDoc.score,
                        CheckDistance(ItemCoordinate(document.get("item_id")), new Coordinate(longitude, latitude)),
                        ItemPrice(document.get("item_id")));
            }


            return topDocs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String ItemPrice(String item_id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        String result = null;
        try {
            conn = DbManager.getConnection(true);
            String sql = "select current_price from auction where item_id = ? ;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, item_id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result = String.valueOf(rs.getDouble("current_price"));
            }
            rs.close();
            conn.close();
            return result;
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        return null;
    }

    private static String ItemTitle(String item_id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        String result = null;
        try {
            conn = DbManager.getConnection(true);
            String sql = "select item_name from item where item_id = ? ;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, item_id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result = String.valueOf(rs.getString("item_name"));
            }
            rs.close();
            conn.close();
            return result;
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        return null;
    }

    private static Coordinate ItemCoordinate(String item_id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        Coordinate coordinate = null;
        try {
            conn = DbManager.getConnection(true);
            String sql = "select longitude, latitude from item_coordinates where item_id = ? ;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, item_id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                coordinate = new Coordinate(rs.getDouble("longitude"), rs.getDouble("latitude"));
            }
            rs.close();
            conn.close();
            return coordinate;
        } catch (SQLException ex) {
            System.out.println(ex);
        }

        return null;
    }

    private static List<Coordinate> BoundingBox(Coordinate coordinate, Double radius) {

        //+10% radius
        radius += radius * 0.1;

        double latRadian = Math.toRadians(coordinate.latitude);

        double degLat = 110.574235;
        double degLong = 110.572833 * Math.cos(latRadian);
        double deltaLat = radius / degLat;
        double deltaLong = radius / degLong;

        Coordinate bottomLeft = new Coordinate(coordinate.longitude - deltaLong,
                coordinate.latitude - deltaLat);
        Coordinate topLeft = new Coordinate(coordinate.longitude - deltaLong,
                coordinate.latitude + deltaLat);
        Coordinate topRight = new Coordinate(coordinate.longitude + deltaLong,
                coordinate.latitude + deltaLat);
        Coordinate bottomRight = new Coordinate(coordinate.longitude + deltaLong,
                coordinate.latitude - deltaLat);

        List<Coordinate> boundingBox = new ArrayList<>();

        boundingBox.add(bottomLeft);
        boundingBox.add(topLeft);
        boundingBox.add(topRight);
        boundingBox.add(bottomRight);

        return boundingBox;
    }

    private static HashMap<String, Coordinate> ItemsWithinBoundingBox(List<Coordinate> boundingBox) {
        Connection conn = null;
        PreparedStatement stmt = null;
        HashMap<String, Coordinate> itemsWithinBoudingBox = new HashMap<>();
        try {
            conn = DbManager.getConnection(true);
            String sqlSetPolygon = "select geo.itemID, item_c.latitude, item_c.longitude from geocoordinates_itemID" +
                    " geo left join item_coordinates item_c on geo.itemID = item_c.item_id where MBRContains" +
                    "(GeomFromText(concat('Polygon((', ?, ' ', ?, ',', ?, ' ', ?, ',', ?, ' ', ?, ',', ?, ' '," +
                    " ?, ',', ?, ' ', ?, '))')), geocoordinate);";

            stmt = conn.prepareStatement(sqlSetPolygon);
            stmt.setDouble(1, boundingBox.get(0).getCoordinateLatitude());
            stmt.setDouble(2, boundingBox.get(0).getCoordinateLongitude());
            stmt.setDouble(3, boundingBox.get(1).getCoordinateLatitude());
            stmt.setDouble(4, boundingBox.get(1).getCoordinateLongitude());
            stmt.setDouble(5, boundingBox.get(2).getCoordinateLatitude());
            stmt.setDouble(6, boundingBox.get(2).getCoordinateLongitude());
            stmt.setDouble(7, boundingBox.get(3).getCoordinateLatitude());
            stmt.setDouble(8, boundingBox.get(3).getCoordinateLongitude());
            stmt.setDouble(9, boundingBox.get(0).getCoordinateLatitude());
            stmt.setDouble(10, boundingBox.get(0).getCoordinateLongitude());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                itemsWithinBoudingBox.put(rs.getString("itemID"),
                        new Coordinate(rs.getDouble("latitude"), rs.getDouble("longitude")));
            }
            rs.close();
            conn.close();
            return itemsWithinBoudingBox;
        } catch (SQLException ex) {
            System.out.println(ex);
        }

        return null;
    }

    private static Double CheckDistance(Coordinate itemLocation, Coordinate middleCoordinate) {
        Connection conn = null;
        PreparedStatement stmt = null;
        Double distance = null;
        try {
            conn = DbManager.getConnection(true);
            String sqlSetPolygon = "SELECT get_distance_in_miles_between_geo_locations(?, ?, ?, ?) as dist;";
            stmt = conn.prepareStatement(sqlSetPolygon);
            stmt.setDouble(1, itemLocation.getCoordinateLatitude());
            stmt.setDouble(2, itemLocation.getCoordinateLongitude());
            stmt.setDouble(3, middleCoordinate.getCoordinateLatitude());
            stmt.setDouble(4, middleCoordinate.getCoordinateLongitude());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                distance = rs.getDouble("dist") * 1.609;
            }
            rs.close();
            conn.close();
            return distance;
        } catch (SQLException ex) {
            System.out.println(ex);
        }

        return null;
    }

    public static class Coordinate {
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

}
