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
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AddAssignmentSystemTest {

    static final String CHROME_DRIVER_FILE_LOCATION = "C:/Users/tstud/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";
    static final String URL = "http://localhost:5173"; // react dev server

    static final int DELAY = 2000;
    WebDriver driver;

    Wait<WebDriver> wait;

    Random random = new Random();

    @BeforeEach
    public void setUpDriver() {
        // set properties required by Chrome Driver
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
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
    public void testAddAssignment() throws InterruptedException {

        // Random assignment title
        int randomInt = random.nextInt(100, 1000);
        String assignmentTitle = "Assignment " + randomInt;
        // Assignment due date between 2025-08-20 and 2025-12-17
        String dueDate = "10/10/2025"; // MM/DD/YYYY format

        // Instructor ted@csumb.edu logs in
        driver.findElement(By.id("email")).sendKeys("ted@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("ted2025");
        driver.findElement(By.id("loginButton")).click();
        Thread.sleep(DELAY);

        // On the home page for instructor, enter 2025 and Fall to view the list of sections.
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("selectTermButton")).click();

        // Select view assignment for the section CST599 and add a new assignment.
        // find the element based on one of its sibling elements containing the section name
        WebElement sectionRow = driver.findElement(By.xpath("//td[contains(text(), 'cst599')]/.."));
        assertNotNull(sectionRow, "Section row should be present in the list of sections.");
//        System.out.println(sectionRow.getText());
        sectionRow.findElement(By.id("assignmentsLink")).click();
        Thread.sleep(DELAY);
        driver.findElement(By.id("addAssignmentButton")).click();

        // Enter a random title and due date for the assignment.
        Thread.sleep(DELAY);
        driver.findElement(By.id("title")).sendKeys(assignmentTitle);
        driver.findElement(By.id("dueDate")).sendKeys(dueDate);

        // Save the assignment then close the dialog.
        driver.findElement(By.id("save")).click();
        Thread.sleep(DELAY);

        // Verify that the new assignment title shows on the assignments page.
        WebElement newAssignment = driver.findElement(By.xpath("//td[contains(text(), '"+assignmentTitle+ "')]/.."));
        assertNotNull(newAssignment, "New assignment should be present in the list of assignments.");
//        System.out.println(newAssignment.getText());

        // Select the new assignment for grading.
        newAssignment.findElement(By.id("gradeButton")).click();
        Thread.sleep(DELAY);

        // Enter scores of 60, 88 and 98 for the 3 students enrolled in the section.
        // iterate through the rows of the table containing the grades
        List<WebElement> rows = driver.findElements(By.id("gradeInput"));
        assertEquals(3, rows.size(), "There should be 3 students enrolled in the section.");
        rows.get(0).sendKeys("60");
        rows.get(1).sendKeys("88");
        rows.get(2).sendKeys("98");
        Thread.sleep(DELAY);
        WebElement dialog = new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> d.findElement(By.cssSelector("dialog[open]")));

        // Save the grades and close the dialog.
        dialog.findElement(By.id("saveButton")).click();
        Thread.sleep(DELAY);

        // Grade the assignment again and verify the scores.
        newAssignment.findElement(By.id("gradeButton")).click();
        Thread.sleep(DELAY);
        WebElement dialog2 = new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> d.findElement(By.cssSelector("dialog[open]")));
        List<WebElement> updatedRows = dialog2.findElements(By.id("gradeInput"));
//        System.out.println("SIZE: " + updatedRows.size());
        assertEquals(60, Integer.parseInt(Objects.requireNonNull(updatedRows.get(0).getAttribute("value"))), "First student's score should be 60.");
        assertEquals(88, Integer.parseInt(Objects.requireNonNull(updatedRows.get(1).getAttribute("value"))), "Second student's score should be 88.");
        assertEquals(98, Integer.parseInt(Objects.requireNonNull(updatedRows.get(2).getAttribute("value"))), "Third student's score should be 98.");

        // Close the dialog.
        dialog2.findElement(By.id("closeButton")).click();
    }
}
