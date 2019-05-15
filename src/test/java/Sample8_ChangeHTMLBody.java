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
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class Sample8_ChangeHTMLBody {
    private WebDriver driver;
    private BrowserMobProxy proxy = new BrowserMobProxyServer();

    @BeforeTest
    public void setUp() {
        proxy.start(8071);
        proxy.removeHeader("User-Agent");

        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-popup-blocking");
        options.setProxy(seleniumProxy);

//        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
//        desiredCapabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

        File fileFF = new File("./drivers/chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", fileFF.getAbsolutePath());

//        driver = new ChromeDriver(desiredCapabilities);
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        driver.manage().window().maximize();

        proxy.addResponseFilter(new ResponseFilter() {
            @Override
            public void filterResponse(HttpResponse httpResponse, HttpMessageContents httpMessageContents, HttpMessageInfo httpMessageInfo) {
                if(httpResponse.headers().entries().toString().contains("text/html")){
                    System.out.println(httpMessageContents.getTextContents());

                    httpMessageContents.setTextContents("Hello world ");

                    System.out.println(httpMessageContents.getTextContents());
                }
            }
        });
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
}