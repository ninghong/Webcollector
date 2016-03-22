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
import org.openqa.selenium.firefox.internal.ProfilesIni;

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
public class ShowrateCrawler {

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
                if (s.equals("show_rate")) {
             	   db.getCollection(s).drop();
                }
                }
                DBCollection dbCollection = db.getCollection("show_rate");
//                ProfilesIni pi = new ProfilesIni();
//                FirefoxProfile profile = pi.getProfile("default");
                WebDriver driver = new FirefoxDriver();
                driver.manage().window().maximize();
                driver.manage().timeouts().pageLoadTimeout(3, TimeUnit.SECONDS);
//                driver.setJavascriptEnabled(false);
                System.out.println("打开非黄金时段的座位\n");
                driver.get(datum.getUrl());
//                System.out.println(driver.getPageSource());
                List<WebElement> movie_name = driver.findElements(By.xpath("//div[@id='playPlan_table']/ul/li[@class='c1 lineDot']"));
                List<WebElement> boxoffice_rate = driver.findElements(By.xpath("//div[@id='playPlan_table']/ul/li[@class='c2 red']"));
                List<WebElement> visit_pershow = driver.findElements(By.xpath("//div[@id='playPlan_table']/ul/li[@class='c3 gray']"));
                WebElement title = driver.findElement(By.xpath("//p[@id='pieTip']"));
                for(int i = 0;i<movie_name.size();i++){
                	String movie_name_val = movie_name.get(i).getText();
                	String boxofficerate_val  = boxoffice_rate.get(i).getText();
                	String visit_pershow_val = visit_pershow.get(i).getText();
                	BasicDBObject dbObject = new BasicDBObject();
     			    dbObject.append("title", title.getText()).append("is_gold", "非黄金时段").append("show_type", "座位").append("movie_name", movie_name_val).append("boxoffice_rate", boxofficerate_val).append("visit_pershow", visit_pershow_val);
     			    dbCollection.insert(dbObject);
                	}
                System.out.println("打开黄金时段的复选框\n");
                WebElement click_gold = driver.findElement(By.id("playPlan_time"));
                click_gold.click();
                String gold_seat = driver.getWindowHandle();
                driver.switchTo().window(gold_seat);
                List<WebElement> movie_name_gold = driver.findElements(By.xpath("//div[@id='playPlan_table']/ul/li[@class='c1 lineDot']"));
                List<WebElement> boxoffice_rate_gold = driver.findElements(By.xpath("//div[@id='playPlan_table']/ul/li[@class='c2 red']"));
                List<WebElement> visit_pershow_gold = driver.findElements(By.xpath("//div[@id='playPlan_table']/ul/li[@class='c3 gray']"));
                WebElement title_gold = driver.findElement(By.xpath("//p[@id='pieTip']"));
                for(int i = 0;i<movie_name_gold.size();i++){
                	String movie_name_val = movie_name_gold.get(i).getText();
                	String boxofficerate_val  = boxoffice_rate_gold.get(i).getText();
                	String visit_pershow_val = visit_pershow_gold.get(i).getText();
                	BasicDBObject dbObject = new BasicDBObject();
                	dbObject.append("title", title.getText()).append("is_gold", "黄金时段").append("show_type", "座位").append("movie_name", movie_name_val).append("boxoffice_rate", boxofficerate_val).append("visit_pershow", visit_pershow_val);
     			    dbCollection.insert(dbObject);
                	}
                System.out.println("打开场次的复选框\n");
                WebElement click_vist = driver.findElement(By.xpath("//*[@id='show--type']"));
                click_vist.click();
                String gold_vist = driver.getWindowHandle();
                driver.switchTo().window(gold_vist);
                List<WebElement> movie_name_gold_visit = driver.findElements(By.xpath("//div[@id='playPlan_table']/ul/li[@class='c1 lineDot']"));
                List<WebElement> boxoffice_rate_gold_visit = driver.findElements(By.xpath("//div[@id='playPlan_table']/ul/li[@class='c2 red']"));
                List<WebElement> visit_pershow_gold_visit = driver.findElements(By.xpath("//div[@id='playPlan_table']/ul/li[@class='c3 gray']"));
                WebElement title_gold_visit = driver.findElement(By.xpath("//p[@id='pieTip']"));
                for(int i = 0;i<movie_name_gold_visit.size();i++){
                	String movie_name_val = movie_name_gold_visit.get(i).getText();
                	String boxofficerate_val  = boxoffice_rate_gold_visit.get(i).getText();
                	String visit_pershow_val = visit_pershow_gold_visit.get(i).getText();
                	BasicDBObject dbObject = new BasicDBObject();
                	dbObject.append("title", title.getText()).append("is_gold", "黄金时段").append("show_type", "场次").append("movie_name", movie_name_val).append("boxoffice_rate", boxofficerate_val).append("visit_pershow", visit_pershow_val);
     			    dbCollection.insert(dbObject);
                	}
                System.out.println("打开正常的复选框\n");
                click_gold.click();
                String normal_seat = driver.getWindowHandle();
                driver.switchTo().window(normal_seat);
                List<WebElement> movie_name_normal_seat = driver.findElements(By.xpath("//div[@id='playPlan_table']/ul/li[@class='c1 lineDot']"));
                List<WebElement> boxoffice_rate_normal_seat= driver.findElements(By.xpath("//div[@id='playPlan_table']/ul/li[@class='c2 red']"));
                List<WebElement> visit_pershow_normal_seat = driver.findElements(By.xpath("//div[@id='playPlan_table']/ul/li[@class='c3 gray']"));
                WebElement title_normal_seat = driver.findElement(By.xpath("//p[@id='pieTip']"));
                for(int i = 0;i<movie_name_normal_seat.size();i++){
                	String movie_name_val = movie_name_normal_seat.get(i).getText();
                	String boxofficerate_val  = boxoffice_rate_normal_seat.get(i).getText();
                	String visit_pershow_val = visit_pershow_normal_seat.get(i).getText();
                	BasicDBObject dbObject = new BasicDBObject();
                	dbObject.append("title", title.getText()).append("is_gold", "非黄金时段").append("show_type", "场次").append("movie_name", movie_name_val).append("boxoffice_rate", boxofficerate_val).append("visit_pershow", visit_pershow_val);
     			    dbCollection.insert(dbObject);
                	}
                
                driver.close();
                driver.quit();
                mongoClient.close();   
            }
        };

        //创建一个基于伯克利DB的DBManager
        DBManager manager=new BerkeleyDBManager("maoyan");
        //创建一个Crawler需要有DBManager和Executor
        Crawler crawler= new Crawler(manager,executor);
        crawler.addSeed("http://pf.maoyan.com/show/rate");
        crawler.start(1);
    }

}
