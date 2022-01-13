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
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.apache.lucene.search.Explanation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import java.io.*;
import java.nio.file.Path;

public class Searcher {

    public Searcher() {}
    public static void main(String[] args) throws Exception {
	String usage = "java Searcher";
	search(args[0], "indexes");
    }    
    
    private static TopDocs search(String searchText, String p) {   
	System.out.println("Running search(" + searchText + ")");
	try {   
	    Path path = Paths.get(p);
	    Directory directory = FSDirectory.open(path);       
	    IndexReader indexReader =  DirectoryReader.open(directory);
	    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
	    QueryParser queryParser = new QueryParser("line", new SimpleAnalyzer());  
	    Query query = queryParser.parse(searchText);
	    TopDocs topDocs = indexSearcher.search(query,10000);
	    System.out.println("Number of Hits: " + topDocs.totalHits);
	    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {           
		Document document = indexSearcher.doc(scoreDoc.doc);
		System.out.println("doc_id: " + document.get("doc_id") 
				   + ", score: " + scoreDoc.score + " [" + document.get("line") +"]");
	    }
	    return topDocs;
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }
}
