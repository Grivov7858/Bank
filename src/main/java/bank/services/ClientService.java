package bank.services;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public interface ClientService {

    Boolean clientsAreImported();

    String readClientsJsonFile() throws IOException;

    String importClients(String clients);

    String exportFamilyGuy() throws JAXBException;
}
