package bank.services;

import bank.domain.dtos.card.CardImportDto;
import bank.domain.dtos.card.CardImportRootDto;
import bank.domain.entities.BankAccount;
import bank.domain.entities.Card;
import bank.repositories.BankAccountRepository;
import bank.repositories.CardRepository;
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
public class CardServiceImpl implements CardService {
    private final static String CARDS_XML_FILE_PATH =
            "D:\\Java\\bank\\src\\main\\resources\\files\\xml\\cards.xml";
    private final CardRepository cardRepository;
    private final BankAccountRepository bankAccountRepository;
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    @Autowired
    public CardServiceImpl(CardRepository cardRepository, BankAccountRepository bankAccountRepository, FileUtil fileUtil, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.cardRepository = cardRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean cardsAreImported() {
        return this.cardRepository.count() != 0;
    }

    @Override
    public String readCardsXmlFile() throws IOException {
        String cardsFileContent = this.fileUtil.readFile(CARDS_XML_FILE_PATH);
        return cardsFileContent;
    }

    @Override
    public String importCards() throws JAXBException {
        StringBuilder importResult = new StringBuilder();

        JAXBContext context = JAXBContext.newInstance(CardImportRootDto.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        CardImportRootDto cardImportRootDto =
                (CardImportRootDto) unmarshaller.unmarshal(new File(CARDS_XML_FILE_PATH));

        for (CardImportDto cardImportDto : cardImportRootDto.getCardImportDtos()) {
            if (!this.validationUtil.isValid(cardImportDto)) {
                importResult.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            BankAccount bankAccountEntity = this.bankAccountRepository
                    .findByAccountNumber(cardImportDto.getAccountNumber())
                    .orElse(null);

            if (bankAccountEntity == null) {
                importResult.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            Card cardEntity = this.modelMapper.map(cardImportDto, Card.class);
            cardEntity.setBankAccount(bankAccountEntity);

            this.cardRepository.saveAndFlush(cardEntity);

            importResult.append(String
                    .format("Successfully imported Card - %s",
                            cardEntity.getCardNumber()))
                    .append(System.lineSeparator());
        }

        return importResult.toString().trim();
    }
}
