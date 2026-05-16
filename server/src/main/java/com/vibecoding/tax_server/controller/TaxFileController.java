package com.vibecoding.tax_server.controller;

import com.vibecoding.tax_server.dto.TaxUploadResponse;
import com.vibecoding.tax_server.service.TaxFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/tax")
@RequiredArgsConstructor
public class TaxFileController {

    private final TaxFileService taxFileService;

    // 임시 인증: loginId를 쿼리 파라미터로 전달
    @PostMapping("/upload")
    public TaxUploadResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("loginId") String loginId
    ) throws Exception {
        return taxFileService.upload(file, loginId);
    }
}
