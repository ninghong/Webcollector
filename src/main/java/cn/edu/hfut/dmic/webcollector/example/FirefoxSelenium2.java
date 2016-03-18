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

import cn.edu.hfut.dmic.webcollector.crawldb.DBManager;
import cn.edu.hfut.dmic.webcollector.crawler.Crawler;
import cn.edu.hfut.dmic.webcollector.fetcher.Executor;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BerkeleyDBManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 本教程演示如何利用WebCollector爬取javascript生成的数据
 *
 * @author hu
 */
public class FirefoxSelenium2 {

    static {
        //禁用Selenium的日志
        Logger logger = Logger.getLogger("com.gargoylesoftware.htmlunit");
        logger.setLevel(Level.OFF);
    }


    public static void main(String[] args) throws Exception {
        Executor executor=new Executor() {
            @Override
            public void execute(CrawlDatum datum, CrawlDatums next) throws Exception {
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
                DBCollection dbCollection = db.getCollection("attend_rate");
                ProfilesIni pi = new ProfilesIni();
                FirefoxProfile profile = pi.getProfile("default");
                WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);
 //             driver.setJavascriptEnabled(false);
                webClient.getOptions().setCssEnabled(true);
                HtmlPage page = webClient.getPage(datum.getUrl());
//                System.out.println(driver.getPageSource());
//                System.out.println(page.getByXPath("//div[@id='seat_table']//ul//li[@class='c1 lineDot']/text()"));
               
                System.out.println(page.getByXPath("//span[@class='today']/em/text()"));
                System.out.println(page.getByXPath("//span[@class='today']/text()"));
                List<?> movie_name = page.getByXPath("//div[@id='seat_table']//ul//li[@class='c1 lineDot']/text()");
                List<?> boxoffice_rate = page.getByXPath("//div[@id='seat_table']//ul//li[@class='c2 red']/text()");
                List<?> visit_pershow = page.getByXPath("//div[@id='seat_table']//ul//li[@class='c3 gray']/text()");
                for(int i = 0;i<movie_name.size();i++){
                	System.out.println(movie_name.get(i));
                	System.out.println(boxoffice_rate.get(i));
                	System.out.println(visit_pershow.get(i));
                }
//                	BasicDBObject dbObject = new BasicDBObject();
//     			    dbObject.append("title", title).append("rank", amList.get(0)).append("mov_cnname", cn_name).append("mov_enname", en_name).append("toweek_rev", amList.get(2)).append("total_rev", amList.get(3)).append("val_week", amList.get(4));
//         		    dbCollection.insert(dbObject);
                webClient.closeAllWindows();

            }
        };

        //创建一个基于伯克利DB的DBManager
        DBManager manager=new BerkeleyDBManager("crawl");
        //创建一个Crawler需要有DBManager和Executor
        Crawler crawler= new Crawler(manager,executor);
        crawler.addSeed("http://pf.maoyan.com/attend/rate");
        crawler.start(1);
    }

}
