/*
 * Copyright (C) 2015 hu
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package cn.edu.hfut.dmic.webcollector.example;

import cn.edu.hfut.dmic.webcollector.example.WeiboCN;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequest;
import cn.edu.hfut.dmic.webcollector.net.HttpResponse;
import cn.edu.hfut.dmic.webcollector.plugin.mongo.MongoCrawler;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

/**
 * 利用WebCollector和获取的cookie爬取新浪微博并抽取数据
 * @author hu
 */
public class WeiboCrawler extends MongoCrawler {

    String cookie;

    public WeiboCrawler(String crawlPath, MongoClient client ,boolean autoParse) throws Exception {
        super(crawlPath, client, autoParse);
        /*获取新浪微博的cookie，账号密码以明文形式传输，请使用小号*/
        cookie = WeiboCN.getSinaCookie("eyuhn2000@163.com", "xxnda2011");
    }

    @Override
    public HttpResponse getResponse(CrawlDatum crawlDatum) throws Exception {
        HttpRequest request = new HttpRequest(crawlDatum);
        request.setCookie(cookie);
        return request.getResponse();
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        int pageNum = Integer.valueOf(page.getMetaData("pageNum"));
        /*抽取微博*/
        Elements weibos = page.select("div.c");
        try{       
            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
           
           // 连接到数据库
           MongoDatabase mongoDatabase = mongoClient.getDatabase("weibo_crawler");  
           System.out.println("Connect to database successfully");    
           MongoCollection<Document> collection = mongoDatabase.getCollection("webpage");
           //插入文档  
           /** 
           * 1. 创建文档 org.bson.Document 参数为key-value的格式 
           * 2. 创建文档集合List<Document> 
           * 3. 将文档集合插入数据库集合中 mongoCollection.insertMany(List<Document>) 插入单个文档可以用 mongoCollection.insertOne(Document) 
           * */
           for (Element weibo : weibos) {
           Document document = new Document("content","第" + pageNum + "页" + ":" + weibo.text());  
           List<Document> documents = new ArrayList<Document>();  
           documents.add(document); 
           collection.insertMany(documents);  
           }   
           System.out.println("文档插入成功");
           mongoClient.close();
        }catch(Exception e){
           System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    public static void main(String[] args) throws Exception {
    	MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        WeiboCrawler crawler = new WeiboCrawler("weibo_crawler",mongoClient, false);
        crawler.setThreads(3);
        /*对某人微博前5页进行爬取*/
        for (int i = 1; i <= 5; i++) {
            crawler.addSeed(new CrawlDatum("http://weibo.cn/zhouhongyi?vt=4&page=" + i)
                    .putMetaData("pageNum", i + ""));
        }
        crawler.start(1);
    }

}