package trabalhoPOO.repository;

import trabalhoPOO.model.LocalStarWars;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalRepository extends JpaRepository<LocalStarWars, String> {
}
