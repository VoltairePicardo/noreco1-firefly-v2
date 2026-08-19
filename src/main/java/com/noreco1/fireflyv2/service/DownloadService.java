package com.noreco1.fireflyv2.service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.noreco1.fireflyv2.service.implementation.TokenService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {
	protected final Logger logger = Logger.getLogger("service");
	
	@Autowired
	private ExporterService exporter;
	
	@Autowired
	private TokenService tokenService;

    @Autowired
    Environment env;
	
	public void download(String type, String token,
                         HttpServletResponse response,
                         HashMap<String, Object> params,
                         String template, JRDataSource dataSource) {
		 
		try {

			InputStream reportStream = this.getClass().getResourceAsStream("/" + template);
			JasperDesign jd = JRXmlLoader.load(reportStream);
			JasperReport jr = JasperCompileManager.compileReport(jd);
			// Make sure to pass the JasperReport, report parameters, and data source
            JasperPrint jp = null;
            if (dataSource != null) {
                jp = JasperFillManager.fillReport(jr, params, dataSource);
            } else {
                jp = JasperFillManager.fillReport(jr, params, dataSource);
            }
			// Create an output byte stream where data will be written
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			// Export report
			exporter.export(type, jp, response, baos);
			// Write to response stream
			write(token, response, baos);
		
		} catch (JRException jre) {
			logger.error("Unable to process download");
            jre.printStackTrace();
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

                InputStream reportStream = this.getClass().getResourceAsStream("/" + template);
                JasperDesign jd = JRXmlLoader.load(reportStream);
                JasperReport jr = JasperCompileManager.compileReport(jd);
                // Make sure to pass the JasperReport, report parameters, and data source
                JasperPrint jp = null;
                if (dataSource != null) {
                    jp = JasperFillManager.fillReport(jr, params, dataSource);
                } else {
                    jp = JasperFillManager.fillReport(jr, params, dataSource);
                }
                // Create an output byte stream where data will be written
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // Export report
                exporter.export(type, jp, response, baos);
                // Write to response stream
                write(token, response, baos);
            }
        } catch (JRException jre) {
            logger.error("Unable to process download");
            jre.printStackTrace();
        } catch (IOException e) {
            logger.error("Unable to process download");
            e.printStackTrace();
        } catch (DocumentException e) {
            logger.error("Unable to process download");
            e.printStackTrace();
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
			logger.error("Unable to write report to the output stream");
			throw new RuntimeException(e);
		}
	}

    public void savePdf(String fileName, HashMap<String, Object> params, String template, JRDataSource dataSource) {

        try {

            InputStream reportStream = this.getClass().getResourceAsStream("/" + template);
            JasperDesign jd = JRXmlLoader.load(reportStream);
            JasperReport jr = JasperCompileManager.compileReport(jd);

            // Make sure to pass the JasperReport, report parameters, and data source
            JasperPrint jp = null;
            if (dataSource != null) {
                jp = JasperFillManager.fillReport(jr, params, dataSource);
            } else {
                jp = JasperFillManager.fillReport(jr, params, dataSource);
            }

            JasperExportManager.exportReportToPdfFile(jp, fileName);

        } catch (JRException jre) {
            jre.printStackTrace();
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

        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
