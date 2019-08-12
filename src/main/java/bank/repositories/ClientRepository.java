package bank.repositories;

import bank.domain.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    Optional<Client> findByFullName(String fullName);

    @Query("SELECT c FROM bank.domain.entities.Client c JOIN c.bankAccount b JOIN b.cards GROUP BY c.id ORDER BY size(b.cards) DESC")
    List<Client> exportFamilyGuy();
}
