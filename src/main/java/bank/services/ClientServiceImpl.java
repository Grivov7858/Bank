package bank.services;

import bank.domain.dtos.ClientImportDto;
import bank.domain.dtos.bankaccount.BankAccountImportDto;
import bank.domain.dtos.bankaccount.BankAccountImportRootDto;
import bank.domain.entities.BankAccount;
import bank.domain.entities.Card;
import bank.domain.entities.Client;
import bank.domain.entities.Employee;
import bank.repositories.ClientRepository;
import bank.repositories.EmployeeRepository;
import bank.util.FileUtil;
import bank.util.ValidationUtil;
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {
    private final static String CLIENTS_JSON_FILE_PATH =
            "D:\\Java\\bank\\src\\main\\resources\\files\\json\\clients.json";
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final FileUtil fileUtil;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, EmployeeRepository employeeRepository, FileUtil fileUtil, Gson gson, Gson gson1, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.fileUtil = fileUtil;
        this.gson = gson1;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean clientsAreImported() {
        return this.clientRepository.count() != 0;
    }

    @Override
    public String readClientsJsonFile() throws IOException {
        String clientsFileContent = this.fileUtil.readFile(CLIENTS_JSON_FILE_PATH);

        return clientsFileContent;
    }

    @Override
    public String importClients(String clients) {
        StringBuilder importResult = new StringBuilder();

        ClientImportDto[] clientImportDtos = this.gson.fromJson(clients, ClientImportDto[].class);
        for (ClientImportDto clientImportDto: clientImportDtos) {
            if (!validationUtil.isValid(clientImportDto)) {
                importResult.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            Employee employeeEntity = this.employeeRepository
                    .findByFullName(clientImportDto.getAppointedEmployee()).orElse(null);

            if (employeeEntity == null) {
                importResult.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            Client clientEntity = this.clientRepository.findByFullName(String.format("%s %s",
                    clientImportDto.getFirstName(),
                    clientImportDto.getLastName())).orElse(null);

            if (clientEntity != null) {
                importResult.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            clientEntity = this.modelMapper.map(clientImportDto, Client.class);
            clientEntity.setFullName(String.format("%s %s",
                    clientImportDto.getFirstName(),
                    clientImportDto.getLastName()));
            clientEntity.getEmployees().add(employeeEntity);

            importResult.append(String
                    .format("Successfully imported Client - %s",
                            clientEntity.getFullName()))
                    .append(System.lineSeparator());

            this.clientRepository.saveAndFlush(clientEntity);
        }

        return importResult.toString().trim();
    }

    @Override
    public String exportFamilyGuy() {
        StringBuilder sb = new StringBuilder();

        List<Client> clients = this.clientRepository.exportFamilyGuy();

        Client familyGuy = clients.get(0);

        sb.append(String.format("Full Name: %s\nAge: %d\nBank Account: %s\n",
                familyGuy.getFullName(),
                familyGuy.getAge(),
                familyGuy.getBankAccount().getAccountNumber()));

        for (Card card : familyGuy.getBankAccount().getCards()) {
            sb.append(String.format("Card Number: %s\nCard Status: %s\n",
                    card.getCardNumber(),
                    card.getCardStatus()));
        }

        return sb.toString().trim();
    }
}






