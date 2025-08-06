package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StudentEnrollsInSectionSystemTest {
    static final String CHROME_DRIVER_FILE_LOCATION = "/Users/JamesMondragon/Documents/School/chromedriver-mac-arm64/chromedriver";
    static final String URL = "http://localhost:5173";   // react dev server
    static final int DELAY = 2000;

    WebDriver driver;
    Wait<WebDriver> wait;

    @BeforeEach
    public void driverSetup() {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        driver.get(URL);
    }

    @AfterEach
    public void tearDown() { driver.quit(); }

    @Test
    public void studentEnrollsIntoASection() throws InterruptedException {
        //  Login as Sama
        driver.findElement(By.id("email")).sendKeys("sama@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("sama2025");
        driver.findElement(By.id("loginButton")).click();

        //  Wait for login to occur
        Thread.sleep(DELAY);

        // Navigate to Fall 2025 schedule
        driver.findElement(By.id("scheduleLink")).click();
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("selectTermButton")).click();

        //  Wait for term search to occur
        Thread.sleep(DELAY);

        //  Find CST 599 course and drop it
        WebElement cst599Row = driver.findElement(By.xpath("//tr[./td[text()='cst599']]"));
        cst599Row.findElement(By.xpath("//button[text()='Drop']")).click();

        //  Wait for react-confirm alert and confirm the drop
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath
                ("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")));
        WebElement yesButton =  driver.findElement(By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']"));
        yesButton.click();

        //  Navigate to add courses page
        driver.findElement(By.id("addCourseLink")).click();

        //  Wait for page to load
        Thread.sleep(DELAY);

        //  Enroll in CST 599
        WebElement enrollPageCst599Row = driver.findElement(By.xpath("//tr[td[text()='cst599']]"));
        enrollPageCst599Row.findElement(By.xpath(".//button[text()='Add']")).click();

        //  Wait for react-confirm alert and confirm the enrollment
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath
                ("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")));
        WebElement enrollmentPageYesButton = driver.findElement(By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']"));
        enrollmentPageYesButton.click();

        //  Wait for enrollment confirmation to go through
        Thread.sleep(DELAY);

        //  Logout
        driver.findElement(By.id("logoutLink")).click();

        //  Login as instructor ted
        driver.findElement(By.id("email")).sendKeys("ted@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("ted2025");
        driver.findElement(By.id("loginButton")).click();

        //  Wait for login to occur
        Thread.sleep(DELAY);

        //  Search for 2025 Fall term sections
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("selectTermButton")).click();

        //  Wait for term search to occur
        Thread.sleep(DELAY);

        //  Select CST 599 row and go to CST 599 Enrollments Page
        WebElement instructorPageCst599Row = driver.findElement(By.xpath("//tr[./td[text()='cst599']]"));
        instructorPageCst599Row.findElement(By.id("enrollmentsLink")).click();
        Thread.sleep(DELAY);

        //  Find any/all occurrences of sama on the website
        List<WebElement> studentSamaRow = driver.findElements(By.xpath("//tr[./td[text()='sama']]"));

        //  Expect only one Sama to be found
        assertEquals(1, studentSamaRow.size(), "Expected only one 'sama' enrollment");
    }
}