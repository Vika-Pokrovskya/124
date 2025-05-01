package com.hstn.sec.service;

import com.hstn.sec.entity.EmployeeTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class TaskService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public List<EmployeeTask> getTasksByEmployeeAndMonth(String username, int monthId) {
        String sql = "SELECT et.id, m.month_name, et.task, et.price, et.payment_status, et.partial_amount " +
                "FROM employee_tasks et " +
                "JOIN months m ON et.month_id = m.id " +
                "WHERE et.user_username = ? AND et.month_id = ?";
        return jdbcTemplate.query(sql, new Object[]{username, monthId}, (rs, rowNum) -> {
            EmployeeTask task = new EmployeeTask();
            task.setId(rs.getInt("id"));
            task.setMonthName(rs.getString("month_name"));
            task.setTask(rs.getString("task"));
            task.setPrice(rs.getDouble("price"));
            task.setPaymentStatus(rs.getString("payment_status"));
            task.setPartialAmount(rs.getDouble("partial_amount"));
            return task;
        });
    }

    /**
     * Рассчитывает общую сумму для списка задач.
     */
    public double calculateTotalAmount(List<EmployeeTask> tasks) {
        return tasks.stream().mapToDouble(EmployeeTask::getPrice).sum();
    }

    /**
     * Рассчитывает сумму оплаченных задач.
     */
    public double calculatePaidAmount(List<EmployeeTask> tasks) {
        return tasks.stream()
                .mapToDouble(EmployeeTask::getPartialAmount)
                .sum();
    }

    /**
     * Рассчитывает сумму неоплаченных задач.
     */
    public double calculateUnpaidAmount(List<EmployeeTask> tasks) {
        double a=tasks.stream()
                .mapToDouble(EmployeeTask::getPrice)
                .sum();
        double b=tasks.stream()
                .mapToDouble(EmployeeTask::getPartialAmount)
                .sum();
        return  a-b;

    }

    public void addEmployeeTask(String username, int monthId, String task, double price, String paymentStatus) {
        String sql = "INSERT INTO employee_tasks (user_username, month_id, task, price, payment_status) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, username, monthId, task, price, paymentStatus);
    }

    public void updateStatusTask( String paymentStatus, Double particalAmount,int id) {
        String sql = "UPDATE employee_tasks SET payment_status = ?,partial_amount = ? WHERE id = ?";
        jdbcTemplate.update(sql, paymentStatus,particalAmount, id);
    }
    public void updatePrice( double price ,int id) {
        String sql = "UPDATE employee_tasks SET price=? WHERE id = ?";
        jdbcTemplate.update(sql,price, id);
    }
}
