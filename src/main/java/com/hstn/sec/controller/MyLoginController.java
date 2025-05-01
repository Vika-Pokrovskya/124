package com.hstn.sec.controller;

import com.hstn.sec.entity.EmployeeTask;
import com.hstn.sec.entity.Month;
import com.hstn.sec.entity.User;
import com.hstn.sec.service.TaskService;
import com.hstn.sec.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class MyLoginController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService; // Сервис для работы с пользователями

    @GetMapping("/myLoginPage")
    public String myLoginPage() {
        return "my-login-page";
    }

    @GetMapping("/managers")
    public String myManagersPage() {
        return "managers";
    }

    @GetMapping("/admins")
    public String myAdminsPage(Model model) {
        // Получаем список всех пользователей и передаем его в модель
        List<User> users_info = userService.getAllUsers();
        users_info.forEach(System.out::println);
        model.addAttribute("users_info",users_info);


        return "admins";
    }
    @PostMapping("/admins/addUser")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String fullName,
                          @RequestParam String phoneNumber,
                          RedirectAttributes redirectAttributes) {

        try {
            userService.addUser(username, password, fullName, phoneNumber);
            redirectAttributes.addFlashAttribute("success", "Пользователь успешно добавлен");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admins";
    }
    @PostMapping("/admins/updateUser")
    public String updateUser(@RequestParam String username,
                             @RequestParam String fullName,
                             @RequestParam String phoneNumber) {
        userService.updateUser(username, fullName, phoneNumber);
        return "redirect:/admins"; // Перенаправляем обратно на страницу администратора
    }
    @PostMapping("/admins/updateUserRole")
    public String updateRole(@RequestParam String username,
                             @RequestParam(value = "roles", required = false) List<String> roles) {
        // Объединяем роли в строку через запятую
        String rolesString = (roles != null) ? String.join(",", roles) : "";
        userService.updateRole(username, rolesString);
        return "redirect:/admins";
    }
    @PostMapping("/admins/deleteUser")
    public String deleteUser(@RequestParam String username,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        String currentUsername = principal.getName();
        if (currentUsername.equals(username)) {
            redirectAttributes.addFlashAttribute("error", "Вы не можете удалить самого себя");
            return "redirect:/admins";
        }
        userService.deleteUser(username);
        return "redirect:/admins"; // Перенаправляем обратно на страницу администратора
    }

    @GetMapping("/admins/{username}")
    public String getInfoForAdminById(@PathVariable String username, Model model,@RequestParam(defaultValue = "1") int monthId){
        User user = userService.getUserForAdmin(username);

        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("phoneNumber", user.getPhoneNumber());
        // Получаем задачи для конкретного пользователя и месяца
        List<EmployeeTask> tasks = taskService.getTasksByEmployeeAndMonth(username, monthId);
        model.addAttribute("tasks", tasks);

        // Рассчитываем итоговые суммы для выбранного месяца
        double totalAmount = taskService.calculateTotalAmount(tasks);
        double paidAmount = taskService.calculatePaidAmount(tasks);
        double unpaidAmount = taskService.calculateUnpaidAmount(tasks);

        // Добавляем итоговые суммы в модель
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("paidAmount", paidAmount);
        model.addAttribute("unpaidAmount", unpaidAmount);

        // Получаем текущий месяц и год
        LocalDate currentDate = LocalDate.now();
        int currentMonth = currentDate.getMonthValue(); // Текущий месяц (1-12)
        int currentYear = currentDate.getYear(); // Текущий год

        // Добавляем текущий месяц и год в модель
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("currentYear", currentYear);
        // Добавляем выбранный месяц в модель (для отображения в форме)

        model.addAttribute("selectedMonthId", monthId);
        // Добавляем список месяцев в модель
        List<Month> months = List.of(
                new Month(1, "Январь 2025"),
                new Month(2, "Февраль 2025"),
                new Month(3, "Март 2025"),
                new Month(4, "Апрель 2025"),
                new Month(5, "Май 2025"),
                new Month(6, "Июнь 2025"),
                new Month(7, "Июль 2025"),
                new Month(8, "Август 2025"),
                new Month(9, "Сентябрь 2025"),
                new Month(10, "Октябрь 2025"),
                new Month(11, "Ноябрь 2025"),
                new Month(12, "Декабрь 2025")
        );
        model.addAttribute("months", months);

        return "employee-for-admins";
    }


    @PostMapping("/admins/{username}/updateStatus")
    public String updateStatusTask(
            @RequestParam int id,
            @RequestParam String paymentStatus,  // Исправлено с paymantStatus
            @RequestParam(required = false) Double partialAmount,
            @PathVariable String username,
            @RequestParam int monthId
    ) {
        taskService.updateStatusTask(paymentStatus, partialAmount, id);
        return "redirect:/admins/" + username + "?monthId=" + monthId;
    }

    @PostMapping("/admins/{username}/updatePrice")
    public String updatePrice(
            @RequestParam int id,
            @RequestParam double price,
            @PathVariable String username,
            @RequestParam int monthId
    ) {
        taskService.updatePrice(price, id);
        return "redirect:/admins/" + username + "?monthId=" + monthId;
    }
    @GetMapping("/access-denied-page")
    public String myAccessDeniedPage() {
        return "access-denied-page";
    }




}
