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
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 本教程演示如何利用WebCollector爬取javascript生成的数据
 *
 * @author hu
 */
public class MoviestoreCrawler {

	static {
		// 禁用Selenium的日志
		Logger logger = Logger.getLogger("com.gargoylesoftware.htmlunit");
		logger.setLevel(Level.OFF);
	}

	public static boolean doesWebElementExist(WebDriver driver, By selector) {
		try {
			driver.findElement(selector);
			return true;
		} catch (org.openqa.selenium.NoSuchElementException e) {
			return false;
		}
	}

	public static String ReturnString(WebDriver driver, By selector) {
		if (doesWebElementExist(driver, selector)) {
			return driver.findElement(selector).getText();
		} else
			return null;
	}

	public static void main(String[] args) throws Exception {
		Executor executor = new Executor() {
			@Override
			public void execute(CrawlDatum datum, CrawlDatums next) throws Exception {
				MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
				Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
			    mongoLogger.setLevel(Level.ERROR);
				// 连接到数据库
				// DBCollection dbCollection =
				// mongoClient.getDB("maoyan_crawler").getCollection("rankings_am");
				DB db = mongoClient.getDB("maoyan_crawler");
				// 遍历所有集合的名字
				DBCollection dbCollection = db.getCollection("movie_store");
				HtmlUnitDriver driver = new HtmlUnitDriver();
				driver.setJavascriptEnabled(false);
				driver.get(datum.getUrl());
				// System.out.println(driver.getPageSource());
				System.out.println(driver.findElement(By.xpath("//html/body/header/h1")).getText());
				// ProfilesIni pi = new ProfilesIni();
				// FirefoxProfile profile = pi.getProfile("default");
				// WebClient webClient = new
				// WebClient(BrowserVersion.FIREFOX_38);
				// driver.setJavascriptEnabled(false);
				// webClient.getOptions().setCssEnabled(true);
				// HtmlPage page = webClient.getPage(datum.getUrl());
				// System.out.println(page.asXml());
				// List<?> title =
				// page.getByXPath("//html/body/header/h1/text()");
				// List<?> score =
				// page.getByXPath("//html/body/section[1]/article[1]/aside/hgroup/article[1]/i/text()");
				// List<?> score_num =
				// page.getByXPath("//html/body/section[1]/article[1]/aside/hgroup/article[1]/span/text()");
				// List<?> want_num =
				// page.getByXPath("//html/body/section[1]/article[1]/aside/hgroup/article[2]/i/text()");
				// List<?> type =
				// page.getByXPath("//html/body/section[1]/article[1]/aside/p[1]/text()");
				// List<?> timing =
				// page.getByXPath("//html/body/section[1]/article[1]/aside/p[2]/text()");
				// List<?> system =
				// page.getByXPath("//html/body/section[1]/article[1]/aside/p[3]/text()");
				// List<?> onscreendate =
				// page.getByXPath("//html/body/section[1]/article[1]/aside/p[4]/text()");
				// List<?> total_rev =
				// page.getByXPath("//html/body/section[1]/article[2]/span[1]/text()");
				// List<?> firstweek_rev =
				// page.getByXPath("//html/body/section[1]/article[2]/span[2]/text()");
				// List<?> ahead_rev =
				// page.getByXPath("//html/body/section[1]/article[2]/span[3]/text()");
				// List<?> director =
				// page.getByXPath("//*[@id='infoContent']/article[1]/div/div[1]/div/div[2]/div[1]/div[2]/text()");
				// List<?> actor =
				// page.getByXPath("//*[@id='infoContent']/article[1]/div/div[1]/div/div[2]/div[2]/div[2]/text()");
				// List<?> production =
				// page.getByXPath("//*[@id='infoContent']/article[2]/div/div[1]/div/div[2]/text()");
				// List<?> distribution =
				// page.getByXPath("//*[@id='infoContent']/article[4]/div/div[1]/div/div[2]/text()");
				// List<?> technique =
				// page.getByXPath("//*[@id='infoContent']/article[5]/div/div[1]/div/div[2]/text()");
				// List<?> desc =
				// page.getByXPath("//*[@id='infoContent']/article[6]/div/div[2]/text()");
				// List<?> want_by_city =
				// page.getByXPath("//*[@id='wantCity']/ul/li[2]/b/text()");
				// List<?> want_val=
				// page.getByXPath("//*[@id='wantCity']/ul/li[3]/div/@style");
				// System.out.println(title);
				//// System.out.println(page.getByXPath("//span[@class='today']/em/text()"));
				//// System.out.println(page.getByXPath("//span[@class='today']/text()"));
				//// List<?> movie_name =
				// page.getByXPath("//div[@id='seat_table']//ul//li[@class='c1
				// lineDot']/text()");
				//// List<?> boxoffice_rate =
				// page.getByXPath("//div[@id='seat_table']//ul//li[@class='c2
				// red']/text()");
				//// List<?> visit_pershow =
				// page.getByXPath("//div[@id='seat_table']//ul//li[@class='c3
				// gray']/text()");
				//// for(int i = 0;i<movie_name.size();i++){
				//// System.out.println(movie_name.get(i));
				//// System.out.println(boxoffice_rate.get(i));
				//// System.out.println(visit_pershow.get(i));
				//// }
				// List<?> hbList = page.getByXPath("//div");
				// HtmlDivision hb = (HtmlDivision)hbList.get(0);
				// System.out.println(hb.toString());
				// System.out.println(ReturnString(driver,By.xpath("//*[@id='infoContent']/article[4]/div/div[1]/div/div[2]")));
				if (ReturnString(driver, (By.xpath("//html/body/header/h1"))).length() != 0) {
					BasicDBObject dbObject = new BasicDBObject();
					dbObject.append("title", ReturnString(driver, (By.xpath("//html/body/header/h1"))))
							.append("url", driver.getCurrentUrl())
							.append("score",
									ReturnString(driver,
											By.xpath("//html/body/section[1]/article[1]/aside/hgroup/article[1]/i")))
							.append("score_num",
									ReturnString(driver,
											By.xpath("//html/body/section[1]/article[1]/aside/hgroup/article[1]/span")))
							.append("want_num",
									ReturnString(driver,
											By.xpath("//html/body/section[1]/article[1]/aside/hgroup/article[2]/i")))
							.append("type",
									ReturnString(driver, By.xpath("//html/body/section[1]/article[1]/aside/p[1]")))
							.append("timing",
									ReturnString(driver, By.xpath("//html/body/section[1]/article[1]/aside/p[2]")))
							.append("system",
									ReturnString(driver, By.xpath("//html/body/section[1]/article[1]/aside/p[3]")))
							.append("onscreendate",
									ReturnString(driver, By.xpath("//html/body/section[1]/article[1]/aside/p[4]")))
							.append("total_rev",
									ReturnString(driver, By.xpath("//html/body/section[1]/article[2]/span[1]")))
							.append("firstweek_rev",
									ReturnString(driver, By.xpath("//html/body/section[1]/article[2]/span[2]")))
							.append("ahead_rev",
									ReturnString(driver, By.xpath("//html/body/section[1]/article[2]/span[3]")))
							.append("director",
									ReturnString(driver,
											By.xpath(
													"//*[@id='infoContent']/article[@class='m-info-crews m-info-section']/div/div[1]/div/div[2]/div[1]/div[2]")))
							.append("actor",
									ReturnString(driver,
											By.xpath(
													"//*[@id='infoContent']/article[@class='m-info-crews m-info-section']/div/div[1]/div/div[2]/div[2]/div[2]")))
							.append("production",
									ReturnString(driver,
											By.xpath(
													"//*[@id='infoContent']/article[@class='production-companies m-info-section']/div/div[1]/div/div[2]")))
							.append("jointproduction",
									ReturnString(driver,
											By.xpath(
													"//*[@id='infoContent']/article[@class='joint-production-companies m-info-section']/div/div[1]/div/div[2]")))
							.append("distribution",
									ReturnString(driver,
											By.xpath(
													"//*[@id='infoContent']/article[@class='distribution-firm m-info-section']/div/div[1]/div/div[2]")))
							.append("techpara",
									ReturnString(driver,
											By.xpath(
													"//*[@id='infoContent']/article[tech-params m-info-section]/div/div[1]/div/div[2]")))
							.append("infodrama",
									ReturnString(driver,
											By.xpath(
													"//*[@id='infoContent']/article[m-info-drama m-info-section]/div/div[2]")))
							.append("want_by_city", ReturnString(driver, By.xpath("//*[@id='wantCity']")));
					dbCollection.insert(dbObject);
					driver.close();
					driver.quit();
					mongoClient.close();
				}
			}
		};
		MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
		// 连接到数据库
		// DBCollection dbCollection =
		// mongoClient.getDB("maoyan_crawler").getCollection("rankings_am");
		DB db = mongoClient.getDB("maoyan_crawler");
		// 遍历所有集合的名字
		Set<String> colls = db.getCollectionNames();
		for (String s : colls) {
			// 先删除所有Collection(类似于关系数据库中的"表")
			if (s.equals("movie_store")) {
				db.getCollection(s).drop();
			}
		}

		// 创建一个基于伯克利DB的DBManager
		DBManager manager = new BerkeleyDBManager("maoyan");
		// 创建一个Crawler需要有DBManager和Executor
		for (int round = 200; round < 300; round++) {
			System.out.println("Round " + round + " crawling.../n");
			Crawler crawler = new Crawler(manager, executor);
			for (int i = 1 + 100 * round; i <= 100 + 100 * round; i++) {
				crawler.addSeed("http://pf.maoyan.com/movie/" + i + "?_v_=yes");
			}
			/* 可以设置每个线程visit的间隔，这里是毫秒 */
			crawler.setVisitInterval(50);
			/* 可以设置http请求重试的间隔，这里是毫秒 */
			crawler.setRetryInterval(100);
			crawler.setThreads(10);
			crawler.start(1);
		}
	}

}
