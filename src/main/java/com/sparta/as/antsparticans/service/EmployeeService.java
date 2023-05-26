package com.sparta.as.antsparticans.service;


import com.sparta.as.antsparticans.model.dtos.DeptEmpDTO;
import com.sparta.as.antsparticans.model.dtos.EmployeeDTO;
import com.sparta.as.antsparticans.model.repositories.DepartmentDTORepository;
import com.sparta.as.antsparticans.model.repositories.DeptEmpDTORepository;
import com.sparta.as.antsparticans.model.repositories.EmployeeDTORepository;
import com.sparta.as.antsparticans.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    public final EmployeeDTORepository employeeDTORepository;
    public final DeptEmpDTORepository deptEmpDTORepository;
    private final DepartmentDTORepository departmentDTORepository;

    @Autowired
    public EmployeeService(EmployeeDTORepository employeeDTORepository, DeptEmpDTORepository deptEmpDTORepository,
                           DepartmentDTORepository departmentDTORepository) {
        this.employeeDTORepository = employeeDTORepository;
        this.deptEmpDTORepository = deptEmpDTORepository;
        this.departmentDTORepository = departmentDTORepository;
    }

    public List<EmployeeDTO> findEmployeesByLastName(String lastName) {
        Optional<List<EmployeeDTO>> optional = employeeDTORepository.findEmployeeDTOByLastName(lastName);
        if (optional.get().size() > 0) {
            System.out.println("Found");
            return optional.get();
        } else {
            System.out.println("Not Found");
            return optional.get();
        }
    }

    public List<EmployeeDTO> findEmployeesWhoWorkedInDepartmentOnAGivenDate(String deptName, LocalDate date) {
        DeptEmpService deptEmpService = new DeptEmpService(departmentDTORepository, deptEmpDTORepository);

        List<DeptEmpDTO> listOfDeptEmpsWorkingInADepartmentOnAGivenDate =  deptEmpService.getListOfDeptEmpsWorkingInADepartmentOnAGivenDate(deptName, date);

        List<Integer> listOfEmployeeIdsWorkingInADepartmentOnAGivenDate = Utils.extractEmployeeIdsFromListOfDeptEmpDTOs(listOfDeptEmpsWorkingInADepartmentOnAGivenDate);

        List<EmployeeDTO> listOfAllEmployees = employeeDTORepository.getAllEmployees();

        List<EmployeeDTO> employeesWorkingInGivenDepartmentOnGivenDay = listOfAllEmployees.stream().filter(employeeDTO -> listOfEmployeeIdsWorkingInADepartmentOnAGivenDate.contains(employeeDTO.getId())).toList();

        return employeesWorkingInGivenDepartmentOnGivenDay;
    }





}

/*
Find all the employees who worked in a named department on a given date


1. all employees
2. find by department name
3. filter by date
 */

/*
public ArrayList<EmployeeDTO> getEmployeesByDateAndDepartment(LocalDate beforeDate,
                                                                  LocalDate afterDate,
                                                                  String deptName) {
        DepartmentDTO dept = departmentDTORepository.findByDeptName(deptName);
        ArrayList<EmployeeDTO> employees = new ArrayList<>();
        for (DeptEmpDTO deptEmp : deptEmpDTORepository.findByFromDateIsBeforeAndToDateAfterAndDeptNo(beforeDate, afterDate, dept)) {
            Integer employeeId = deptEmp.getId().getEmpNo();
            employees.add(employeeDTORepository.getEmployeeById(employeeId));
        }
        return employees;
    }
 */
