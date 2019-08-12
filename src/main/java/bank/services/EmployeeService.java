package bank.services;

import java.io.IOException;

public interface EmployeeService {

    Boolean employeesAreImported();

    String readEmployeesJsonFile() throws IOException;

    String importEmployees(String employees);

    String exportTopEmployees();
}
