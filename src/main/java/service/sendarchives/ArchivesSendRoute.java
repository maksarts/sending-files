package service.sendarchives;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import service.sendarchives.types.CompanyInfo;
import service.sendarchives.types.CompanyInfoList;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArchivesSendRoute extends RouteBuilder {
    @PropertyInject(value = "sendarchivesWorkingDir")
    protected String workingDir;
    @PropertyInject(value = "info.list")
    protected String companyInfoList;

    @Override
    public void configure() throws Exception {

        // обработка даты за предыдущий день в 9:30
        // при наличии файла в директории за предыдущий день - файл архивируется в промежуточную директорию
        from("file:" + workingDir + "?recursive=true&minDepth=2&maxDepth=2&delete=false&noop=true&scheduler=quartz2&scheduler.cron=0+30+9+*+*+?")
//        from("file:" + workingDir + "?recursive=true&minDepth=2&maxDepth=2&delete=false&noop=true&scheduler=quartz2&scheduler.cron=0/60+*+*+*+*+?") // test
                .routeId("sendarchivesExec7z")

                // получение даты за предыдущий день
                .process(exchange -> {
                    String date = LocalDate.now()
                            .minusDays(1)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    exchange.getIn().setHeader("yesterdayDate", date);
                })

                // обрабатываем только папку за вчерашнюю дату
                .filter(simple("${file:parent} contains '${header.yesterdayDate}'"))

                .setHeader("fileAbsPath", simple("${file:absolute.path}"))
                .setHeader("rootPath", simple("{{karaf.base}}"))

                // определение по имени файла к какой компании он относится
                .process(exchange -> {
                    String absPath = exchange.getIn().getHeader("fileAbsPath", String.class);

                    Gson gson = new GsonBuilder().create();
                    CompanyInfoList list = gson.fromJson(String.format("{\"companyInfoList\":%s}", companyInfoList), CompanyInfoList.class);
                    list.getCompanyInfoList().forEach(item -> {
                        String name = new String (item.getName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                        Pattern pattern = Pattern.compile(".*"+name+".*");
                        Matcher matcher = pattern.matcher(absPath);
                        if(matcher.find()){
                            exchange.getIn().setHeader("name", name);

                            String pass = item.getPassword();
                            exchange.getIn().setHeader("zipPassword", pass);
                        }
                    });
                })

                .filter(header("name").isNotNull())
                .recipientList(simple("exec:${header.rootPath}/bin/7za?args=a -tzip -p${header.zipPassword} \"${header.rootPath}/temp/sendarchives/${header.name}/${header.yesterdayDate}.zip\" \"${header.fileAbsPath}\""));


        // обработка даты за предыдущий день в 9:30, если отчетов нет
        from("quartz2://IsEmptyDir?cron=0+30+9+*+*+?&trigger.timeZone=Europe/Moscow")
//        from("quartz2://IsEmptyDir?cron=0/60+*+*+*+*+?&trigger.timeZone=Europe/Moscow") // test
                .routeId("IsEmptyDir")

                // получение даты за предыдущий день
                .process(exchange -> {
                    String date = LocalDate.now()
                            .minusDays(1)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    exchange.getIn().setHeader("yesterdayDate", date);
                })

                .setHeader("path", simple(workingDir + "${header.yesterdayDate}"))

                // помещаем в хедер список имен всех компаний, для которых
                // необходимо проверить наличие отчетов
                .process(exchange -> {
                    Gson gson = new GsonBuilder().create();
                    CompanyInfoList list = gson.fromJson(String.format("{\"companyInfoList\":%s}", companyInfoList), CompanyInfoList.class);
                    ArrayList<String> namesList = new ArrayList<>();
                    for(CompanyInfo item : list.getCompanyInfoList()){
                        String name = new String (item.getName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                        namesList.add(name);
                    }
                    exchange.getIn().setHeader("namesList", namesList);
                    exchange.getIn().setHeader("namesListSize", namesList.size());
                })

                // проверяем папки на предмет наличия всех имен, отправляем письма тем, чьих имен нет
                .loop(simple("${header.namesListSize}"))
                    .process(exchange -> {
                        ArrayList<String> namesList = exchange.getIn().getHeader("namesList", ArrayList.class);
                        String name = namesList.get(0);
                        exchange.getIn().setHeader("name", name);
                        namesList.remove(0);
                        exchange.getIn().setHeader("namesList", namesList);

                        Gson gson = new GsonBuilder().create();
                        CompanyInfoList list = gson.fromJson(String.format("{\"companyInfoList\":%s}", companyInfoList), CompanyInfoList.class);
                        list.getCompanyInfoList().forEach(item -> {
                            String curName = new String (item.getName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                            if(curName.equals(name)){
                                ArrayList<String> emailList = item.getEmailList();
                                exchange.getIn().setHeader("emailList", emailList);
                                ArrayList<String> emailListCopy = item.getEmailListCopy();
                                exchange.getIn().setHeader("emailListCopy", emailListCopy);
                                ArrayList<String> emailListError = item.getEmailListError();
                                exchange.getIn().setHeader("emailListError", emailListError);
                            }
                        });
                    })

                    .process("ProcessIsEmptyDir") // проверка пуста ли папка
                    .choice()
                        .when(simple("${header.isEmpty} == 'true'")) // отправка письма если папка пуста или не существует
                            .process("ProcessSendEmail")
                .end();



        // отправка архивов
        from("file:temp/sendarchives?recursive=true&maxDepth=2&delete=true&moveFailed=error&include=.*.zip")
                .routeId("SendArchiveEmail")

                .setHeader("fileName", simple("${file:onlyname}"))
                .setHeader("fileAbsPath", simple("${file:absolute.path}"))
                .setHeader("isEmpty", constant("false"))

                .process("ProcessSendEmail");
    }
}
