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

public class Sample5_ChangeLanguage {
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


        proxy.addRequestFilter((httpRequest, httpMessageContents, httpMessageInfo) -> {
            httpRequest.headers().remove("Accept-Language");

            httpRequest.headers().add("Accept-Language", "ua");
//                httpRequest.headers().add("Accept-Language", "ru");   // if need russia use it, otherwise use ua
            System.out.println(httpRequest.headers().entries().toString());
            return null;
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