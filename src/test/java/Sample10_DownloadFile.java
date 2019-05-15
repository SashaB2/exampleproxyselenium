import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.filters.ResponseFilter;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;
import org.openqa.selenium.By;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;


//this logic does't work in way like on video/tutorial. content changed in download file. browser do'
public class Sample10_DownloadFile {
    private WebDriver driver;
    private BrowserMobProxy proxy = new BrowserMobProxyServer();

    @BeforeTest
    public void setUp() {
        proxy.start(8071);
        proxy.removeHeader("User-Agent");

        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

        ResponseFilter downloader = new FileDownloader().addContentType("application/x-msdownload");
        proxy.addResponseFilter(downloader);

        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
//        chromePrefs.put("download.default_directory", System.getProperty("user.dir"));
        chromePrefs.put("safebrowsing.enabled", "true");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-popup-blocking");
        options.setProxy(seleniumProxy);
        options.setExperimentalOption("prefs", chromePrefs);

        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(ChromeOptions.CAPABILITY, options);


        File fileFF = new File("./drivers/chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", fileFF.getAbsolutePath());

        driver = new ChromeDriver(cap);
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
        driver.navigate().to("http://demo.guru99.com/test/yahoo.html");
        driver.findElement(By.id("messenger-download")).click();
        Thread.sleep(1000000);
    }

    private class FileDownloader implements ResponseFilter {
        private Set<String> contentTypes = new HashSet<>();
        private File tempDir = new File(System.getProperty("user.dir"));
        private File tempFile = null;

        public FileDownloader addContentType(String contentType) {
            contentTypes.add(contentType);
            return this;
        }

        @Override
        public void filterResponse(HttpResponse httpResponse, HttpMessageContents httpMessageContents, HttpMessageInfo httpMessageInfo)  {
            String contentType = httpResponse.headers().get("Content-Type");

            if(contentTypes.contains(contentType)){
                System.out.println("Come In");

                String postfix = contentType.substring(contentType.indexOf('/') + 1);
                try {
                    tempFile = File.createTempFile("downloaded", "." + postfix, tempDir);
                } catch (IOException e) {
                    System.out.println("Error's creating temp file");
                }

                try(FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                    outputStream.write(httpMessageContents.getBinaryContents());
                }catch (Exception e){
                    System.out.println("Error's writing content to temp file");

                }

                httpResponse.headers().add("Content-Type", "text/html");
                httpResponse.headers().add("Vary","Accept-Encoding,User-Agent");
                httpResponse.headers().add("Server"," Apache");
                httpResponse.headers().add("Content-Length", "" + tempFile.getAbsolutePath().length());
                httpMessageContents.setTextContents(tempFile.getAbsolutePath());
            }
        }
    }
}