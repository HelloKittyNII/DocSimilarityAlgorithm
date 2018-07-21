package com.nii.sim;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wzj
 * @create 2018-07-21 15:49
 **/
public class DocLuceneWrapper
{
    /**
     * 索引保存的位置
     */
    private String saveIndexPath;

    /**
     * 文档路径
     */
    private String docPath;

    /**
     * 文档数目
     */
    private int docNumbers;

    public DocLuceneWrapper(String saveIndexPath, String docPath)
    {
        this.saveIndexPath = saveIndexPath;
        this.docPath = docPath;

        this.docNumbers = Paths.get(docPath).toFile().listFiles().length;
    }

    /**
     * 将所有的文档加入lucene中
     * @throws IOException
     */
    public void indexDocs() throws IOException
    {
        System.out.println("Number of files : " + docNumbers);

        File[] listOfFiles = Paths.get(docPath).toFile().listFiles();

        NIOFSDirectory dir = new NIOFSDirectory(new File(saveIndexPath));
        IndexWriter indexWriter = new IndexWriter(dir,
                new IndexWriterConfig(Version.LUCENE_36, new WhitespaceAnalyzer(Version.LUCENE_36)));

        for (File file : listOfFiles)
        {
            //读取文件内容，并去除数字标点符号
            String fileContent = fileReader(file);
            fileContent = fileContent.replaceAll("\\d+(?:[.,]\\d+)*\\s*", "");

            String docName = file.getName();

            Document doc = new Document();
            doc.add(new Field("docContent", new StringReader(fileContent), Field.TermVector.YES));
            doc.add(new Field("docName", new StringReader(docName), Field.TermVector.YES));

            indexWriter.addDocument(doc);
        }

        indexWriter.close();
        System.out.println("Add document successful.");
    }

    /**
     * 获取所有文档的tf-idf值
     * @return 结果
     * @throws IOException  IOException
     * @throws ParseException ParseException
     */
    public HashMap<String, Map<String, Float>> getAllTFIDF() throws IOException, ParseException
    {
        HashMap<String, Map<String, Float>> scoreMap = new HashMap<String, Map<String, Float>>();

        IndexReader re = IndexReader.open(NIOFSDirectory.open(new File(saveIndexPath)), true);

        for (int k = 0; k < docNumbers; k++)
        {
            //每一个文档的tf-idf
            Map<String, Float> wordMap = new HashMap<String, Float>();

            //获取当前文档的内容
            TermFreqVector termsFreq = re.getTermFreqVector(k, "docContent");
            TermFreqVector termsFreqDocId = re.getTermFreqVector(k, "docName");

            String docName = termsFreqDocId.getTerms()[0];
            int[] freq = termsFreq.getTermFrequencies();

            String[] terms = termsFreq.getTerms();
            int noOfTerms = terms.length;
            DefaultSimilarity simi = new DefaultSimilarity();
            for (int i = 0; i < noOfTerms; i++)
            {
                int noOfDocsContainTerm = re.docFreq(new Term("docContent", terms[i]));
                float tf = simi.tf(freq[i]);
                float idf = simi.idf(noOfDocsContainTerm, docNumbers);
                wordMap.put(terms[i], (tf * idf));
            }
            scoreMap.put(docName, wordMap);
        }

        return scoreMap;
    }

    /**
     * 获取查找文本的tf-idf
     * @param termList 分词列表
     * @throws IOException 异常
     */
    public Map<String,Float> getSearchTextTfIdf(List<String> termList) throws IOException
    {
        //统计每一个词，在文档中的数目
        Map<String,Integer> termFreqMap = new HashMap<String,Integer>();
        for (String term : termList)
        {
            if (termFreqMap.get(term) == null)
            {
                termFreqMap.put(term,1);
                continue;
            }

            termFreqMap.put(term,termFreqMap.get(term) + 1);
        }

        Map<String, Float> scoreMap = new HashMap<String, Float>();

        IndexReader re = IndexReader.open(NIOFSDirectory.open(new File(saveIndexPath)), true);
        DefaultSimilarity simi = new DefaultSimilarity();

        for (String term : termList)
        {
            int noOfDocsContainTerm = re.docFreq(new Term("docContent", term));
            float tf = simi.tf(termFreqMap.get(term));
            float idf = simi.idf(noOfDocsContainTerm, docNumbers);
            scoreMap.put(term, (tf * idf));
        }

        return scoreMap;
    }

    private  String fileReader(File readFile)
    {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(readFile));

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }

        return stringBuilder.toString();
    }
}
