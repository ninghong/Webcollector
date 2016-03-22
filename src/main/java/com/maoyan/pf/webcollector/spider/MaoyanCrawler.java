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
package com.maoyan.pf.webcollector.spider;


import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

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
public class MaoyanCrawler extends BreadthCrawler {

    public MaoyanCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
    }

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
        if (page.matchUrl("http://pf.maoyan.com/rankings/america.*")) {     
                MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
               // 连接到数据库
               // DBCollection dbCollection = mongoClient.getDB("maoyan_crawler").getCollection("rankings_am"); 
               DB db = mongoClient.getDB("maoyan_crawler");
               // 遍历所有集合的名字
               Set<String> colls = db.getCollectionNames();
               for (String s : colls) {
               // 先删除所有Collection(类似于关系数据库中的"表")
               if (s.equals("rankings_am")) {
            	   db.getCollection(s).drop();
               }
               }
               DBCollection dbCollection = db.getCollection("rankings_am");
               String title = page.select("span[id=year-box]").text();
               Elements table  = page.select("table[id=na-list]");
               Elements data_set = table.select("tr");
               List amList = new ArrayList();
               for (Element id : data_set) { 
            	   Elements tds  = id.select("td");
            	   for (Element td : tds) 
            	   { 
            		   amList.add(td.text());
            	   }
            	   String en_name = tds.select("p[class=first-line]").text();
            	   String cn_name = tds.select("p[class=second-line]").text();
        		   if (amList.size()>0 ){
        			   System.out.println(amList);
                       BasicDBObject dbObject = new BasicDBObject();
        			   dbObject.append("title", title).append("rank", amList.get(0)).append("mov_cnname", cn_name).append("mov_enname", en_name).append("toweek_rev", amList.get(2)).append("total_rev", amList.get(3)).append("val_week", amList.get(4));
        			   amList.removeAll(amList);
            		   dbCollection.insert(dbObject);
        		   }
               }
               mongoClient.close();               
        }
        else if (page.matchUrl("http://pf.maoyan.com/rankings/day.*")) {     
        	 MongoClient mongoClient2 = new MongoClient( "localhost" , 27017 );
             // 连接到数据库
             DB db = mongoClient2.getDB("maoyan_crawler");
             // 遍历所有集合的名字
             Set<String> colls = db.getCollectionNames();
             for (String s : colls) {
             // 先删除所有Collection(类似于关系数据库中的"表")
             if (s.equals("rankings_day")) {
          	   db.getCollection(s).drop();
             }
             }
             DBCollection dbCollection2 = db.getCollection("rankings_day");
             String title = page.select("span[id=year-box]").text();
             String update_time = page.select("span[id=update-time]").text();
             title=title + update_time;
             System.out.println(title);
             Elements data_set = page.select("tr");
             List dayList = new ArrayList();
             for (Element id : data_set) { 
        	     Elements tds  = id.select("td");
        	     for (Element td : tds) 
        	     { 
        		     dayList.add(td.text());
        	     }
    		     if (dayList.size()>0 ){
    			     System.out.println(dayList);
                     BasicDBObject dbObject = new BasicDBObject();
    			     dbObject.append("title", title).append("rank", dayList.get(0)).append("mov_name", dayList.get(1)).append("today_rev", dayList.get(2)).append("date", dayList.get(3)).append("val_week", dayList.get(4));
    			     dayList.removeAll(dayList);
        		     dbCollection2.insert(dbObject);
    		   }
           }
           mongoClient2.close();               
    }
        else if (page.matchUrl("http://pf.maoyan.com/rankings/market.*")) {     
       	 MongoClient mongoClient3 = new MongoClient( "localhost" , 27017 );
            // 连接到数据库
            DB db = mongoClient3.getDB("maoyan_crawler");
            // 遍历所有集合的名字
            Set<String> colls = db.getCollectionNames();
            for (String s : colls) {
            // 先删除所有Collection(类似于关系数据库中的"表")
            if (s.equals("rankings_market")) {
         	   db.getCollection(s).drop();
            }
            }
            DBCollection dbCollection3 = db.getCollection("rankings_market");
          String title = page.select("span[id=year-box]").text();
          String update_time = page.select("span[id=update-time]").text();
          title=title + update_time;
          System.out.println(title);
          Elements data_set = page.select("tr");
          List dayList = new ArrayList();
          for (Element id : data_set) { 
       	   Elements tds  = id.select("td");
       	   for (Element td : tds) 
       	   { 
       		   dayList.add(td.text());
       	   }
   		   if (dayList.size()>0 ){
   			   System.out.println(dayList);
               BasicDBObject dbObject = new BasicDBObject();
   			   dbObject.append("title", title).append("rank", dayList.get(0)).append("date", dayList.get(1)).append("today_rev", dayList.get(2)).append("total_sessions", dayList.get(3)).append("total_visit_count", dayList.get(4));
   			   dayList.removeAll(dayList);
   			   dbCollection3.insert(dbObject);
   		   }
          }
          mongoClient3.close();               
   }
        else if (page.matchUrl("http://pf.maoyan.com/rankings/year.*")) {     
          	 MongoClient mongoClient4 = new MongoClient( "localhost" , 27017 );
               // 连接到数据库
               DB db = mongoClient4.getDB("maoyan_crawler");
               // 遍历所有集合的名字
               Set<String> colls = db.getCollectionNames();
               for (String s : colls) {
               // 先删除所有Collection(类似于关系数据库中的"表")
               if (s.equals("rankings_year")) {
            	   db.getCollection(s).drop();
               }
               }
               DBCollection dbCollection4 = db.getCollection("rankings_year");
             String title = page.select("span[id=year-box]").text();
             String update_time = page.select("span[id=update-time]").text();
             title=title + update_time;
             System.out.println(title);
             Elements table  = page.select("div[id=ranks-list]");
//             System.out.println(table);
             Elements data_set = table.select("ul[class=row]");
//             System.out.println(data_set);
             List dayList = new ArrayList();
             for (Element id : data_set) { 
          	   Elements lis  = id.select("li");
          	   for (Element li : lis) 
          	   { 
          		   dayList.add(li.text());
          	   }
        	   String cn_name = lis.select("p[class=first-line]").text();
        	   String release_date = lis.select("p[class=second-line]").text();
      		   if (dayList.size()>0 ){
      			   System.out.println(dayList);
                  BasicDBObject dbObject = new BasicDBObject();
      			   dbObject.append("title", title).append("rank", dayList.get(0)).append("name", cn_name).append("release date", release_date).append("year_rev", dayList.get(2)).append("avg_price", dayList.get(3)).append("avg_visit_count", dayList.get(4));
      			   dayList.removeAll(dayList);
      			   dbCollection4.insert(dbObject);
      		   }
             }
             mongoClient4.close();               
      }
        else if (page.matchUrl("http://pf.maoyan.com/")) {     
         	 MongoClient mongoClient5 = new MongoClient( "localhost" , 27017 );
              // 连接到数据库
              DB db = mongoClient5.getDB("maoyan_crawler");
              // 遍历所有集合的名字
              Set<String> colls = db.getCollectionNames();
              for (String s : colls) {
              // 先删除所有Collection(类似于关系数据库中的"表")
              if (s.equals("main_page")) {
           	   db.getCollection(s).drop();
              }
              }
              DBCollection dbCollection5 = db.getCollection("main_page");
            String title = page.select("span[id=dayStr]").text();
            String box_type = page.select("span[id=box-type]").text();
            String ticket_count = page.select("span[id=ticket_count]").text();
            box_type=box_type + ticket_count;
            System.out.println(title+ "\n" +box_type);
            Elements table  = page.select("div[id=ticket_tbody]");
            //System.out.println(table);
            Elements data_set = table.select("ul");
            //System.out.println(data_set);
            List dayList = new ArrayList();
            for (Element id : data_set) { 
         	   Elements lis  = id.select("li");
         	   for (Element li : lis) 
         	   { 
         		   dayList.add(li.text());
         	   }
//           System.out.println(lis);
       	   String cn_name = lis.select("b").first().text();
       	   String comment = lis.select("em").text();
  //         System.out.println(cn_name+ "\n" +comment);
     		   if (dayList.size()>0 ){
     			   System.out.println(dayList);
                 BasicDBObject dbObject = new BasicDBObject();
     			   dbObject.append("title", title).append("box_type", box_type).append("name", cn_name).append("comment", comment).append("realtime_rev", dayList.get(1)).append("rev_percent", dayList.get(2)).append("schedule_percent", dayList.get(3)).append("total_rev", dayList.get(4));
     			   dayList.removeAll(dayList);
     			   dbCollection5.insert(dbObject);
     		   }
            }
            mongoClient5.close();               
     }
    }

    public static void main(String[] args) throws Exception {
        MaoyanCrawler crawler = new MaoyanCrawler("maoyan", true);
        crawler.addSeed("http://pf.maoyan.com/rankings/america?_v_=yes");
        crawler.addSeed("http://pf.maoyan.com/rankings/market?_v_=yes");
        crawler.addSeed("http://pf.maoyan.com/rankings/day?_v_=yes");
        crawler.addSeed("http://pf.maoyan.com/rankings/year?_v_=yes");
        crawler.addSeed("http://pf.maoyan.com/");
        //crawler.addRegex("http://blog.csdn.net/.*/article/details/.*");
        
        /*可以设置每个线程visit的间隔，这里是毫秒*/
        //crawler.setVisitInterval(1000);
        /*可以设置http请求重试的间隔，这里是毫秒*/
        //crawler.setRetryInterval(1000);
        
        crawler.setThreads(2);
        crawler.start(1);
    }

}
