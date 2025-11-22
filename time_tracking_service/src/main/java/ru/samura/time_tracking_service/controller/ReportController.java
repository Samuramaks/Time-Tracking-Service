package ru.samura.time_tracking_service.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.samura.time_tracking_service.DTO.CalculationPayment;
import ru.samura.time_tracking_service.DTO.EmployeeDTO;
import ru.samura.time_tracking_service.response.TotalPaymentEmployeeResponse;
import ru.samura.time_tracking_service.service.EmployeeService;
import ru.samura.time_tracking_service.service.TimeEntryService;


/**
 * Контроллер для получения результатов работы за определенный или текущий месяц
 */

@RestController
@RequestMapping("/report")
public class ReportController {
    @Autowired
    EmployeeService employeeService;

    @Autowired
    TimeEntryService timeEntryService;


    /**
     * Метод который возвращает информацию о выплате в текущем или выбранном месяце для 1го пользователя
     * @param employeeId идентификатор пользователя
     * @param month месяц за который получит отчет пользователь (month - может быть пустым)
     * @return возвращает отчет о выплате текущего пользователя
     */
    @GetMapping("/{employee_id}/payment")
    public ResponseEntity<TotalPaymentEmployeeResponse> getPayment(@PathVariable("employee_id") UUID employeeId, 
                                                                    @RequestParam(required = false) String month) {
        EmployeeDTO employeeDTO = employeeService.getEmployeeInfo(employeeId);
        if(month == null || month.isEmpty()){
            LocalDate now = LocalDate.now();
            month = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        CalculationPayment calculationPayment = timeEntryService.totalPaymentEmployee(employeeDTO, month);

        String message = "Отчет за месяц: " + month;

        TotalPaymentEmployeeResponse response = new TotalPaymentEmployeeResponse(message,
                                                employeeDTO, 
                                                calculationPayment.getTotalHours(), 
                                                calculationPayment.getExpectedHours(), 
                                                calculationPayment.getOvertime(), 
                                                calculationPayment.getPay());

        return ResponseEntity.ok(response);
    }

    /**
     * Метод который возвращает информацию о выплате в текущем или выбранном месяце о всех пользователях если смотрит HR
      * @param employeeId идентификатор HR
     * @param month месяц за который получит отчет пользователь (month - может быть пустым)
     * @return возвращает отчет о выплате всех пользователей
     */
    @GetMapping("/{employee_id}/payment-all")
    public ResponseEntity<List<TotalPaymentEmployeeResponse>> getPaymentAllEmployee(@PathVariable("employee_id") UUID employeeId, 
                                                                                    @RequestParam(required = false) String month) {
        EmployeeDTO employeeDTO = employeeService.getEmployeeInfo(employeeId);

        if(!employeeService.hasAccess(employeeDTO, employeeId)){
            return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ArrayList<>());
        }

        if(month == null || month.isEmpty()){
            LocalDate now = LocalDate.now();
            month = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }


        List<TotalPaymentEmployeeResponse> totalPaymentEmployeeResponses = timeEntryService.getTotalPaymentsForEmployees(employeeService.getInfoAboutAllEmployee(), month);
        

        return ResponseEntity.ok(totalPaymentEmployeeResponses);
    }
}
