import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
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

public class Sample6_ChangeResponse {
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
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        driver.manage().window().maximize();

        proxy.addResponseFilter(new ResponseFilter() {
            @Override
            public void filterResponse(HttpResponse httpResponse, HttpMessageContents httpMessageContents, HttpMessageInfo httpMessageInfo) {
                httpResponse.headers().add("Hello-World", "Hellowa");
                System.out.println(httpResponse.headers().entries().toString());
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
        proxy.newHar();
        driver.navigate().to("http://demo.guru99.com/test/yahoo.html");
        Har har = proxy.getHar();

        StringBuffer headers = new StringBuffer();
        String devider = ", ";
        String semocolon = " : ";

        proxy.endHar();

//        for (HarEntry entry : har.getLog().getEntries()) {
//            HarRequest request = entry.getRequest();
//            HarResponse response = entry.getResponse();
//
//            System.out.println(request.getUrl() + " : " + response.getStatus()
//                    + ", " + entry.getTime() + "ms");
//            System.out.println(response.getStatus());
//
//        }


        }
    }
