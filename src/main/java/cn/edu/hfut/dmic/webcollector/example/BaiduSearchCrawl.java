package cn.edu.hfut.dmic.webcollector.example;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.regexp.recompile;
import org.apache.xalan.templates.ElemApplyImport;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class BaiduSearchCrawl {  
    public static void main(String args[])  
            throws FailingHttpStatusCodeException, MalformedURLException,  
            IOException, InterruptedException {  
        final WebClient webclient = new WebClient(BrowserVersion.FIREFOX_38);  
        final HtmlPage htmlpage = webclient.getPage("http://news.baidu.com/");  
        webclient.getOptions().setCssEnabled(false);
        webclient.getOptions().setJavaScriptEnabled(true);
        webclient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webclient.getOptions().setThrowExceptionOnScriptError(false);
        webclient.setAjaxController(new NicelyResynchronizingAjaxController());
         System.out.println(htmlpage.asText());  
         System.out.println(htmlpage.getTitleText());  
        final HtmlForm form = htmlpage.getFormByName("fbaidu");  
        final HtmlSubmitInput button = form.getInputByValue("百度一下");  
        final HtmlTextInput textField = form.getInputByName("word");  
        String keyword = "疯狂动物城";  
        textField.setValueAttribute(keyword);  
        HtmlPage page = button.click();  
        System.out.println(page);  
          
        textField.setValueAttribute(keyword);  
        HashSet<String> ts = new HashSet<String>();  
                int pagenum = 1;  
        while (page != null && pagenum < 38) {  
            java.util.List<HtmlAnchor> achList = page.getAnchors();  
  
            for (HtmlAnchor ach : achList) {  
                String url = ach.getHrefAttribute();  
                String s = url.substring(0, 1);  
                String regex = ".*?baidu.*?";  
                Pattern p = Pattern.compile(regex);  
                Matcher m = p.matcher(url);  
                if (s.equals("/") || s.equals("j") || url.length() > 100  
                        || m.find()) {  
                    continue;  
                }  
                System.out.println(url);  
                ts.add(url);  
            }  
            HtmlElement elepage = page.getHtmlElementById("page");  
            HtmlElement nextpage = null;  
            //获取下一页  
            if(pagenum == 1){  
                nextpage = (HtmlElement) elepage.getByXPath("//a[@class='n']")  
                    .get(0);  
            }else if(elepage.getByXPath("//a[@class='n']").size() == 2){  
                nextpage = (HtmlElement) elepage.getByXPath("//a[@class='n']")  
                        .get(1);  
            }  
            if(nextpage != null){  
                //点击下一页  
                page = nextpage.click();  
            }else {  
                page = null;  
                continue;  
            }  
            System.out.println("pagenum :" + pagenum + nextpage.asText());  
            pagenum ++;  
              
            //生成随机睡眠时间，防止被百度屏蔽  
            double time = Math.random()*45000;  
            int sleeptime = (int)time + 30000;  
            Thread.sleep(sleeptime);  
        }  
        webclient.closeAllWindows();  
    }  
}  