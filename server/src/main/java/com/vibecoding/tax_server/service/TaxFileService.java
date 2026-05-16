package com.vibecoding.tax_server.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.Loader;

@Service
public class TaxFileService {

    public String extractTaxData(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        String extractedText = "";

        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            extractedText = extractTextFromPdf(file.getInputStream());
        } else if (fileName != null && (fileName.toLowerCase().endsWith(".html") || fileName.toLowerCase().endsWith(".htm"))) {
            extractedText = extractTextFromHtml(file.getInputStream());
        } else {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. PDF 또는 HTML 파일만 업로드해주세요.");
        }

        // 참고: 추출된 전체 텍스트에서 연말정산에 필요한 구체적인 금액이나 항목만 파싱하려면 
        // 정규표현식(Regex)을 추가로 작성해야 합니다. 우선은 파일 내용을 읽어오는 기능만 구현했습니다.
        return extractedText;
    }

    private String extractTextFromPdf(InputStream is) throws Exception {
    // PDFBox 3.0 이상 버전에 맞게 Loader를 사용
    byte[] pdfBytes = is.readAllBytes(); 
    try (PDDocument document = Loader.loadPDF(pdfBytes)) { 
        PDFTextStripper stripper = new PDFTextStripper();
        return stripper.getText(document);
    }
}

    private String extractTextFromHtml(InputStream is) throws Exception {
        Document doc = Jsoup.parse(is, StandardCharsets.UTF_8.name(), "");
        return doc.text(); // HTML 태그를 제외한 텍스트만 추출
    }
}