package com.nii.spider;

import com.huaban.analysis.jieba.JiebaSegmenter;
import org.apache.commons.io.IOUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author wzj
 * @create 2018-07-17 22:16
 **/
public class MyPipeline implements Pipeline
{
    /**
     * 保存文件的路径
     */
    private static final String saveDir = "D:\\cache\\";

    /**
     * jieba分词java版
     */
    private JiebaSegmenter segmenter = new JiebaSegmenter();

    /*
     * 统计数目
     */
    private int count = 1;


    public MyPipeline()
    {

    }

    /**
     * Process extracted results.
     *
     * @param resultItems resultItems
     * @param task        task
     */
    public void process(ResultItems resultItems, Task task)
    {
        String appName = resultItems.get("appName");
        String desc = resultItems.get("desc");

        //去除标点符号
        desc = desc.replaceAll("[\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]", "");
        desc = desc.replaceAll("\\t|\\r|\\n","");
        //去除空格
        desc = desc.replaceAll(" ","");

        List<String> vecList = segmenter.sentenceProcess(desc);
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : vecList)
        {
            stringBuilder.append(s + " ");
        }

        //去除最后一个空格
        String writeContent = stringBuilder.toString();
        if (writeContent.length() > 0)
        {
            writeContent = writeContent.substring(0,writeContent.length() - 1);
        }

        String appSavePath = Paths.get(saveDir, appName + ".txt").toString();
        FileWriter fileWriter = null;

        try
        {
            fileWriter = new FileWriter(appSavePath);
            fileWriter.write(writeContent);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly(fileWriter);
        }

        System.out.println(String.valueOf(count++) + " " + appName);
    }
}
