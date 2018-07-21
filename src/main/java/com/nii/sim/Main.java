package com.nii.sim;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.sun.javafx.scene.EnteredExitedHandler;
import org.apache.lucene.queryParser.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wzj
 * @create 2018-07-21 16:46
 **/
public class Main
{
    public static void main(String[] args) throws IOException, ParseException
    {
        String docPath = "H:\\JAVA\\DocSim\\conf\\doc";
        String indexPath = "H:\\JAVA\\DocSim\\conf\\index";
        DocLuceneWrapper docLuceneWrapper = new DocLuceneWrapper(indexPath, docPath);

        index(docLuceneWrapper);

        search(docLuceneWrapper);

        System.out.println("");
    }

    private static void index(DocLuceneWrapper docLuceneWrapper ) throws IOException
    {
        docLuceneWrapper.indexDocs();
    }

    private static void search(DocLuceneWrapper docLuceneWrapper ) throws IOException, ParseException
    {
        String searchText = "小编推荐，3D宝软桌面是达人装机必备，提供海量主题锁屏，支持滚动壁纸，同时支持多种特效及手势，具有高效管理应用机制的桌面启动器软件。" +
                "\n" + "上亿用户选择的主题桌面; 海量精美壁纸锁屏尽情下载;自由定制个性主题皮肤;性能与酷炫完美结合,超丰富实用的特色功能; 风格百变的锁屏，" +
                "惊艳你的手机桌面。奇迹暖暖、龙之谷、长草颜团子、tfboys、Angelababy等主题桌面每日资源推陈出新。\n" + "小而强大,为美而生,蜕变从3D宝软桌面开始。" +
                "\n" + "产品特色：\n" + "1、【超高性能】内存占用小，低能耗，响应速度比普通桌面提升300%！省电省流量，轻松玩转阴阳师、看视频不卡顿；\n" +
                "2、【精美壁纸主题锁屏】每天推陈出新，365天美化你的手机；文艺小清新，二次元，文字控，明星壁纸主题你要的都在；海量桌面壁纸，桌面主题，一键切换；\n"
                + "3、【生活小助理】内存清理，实时天气，更有多种生活小工具等特色功能，便捷你的生活；\n" +
                "4、【隐藏图标】一键选择即可隐藏手机里的隐私文件和社交约爱等小秘密。\n" +
                "5、【一键换图标】我的桌面我做主，图标皮肤随心而变。\n" +
                "6、【自主DIY】壁纸、主题、锁屏随心创作，你就是下一个设计达人！\n"
                + "7、【魔幻手势】让你的指尖生活充满乐趣，打造个性舒适的使用体验！\n"
                + "8、【一键锁屏】一个手势锁屏，省去按电源键的烦恼，延长手机电源键使用寿命。\n" +
                "9、【快速换肤】一键换肤，快速更换主题，让你的手机百变不腻。\n" + "关于我们：\n" + "我们致力为您打造潮流又好用的手机桌面，提供优质的服务！";

        //去除标点符号,特殊字符
        String content = searchText.replaceAll("[\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]", "");
        content = content.replaceAll("\\t|\\r|\\n","");
        content = content.replaceAll(" ","");

        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<String> strings = segmenter.sentenceProcess(content);

        Map<String, Float> searchTextTfIdfMap = docLuceneWrapper.getSearchTextTfIdf(strings);
        HashMap<String, Map<String, Float>> allTfIdfMap = docLuceneWrapper.getAllTFIDF();

        //利用余弦相似度求出与所有文档的相似值
        Map<String, Double> docSimMap = cosineSimilarity(searchTextTfIdfMap, allTfIdfMap);

        //找出最相似的
        double maxSimValue = -1;
        String maxSimDocName = "";
        for (Map.Entry<String, Double> entry : docSimMap.entrySet())
        {
            if (entry.getValue() > maxSimValue)
            {
                maxSimValue = entry.getValue();
                maxSimDocName = entry.getKey();
            }
        }

        System.out.print("" + maxSimDocName + " " + maxSimValue);
    }

    /**
     * 计算余弦相似度
     * @param searchTextTfIdfMap 查找文本的向量
     * @param allTfIdfMap 所有文本向量
     * @return 计算出当前查询文本与所有文本的相似度
     */
    private static Map<String,Double> cosineSimilarity(Map<String, Float> searchTextTfIdfMap,HashMap<String, Map<String, Float>> allTfIdfMap)
    {
        //key是相似的文档名称，value是与当前文档的相似度
        Map<String,Double> similarityMap = new HashMap<String,Double>();

        //计算查找文本向量绝对值
        double searchValue = 0;
        for (Map.Entry<String, Float> entry : searchTextTfIdfMap.entrySet())
        {
            searchValue += entry.getValue() * entry.getValue();
        }

        for (Map.Entry<String, Map<String, Float>> docEntry : allTfIdfMap.entrySet())
        {
            String docName = docEntry.getKey();
            Map<String, Float> docScoreMap = docEntry.getValue();

            double termValue = 0;
            double acrossValue = 0;
            for (Map.Entry<String, Float> termEntry : docScoreMap.entrySet())
            {
                if (searchTextTfIdfMap.get(termEntry.getKey()) != null)
                {
                    acrossValue += termEntry.getValue() * searchTextTfIdfMap.get(termEntry.getKey());
                }

                termValue += termEntry.getValue() * termEntry.getValue();
            }

            similarityMap.put(docName,acrossValue/(termValue * searchValue));
        }

        return similarityMap;
    }
}
