package bank.repositories;

import bank.domain.entities.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {
    Optional<Branch> findByName(String name);
}
