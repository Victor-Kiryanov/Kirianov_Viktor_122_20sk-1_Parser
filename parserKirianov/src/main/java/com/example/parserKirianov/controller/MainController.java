package com.example.parserKirianov.controller;

import com.example.parserKirianov.model.Info;
import com.example.parserKirianov.servises.ExcelGeneration;
import com.example.parserKirianov.servises.WebsiteParser;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Controller
public class MainController {

    @GetMapping("/")
    public String mainPage() {
        return "index";
    }

    @GetMapping("/index")
    public String mainPageIndex() {
        return "index";
    }

    @GetMapping("/parser")
    public String parserPage(Model model) {
        model.addAttribute("infoText", "It's a parser for the ROZETKA.");
        model.addAttribute("infoTime", "Performance Information! The execution "
                + "time depends on the number of sheets found. The fewer pages "
                + "the faster the collection and processing of the information!");
        model.addAttribute("text", "P.S. Файл повернеться, коли в назві вкладки перестане крутитися кружечок))");
        return "parser";
    }

    @GetMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("infoHeader", "Привіт! Я, Кір'янов Віктор Андрійович. Студент групи 122-20ск-1.");
        model.addAttribute("infoAboutMe", "Це моя лабораторна робота з дисципліни: Поглиблена Java. Я розробив парсер для сайту РОЗЕТКА.");
        return "about";
    }

    @PostMapping("/inject")
    public ResponseEntity<ByteArrayResource> search(Model model, @RequestParam String name) throws InterruptedException, IOException {
        List<Info> adList = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("MM_dd_yyy_hh_mm_ss_a");
        String date = dateFormat.format(Calendar.getInstance().getTime());
        String fileName = "Search_result_on_Rosetka_" + date;
        if (name.isEmpty()){
            fileName = "Request_Was_Empty_" + date;
            Info emp = new Info();
            emp.setSearch("Request_Was_Empty");
            emp.setShortDescription("Request_Was_Empty");
            adList.add(emp);
        }
        else {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--remote-allow-origins=*");
            WebDriver driver = new ChromeDriver(options);
            WebsiteParser pars = new WebsiteParser();
            pars.establishConnection(driver);
            adList = pars.parsingPerRequestOnRozetka(name);
            pars.disconnectBrowserConnection(driver);
        }
            if (adList.size() != 0) {
                ExcelGeneration excel = new ExcelGeneration();
                List<String> captionColumn = new ArrayList<>();
                captionColumn.add("№ стр.");
                captionColumn.add("Запит до сайта");
                captionColumn.add("Внутрішній номер товара");
                captionColumn.add("Короткий опис");
                captionColumn.add("Ціна");
                captionColumn.add("Наявність");
                captionColumn.add("Посилання на товар");
                excel.createColumnCaptions(captionColumn);
                excel.createExcel(fileName, adList);
            }
        String FILE_PATH = "src/main/resources/GeneratedFiles/" + fileName + ".xls";
        Path path = Paths.get(FILE_PATH);
        byte[] data = Files.readAllBytes(path);
        Thread.sleep(1000);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path.getFileName().toString())
                .contentType(MediaType.parseMediaType("application/octet-atream"))
                .contentLength(data.length)
                .body(new ByteArrayResource(data));
    }
}
