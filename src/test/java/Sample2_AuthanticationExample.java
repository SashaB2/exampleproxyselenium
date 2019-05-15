import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class Sample2_AuthanticationExample {
    private WebDriver driver;
    private BrowserMobProxy proxy = new BrowserMobProxyServer();

    @BeforeTest
    public void setUp() {
        proxy.start(8071);

//        proxy.autoAuthorization("", "guest", "guest", AuthType.BASIC); feature does not work
        String encodedCreadentials = "Basic " + (Base64.getEncoder().encodeToString("guest:guest".getBytes()));
        proxy.addHeader("Authorization", encodedCreadentials);

        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

        File fileFF = new File("./drivers/chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", fileFF.getAbsolutePath());

        driver = new ChromeDriver(desiredCapabilities);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        driver.manage().window().maximize();
    }

    @AfterTest
    public void tearDown() {
        driver.quit();
        proxy.stop();
    }

    @Test
    public void readRequestResponceTest() throws InterruptedException {
        Thread.sleep(5000);
        proxy.newHar();
        driver.navigate().to("https://jigsaw.w3.org/HTTP/Basic/");
        Har har = proxy.endHar();
        Assert.assertEquals(driver.findElement(By.xpath("//p[contains(text(),'Your browser made it!')]")).getText(), "Your browser made it!");

        Thread.sleep(5000);
    }
}