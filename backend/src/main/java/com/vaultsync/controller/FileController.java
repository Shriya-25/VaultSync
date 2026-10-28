package com.vaultsync.controller;

import com.vaultsync.dto.FileDto;
import com.vaultsync.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * GET /api/files
     * Returns the list of stored file metadata and their replica records.
     */
    @GetMapping
    public ResponseEntity<List<FileDto>> getAllFiles() {
        List<FileDto> files = fileService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    /**
     * GET /api/files/{id}
     * Returns metadata and replica statuses for a single file.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FileDto> getFileById(@PathVariable String id) {
        FileDto file = fileService.getFileById(id);
        return ResponseEntity.ok(file);
    }
}
