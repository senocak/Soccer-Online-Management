package com.github.senocak.model;

import com.github.senocak.util.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table
@Builder
@AllArgsConstructor
@EntityListeners(Player.Listener.class)
public class Player extends BaseEntity {
    @Size(min = 3)
    @Column(nullable = false)
    private String firstName;

    @Size(min = 3)
    @Column(nullable = false)
    private String lastName;

    @Size(min = 3)
    @Column(nullable = false)
    private String country;

    @Min(value = 18)
    @Max(value = 40)
    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppConstants.PlayerPosition position;

    @Column(nullable = false)
    private int marketValue;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    public static class Listener {
        @PrePersist
        public void prePersist(final Player player) {
            player.setMarketValue(1_000_000);
        }
    }
}
