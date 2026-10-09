
package com.jobcopilot.backend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeParserService {

    public String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a resume.");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException(
                "The uploaded resume must have a filename."
            );
        }

        String lowerFilename = filename.toLowerCase(Locale.ROOT);

        if (lowerFilename.endsWith(".pdf")) {
            return extractPdfText(file);
        }

        if (lowerFilename.endsWith(".docx")) {
            return extractDocxText(file);
        }

        throw new IllegalArgumentException(
            "Unsupported resume format. Upload a PDF or DOCX file."
        );
    }

    private String extractPdfText(MultipartFile file) throws IOException {
        try (var document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return validateExtractedText(text);
        }
    }

    private String extractDocxText(MultipartFile file) throws IOException {
        try (var document = new XWPFDocument(
                new ByteArrayInputStream(file.getBytes()))) {

            StringBuilder text = new StringBuilder();

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }

            return validateExtractedText(text.toString());
        }
    }

    private String validateExtractedText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                "No readable text was found. If this is a scanned PDF, "
                + "it may need OCR before it can be analyzed."
            );
        }

        return text.trim();
    }
}
