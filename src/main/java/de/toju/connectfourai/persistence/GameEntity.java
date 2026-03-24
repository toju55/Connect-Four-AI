package de.toju.connectfourai.persistence;

import de.toju.connectfourai.model.PlayerType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PlayerType ai1;

    @Enumerated(EnumType.STRING)
    private PlayerType ai2;

    // 0 = draw, 1 = player1, 2 = player2
    private int winner;

    // e.g. "1212121"
    @Column(length = 255)
    private String moves;

    private LocalDateTime createdAt;

    public GameEntity() {}

    public GameEntity(PlayerType ai1, PlayerType ai2, int winner, String moves) {
        this.ai1 = ai1;
        this.ai2 = ai2;
        this.winner = winner;
        this.moves = moves;
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}