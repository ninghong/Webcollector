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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.Document;
import org.jsoup.nodes.Element;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamCrawler;


/**
 * WebCollector 2.x版本的tutorial(version>=2.20) 2.x版本特性：
 * 1）自定义遍历策略，可完成更为复杂的遍历业务，例如分页、AJAX
 * 2）可以为每个URL设置附加信息(MetaData)，利用附加信息可以完成很多复杂业务，例如深度获取、锚文本获取、引用页面获取、POST参数传递、增量更新等。
 * 3）使用插件机制，WebCollector内置两套插件。
 * 4）内置一套基于内存的插件（RamCrawler)，不依赖文件系统或数据库，适合一次性爬取，例如实时爬取搜索引擎。
 * 5）内置一套基于Berkeley DB（BreadthCrawler)的插件：适合处理长期和大量级的任务，并具有断点爬取功能，不会因为宕机、关闭导致数据丢失。 
 * 6）集成selenium，可以对javascript生成信息进行抽取
 * 7）可轻松自定义http请求，并内置多代理随机切换功能。 可通过定义http请求实现模拟登录。 
 * 8）使用slf4j作为日志门面，可对接多种日志
 *
 * 可在cn.edu.hfut.dmic.webcollector.example包中找到例子(Demo)
 *
 * @author hu
 */
public class Douban2Crawler extends RamCrawler {
	
	private int index = 0;

    /*
        可以往next中添加希望后续爬取的任务，任务可以是URL或者CrawlDatum
        爬虫不会重复爬取任务，从2.20版之后，爬虫根据CrawlDatum的key去重，而不是URL
        因此如果希望重复爬取某个URL，只要将CrawlDatum的key设置为一个历史中不存在的值即可
        例如增量爬取，可以使用 爬取时间+URL作为key。
    
        新版本中，可以直接通过 page.select(css选择器)方法来抽取网页中的信息，等价于
        page.getDoc().select(css选择器)方法，page.getDoc()获取到的是Jsoup中的
        Document对象，细节请参考Jsoup教程
     */
    @Override
    public void visit(Page page, CrawlDatums next) {
    	System.out.println(page.getUrl());
    	MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        // 连接到数据库
        MongoDatabase mongoDatabase = mongoClient.getDatabase("douban_crawler");  
        System.out.println("Connect to database successfully");    
        MongoCollection<org.bson.Document> collection = mongoDatabase.getCollection("moviereview");
        List<Document> documents = new ArrayList<Document>();  
        if (page.matchUrl("https://movie.douban.com/review/\\d*/")) {   
        	index = index+1;
            //String title = page.select("div[class=article_title]").first().text();
            //String author = page.select("div[id=blog_userface]").first().text();
            //System.out.println("title:" + title + "\tauthor:" + author);
        	//HashMap md = page.getMetaData();
        	//String review = page.getMetaData("og:description").toString();
        	//Document doc = page.getDoc();
        	//System.out.println(doc);
        	System.out.println("index:" + index);
        	String review = page.select("div.main-bd").text();
            System.out.println("review:" + review);
            String movieName = page.select("div.side-back").text();
            System.out.println("movieName:" + movieName);
            ArrayList rc = page.select("div.comment-item");
            Document document = new Document();
        	document.put("moviename", movieName);
        	document.put("review", review);
        	List<String> subreviews = new ArrayList<String>();
            for(int i=0; i<rc.size();i++){
            	//System.out.println(rc.get(i).getClass());
            	//((org.jsoup.nodes.Element)rc.get(i)).getAllElements().get(3);
            	org.jsoup.nodes.Element element = (org.jsoup.nodes.Element)((org.jsoup.nodes.Element)((org.jsoup.nodes.Element)rc.get(i)).childNodes().get(3)).childNode(3);
            	//Object nodes = ((org.jsoup.nodes.Element)((org.jsoup.nodes.Element)rc.get(i)).childNodes()).getAllElements().get(3);
            	String subReview = element.childNode(0).toString();
            	System.out.println("subReview:" + subReview);
            	if(subReview!="")
            		subreviews.add(subReview);
            }
            document.put("subreviews", subreviews);
            collection.insertOne(document);
            //documents.add(document);
            //collection.insertMany(documents);
        }else if(page.matchUrl("https://movie.douban.com/review/best/\\?start=\\d*")){
        	//Document doc = page.getDoc();
        	System.out.println("add seed");
        	//this.addSeed(page.getUrl());
        }
    }

    public static void main(String[] args) throws Exception {
        Douban2Crawler crawler = new Douban2Crawler();
        crawler.addSeed("https://movie.douban.com/review/best/");
        crawler.addRegex("https://movie.douban.com/review/.*");
        //System.out.println(Pattern.matches("https://movie.douban.com/review/best/\\?start=\\d*", "https://movie.douban.com/review/best/?start=100"));
        //System.out.println(Pattern.matches("\\?", "?"));
        //https://movie.douban.com/review/7815895/
        /*可以设置每个线程visit的间隔，这里是毫秒*/
        //crawler.setVisitInterval(1000);
        /*可以设置http请求重试的间隔，这里是毫秒*/
        //crawler.setRetryInterval(1000);
        crawler.setThreads(1);
        crawler.start(3);
    }

}
