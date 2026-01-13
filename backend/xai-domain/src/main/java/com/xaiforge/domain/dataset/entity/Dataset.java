package com.xaiforge.domain.dataset.entity;

import com.xaiforge.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "datasets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dataset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;
    
    @ElementCollection
    @CollectionTable(name = "dataset_headers", joinColumns = @JoinColumn(name = "dataset_id"))
    @Column(name = "header")
    private List<String> headers;
    
    @Column(name = "row_count")
    private Long rowCount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
}

