
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
public class Search {
    String str="Search Result:\n";
    double relevant=0.0;
    ArrayList<Integer> list=new ArrayList<>();
  public  Search(String str)throws IOException, ParseException{
    String usage ="error";

    String index = "index";
    String field = "contents";
    String queries = null;
    int repeat = 0;
    boolean raw = false;
    String queryString = null;
    int hitsPerPage = 10;
    
    
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    IndexSearcher searcher = new IndexSearcher(reader);
    Analyzer analyzer = new StandardAnalyzer();   
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    QueryParser parser = new QueryParser(field, analyzer);
    
      
      Query query = parser.parse(str); //for searcing give query here
      System.out.println("Searching for: " + query.toString(field));
            
      if (repeat > 0) {                           // repeat & time as benchmark
        Date start = new Date();
        for (int i = 0; i < repeat; i++) {
          searcher.search(query, 100);
        }
        Date end = new Date();
        System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
      }

      doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);

    
    
    reader.close();
  }

 public  void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, 
                                     int hitsPerPage, boolean raw, boolean interactive) throws IOException {
 
    // Collect enough docs to show 5 pages
    TopDocs results = searcher.search(query, 5 * hitsPerPage);
    ScoreDoc[] hits = results.scoreDocs;
    
    int numTotalHits = Math.toIntExact(results.totalHits.value);
    System.out.println(numTotalHits + " total matching documents");
    str+=numTotalHits + " total matching documents\n";
    relevant=numTotalHits;

    int start = 0;
    int end = Math.min(numTotalHits, hitsPerPage);
        
  
      
      end = Math.min(hits.length, start + hitsPerPage);
      
      for (int i = start; i < end; i++) {
        if (raw) {                              // output raw format
          System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
          continue;
        }
        Document doc = searcher.doc(hits[i].doc);
        String path = doc.get("path");
        if (path != null) {
          System.out.println((i+1) + ". " + path);
          str+=(i+1)+". "+path.substring(25)+"\n";
          int x=1;
            File dir = new File("docFile");
            String[] fileNames = dir.list();
            for (String fileName : fileNames) {
                if(fileName.equals(path.substring(25))){
                     list.add(x);
                }
                x++;     
            }
          
          String title = doc.get("title");
          if (title != null) {
            System.out.println("   Title: " + doc.get("title"));
          }
        } else {
          System.out.println((i+1) + ". " + "No path for this document");
        }
                 
      }
      return; 
  
  }

    String getText() {

        return str;
    }

    ArrayList<Integer> getList() {

        return list;
    }

    double getRelevant() {

        return relevant;
    }
}
