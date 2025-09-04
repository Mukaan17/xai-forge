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
}
