package com.hstn.sec.controller;

import com.hstn.sec.entity.EmployeeTask;
import com.hstn.sec.entity.Month;
import com.hstn.sec.entity.User;
import com.hstn.sec.service.TaskService;
import com.hstn.sec.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class MyController {
    @Autowired
    private UserService userService;
    @Autowired
    private TaskService taskService;
    @GetMapping("/")
    public String homePage(Model model, Principal principal,@RequestParam(defaultValue = "1") int monthId) {
        String username = principal.getName();
        User userInfo = userService.getUserInfo(username);
        model.addAttribute("username", username);
        model.addAttribute("fullName", userInfo.getFullName());
        model.addAttribute("phoneNumber", userInfo.getPhoneNumber());



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

        return "home-page"; // Имя Thymeleaf-шаблона


        // Рассчитываем итоговую сумму

    }


    @PostMapping("/addTask")
    public String addTask(
            @RequestParam String task,
            @RequestParam int monthId,
            Principal principal) {

        // Получаем текущий месяц
        int currentMonth = LocalDate.now().getMonthValue();

        // Проверяем, что задача добавляется только для текущего месяца
        if (monthId != currentMonth) {
            throw new IllegalArgumentException("Задачи можно добавлять только для текущего месяца.");
        }

        // Добавляем задачу
        String username = principal.getName();
        taskService.addEmployeeTask(username, monthId, task,0.0, "Не оплачено");

        // Перенаправляем на главную страницу
        return "redirect:/?monthId=" + monthId;
    }
    @PostMapping("/updateUser")
    public String updateUser(Principal principal,
                             @RequestParam String fullName,
                             @RequestParam String phoneNumber) {
        String username = principal.getName();
        userService.updateUser(username, fullName, phoneNumber);
        return "redirect:/"; //
    }
}
