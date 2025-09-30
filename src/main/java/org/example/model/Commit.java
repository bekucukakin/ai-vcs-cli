package org.example.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.CommitType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commit {

    @Id
    private String id; // Commit hash

    private String message; // Commit mesajı
    private String author; // Commit yapan kişi
    private String timestamp; // Zaman bilgisi
    private String branchName; // Commit hangi branch üzerinde

    @Enumerated(EnumType.STRING)
    private CommitType type; // Commit türü (NORMAL, MERGE, REVERT)

    @ManyToMany
    private List<Commit> parentCommits; // Parent commitler (merge için)

    @ElementCollection
    private Map<String, String> changedFiles = new HashMap<>();


}
