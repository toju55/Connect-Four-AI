package de.toju.connectfourai.persistence;

import de.toju.connectfourai.model.PlayerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameEntityRepository extends JpaRepository<GameEntity, Long> {

    @Query("""
    SELECT g FROM GameEntity g
    WHERE 
        (:ai1 IS NULL OR g.ai1 = :ai1 OR g.ai2 = :ai1)
    AND (:ai2 IS NULL OR g.ai1 = :ai2 OR g.ai2 = :ai2)
    AND (
        :winnerAi IS NULL OR
        (g.winner = 1 AND g.ai1 = :winnerAi) OR
        (g.winner = 2 AND g.ai2 = :winnerAi)
    )
""")
    Page<GameEntity> findFiltered(
            @Param("ai1") PlayerType ai1,
            @Param("ai2") PlayerType ai2,
            @Param("winnerAi") PlayerType winnerAi,
            Pageable pageable
    );

    Page<GameEntity> findAll(Pageable pageable);

    List<GameEntity> findByAi1AndAi2(String ai1, String ai2);

    List<GameEntity> findByWinner(int winner);

    List<GameEntity> findByAi1AndWinner(String ai1, int winner);

    List<GameEntity> findAllByOrderByCreatedAtDesc();

}