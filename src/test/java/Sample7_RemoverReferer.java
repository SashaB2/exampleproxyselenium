import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.filters.RequestFilter;
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

public class Sample7_RemoverReferer {
    //https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Referer
    /*
    * The Referer request header contains the address of the previous web page from which a link
    * to the currently requested page was followed. The Referer header allows servers to identify
    * where people are visiting them from and may use that data for analytics, logging, or optimized
    * caching, for example.
    */

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


        proxy.addRequestFilter(new RequestFilter() {
            @Override
            public HttpResponse filterRequest(HttpRequest httpRequest, HttpMessageContents httpMessageContents, HttpMessageInfo httpMessageInfo) {
                httpRequest.headers().remove("Referer");
                httpRequest.headers().remove("referer"); //need both variant? the best variant verify before
                System.out.println(httpRequest.headers().entries().toString());
                return null;
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
        driver.navigate().to("https://rozetka.com.ua/ua/");
        Thread.sleep(10000);
    }
}
