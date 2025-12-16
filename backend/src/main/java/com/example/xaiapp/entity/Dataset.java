package com.example.xaiapp.entity;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "datasets")
@Data
@Getter
@Setter
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
    @JsonIgnore
    private User owner;
    
    @OneToOne(mappedBy = "dataset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private MLModel mlModel;
    
    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
    
    // Manual getters and setters (Lombok not generating them)
    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }
    public Long getRowCount() { return rowCount; }
    public void setRowCount(Long rowCount) { this.rowCount = rowCount; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
}
