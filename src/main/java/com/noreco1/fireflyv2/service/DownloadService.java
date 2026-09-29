package com.noreco1.fireflyv2.service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.noreco1.fireflyv2.service.implementation.TokenService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DownloadService {

	private final ExporterService exporter;
	private final TokenService tokenService;
	
	public void download(String type, HttpServletResponse response, HashMap<String, Object> params,
                         String template, JRDataSource dataSource) {
		download(type, null, response, params, template, dataSource);
	}

	public void download(String type, String token,
                         HttpServletResponse response,
                         HashMap<String, Object> params,
                         String template, JRDataSource dataSource) {

		try {
			download(type, token, response, fillReport(template, params, dataSource));
		} catch (JRException jre) {
            log.error("Unable to process download: {}", jre.getMessage(), jre);
		}
	}

    public void download(String type, String token, HttpServletResponse response, HashMap<String, Object> params,
                         String template, JRDataSource dataSource, Boolean withTempPdf) {

        try {
             if (withTempPdf) {
                // Create an output byte stream where data will be written
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                String newPdfFilename = "/bir2307-" + (new SimpleDateFormat("MMMddyyyy").format(new Date())) + ".pdf";

                PdfReader.unethicalreading = true;
                PdfReader pdfReader = new PdfReader(template);
                PdfStamper pdfStamper = new PdfStamper(pdfReader, baos);

                // write contents here:
                BaseFont font = BaseFont.createFont();
                PdfContentByte overContent = pdfStamper.getOverContent(1);
                overContent.saveState();
                overContent.beginText();
                overContent.setFontAndSize(font, 10.0f);
                overContent.moveText(115, 845);
                overContent.showText(params.get("PAYEE_NAME").toString());
                overContent.endText();
                overContent.restoreState();

                pdfStamper.close();
                pdfReader.close();

                // Set our response properties
                response.setHeader("Content-Disposition", "inline; filename="+ newPdfFilename);

                // Set content type
                response.setContentType("application/pdf");
                response.setContentLength(baos.size());

                // Write to response stream
                OutputStream os = response.getOutputStream();
                baos.writeTo(os);
                os.flush();

                tokenService.remove(token);
            } else {
                download(type, token, response, fillReport(template, params, dataSource));
            }
        } catch (JRException | IOException | DocumentException e) {
            log.error("Unable to process download: {}", e.getMessage(), e);
        }
    }

    public void download(String type, String token, HttpServletResponse response, JasperPrint jp) {

        // Create an output byte stream where data will be written
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Export report
        exporter.export(type, jp, response, baos);
        // Write to response stream
        write(token, response, baos);
    }

	/**
	* Writes the report to the output stream
	*/
	private void write(String token, HttpServletResponse response, ByteArrayOutputStream baos) {
		 
		try {
			// Retrieve output stream
			ServletOutputStream outputStream = response.getOutputStream();
			// Write to output stream
			baos.writeTo(outputStream);
			// Flush the stream
			outputStream.flush();

			// Remove download token
			tokenService.remove(token);

		} catch (Exception e) {
            log.error("Unable to write report to the output stream: {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

    public void savePdf(String fileName, HashMap<String, Object> params, String template, JRDataSource dataSource) {

        try {
            JasperExportManager.exportReportToPdfFile(fillReport(template, params, dataSource), fileName);
        } catch (JRException jre) {
            log.error("Unable to save pdf: {}", jre.getMessage(), jre);
        }
    }

    private JasperPrint fillReport(String template, HashMap<String, Object> params, JRDataSource dataSource)
            throws JRException {
        try (InputStream reportStream = getClass().getResourceAsStream("/" + template)) {
            if (reportStream == null) {
                throw new JRException("Report template not found: " + template);
            }
            JasperDesign jd = JRXmlLoader.load(reportStream);
            JasperReport jr = JasperCompileManager.compileReport(jd);
            return JasperFillManager.fillReport(jr, params, dataSource);
        } catch (IOException e) {
            throw new JRException("Unable to read report template: " + template, e);
        }
    }

    public void showPdfFromDisk(String filename, HttpServletResponse response) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {

            String template = "/" + filename;
            PdfReader pdfReader = new PdfReader(template);

            PdfStamper pdfStamper = new PdfStamper(pdfReader, baos);

            pdfStamper.close();
            pdfReader.close();

            // Set our response properties
            response.setHeader("Content-Disposition", "inline; filename="+ filename);

            // Set content type
            response.setContentType("application/pdf");
            response.setContentLength(baos.size());

            // Write to response stream
            OutputStream os = response.getOutputStream();
            baos.writeTo(os);
            os.flush();

        } catch (IOException | DocumentException e) {
            log.error("Unable to process download: {}", e.getMessage(), e);
        }
    }
}
