package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
public class EnterEnrollmentGradesSystemTest {

    static final String CHROME_DRIVER_FILE_LOCATION = "D:\\CSUMB\\CST438\\labs\\lab4\\chromedriver-win64\\chromedriver.exe";
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
    public void finalGradeSystemTest() throws InterruptedException {

        //LOGIN
        String email ="ted@csumb.edu";
        String password="ted2025";
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("loginButton")).click();
        //Thread.sleep(DELAY);



        //ACCESS SECTIONS
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("selectTermButton")).click();
        //Thread.sleep(DELAY);

        //GO TO ENROLLMENT FOR CST599
        driver.findElement(By.xpath("//tr[td[contains(text(),'cst599')]]//a[contains(text(),'Enrollments')]")).click();
        //Thread.sleep(DELAY);


        //GRADE EACH STUDENT
        Map<String, String> emailToGrade = new HashMap<>();
        emailToGrade.put("sama@csumb.edu", "A");
        emailToGrade.put("samb@csumb.edu", "B+");
        emailToGrade.put("samc@csumb.edu", "C");

        // Find all table rows in tbody
        List<WebElement> rows = driver.findElements(By.xpath("//tbody/tr"));

        // Loop through each row
        for (WebElement row : rows) {
            // Get email text in the 4th td (index 3, since it's zero-based)
            String studentEmail = row.findElement(By.xpath("td[4]")).getText().trim();

            // If the email is one we want to grade
            if (emailToGrade.containsKey(studentEmail)) {
                // Find the input box for grade
                WebElement gradeInput = row.findElement(By.xpath("td[5]//input"));
                // Clear any existing value and enter the new grade
                gradeInput.clear();
                gradeInput.sendKeys(emailToGrade.get(studentEmail));
            }
        }

        // Click Save All Grades button
        WebElement saveButton = driver.findElement(By.xpath("//button[contains(text(),'Save All Grades')]"));
        saveButton.click();
        //Thread.sleep(DELAY);


        //VIEW THE CLASS ROSTER AGAIN AND VERIFY THE GRADE IS LISTED
        driver.findElement(By.id("homeLink")).click();
        //Thread.sleep(DELAY);
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("selectTermButton")).click();
        //Thread.sleep(DELAY);

        driver.findElement(By.xpath("//tr[td[contains(text(),'cst599')]]//a[contains(text(),'Enrollments')]")).click();
        //Thread.sleep(DELAY);



        //LOOP THROUGH THE TABLE AND ASSERT THE GRADES ARE A B+ C FOR SAMA SAMB SAMC
        List<WebElement> verifyRows = driver.findElements(By.xpath("//tbody/tr"));
        for (WebElement row : verifyRows) {
            // Get email text in the 4th td (index 3, since it's zero-based)
            String studentEmail = row.findElement(By.xpath("td[4]")).getText().trim();

            // If the email is one we want to verify
            if (emailToGrade.containsKey(studentEmail)) {
                // Find the input box for grade in the 5th td (index 4)
                WebElement gradeInput = row.findElement(By.xpath("td[5]//input"));
                String actualGrade = gradeInput.getAttribute("value").trim();
                String expectedGrade = emailToGrade.get(studentEmail);
                // Assert that the grade matches what we set earlier
                assertEquals(expectedGrade, actualGrade);
            }
        }

        //LOGOUT
        driver.findElement(By.id("logoutLink")).click();


        //LOGIN AS SAM
        driver.findElement(By.id("email")).sendKeys("samb@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("samb2025");
        driver.findElement(By.id("loginButton")).click();
//        Thread.sleep(DELAY);

        //VERIFY GRADE IS B+ FOR CST599
        driver.findElement(By.id("transcriptLink")).click();
        WebElement row = driver.findElement(By.xpath("//tbody/tr[td[contains(text(),'cst599')]]"));
        WebElement gradeTd = row.findElement(By.xpath("td[7]"));
        String grade = gradeTd.getText().trim();
        assertEquals("B+",grade);
//        Thread.sleep(DELAY);
    }

}
