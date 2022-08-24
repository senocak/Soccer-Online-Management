package com.github.senocak.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table
@Builder
@AllArgsConstructor
public class Transfer extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Player player;

    @Column
    private int marketValue;

    @Column(nullable = false)
    private int askedPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team transferredFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team transferredTo;

    @Column(nullable = false)
    private boolean transferred;
}
