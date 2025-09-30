package org.example.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Branch {
    @Id
    private String name;

    private String lastCommitId;

    @OneToMany
    private List<Commit> commits = new ArrayList<>();

}
