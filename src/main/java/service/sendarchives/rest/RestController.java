package service.sendarchives.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import service.sendarchives.types.CompanyInfoList;
import usr.lib.global.DateLib;
import usr.lib.global.StringLib;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RestController extends RouteBuilder {

    @PropertyInject(value = "sendarchivesWorkingDir")
    protected String workingDir;
    @PropertyInject(value = "fileimport-misc.sendarchives.info.list")
    protected String companyInfoList;

    @Override
    public void configure() throws Exception {

        errorHandler(defaultErrorHandler().log(log));

        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.auto);

        rest("/sendarchives")

                .get("/test")
                .route().routeId("sendarchives-test")
                    .transform().simple("Test is OK")
                .endRest()


                .post("/processDate").produces("application/json").consumes("application/json")
                .type(RequestDate.class)
                .route().routeId("sendarchives-processDate")

                    .process(exchange -> {
                        // получаем даты
                        String date = exchange.getIn().getBody(RequestDate.class).getDate();
                        String endDate = exchange.getIn().getBody(RequestDate.class).getEndDate();

                        // по коду задаем имя компании, по которому будет поиск отчетов, адреса и пароль
                        String code = exchange.getIn().getBody(RequestDate.class).getCode();
                        Gson gson = new GsonBuilder().create();
                        CompanyInfoList list = gson.fromJson(String.format("{\"companyInfoList\":%s}", companyInfoList), CompanyInfoList.class);
                        list.getCompanyInfoList().forEach(item -> {
                            if(item.getCode().equals(code)){
                                String name = new String (item.getName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                                exchange.getIn().setHeader("name", name);

                                String pass = item.getPassword();
                                exchange.getIn().setHeader("zipPassword", pass);

                                ArrayList<String> emailList = item.getEmailList();
                                exchange.getIn().setHeader("emailList", emailList);
                                ArrayList<String> emailListCopy = item.getEmailListCopy();
                                exchange.getIn().setHeader("emailListCopy", emailListCopy);
                                ArrayList<String> emailListError = item.getEmailListError();
                                exchange.getIn().setHeader("emailListError", emailListError);
                            }
                        });

                        exchange.getIn().setHeader("date", date);
                        exchange.getIn().setHeader("endDate", endDate);
                    })

                    .validate(header("name").isNotNull())

                    .choice()
                        .when(simple("${header.endDate} == null"))
                            .to("direct:oneDate")
                        .otherwise()
                            .to("direct:period")

                .endRest();



        // обработка за одну конкретную дату
        from("direct:oneDate").routeId("rest-oneDate")
                .validate(header("date").regex("\\d{4}-\\d{2}-\\d{2}"))
                .setHeader("path", simple(workingDir + "${header.date}"))

                .process("ProcessIsEmptyDir") // проверка есть ли в header.path отчеты header.name

                .choice()
                    .when(simple("${header.isEmpty} == 'true'")) // отправка письма если папка пуста или не существует
                        .process("ProcessSendEmail")
                        .setBody(constant("{status:OK}"))

                    .otherwise()
                        .setHeader("rootPath", simple("{{karaf.base}}"))
                        .recipientList(simple("exec:${header.rootPath}/bin/7za?args=a -tzip -p${header.zipPassword} \"${header.rootPath}/temp/sendarchives/${header.name}/${header.date}.zip\" \"${header.rootPath}/${header.path}/*${header.name}*.*\""))
                        .setBody(constant("{status:OK}"))
                ;




        // обработка за период [date, endDate] (включительно)
        from("direct:period").routeId("rest-period")
                .validate(header("date").regex("\\d{4}-\\d{2}-\\d{2}"))
                .validate(header("endDate").regex("\\d{4}-\\d{2}-\\d{2}"))

                .setHeader("rootPath", simple("{{karaf.base}}"))

                .setHeader("loopFlag", constant(true))
                .loopDoWhile(simple("${header.loopFlag} == 'true'"))
                    .log("processing date=${header.date}")
                    .setHeader("path", simple(workingDir + "${header.date}"))

                    // получение следующей даты
                    .process(exchange -> {
                        String endDateStr = exchange.getIn().getHeader("endDate", String.class);
                        String dtStr = exchange.getIn().getHeader("date", String.class);

                        if(endDateStr.equals(dtStr)){
                            exchange.getIn().setHeader("loopFlag", false);
                        }

                        Date prevDate;
                        Date dt = StringLib.toDateThrow(dtStr, "yyyy-MM-dd");
                        if (dt==null) {
                            prevDate = DateLib.addToDate(new Date(), Calendar.DAY_OF_YEAR, -1);
                        } else {
                            prevDate = dt;
                        }

                        // получаем следующую дату
                        long nextDateMilliSeconds = prevDate.getTime() + 24 * 60 * 60 * 1000;
                        Date newDate = new Date(nextDateMilliSeconds);
                        String newDateStr = StringLib.toString(newDate, "yyyy-MM-dd");
                        exchange.getIn().setHeader("newDate", newDateStr);
                    })

                    .process("ProcessIsEmptyDir") // проверка есть ли в header.path отчеты header.name

                    .choice()
                        .when(simple("${header.isEmpty} == 'true'")) // отправка письма если папка пуста или не существует
                            .process("ProcessSendEmail")
                            .setHeader("date", simple("${header.newDate}"))
                        .otherwise()
                            .recipientList(simple("exec:${header.rootPath}/bin/7za?args=a -tzip -p${header.zipPassword} \"${header.rootPath}/temp/sendarchives/${header.name}/${header.date}.zip\" \"${header.rootPath}/${header.path}/*${header.name}*.*\""))
                            .setHeader("date", simple("${header.newDate}"))
                    .endChoice()

                .setBody(constant("{status:OK}"))
                .end();



    }
}
