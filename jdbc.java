import java.io.StringReader;
import java.io.File;
import java.nio.file.*;
import java.lang.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.sql.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class jdbc {
    public jdbc() {
    }
    public static void main(String args[]) {
	String usage = "java jdbc";
	rebuildIndexes("indexes");
    }
    public static void rebuildIndexes(String indexPath) {
	Connection conn = null;
	Statement stmt = null;
	try {
	    conn = DbManager.getConnection(true);
	    stmt = conn.createStatement();
	    //String sql = "SELECT * from item limit 3;";
	    String sql = "SELECT count(*) as count from item;";
	    ResultSet rs = stmt.executeQuery(sql);
	    while(rs.next()){
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

