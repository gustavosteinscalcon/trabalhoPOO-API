package trabalhoPOO.repository;

import trabalhoPOO.model.Partida;
import trabalhoPOO.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartidaRepository extends JpaRepository<Partida, Long> {
    List<Partida> findByUsuarioOrderByDataInicioDesc(Usuario usuario);
}
