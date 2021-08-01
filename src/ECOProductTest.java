import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ECOProductTest {
    WebDriver driver;
    String[] credentialsTable;
    CredentialsProvider cp;
    String excelFile;
    List<String> productTable = new ArrayList<>();

    public static void main(String[] args) throws IOException, InvalidFormatException {
        ECOProductTest et = new ECOProductTest();
        et.setUp();
        et.checkIfProductsAreDisplayed();
        et.tearDown();
    }

    public void setUp() throws InvalidFormatException, IOException {
        credentialsTable = FileOperations.selectAndOpenTextFile();
        credentialsTable = FormsOperations.modifyDataBasedOnFormInput(credentialsTable);
        this.cp = new CredentialsProvider(credentialsTable);

        excelFile = FileOperations.selectAndOpenExcelFile();
        productTable = FileOperations.getProductNumbersFromExcelFile(excelFile);

        WebDriverManager.chromedriver().setup();
        this.driver = new ChromeDriver();

        this.driver.manage().window().maximize();
        //this.driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
    }

    public void tearDown() {
        this.driver.quit();
    }

    public void checkIfProductsAreDisplayed() throws IOException, InvalidFormatException {
        String basePageUrl = this.cp.getUrl();
        this.driver.get(basePageUrl+"/login");

        LoginPage lp = new LoginPage(this.driver);
        lp.selectLanguage(cp.getLanguage());
        lp.fillUserName(cp.getUsername());
        lp.fillPassword(cp.getPassword());
        lp.clickSubmit();

        SoldToSelectionPage stsp = new SoldToSelectionPage(this.driver);
        stsp.selectSoldToParty(cp.getStp());

        ProductPage pp = new ProductPage(this.driver, basePageUrl);
        Boolean productDisplayStatus;

        for(int i=0; i<productTable.size(); ++i) {
            productDisplayStatus = pp.checkSingleProductDisplay(productTable.get(i));
            FileOperations.writeStatusToProductDisplayedInExcel(excelFile, productTable.get(i), productDisplayStatus);
        }
    }
}
