package com.example.parserKirianov.servises;

import com.example.parserKirianov.model.Info;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelGeneration {
    private HSSFWorkbook workbook;
    private HSSFSheet sheet;

    public ExcelGeneration(){
        workbook = new HSSFWorkbook();
        sheet = workbook.createSheet("Parser_Rozetka");
    }

    public HSSFWorkbook getWorkbook(){
        return workbook;
    }

    public HSSFSheet getSheet(){
        return sheet;
    }

    private void addColumnCaptions(Row row, int numberCell, String captions){
        row.createCell(numberCell).setCellValue(captions);
    }

    public void createColumnCaptions(List<String> captions){
        int numberCell = 0;
        Row row = getSheet().createRow(0);
        for(String capt : captions){
            addColumnCaptions(row, numberCell, capt);
            numberCell += 1;
        }
    }

    private void fillOutSheet(List<Info> adList){
        int rowNum = 0;
        for (Info dataModel : adList) {
            fillExcel(sheet, ++rowNum, dataModel);
        }
    }

    // заполнение строки (rowNum) определенного листа (sheet)
    // данными  из dataModel созданного в памяти Excel файла
    private static void fillExcel(HSSFSheet sheet, int rowNum, Info dataModel) {
        Row row = sheet.createRow(rowNum);
        sheet.autoSizeColumn(rowNum);
        row.createCell(0).setCellValue(dataModel.getPageNumber());
        sheet.autoSizeColumn(0);
        row.createCell(1).setCellValue(dataModel.getSearch());
        sheet.autoSizeColumn(1);
        row.createCell(2).setCellValue(dataModel.getInternalNumber());
        sheet.autoSizeColumn(2);
        row.createCell(3).setCellValue(dataModel.getShortDescription());
        sheet.autoSizeColumn(3);
        row.createCell(4).setCellValue(dataModel.getPrice());
        sheet.autoSizeColumn(4);
        row.createCell(5).setCellValue(dataModel.getAvailability());
        sheet.autoSizeColumn(5);
        row.createCell(6).setCellValue(dataModel.getProductLink());
        sheet.autoSizeColumn(6);
    }

    // записываем созданный в памяти Excel документ в файл
    public void createExcel(String fileName, List<Info> adList){
        File directory_PATH = new File("./GeneratedFiles");
        if(directory_PATH.mkdirs())
        {
            System.out.println("Directory created successfully");
        }
       /* else{
            directory_PATH = new File("src/main/resources/GeneratedFiles/" + fileName + ".xls");
        }*/
        System.out.println(directory_PATH);
        fillOutSheet(adList);
        try (FileOutputStream out = new FileOutputStream(directory_PATH + "/" + fileName + ".xls")) {
            getWorkbook().write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Excel файл успешно создан!");
    }
}
