package com.cst438.controller;

import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.LoginDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testGetSchedule() throws Exception {
        String email = "sam@csumb.edu";
        String password = "sam2025";

        // Login and get JWT
        EntityExchangeResult<LoginDTO> loginResult = webTestClient.get().uri("/login")
            .headers(h -> h.setBasicAuth(email, password))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(LoginDTO.class)
            .returnResult();

        LoginDTO login = loginResult.getResponseBody();
        assertNotNull(login, "LoginDTO should not be null");
        String jwt = login.jwt();
        assertNotNull(jwt, "JWT token should not be null");

        // Call GET /enrollments with year and semester query params
        EntityExchangeResult<EnrollmentDTO[]> result = webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/enrollments")
                .queryParam("year", 2025)
                .queryParam("semester", "Fall")
                .build())
            .headers(h -> h.setBearerAuth(jwt))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(EnrollmentDTO[].class)
            .returnResult();

        EnrollmentDTO[] enrollments = result.getResponseBody();
        assertNotNull(enrollments, "Enrollments response should not be null");
        assertTrue(enrollments.length > 0, "Student should have at least one enrollment in the schedule");
    }

    @Test
    public void testGetTranscript() throws Exception {
        String email = "sam@csumb.edu";
        String password = "sam2025";

        // Login and get JWT
        EntityExchangeResult<LoginDTO> loginResult = webTestClient.get().uri("/login")
            .headers(h -> h.setBasicAuth(email, password))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(LoginDTO.class)
            .returnResult();

        LoginDTO login = loginResult.getResponseBody();
        assertNotNull(login, "LoginDTO should not be null");
        String jwt = login.jwt();
        assertNotNull(jwt, "JWT token should not be null");

        // Call GET /transcripts
        EntityExchangeResult<EnrollmentDTO[]> result = webTestClient.get()
            .uri("/transcripts")
            .headers(h -> h.setBearerAuth(jwt))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(EnrollmentDTO[].class)
            .returnResult();

        EnrollmentDTO[] transcript = result.getResponseBody();
        assertNotNull(transcript, "Transcript response should not be null");
        assertTrue(transcript.length > 0, "Student should have at least one enrollment in the transcript");
    }
}
