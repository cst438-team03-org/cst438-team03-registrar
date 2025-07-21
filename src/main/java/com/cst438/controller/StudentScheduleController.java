package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.service.GradebookServiceProxy;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
public class StudentScheduleController {

    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final GradebookServiceProxy gradebook;
    private final CourseRepository courseRepository;

    public StudentScheduleController(
            EnrollmentRepository enrollmentRepository,
            SectionRepository sectionRepository,
            UserRepository userRepository,
            GradebookServiceProxy gradebook,
            CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.gradebook = gradebook;
        this.courseRepository = courseRepository;
    }


    @PostMapping("/enrollments/sections/{sectionNo}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public EnrollmentDTO addCourse(
            @PathVariable int sectionNo,
            Principal principal ) throws Exception  {

        // create and save an EnrollmentEntity
        Enrollment  enrollment = new Enrollment();
        //  relate enrollment to the student's User entity and to the Section entity
        User student = userRepository.findByEmail(principal.getName());
        //  check that student is not already enrolled in the section
        Enrollment enrollmentCheck = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(
                sectionNo, student.getId()
        );
        if (enrollmentCheck != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    student.getName() + " already enrolled in section " + sectionNo);
        }
        enrollment.setStudent(student);
        //  check that the current date is not before addDate, not after addDeadline
        Section section = sectionRepository.findOpenSectionBySectionNo(sectionNo);
        if (section == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Section " + sectionNo + " either not open for enrollment or not found"
            );
        }
		//  of the section's term.  Return an EnrollmentDTO with the id of the
        enrollment.setSection(section);
		//  Enrollment and other fields.
		enrollmentRepository.save(enrollment);
        EnrollmentDTO enrollmentDTO = new EnrollmentDTO(
                enrollment.getEnrollmentId(),
                enrollment.getGrade(),
                student.getId(),
                student.getName(),
                student.getEmail(),
                section.getCourse().getCourseId(),
                section.getCourse().getTitle(),
                section.getSectionId(),
                section.getSectionNo(),
                section.getBuilding(),
                section.getRoom(),
                section.getTimes(),
                section.getCourse().getCredits(),
                section.getTerm().getYear(),
                section.getTerm().getSemester()
        );
        gradebook.sendMessage("addEnrollment", enrollmentDTO);
        return enrollmentDTO;
    }

    // student drops a course
    @DeleteMapping("/enrollments/{enrollmentId}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId, Principal principal) throws Exception {

        // check that enrollment belongs to the logged in student
        User student = userRepository.findByEmail(principal.getName());
        Enrollment enrollment = enrollmentRepository.findEnrollmentByStudentIdAndEnrollmentId(student.getId(), enrollmentId);
        if (enrollment == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    student.getName() + " not enrolled in course"
            );
        }
        Section section = enrollment.getSection();

		// and that today is not after the dropDeadLine for the term.
        Section sectionDeadlineCheck = sectionRepository.findOpenSectionBySectionNo(section.getSectionNo());
        if (sectionDeadlineCheck == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Deadline to drop " + section.getSectionNo() + " has passed"
            );
        }
        enrollmentRepository.delete(enrollment);
        gradebook.sendMessage("deleteEnrollment", enrollment.getEnrollmentId());
    }
}
