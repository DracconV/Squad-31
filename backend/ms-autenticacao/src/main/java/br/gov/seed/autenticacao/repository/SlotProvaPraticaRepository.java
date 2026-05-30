package br.gov.seed.autenticacao.repository;

import br.gov.seed.autenticacao.entity.SlotProvaPratica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface SlotProvaPraticaRepository extends JpaRepository<SlotProvaPratica, UUID> {

    @Query("SELECT s FROM SlotProvaPratica s WHERE s.vagasOcupadas < s.vagasTotais AND s.data > CURRENT_TIMESTAMP ORDER BY s.data ASC")
    List<SlotProvaPratica> findDisponiveisOrderByData();
}
