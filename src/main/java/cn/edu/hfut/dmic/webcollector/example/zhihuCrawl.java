package cn.edu.hfut.dmic.webcollector.example;

import java.util.regex.Pattern;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;

public class zhihuCrawl extends BreadthCrawler {

	public zhihuCrawl(String crawlPath, boolean autoParse) {
		super(crawlPath, autoParse);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void visit(Page page, CrawlDatums next) {
		// TODO Auto-generated method stub
		String question_regex="^http://www.zhihu.com/question/[0-9]+";
//	    if(Pattern.matches(question_regex, page.getUrl())){
	      System.out.println("正在抽取"+page.getUrl());
	      String title=page.getDoc().title();
	      System.out.println(title);
	      String question=page.getDoc().select("div[id=zh-question-detail]").text();
	      System.out.println(question);
	      
//	    }
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		zhihuCrawl zhc = new zhihuCrawl("zhc",true);
		zhc.addSeed("http://www.zhihu.com/topic/19552330/top-answers");
		zhc.addRegex("http://www.zhihu.com/topic/.*");
        /*不要爬取jpg|png|gif*/
		zhc.addRegex("-.*\\.(jpg|png|gif).*");
        /*不要爬取包含"#"的链接*/
		zhc.addRegex("-.*#.*");
		zhc.setVisitInterval(1000);
        /*可以设置http请求重试的间隔，这里是毫秒*/
		zhc.setRetryInterval(1000);
		zhc.setThreads(2);
		zhc.start(5);
		

	}

}