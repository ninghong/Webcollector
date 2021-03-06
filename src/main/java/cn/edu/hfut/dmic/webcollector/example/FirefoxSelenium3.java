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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.interactions.Actions;

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
public class FirefoxSelenium3 {

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
                if (s.equals("attend_rate")) {
             	   db.getCollection(s).drop();
                }
                }
                DBCollection dbCollection = db.getCollection("attend_rate");
                ProfilesIni pi = new ProfilesIni();
                FirefoxProfile profile = pi.getProfile("default");
                WebDriver driver = new FirefoxDriver(profile);
                driver.manage().window().maximize();
                driver.manage().timeouts().pageLoadTimeout(3, TimeUnit.SECONDS);
//                driver.setJavascriptEnabled(false);
                driver.get(datum.getUrl());
//                System.out.println(driver.getPageSource());
                driver.findElement(By.xpath("//*[@id='seat_city']")).click();
                driver.switchTo().window(driver.getWindowHandle());
                
                int city_num = driver.findElements(By.xpath("//div[@id='all-citys']/div/ul/li/a")).size();
                for(int i = 0;i<city_num;i++){
                System.out.println("A city chosen" + i);
                System.out.println(driver.findElements(By.xpath("//div[@id='all-citys']/div/ul/li/a")).get(i).getText());
                String city = driver.findElements(By.xpath("//div[@id='all-citys']/div/ul/li/a")).get(i).getText();
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", driver.findElements(By.xpath("//div[@id='all-citys']/div/ul/li/a")).get(i));
                ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, -250)", "");
                Thread.sleep(1000);
                new Actions(driver).moveToElement(driver.findElements(By.xpath("//div[@id='all-citys']/div/ul/li/a")).get(i)).click().perform();
                driver.switchTo().window(driver.getWindowHandle());                
//                System.out.println(driver.findElement(By.xpath("//span[@class='today']/em")).getText());
                System.out.println(driver.findElement(By.xpath("//span[@class='today']")).getText());
                for(int j = 0;j<driver.findElements(By.xpath("//div[@id='seat_table']//ul//li[@class='c1 lineDot']")).size();j++){
                	System.out.println(driver.findElements(By.xpath("//div[@id='seat_table']//ul//li[@class='c1 lineDot']")).get(j).getText());
                	System.out.println(driver.findElements(By.xpath("//div[@id='seat_table']//ul//li[@class='c2 red']")).get(j).getText());
                	System.out.println(driver.findElements(By.xpath("//div[@id='seat_table']//ul//li[@class='c3 gray']")).get(j).getText());
                	BasicDBObject dbObject = new BasicDBObject();
     			    dbObject.append("title", driver.findElement(By.xpath("//span[@class='today']")).getText())
     			    .append("city", city)
     			    .append("mov_cnname", driver.findElements(By.xpath("//div[@id='seat_table']//ul//li[@class='c1 lineDot']")).get(j).getText())
     			    .append("boxoffice_rate", driver.findElements(By.xpath("//div[@id='seat_table']//ul//li[@class='c2 red']")).get(j).getText())
     			    .append("visit_pershow", driver.findElements(By.xpath("//div[@id='seat_table']//ul//li[@class='c3 gray']")).get(j).getText());
         		    dbCollection.insert(dbObject);
                	}
                System.out.println("new city list to choose"); 
                new Actions(driver).moveToElement(driver.findElement(By.xpath("//*[@id='seat_city']"))).click().perform();
                driver.switchTo().window(driver.getWindowHandle());
                Thread.sleep(500);
               }
                driver.close();
                driver.quit();
                mongoClient.close(); 
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
