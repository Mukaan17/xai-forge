package com.example.xaiapp.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetDto {
    
    private Long id;
    private String fileName;
    private LocalDateTime uploadDate;
    private List<String> headers;
    private Long rowCount;
    private Long ownerId;
    
    // Manual setters (Lombok not generating them)
    public void setId(Long id) { this.id = id; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    public void setHeaders(List<String> headers) { this.headers = headers; }
    public void setRowCount(Long rowCount) { this.rowCount = rowCount; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}
