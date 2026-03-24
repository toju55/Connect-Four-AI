package de.toju.connectfourai.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameEntityRepository extends JpaRepository<GameEntity, Long> {

    Page<GameEntity> findAll(Pageable pageable);

    List<GameEntity> findByAi1AndAi2(String ai1, String ai2);

    List<GameEntity> findByWinner(int winner);

    List<GameEntity> findByAi1AndWinner(String ai1, int winner);

    List<GameEntity> findAllByOrderByCreatedAtDesc();

}