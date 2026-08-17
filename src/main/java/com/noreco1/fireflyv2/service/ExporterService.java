package com.noreco1.fireflyv2.service;

import java.io.ByteArrayOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.*;
import org.springframework.stereotype.Service;

@Service
public class ExporterService {

	public static final String MEDIA_TYPE_EXCEL = "application/vnd.ms-excel";
	public static final String MEDIA_TYPE_PDF = "application/pdf";
	public static final String EXTENSION_TYPE_EXCEL = "xls";
	public static final String EXTENSION_TYPE_PDF = "pdf";
	
	public HttpServletResponse export(String type,
			JasperPrint jp, 
			HttpServletResponse response,
			ByteArrayOutputStream baos) {
		
		if (type.equalsIgnoreCase(EXTENSION_TYPE_EXCEL)) {
			// Export to output stream
			exportXls(jp, baos);
			 
			// Set our response properties
			// Here you can declare a custom filename
			String fileName = "UserReport.xls";
			response.setHeader("Content-Disposition", "inline; filename=" + fileName);
			
			// Set content type
			response.setContentType(MEDIA_TYPE_EXCEL);
			response.setContentLength(baos.size());
			
			return response;
		}
		
		if (type.equalsIgnoreCase(EXTENSION_TYPE_PDF)) {
			// Export to output stream
			exportPdf(jp, baos);
			 
			// Set our response properties
			// Here you can declare a custom filename
			String fileName = "UserReport.pdf";
			response.setHeader("Content-Disposition", "inline; filename="+ fileName);
			
			// Set content type
			response.setContentType(MEDIA_TYPE_PDF);
			response.setContentLength(baos.size());
			
			return response;
			
		} 
		
		throw new RuntimeException("No type set for type " + type);
	}
	
	public void exportXls(JasperPrint jp, ByteArrayOutputStream baos) {
		// Create a JRXlsExporter instance
		JRXlsExporter exporter = new JRXlsExporter();
		 
		try {
            exporter.setExporterInput(new SimpleExporterInput(jp));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));

            SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();

            configuration.setOnePagePerSheet(false);
            configuration.setRemoveEmptySpaceBetweenRows(true);
            configuration.setWhitePageBackground(false);

            exporter.setConfiguration(configuration);

			exporter.exportReport();
		} catch (JRException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void exportPdf(JasperPrint jp, ByteArrayOutputStream baos) {
		// Create a JRPdfExporter instance
		JRPdfExporter exporter = new JRPdfExporter();
        try {
            exporter.setExporterInput(new SimpleExporterInput(jp));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));

            SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
            exporter.setConfiguration(configuration);

			exporter.exportReport();
		} catch (JRException e) {
			throw new RuntimeException(e);
		}
	}
	
}
