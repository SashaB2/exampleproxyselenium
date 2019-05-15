import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.filters.ResponseFilter;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class Sample9_InjectJavaScript {
    private WebDriver driver;
    private BrowserMobProxy proxy = new BrowserMobProxyServer();

    @BeforeTest
    public void setUp() {
        proxy.start(8071);
        proxy.removeHeader("User-Agent");

        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

        File fileFF = new File("./drivers/chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", fileFF.getAbsolutePath());

        driver = new ChromeDriver(desiredCapabilities);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        driver.manage().window().maximize();


        InjectJavaScript injectJavaScript = new InjectJavaScript();
        proxy.addResponseFilter(injectJavaScript);
    }

    @AfterTest
    public void tearDown() {
        driver.quit();
        proxy.stop();
    }

    @Test
    public void readRequestResponceTest() throws InterruptedException {
        driver.navigate().to("http://demo.guru99.com/test/yahoo.html");
        Thread.sleep(10000);
    }

    private class InjectJavaScript implements ResponseFilter{

        @Override
        public void filterResponse(HttpResponse httpResponse, HttpMessageContents httpMessageContents, HttpMessageInfo httpMessageInfo) {
            if(httpResponse.headers().entries().toString().contains("text/html")){
                System.out.println(httpMessageContents.getTextContents());

                String content = httpMessageContents.getTextContents();  //get content

                String newContent = content.replace("</head>", "<script src=\"https://code.jquery.com/jquery-[l[]l.js\" integrity=\"sha256-WpOohJOqMqqyKL9FccASB9O0KwACQJpFTUBLTYOVvVU=\" crossorigin=\"anonymous\"></script></head>");  // change content

                httpMessageContents.setTextContents(newContent);  //set content

                System.out.println(httpMessageContents.getTextContents());
            }
        }
    }
}