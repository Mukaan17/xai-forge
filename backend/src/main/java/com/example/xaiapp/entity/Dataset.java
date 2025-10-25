/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:05:31
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:53
 */
package com.example.xaiapp.entity;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    @OneToOne(mappedBy = "dataset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MLModel mlModel;
    
    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
}
