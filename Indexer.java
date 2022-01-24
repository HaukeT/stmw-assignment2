import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Indexer {
    public Indexer() {
    }

    public static IndexWriter indexWriter;

    public static void main(String args[]) {
        String usage = "java Indexer";
        rebuildIndexes("indexes");
    }

    public static void insertDoc(IndexWriter i, String item_id, String concatColumns) {
        Document doc = new Document();
        doc.add(new TextField("item_id", item_id, Field.Store.YES));
        doc.add(new TextField("concatColumns", concatColumns, Field.Store.YES));

        try {
            i.addDocument(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void rebuildIndexes(String indexPath) {
        try {
            Path path = Paths.get(indexPath);
            System.out.println("Indexing to directory '" + indexPath + "'...\n");
            Directory directory = FSDirectory.open(path);
            IndexWriterConfig config = new IndexWriterConfig(new SimpleAnalyzer());
            //	    IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
            //IndexWriterConfig config = new IndexWriterConfig(new EnglishAnalyzer());
            IndexWriter i = new IndexWriter(directory, config);
            i.deleteAll();
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                conn = DbManager.getConnection(true);
                String sql = "select i.item_id, item_name, item_group_categories, i.description from item i " +
                        "left join (select item_id, group_concat(category_name separator ' ')" +
                        " as item_group_categories from has_category group by item_id) id_categories" +
                        " on id_categories.item_id = i.item_id;";
                stmt = conn.prepareStatement(sql);
                //String sql = "SELECT * from item limit 3;";
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    insertDoc(i, rs.getString("item_id"),
                            rs.getString("item_name") + " " +
                                    rs.getString("item_group_categories") + " " +
                                    rs.getString("description"));
                }
                rs.close();
                conn.close();
            } catch (SQLException ex) {
                System.out.println(ex);
            }
            i.close();
            directory.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
