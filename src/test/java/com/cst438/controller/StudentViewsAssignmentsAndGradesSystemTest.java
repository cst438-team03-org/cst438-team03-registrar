package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
public class StudentViewsAssignmentsAndGradesSystemTest {

    static final String CHROME_DRIVER_FILE_LOCATION = "C:/chromedriver-win64/chromedriver.exe";
    static final String URL = "http://localhost:5173";   // react dev server

    static final int DELAY = 2000;
    WebDriver driver;

    Wait<WebDriver> wait;

    Random random = new Random();

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        driver.get(URL);
    }

    @AfterEach
    public void quit() {
        driver.quit();
    }

    @Test
    public void studentViewsAssignmentsAndGradesSystemTest() throws InterruptedException {
        String assignmentTitle = "Test Assignment " + new Random().nextInt(1000);
        String dueDate = "12012025";

        // === LOGIN AS INSTRUCTOR ===
        driver.findElement(By.id("email")).sendKeys("ted@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("ted2025");
        driver.findElement(By.id("loginButton")).click();

        // SELECT TERM
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("selectTermButton")).click();

        // OPEN CST599 ASSIGNMENTS
        driver.findElement(By.xpath("//tr[td[contains(text(),'cst599')]]//a[contains(text(),'Assignments')]")).click();

        // ADD ASSIGNMENT
        driver.findElement(By.xpath("//button[contains(text(),'Add Assignment')]")).click();
        driver.findElement(By.xpath("//input[@placeholder='Assignment title']")).sendKeys(assignmentTitle);
        driver.findElement(By.xpath("//input[@type='date']")).sendKeys(dueDate);
        driver.findElement(By.xpath("//dialog//button[contains(text(),'Save')]")).click();

        // VERIFY IT WAS ADDED
        Thread.sleep(DELAY);
        WebElement createdRow = driver.findElement(By.xpath("//td[contains(text(),'" + assignmentTitle + "')]/parent::tr"));
        assertNotNull(createdRow);

        // LOGOUT
        driver.findElement(By.id("logoutLink")).click();

        // LOGIN AS STUDENT
        driver.findElement(By.id("email")).sendKeys("samb@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("samb2025");
        driver.findElement(By.id("loginButton")).click();

        // GO TO VIEW ASSIGNMENTS
        driver.findElement(By.id("viewAssignmentsLink")).click();

        // ENTER YEAR + SEMESTER
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.xpath("//button[text()='Get Assignments']")).click();

        // VERIFY ASSIGNMENT APPEARS, SCORE IS BLANK
        Thread.sleep(DELAY);
        WebElement assignmentRow = driver.findElement(By.xpath("//tr[td[contains(text(),'" + assignmentTitle + "')]]"));
        String scoreText = assignmentRow.findElement(By.xpath("td[last()]")).getText().trim(); // Assuming last column is Score
        assertEquals("", scoreText);
    }

}