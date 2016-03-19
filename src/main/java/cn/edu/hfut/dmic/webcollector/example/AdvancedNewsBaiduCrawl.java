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
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
public class AdvancedNewsBaiduCrawl{
    public static void main(String[] args) throws Exception{
 final WebClient webclient = new WebClient(BrowserVersion.CHROME);  
            final HtmlPage htmlpage = webclient  
                    .getPage("http://news.baidu.com/advanced_news.html");  
            webclient.getOptions().setCssEnabled(false);
            webclient.getOptions().setJavaScriptEnabled(true);
            // System.out.println(htmlpage.getTitleText());  
            final HtmlForm form = htmlpage.getFormByName("f");  
            final HtmlSubmitInput button = form.getInputByValue("百度一下");  
            final HtmlTextInput textField = form.getInputByName("q1");  
			textField.setValueAttribute("疯狂动物城");  
            final List<HtmlRadioButtonInput> radioButtons = form  
                    .getRadioButtonsByName("s");  
            radioButtons.get(0).setChecked(false);  
            radioButtons.get(1).setChecked(true);// 选中限定时间段的radion button  
            final List<HtmlRadioButtonInput> titleButtons = form  
                    .getRadioButtonsByName("tn");  
            titleButtons.get(0).setChecked(false);  
            titleButtons.get(1).setChecked(true); //选中“仅在新闻的标题中”的radion button  
            HtmlHiddenInput bt = form.getInputByName("bt");  
            bt.setValueAttribute("1456761600"); //2007-1-1的时间戳  
            HtmlHiddenInput y0 = form.getInputByName("y0");  
            y0.setValueAttribute("2016"); //2007-1-1的时间戳 
            HtmlHiddenInput m0 = form.getInputByName("m0");  
            m0.setValueAttribute("3"); //2007-1-1的时间戳 
            HtmlHiddenInput d0 = form.getInputByName("d0");  
            d0.setValueAttribute("1"); //2007-1-1的时间戳 
            HtmlHiddenInput et = form.getInputByName("et");  
            et.setValueAttribute("1457107200"); //2007-12-31的时间戳  
            HtmlHiddenInput y1 = form.getInputByName("y1");  
            y1.setValueAttribute("2016"); //2007-1-1的时间戳 
            HtmlHiddenInput m1 = form.getInputByName("m1");  
            m1.setValueAttribute("3"); //2007-1-1的时间戳 
            HtmlHiddenInput d1 = form.getInputByName("d1");  
            d1.setValueAttribute("5"); //2007-1-1的时间戳
            System.out.println(form.asXml()); 
            System.out.println(form.asText()); 
            final HtmlPage page2 = button.click();  
            String result = page2.asText();  
            System.out.println(result); 
            Pattern pattern = Pattern.compile("找到相关新闻 约(.*) 篇");
            Matcher matcher = pattern.matcher(result);  
            webclient.closeAllWindows();  
                    if (matcher.find())  
                System.out.println(matcher.group(1));  
    }
}