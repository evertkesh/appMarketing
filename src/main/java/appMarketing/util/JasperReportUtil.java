package appMarketing.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * Utilidad para compilar y generar reportes PDF con JasperReports
 */
@Component
public class JasperReportUtil {

    @Autowired
    private DataSource dataSource;

    /**
     * Genera un PDF a partir de un archivo JRXML conectándose directamente a la BD
     * 
     * @param jrxmlPath Ruta al archivo JRXML dentro de resources
     * @param sql Query SQL a ejecutar
     * @param parameters Parámetros adicionales para el reporte (pueden ser null)
     * @return Arreglo de bytes con el contenido del PDF
     * @throws JRException Si ocurre un error al procesar el reporte
     * @throws IOException Si ocurre un error al leer el archivo JRXML
     * @throws SQLException Si ocurre un error al conectarse a la BD
     */
    public byte[] generatePDFWithDatabaseConnection(String jrxmlPath, String sql, Map<String, Object> parameters) 
            throws JRException, IOException, SQLException {
        
        System.out.println("=== GENERANDO PDF DESDE BASE DE DATOS ===");
        System.out.println("Ruta JRXML: " + jrxmlPath);
        System.out.println("SQL Query: " + sql);
        
        Connection connection = null;
        try {
            // Cargar el archivo JRXML desde recursos
            InputStream jrxmlStream = getClass().getResourceAsStream(jrxmlPath);
            if (jrxmlStream == null) {
                throw new IOException("No se encontró el archivo de reporte: " + jrxmlPath);
            }
            
            System.out.println("Archivo JRXML cargado correctamente");

            // Compilar el reporte JRXML
            System.out.println("Compilando reporte JRXML...");
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
            System.out.println("Reporte compilado exitosamente");

            // Obtener conexión a la BD
            System.out.println("Obteniendo conexión a la BD...");
            connection = dataSource.getConnection();
            System.out.println("Conexión a BD establecida exitosamente");
            
            // Si no hay parámetros, crear un mapa vacío
            if (parameters == null) {
                parameters = new HashMap<>();
            }

            // Llenar el reporte con los datos desde la BD
            System.out.println("Llenando reporte con datos desde BD...");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            System.out.println("Reporte llenado exitosamente desde BD");

            // Exportar a PDF
            System.out.println("Exportando a PDF...");
            byte[] result = exportPDF(jasperPrint);
            System.out.println("PDF exportado exitosamente. Tamaño: " + result.length + " bytes");
            System.out.println("=== PDF GENERADO CORRECTAMENTE DESDE BD ===");
            
            return result;
            
        } finally {
            // Cerrar la conexión
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Conexión a BD cerrada");
                } catch (Exception e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Genera un PDF a partir de un archivo JRXML y una lista de datos
     * 
     * @param jrxmlPath Ruta al archivo JRXML dentro de resources
     * @param dataList Lista de objetos con los datos para el reporte
     * @param parameters Parámetros adicionales para el reporte (pueden ser null)
     * @return Arreglo de bytes con el contenido del PDF
     * @throws JRException Si ocurre un error al procesar el reporte
     * @throws IOException Si ocurre un error al leer el archivo JRXML
     */
    public byte[] generatePDF(String jrxmlPath, List<?> dataList, Map<String, Object> parameters) 
            throws JRException, IOException {
        
        System.out.println("GenerarPDF iniciado con path: " + jrxmlPath);
        System.out.println("Cantidad de registros: " + (dataList != null ? dataList.size() : 0));
        
        // Cargar el archivo JRXML desde recursos
        InputStream jrxmlStream = getClass().getResourceAsStream(jrxmlPath);
        if (jrxmlStream == null) {
            System.err.println("ERROR: No se encontró el archivo: " + jrxmlPath);
            throw new IOException("No se encontró el archivo de reporte: " + jrxmlPath);
        }
        
        System.out.println("Archivo JRXML cargado correctamente");

        // Compilar el reporte JRXML
        System.out.println("Compilando reporte JRXML...");
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
        System.out.println("Reporte compilado exitosamente");

        // Crear una fuente de datos desde la lista de objetos
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

        // Si no hay parámetros, crear un mapa vacío
        if (parameters == null) {
            parameters = new HashMap<>();
        }

        // Llenar el reporte con los datos
        System.out.println("Llenando reporte con datos...");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        System.out.println("Reporte llenado exitosamente");

        // Exportar a PDF
        System.out.println("Exportando a PDF...");
        byte[] result = exportPDF(jasperPrint);
        System.out.println("PDF exportado exitosamente. Tamaño: " + result.length);
        return result;
    }

    /**
     * Genera un PDF con parámetros
     * 
     * @param jrxmlPath Ruta al archivo JRXML
     * @param dataList Lista de datos
     * @param parameters Mapa de parámetros
     * @return PDF en bytes
     * @throws JRException
     * @throws IOException
     */
    public byte[] generatePDFWithParameters(String jrxmlPath, List<?> dataList, 
            Map<String, Object> parameters) throws JRException, IOException {
        return generatePDF(jrxmlPath, dataList, parameters);
    }

    /**
     * Exporta un JasperPrint a PDF
     * 
     * @param jasperPrint El documento JasperPrint compilado
     * @return PDF en bytes
     * @throws JRException
     */
    private byte[] exportPDF(JasperPrint jasperPrint) throws JRException {
        // Usar JasperExportManager para exportar a PDF
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}

