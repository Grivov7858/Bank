package bank.services;

import bank.domain.dtos.EmployeeImportDto;
import bank.domain.entities.Branch;
import bank.domain.entities.Client;
import bank.domain.entities.Employee;
import bank.repositories.BranchRepository;
import bank.repositories.EmployeeRepository;
import bank.util.FileUtil;
import bank.util.ValidationUtil;
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final static String EMPLOYEES_JSON_FILE_PATH =
            "D:\\Java\\bank\\src\\main\\resources\\files\\json\\employees.json";
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final FileUtil fileUtil;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, BranchRepository branchRepository, FileUtil fileUtil, Gson gson, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.branchRepository = branchRepository;
        this.fileUtil = fileUtil;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean employeesAreImported() {
        return this.employeeRepository.count() != 0;
    }

    @Override
    public String readEmployeesJsonFile() throws IOException {
        String employeesFileContent = this.fileUtil.readFile(EMPLOYEES_JSON_FILE_PATH);

        return employeesFileContent;
    }

    @Override
    public String importEmployees(String employees) {
        StringBuilder importResult = new StringBuilder();
        EmployeeImportDto[] employeeImportDtos = this.gson.fromJson(employees, EmployeeImportDto[].class);

        for (EmployeeImportDto employeeImportDto : employeeImportDtos) {
            if (!this.validationUtil.isValid(employeeImportDto)) {
                importResult.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            Branch branchEntity = this.branchRepository.findByName(employeeImportDto.getBranchName()).orElse(null);

            if (branchEntity == null) {
                importResult.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            Employee employeeEntity = this.modelMapper.map(employeeImportDto, Employee.class);
            employeeEntity.setFirstName(employeeImportDto.getFullName().split("\\s+")[0]);
            employeeEntity.setLastName(employeeImportDto.getFullName().split("\\s+")[1]);
            employeeEntity.setStartedOn(LocalDate.parse(employeeImportDto.getStartedOn()));
            employeeEntity.setBranch(branchEntity);

            this.employeeRepository.saveAndFlush(employeeEntity);

            importResult.append(String
                    .format("Successfully imported Employee - %s %s",
                            employeeEntity.getFirstName(),
                            employeeEntity.getLastName()))
                    .append(System.lineSeparator());
        }


        return importResult.toString().trim();
    }

    @Override
    public String exportTopEmployees() {
        List<Employee> employees = this.employeeRepository.extractTopEmployees();

        StringBuilder topEmployees = new StringBuilder();

        for (Employee employee : employees) {
            topEmployees.append(String.format("Full name: %s %s\nSalary: %.2f\nStarted on: %s\nClients:\n",
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getSalary(),
                    employee.getStartedOn()));

            for (Client client : employee.getClients()) {
                topEmployees.append(String.format("%s\n"
                        , client.getFullName()));
            }
        }


        return topEmployees.toString().trim();
    }
}
