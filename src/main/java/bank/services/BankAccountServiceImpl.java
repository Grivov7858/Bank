package bank.services;

import bank.domain.dtos.bankaccount.BankAccountImportDto;
import bank.domain.dtos.bankaccount.BankAccountImportRootDto;
import bank.domain.entities.BankAccount;
import bank.domain.entities.Client;
import bank.repositories.BankAccountRepository;
import bank.repositories.ClientRepository;
import bank.util.FileUtil;
import bank.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;

@Service
public class BankAccountServiceImpl implements BankAccountService {
    private final static String BANK_ACCOUNTS_XML_FILE_PATH =
            "D:\\Java\\bank\\src\\main\\resources\\files\\xml\\bank-accounts.xml";
    private final BankAccountRepository bankAccountRepository;
    private final ClientRepository clientRepository;
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    @Autowired
    public BankAccountServiceImpl(BankAccountRepository bankAccountRepository, ClientRepository clientRepository, FileUtil fileUtil, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.bankAccountRepository = bankAccountRepository;
        this.clientRepository = clientRepository;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean bankAccountsAreImported() {
        return this.bankAccountRepository.count() != 0;
    }

    @Override
    public String readBankAccountsXmlFile() throws IOException {
        String bankAccountFileContent = this.fileUtil.readFile(BANK_ACCOUNTS_XML_FILE_PATH);
        return bankAccountFileContent;
    }

    @Override
    public String importBankAccounts() throws JAXBException {
        StringBuilder importResult = new StringBuilder();

        JAXBContext context = JAXBContext.newInstance(BankAccountImportRootDto.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        BankAccountImportRootDto bankAccountImportRootDto =
                (BankAccountImportRootDto) unmarshaller.unmarshal(new File(BANK_ACCOUNTS_XML_FILE_PATH));

        for (BankAccountImportDto bankAccountImportDto : bankAccountImportRootDto.getBankAccountImportDtos()) {
            if (!this.validationUtil.isValid(bankAccountImportDto)) {
                importResult.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            Client clientEntity = this.clientRepository
                    .findByFullName(bankAccountImportDto.getClient())
                    .orElse(null);

            if (clientEntity == null) {
                importResult.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            BankAccount bankAccountEntity = this.modelMapper.map(bankAccountImportDto, BankAccount.class);
            bankAccountEntity.setClient(clientEntity);

            this.bankAccountRepository.saveAndFlush(bankAccountEntity);

            importResult.append(String
                    .format("Successfully imported BankAccount - %s",
                            bankAccountEntity.getAccountNumber()))
                    .append(System.lineSeparator());
        }
        return importResult.toString().trim();
    }
}
