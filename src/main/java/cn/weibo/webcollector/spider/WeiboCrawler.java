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
 * Foundation, Inc., 109 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package cn.weibo.webcollector.spider;

import cn.edu.hfut.dmic.webcollector.example.WeiboCN;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequest;
import cn.edu.hfut.dmic.webcollector.net.HttpResponse;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.plugin.mongo.MongoCrawler;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.mongodb.client.MongoDatabase;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 利用WebCollector和获取的cookie爬取新浪微博并抽取数据
 * @author hu
 */
public class WeiboCrawler extends BreadthCrawler {

    String cookie;

    public WeiboCrawler(String crawlPath,boolean autoParse) throws Exception {
        super(crawlPath, autoParse);
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
        String inlink = page.meta("inlink");
        String title = page.select("title").text();
        String url = page.getUrl();
        /*抽取微博*/
        Elements weibos = page.select("div[id].c");
        try{       
            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
            Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
		    mongoLogger.setLevel(Level.ERROR);
           
           // 连接到数据库
           MongoDatabase mongoDatabase = mongoClient.getDatabase("weibo_crawler");  
           System.out.println("Connect to database successfully");    
           MongoCollection<Document> collection = mongoDatabase.getCollection("weibo_page");
           //插入文档  
           /** 
           * 1. 创建文档 org.bson.Document 参数为key-value的格式 
           * 2. 创建文档集合List<Document> 
           * 3. 将文档集合插入数据库集合中 mongoCollection.insertMany(List<Document>) 插入单个文档可以用 mongoCollection.insertOne(Document) 
           * */
           for (Element weibo : weibos) {
        	   if (weibo.text().length() !=0){
           Document document = new Document("url",url).append("title",title).append("content",weibo.text()).append("inlink", inlink);  
           List<Document> documents = new ArrayList<Document>();  
           documents.add(document); 
           collection.insertMany(documents); } 
           }   
           System.out.println("文档插入成功");
           mongoClient.close();
        }catch(Exception e){
           System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    public static void main(String[] args) throws Exception {
		MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
		Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
	    mongoLogger.setLevel(Level.ERROR);
		// 连接到数据库
		// DBCollection dbCollection =
		// mongoClient.getDB("maoyan_crawler").getCollection("rankings_am");
		DB db = mongoClient.getDB("weibo_crawler");
		// 遍历所有集合的名字
		Set<String> colls = db.getCollectionNames();
		for (String s : colls) {
			// 先删除所有Collection(类似于关系数据库中的"表")
			if (s.equals("weibo_page")) {
				db.getCollection(s).drop();
			}
		}
        WeiboCrawler crawler = new WeiboCrawler("weibo_crawler", true);
        crawler.setThreads(3);
        /*对某人微博前10页进行爬取*/
        for (int i = 1; i <= 10; i++) {
            crawler.addSeed(new CrawlDatum("http://weibo.cn/entpaparazzi?vt=4&page=" + i) //新浪娱乐
                    .meta("inlink","seed")
                    .meta("depth","1"));
        }
        for (int i = 1; i <= 10; i++) {
            crawler.addSeed(new CrawlDatum("http://weibo.cn/dianyingpiaofangba?vt=4&page=" + i) //电影票房吧
            		.meta("inlink","seed")
            		.meta("depth","1"));
        }
        for (int i = 1; i <= 10; i++) {
            crawler.addSeed(new CrawlDatum("http://weibo.cn/houson100037?vt=4&page=" + i) //Houson猴姆
                    .meta("inlink","seed")
                    .meta("depth","1"));
        }
        for (int i = 1; i <= 10; i++) {
            crawler.addSeed(new CrawlDatum("http://weibo.cn/kaopuyingping?vt=4&page=" + i) //靠谱影评
                    .meta("inlink","seed")
                    .meta("depth","1"));
        }
        for (int i = 1; i <= 10; i++) {
            crawler.addSeed(new CrawlDatum("http://weibo.cn/rottentomato?vt=4&page=" + i) //烂番茄影评
                    .meta("inlink","seed")
                    .meta("depth","1"));
        }
        for (int i = 1; i <= 10; i++) {
            crawler.addSeed(new CrawlDatum("http://weibo.cn/cfcu?vt=4&page=" + i) //影评老大爷暗夜骑士
                    .meta("inlink","seed")
                    .meta("depth","1"));
        }
        for (int i = 1; i <= 10; i++) {
            crawler.addSeed(new CrawlDatum("http://weibo.cn/moviefactory?vt=4&page=" + i) //电影工厂
                    .meta("inlink","seed")
                    .meta("depth","1"));
        }
        for (int i = 1; i <= 10; i++) {
            crawler.addSeed(new CrawlDatum("http://weibo.cn/wodianying?vt=4&page=" + i) //电影Mr
                    .meta("inlink","seed")
                    .meta("depth","1"));
        }
        for (int i = 1; i <= 10; i++) {
            crawler.addSeed(new CrawlDatum("http://weibo.cn/movietheworld?vt=4&page=" + i) //微博电影
                    .meta("inlink","seed")
                    .meta("depth","1"));
        }
        for (int i = 1; i <= 10; i++) {
            crawler.addSeed(new CrawlDatum("http://weibo.cn/badmovie?vt=4&page=" + i) //电影通缉令
                    .meta("inlink","seed")
                    .meta("depth","1"));
        }
        crawler.addRegex("-.*\\.(jpg|png|gif).*");
        crawler.addRegex("-.*top.*");
        crawler.addRegex("http://weibo.cn/badmovie.*");
        crawler.addRegex("http://weibo.cn/movietheworld.*");
        crawler.addRegex("http://weibo.cn/wodianying.*");
        crawler.addRegex("http://weibo.cn/moviefactory.*");
        crawler.addRegex("http://weibo.cn/cfcu.*");
        crawler.addRegex("http://weibo.cn/rottentomato.*");
        crawler.addRegex("http://weibo.cn/kaopuyingping.*");
        crawler.addRegex("http://weibo.cn/houson100037.*");
        crawler.addRegex("http://weibo.cn/dianyingpiaofangba.*");
        crawler.addRegex("http://weibo.cn/entpaparazzi.*");
        crawler.addRegex("http://weibo.cn/comment/.*");
        crawler.start(1);
    }

}