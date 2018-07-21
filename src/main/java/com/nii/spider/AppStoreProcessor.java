package com.nii.spider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

/**
 * @author wzj
 * @create 2018-07-17 22:06
 **/
public class AppStoreProcessor implements PageProcessor
{
    // 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

    public void process(Page page)
    {
        //获取名称
        String name = page.getHtml().xpath("//p/span[@class='title']/text()").toString();
        page.putField("appName",name );

        String desc =  page.getHtml().xpath("//div[@id='app_strdesc']/text()").toString();
        page.putField("desc",desc );

        if (page.getResultItems().get("appName") == null)
        {
            //skip this page
            page.setSkip(true);
        }

        //获取页面其他链接
        Selectable links = page.getHtml().links();
        page.addTargetRequests(links.regex("(http://app.hicloud.com/app/C\\d+)").all());
    }


    public Site getSite()
    {
        return site;
    }

    public static void main(String[] args)
    {
        Spider.create(new AppStoreProcessor())

                .addUrl("http://app.hicloud.com")
                .addPipeline(new MyPipeline())
                .thread(20)
                .run();
    }
}
