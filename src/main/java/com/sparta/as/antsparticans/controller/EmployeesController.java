package com.sparta.as.antsparticans.controller;

import com.sparta.as.antsparticans.exceptions.*;
import com.sparta.as.antsparticans.logging.FileHandlerConfig;
import com.sparta.as.antsparticans.model.dtos.DepartmentDTO;
import com.sparta.as.antsparticans.model.dtos.EmployeeDTO;
import com.sparta.as.antsparticans.model.repositories.DepartmentDTORepository;
import com.sparta.as.antsparticans.model.repositories.DeptEmpDTORepository;
import com.sparta.as.antsparticans.model.repositories.EmployeeDTORepository;
import com.sparta.as.antsparticans.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class EmployeesController {

    private EmployeeDTORepository employeeDTORepository;
    private DeptEmpDTORepository deptEmpDTORepository;
    private DepartmentDTORepository departmentDTORepository;

    private static final Logger employeesControllerLogger = Logger.getLogger(EmployeesController.class.getName());

    static {
        employeesControllerLogger.setUseParentHandlers(false);
        employeesControllerLogger.setLevel(Level.ALL);
        employeesControllerLogger.addHandler(FileHandlerConfig.getFileHandler());
    }


    @Autowired
    public EmployeesController(EmployeeDTORepository employeeDTORepository, DeptEmpDTORepository deptEmpDTORepository, DepartmentDTORepository departmentDTORepository) {
        this.employeeDTORepository = employeeDTORepository;
        this.departmentDTORepository = departmentDTORepository;
        this.deptEmpDTORepository = deptEmpDTORepository;
        employeesControllerLogger.log(Level.INFO, "Employees controller constructor employeeDTO dependency created");
    }

    @GetMapping("/employees/{id}")
    public EmployeeDTO getEmployeeByID(@PathVariable Integer id) throws EmployeeNotFoundException {
        employeesControllerLogger.log(Level.INFO, "Fetching employee with id: " + id + " from the database...");
        return employeeDTORepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    @GetMapping("/employees")
    public List<EmployeeDTO> getEmployees(@RequestParam(name = "last_name") Optional<String> name, @RequestParam(name = "department") Optional<String> department, @RequestParam(name = "date") Optional<String> date) throws EmployeeNotFoundException, DateConversionException, DepartmentNotFoundException {
        employeesControllerLogger.log(Level.INFO, "Fetching employee with lastName: " + name + " from the database...");

        if (name.isPresent()) {
            List<EmployeeDTO> employeesByLastName = employeeDTORepository.findEmployeeDTOByLastName(name.get()).get();
            if (employeesByLastName.isEmpty()) throw new EmployeeNotFoundException(name.get());
            return employeesByLastName;
        } else if (department.isPresent() && date.isPresent()) {
            EmployeeService employeeService = new EmployeeService(employeeDTORepository, deptEmpDTORepository, departmentDTORepository);
            LocalDate givenDate = null;
            try {
                givenDate = LocalDate.parse(date.get());
            } catch (Exception e) {
                throw new DateConversionException(e);
            }
            DepartmentDTO departmentDTO = departmentDTORepository.findByDeptName(department.get());

            if (departmentDTO == null) {
                throw new DepartmentNotFoundException(department.get(), true);
            }

            List<EmployeeDTO> employeeDTOList = employeeService.findEmployeesWhoWorkedInDepartmentOnAGivenDate(department.get(), givenDate);

            if (employeeDTOList.size() == 0) {
                throw new EmployeeNotFoundException(department.get(), givenDate);
            }
            return employeeDTOList;
        } else
            return employeeDTORepository.getAllEmployees();
    }

    @PutMapping("/employees/{id}")
    public EmployeeDTO replaceEmployeeById(@RequestBody EmployeeDTO employeeDTO, @PathVariable Integer id) throws EmployeeNotFoundException, EmployeeViolatesConstraintException {

        employeesControllerLogger.log(Level.INFO, "Received: " + employeeDTO + " for " + id);

        Optional<EmployeeDTO> optionalEmployeeDTO = employeeDTORepository.findById(id);
        if (optionalEmployeeDTO.isPresent()) {
            EmployeeDTO oldEmployeeDTO = optionalEmployeeDTO.get();

            // Update only the fields which were passed
            if (employeeDTO.getId() != null)
                oldEmployeeDTO.setId(employeeDTO.getId());
            if (employeeDTO.getFirstName() != null)
                oldEmployeeDTO.setFirstName(employeeDTO.getFirstName());
            if (employeeDTO.getLastName() != null)
                oldEmployeeDTO.setLastName(employeeDTO.getLastName());
            if (employeeDTO.getGender() != null)
                oldEmployeeDTO.setGender(employeeDTO.getGender());
            if (employeeDTO.getBirthDate() != null)
                oldEmployeeDTO.setBirthDate(employeeDTO.getBirthDate());
            if (employeeDTO.getHireDate() != null)
                oldEmployeeDTO.setHireDate(employeeDTO.getHireDate());

            employeesControllerLogger.log(Level.INFO, "Attempting to save: " + oldEmployeeDTO + " to the database");
            EmployeeDTO toBeReturned = null;
            try {
                toBeReturned = employeeDTORepository.save(oldEmployeeDTO);
            } catch (Exception e) {
                employeesControllerLogger.log(Level.INFO, "Constaint Violation Exception Thrown");
                throw new EmployeeViolatesConstraintException(e);
            }
            employeesControllerLogger.log(Level.INFO, "Save successful. About to return: " + toBeReturned + " to the view");
            return toBeReturned;
        } else {
            throw new EmployeeNotFoundException(id);
        }
    }

    private boolean canNewEmployeeBeCreated(EmployeeDTO e) {
        return e.getId() != null && e.getGender() != null && e.getFirstName() != null && e.getLastName() != null && e.getBirthDate() != null && e.getHireDate() != null && employeeDTORepository.findById(e.getId()).isEmpty();
    }

    @PostMapping("/employees")
    public EmployeeDTO createEmployee(@RequestBody EmployeeDTO employeeDTO) throws EmployeeViolatesConstraintException, EmployeeAlreadyExistsException {

        employeesControllerLogger.log(Level.INFO, "Received: " + employeeDTO);

        if (canNewEmployeeBeCreated(employeeDTO)) {
            employeesControllerLogger.log(Level.INFO, "Can be created");
            EmployeeDTO toBeReturned = null;

            employeesControllerLogger.log(Level.INFO, "Attempting to create: " + employeeDTO);
            try {
                toBeReturned = employeeDTORepository.save(employeeDTO);
            } catch (Exception e) {
                employeesControllerLogger.log(Level.INFO, "Constraint Violation Exception Thrown");
                throw new EmployeeViolatesConstraintException(e);
            }
            employeesControllerLogger.log(Level.INFO, "Save successful. About to return: " + toBeReturned + " to the view");
            return toBeReturned;

        } else {
            throw new EmployeeAlreadyExistsException();
        }
    }

    @DeleteMapping("/employees/{id}")
    public String deleteEmployeeById(@PathVariable Integer id) throws EmployeeNotFoundException {


        employeesControllerLogger.log(Level.INFO, "Received id: " + id + " for deletion...");

        Optional<EmployeeDTO> optionalEmployeeDTO = employeeDTORepository.findById(id);
        if (optionalEmployeeDTO.isPresent()) {
            employeesControllerLogger.log(Level.INFO, "Attempting to delete employee with id: " + id + " from the database");
            boolean successfull = false;
            try {
                employeeDTORepository.deleteById(id);
                successfull = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return successfull ? "successfully deleted" : "deletion unsuccessfull";
        } else throw new EmployeeNotFoundException(id);
    }
}
