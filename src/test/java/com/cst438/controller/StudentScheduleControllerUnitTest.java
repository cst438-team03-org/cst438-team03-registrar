package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.LoginDTO;
import com.cst438.service.GradebookServiceProxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentScheduleControllerUnitTest {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @MockitoBean
    GradebookServiceProxy gradebookServiceProxy;

    @Test
    public void addEnrollmentThenDeleteEnrollment() throws Exception {
        //  Login as student and get security token
        String studentEmail = "sam@csumb.edu";
        String studentPassword = "sam2025";

        EntityExchangeResult<LoginDTO> login_dto =  webTestClient.get().uri("/login")
                .headers(headers -> headers.setBasicAuth(studentEmail, studentPassword))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginDTO.class).returnResult();

        LoginDTO login = login_dto.getResponseBody();
        assertNotNull(login);

        String jwt = login_dto.getResponseBody().jwt();
        assertNotNull(jwt);

        //  Enroll Sam in section 1: CST 489
        EntityExchangeResult<EnrollmentDTO> enrollmentResponse = webTestClient.post().uri("/enrollments/sections/1")
                .headers(headers -> headers.setBearerAuth(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnrollmentDTO.class).returnResult();
        assertNotNull(enrollmentResponse.getResponseBody());
        int studentId = enrollmentResponse.getResponseBody().studentId();
        int enrollmentId = enrollmentResponse.getResponseBody().enrollmentId();

        //  Confirm enrollment is in database
        Enrollment enrollment = enrollmentRepository.findEnrollmentByStudentIdAndEnrollmentId(studentId, enrollmentId);
        assertNotNull(enrollment);

        //  Verify that the "addEnrollment" message was sent to the gradebook service
        verify(gradebookServiceProxy, times(1)).sendMessage(eq("addEnrollment"), any());

        //  Delete Sam's enrollment
        webTestClient.delete().uri("/enrollments/{enrollmentId}", enrollmentId)
                .headers(headers -> headers.setBearerAuth(jwt))
                .exchange()
                .expectStatus().isOk();

        //  Confirm Sam's enrollment was deleted
        Enrollment enrollmentDeleteCheck = enrollmentRepository.findEnrollmentByStudentIdAndEnrollmentId(studentId, enrollmentId);
        assertNull(enrollmentDeleteCheck);

        //  Verify that the "deleteEnrollment" message was sent to the gradebook service
        verify(gradebookServiceProxy, times(1)).sendMessage(eq("deleteEnrollment"), any());
    }
}
