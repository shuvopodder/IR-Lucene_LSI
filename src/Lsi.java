
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import la4j.decomposition.SingularValueDecompositor;
import la4j.err.MatrixDecompositionException;
import la4j.err.MatrixException;
import la4j.factory.DenseFactory;
import la4j.factory.Factory;
import la4j.factory.SparseFactory;
import la4j.matrix.Matrix;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Shuvo Podder
 */
public class Lsi {
    
String str2=""; double r=0.0;
    public Lsi(String str) throws FileNotFoundException, MatrixDecompositionException, MatrixException {
        int count = 0, i = 1;
        String series[] = new String[100];
        Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();//posting

        File dir = new File("docFile");
        String[] fileNames = dir.list();
        for (String fileName : fileNames) {

            File f = new File(dir, fileName);
            series[i] = fileName;

            String st = "";
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                while ((st = br.readLine()) != null) {
                    st = normalizeText(st);
                    //System.out.println(st);
                    Pattern p = Pattern.compile("[a-zA-Z]+");
                    Matcher m = p.matcher(st.toLowerCase());

                    while (m.find()) {
                        String word = m.group(); //spilt word from sentense

                        if (map.get(word) == null) {
                            map.put(word, new ArrayList<Integer>());
                        }
                        map.get(word).add(i);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            i++;
            count++;

        }

        removeDulpicate(map);//merge
        map = Sort(map);//sort
        svd(map, count,str);
    }

    private String normalizeText(String str) throws FileNotFoundException {

        Scanner s = new Scanner(new File("stopwords.txt"));
        ArrayList<String> list2 = new ArrayList<String>();
        while (s.hasNext()) {
            list2.add(s.next());
        }
        s.close();
        List<String> allWords = new ArrayList<>(Arrays.asList(str.toLowerCase().split(" ")));
        //System.out.println("Test:"+allWords);
        allWords.removeAll(list2);
        String result = String.join(" ", allWords);
        //System.out.println("Test2:"+result);
        return result;
    }

    private void removeDulpicate(Map<String, List<Integer>> map) {

        Set< Map.Entry< String, List<Integer>>> srr = map.entrySet();

        for (Map.Entry< String, List<Integer>> e : srr) {
            List<Integer> list3 = new ArrayList<>(e.getValue());

            List<Integer> newList = list3.stream().distinct().collect(Collectors.toList());

            map.replace(e.getKey(), newList);

        }
    }

    private Map<String, List<Integer>> Sort(Map<String, List<Integer>> map) {

        List<Map.Entry<String, List<Integer>>> list4
                = new LinkedList<Map.Entry<String, List<Integer>>>(map.entrySet());

        // Sort the list 
        Collections.sort(list4, new Comparator<Map.Entry<String, List<Integer>>>() {
            public int compare(Map.Entry<String, List<Integer>> o1,
                    Map.Entry<String, List<Integer>> o2) {
                return (o1.getKey()).compareTo(o2.getKey());
            }
        });

        // put data from sorted list to hashmap  
        HashMap<String, List<Integer>> temp = new LinkedHashMap<String, List<Integer>>();
        for (Map.Entry<String, List<Integer>> aa : list4) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    private void svd(Map<String, List<Integer>> map, int count,String str) throws MatrixDecompositionException, MatrixException {

        Set< Map.Entry< String, List<Integer>>> sst = map.entrySet();

        double arr[][] = new double[map.size()][count];
        int i = 0, j = 0;
        for (Map.Entry< String, List<Integer>> me : sst) {
            List<Integer> stringList = new ArrayList(me.getValue());
            Integer[] itemsArray = new Integer[stringList.size()];
            itemsArray = stringList.toArray(itemsArray);

            for (Integer sw : itemsArray) {
                arr[i][sw - 1] = 1;                                   //matrix  DOC1   Doc2   Doc3
                j++;
            }                                                        //word1    1      0       1
            i++;                                                    // word2    0      1       0
        }                                                           //word3     0      0       1
        //word3     1      1       1

        Factory denseFactory = new DenseFactory();
        Factory sparseFactory = new SparseFactory();

        Matrix aaa2 = denseFactory.createMatrix(arr);
        Matrix[] qqr2 = aaa2.decompose(new SingularValueDecompositor());

        System.out.println("Transpose: \n" + qqr2[1]);

        Matrix m = qqr2[0].multiply(qqr2[1].multiply(qqr2[2]));

        System.out.println(qqr2[2]);
        Matrix x = (qqr2[0]).multiply(qqr2[1]).multiply(qqr2[2].transpose());

        int ii = 0;
        for (Map.Entry< String, List<Integer>> me : sst) {

            System.out.print(me.getKey() + ": " + x.getRow(ii) + "\n");                                                     //word1    1      0       1
            ii++;                                                    // word2    0      1       0
        }                                                           //word3     0      0       1

        ii = 0;
        double arr2[] = new double[100];
        List<Double> ls = new ArrayList<Double>();
        Map<Double, Integer> hs = new HashMap<>();
        for (Map.Entry< String, List<Integer>> me : sst) {
            if (me.getKey().equals(str)) {

                for (int a = 0; a < count; a++) {
                    arr2[a] = x.get(ii, a);
                    ls.add(a, x.get(ii, a));
                    hs.put(x.get(ii, a), a);
                    System.out.println("test: " + a + " " + arr[a] + " " + x.get(ii, a));
                }

                System.out.print(me.getKey() + ": test" + x.getRow(ii) + "\n");
                System.out.println(ls);
                Collections.sort(ls, Collections.reverseOrder());
                System.out.println(ls);
                System.out.println(ls.get(0));
                System.out.println(hs.get(ls.get(0)));
                File dir = new File("docFile");
                String[] fileNames = dir.list();
                int y=1;
                Map<Integer,String> ls2 = new HashMap();
                for (String fileName : fileNames) {
                    ls2.put(y,fileName.toString());
                    if(y==hs.get(ls.get(0))||y==hs.get(ls.get(1))||y==hs.get(ls.get(2))){
                        System.out.println(fileName);
                        //str2+=fileName+"\n";
                    }y++;
                }
               
               for(int q=0;q<ls.size();q++){
                   if(ls.get(q)>=.5){
                       System.out.println(ls2.get(hs.get(ls.get(q))));
                       str2+=ls2.get(hs.get(ls.get(q)))+"\n";
                       r++;
                   }
               }
            }
            ii++;
        }
    }

    String getResult() {

        return str2="Total found: "+r+"Document\n"+str2;
    }

    double getR() {

        return r;
    }

}
