package com.ems.app.controller;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.ems.app.pojo.ConfirmationForm;
import com.ems.app.pojo.Employee;
import com.ems.app.repo.EmployeeRepo;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeRepo employeeRepo;

    // Display the home page (employee list)
    @GetMapping("/")
    public String getIndex(Model model) {
        List<Employee> employeeList = employeeRepo.findAll();
        model.addAttribute("employees", employeeList);
        model.addAttribute("employee", new Employee());
        model.addAttribute("confirmationForm", new ConfirmationForm());
        return "index";
    }

    // Create a new employee
    @PostMapping("/create")
    public String newEmployee(@ModelAttribute Employee employee, Model model) {
        // Create dynamic Employee ID
        String empId = "EMP" + (1000 + new Random().nextInt(9000));
        employee.setId(empId);

        // Save to database
        employeeRepo.save(employee);

        return "redirect:/";
    }

    // Update an existing employee
    @PostMapping("/update")
    public String updateEmployee(@ModelAttribute Employee employee, Model model) {
        Optional<Employee> existingEmployee = employeeRepo.findById(employee.getId());

        if (existingEmployee.isPresent()) {
            Employee e = existingEmployee.get();

            // Only update fields that are filled
            if (employee.getEmployeeName() != null && !employee.getEmployeeName().isEmpty())
                e.setEmployeeName(employee.getEmployeeName());

            if (employee.getEmployeeEmail() != null && !employee.getEmployeeEmail().isEmpty())
                e.setEmployeeEmail(employee.getEmployeeEmail());

            if (employee.getEmployeePhone() != null)
                e.setEmployeePhone(employee.getEmployeePhone());

            if (employee.getEmployeeGender() != null && !employee.getEmployeeGender().isEmpty())
                e.setEmployeeGender(employee.getEmployeeGender());

            if (employee.getEmployeeSalary() != null && !employee.getEmployeeSalary().isEmpty())
                e.setEmployeeSalary(employee.getEmployeeSalary());

            if (employee.getEmployeeRole() != null && !employee.getEmployeeRole().isEmpty())
                e.setEmployeeRole(employee.getEmployeeRole());

            employeeRepo.save(e);
        } else {
            model.addAttribute("errorMessage", "Employee with ID " + employee.getId() + " not found.");
        }

        return "redirect:/";
    }

    // Delete an employee by ID
    @PostMapping("/remove")
    public String removeEmployee(@ModelAttribute Employee employee, Model model) {
        Optional<Employee> existingEmployee = employeeRepo.findById(employee.getId());

        if (existingEmployee.isPresent()) {
            employeeRepo.deleteById(employee.getId());
        }

        return "redirect:/";
    }

    // Delete all employees (confirmation required)
    @PostMapping("/remove/all")
    public String removeAll(@ModelAttribute ConfirmationForm confirmationForm, Model model) {
        String confirmation = confirmationForm.getConfirmation();

        if ("Yes".equalsIgnoreCase(confirmation)) {
            employeeRepo.deleteAll();
        }

        return "redirect:/";
    }
}
