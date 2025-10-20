package com.ems.app.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ems.app.pojo.ConfirmationForm;
import com.ems.app.pojo.Employee;
import com.ems.app.repo.EmployeeRepo;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeRepo employeeRepo;

    @GetMapping("/")
    public String getIndex(Model model, @RequestParam(required = false) String searchKeyword) {
        List<Employee> employees = employeeRepo.findAll();
        model.addAttribute("employees", employees);
        model.addAttribute("employee", new Employee());
        model.addAttribute("confirmationForm", new ConfirmationForm());
        model.addAttribute("searchKeyword", searchKeyword != null ? searchKeyword : "");
        return "index";
    }

    @GetMapping("/search")
    public String searchEmployees(@RequestParam("keyword") String keyword, Model model) {
        List<Employee> employees = employeeRepo.findAll();

        List<Employee> filtered = employees.stream()
                .filter(emp ->
                        emp.getEmployeeName().toLowerCase().contains(keyword.toLowerCase()) ||
                                emp.getEmployeeRole().toLowerCase().contains(keyword.toLowerCase()))
                .toList();

        model.addAttribute("employees", filtered);
        model.addAttribute("employee", new Employee());
        model.addAttribute("confirmationForm", new ConfirmationForm());
        model.addAttribute("searchKeyword", keyword);

        return "index";
    }

    @PostMapping("/create")
    public String newEmployee(@ModelAttribute Employee employee, RedirectAttributes redirectAttributes) {
        List<Employee> all = employeeRepo.findAll();

        // Check for duplicate email
        boolean emailExists = all.stream()
                .anyMatch(emp -> emp.getEmployeeEmail().equalsIgnoreCase(employee.getEmployeeEmail()));

        if (emailExists) {
            redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Email already in use! Please use another email.");
            return "redirect:/";
        }

        int nextId = 1;
        if (!all.isEmpty()) {
            Optional<Integer> max = all.stream()
                    .map(emp -> {
                        try {
                            return Integer.parseInt(emp.getId().replace("EMP", ""));
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    })
                    .max(Comparator.naturalOrder());
            if (max.isPresent()) {
                nextId = max.get() + 1;
            }
        }

        employee.setId("EMP" + nextId);
        employeeRepo.save(employee);

        return "redirect:/";
    }

    @PostMapping("/update")
    public String updateEmployee(@ModelAttribute Employee employee, Model model, RedirectAttributes redirectAttributes) {
        Optional<Employee> existingOpt = employeeRepo.findById(employee.getId());

        if (existingOpt.isPresent()) {
            Employee existing = existingOpt.get();

            // Check if the new email is already used by another employee
            boolean emailExists = employeeRepo.findAll().stream()
                    .anyMatch(emp ->
                            !emp.getId().equals(existing.getId()) &&
                                    emp.getEmployeeEmail().equalsIgnoreCase(employee.getEmployeeEmail()));

            if (emailExists) {
                // Return to index view with an error message without redirect
                List<Employee> employees = employeeRepo.findAll();
                model.addAttribute("employees", employees);
                model.addAttribute("employee", existing); // keep current employee data
                model.addAttribute("confirmationForm", new ConfirmationForm());
                model.addAttribute("searchKeyword", "");
                model.addAttribute("errorMessage", "⚠️ Email already in use! Please use another email.");

                return "index";
            }
            //Update only non-empty fields
            if (employee.getEmployeeName() != null && !employee.getEmployeeName().isEmpty())
                existing.setEmployeeName(employee.getEmployeeName());

            if (employee.getEmployeeEmail() != null && !employee.getEmployeeEmail().isEmpty())
                existing.setEmployeeEmail(employee.getEmployeeEmail());

            if (employee.getEmployeePhone() != null)
                existing.setEmployeePhone(employee.getEmployeePhone());

            if (employee.getEmployeeGender() != null && !employee.getEmployeeGender().isEmpty())
                existing.setEmployeeGender(employee.getEmployeeGender());

            if (employee.getEmployeeSalary() != null && !employee.getEmployeeSalary().isEmpty())
                existing.setEmployeeSalary(employee.getEmployeeSalary());

            if (employee.getEmployeeRole() != null && !employee.getEmployeeRole().isEmpty())
                existing.setEmployeeRole(employee.getEmployeeRole());

            employeeRepo.save(existing);
        }

        return "redirect:/";
    }

    @PostMapping("/remove")
    public String removeEmployee(@ModelAttribute Employee employee) {
        employeeRepo.findById(employee.getId()).ifPresent(e -> employeeRepo.deleteById(e.getId()));
        return "redirect:/";
    }

    @PostMapping("/remove/all")
    public String removeAll(@ModelAttribute ConfirmationForm confirmationForm) {
        if ("Yes".equalsIgnoreCase(confirmationForm.getConfirmation())) {
            employeeRepo.deleteAll();
        }
        return "redirect:/";
    }
}
