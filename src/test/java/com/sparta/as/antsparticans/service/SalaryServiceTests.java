package com.sparta.as.antsparticans.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class SalaryServiceTests {

    @Autowired
    SalaryService salaryService;

    @Test
    @DisplayName("Check Average Salary Works")
    void checkAverageSalaryWorks() {
        double average = salaryService.getAverageSalaryForADepartmentOnAGivenDate("Marketing", LocalDate.parse("2005-10-10"));
        System.out.printf("average salary for \"Marketing\" on \"2005-10-10\": $ %,.2f\n", average);
    }
}
