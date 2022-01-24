import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class jdbc {
    public jdbc() {
    }

    public static void main(String args[]) {
        String usage = "java jdbc";
        rebuildIndexes("indexes");
    }

	public static void insertDoc(IndexWriter i, String doc_id, String line){
		Document doc = new Document();
		doc.add(new TextField("doc_id", doc_id, Field.Store.YES));
		doc.add(new TextField("line", line,Field.Store.YES));
		try { i.addDocument(doc); } catch (Exception e) { e.printStackTrace(); }
	}

    public static void rebuildIndexes(String indexPath) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DbManager.getConnection(true);
            stmt = conn.createStatement();
            //String sql = "SELECT * from item limit 3;";
            String sql = "select i.item_id, item_name, item_group_categories, i.description from item i "+
					"left join (select item_id, group_concat(category_name separator ' ')"+
					" as item_group_categories from has_category group by item_id) id_categories"+
					" on id_categories.item_id = i.item_id;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String count = rs.getString("count");
                System.out.println("count: " + count);
            }
            rs.close();
            conn.close();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }
}

