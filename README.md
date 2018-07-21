## 文本相似度算法
基于Lucene3.5版本、TF-IDF、余弦相似实现的文本相似度算法。

## 样本库提取
使用webmagic爬取华为应用市场应用的描述信息，当做样本。

在工程的conf/doc目录有1000多个应用样本。

具体代码实现在工程下面的AppStoreProcessor.java类

## 分词
在使用Lucene进行TF-IDF计算之前，需要先对长文本进行分词，选取的是jieba的java版本。

[https://github.com/huaban/jieba-analysis](https://github.com/huaban/jieba-analysis)


    




