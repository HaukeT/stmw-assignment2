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

public class Indexer {
    public Indexer() {}
    public static IndexWriter indexWriter;
    public static void main(String args[]) {
	String usage = "java Indexer";
	rebuildIndexes("indexes");
    }
    public static void insertDoc(IndexWriter i, String doc_id, String line){
	Document doc = new Document();
	doc.add(new TextField("doc_id", doc_id, Field.Store.YES));
	doc.add(new TextField("line", line,Field.Store.YES));
	try { i.addDocument(doc); } catch (Exception e) { e.printStackTrace(); }
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
	    insertDoc(i, "1", "The old night keeper keeps the keep in the town");
	    insertDoc(i, "2", "In the big old house in the big old gown.");
	    insertDoc(i, "3", "The house in the town had the big old keep");
	    insertDoc(i, "4", "Where the old night keeper never did sleep.");
	    insertDoc(i, "5", "The night keeper keeps the keep in the night");
	    insertDoc(i, "6", "And keeps in the dark and sleeps in the light.");
	    insertDoc(i, "7", "The house is the house.");
	    insertDoc(i, "8", "The-the");
	    insertDoc(i, "9", "the-the.");
	    insertDoc(i, "10", "the");
	    insertDoc(i, "11", "the the");
	    insertDoc(i, "12", "the the the");
	    insertDoc(i, "13", "the the the the");
	    //	    insertDoc(i, "3", "the-the-the.");
	    //	    insertDoc(i, "4", "the-thethe__the.");
	    //	    insertDoc(i, "5", "The__the");
	    //	    insertDoc(i, "6", "The-the");
	    //	    insertDoc(i, "14", "The a b c");
	    //	    insertDoc(i, "15", "The a b.");
	    //	    insertDoc(i, "16", "The a.");
	    //	    insertDoc(i, "17", "The the the the.");
	    //	    insertDoc(i, "18", "The the the the the the the the the.");
	    i.close();
	    directory.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
