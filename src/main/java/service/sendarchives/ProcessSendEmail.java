package service.sendarchives;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.PropertyInject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.sendarchives.types.CompanyInfo;
import service.sendarchives.types.CompanyInfoList;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessSendEmail implements Processor {

    @PropertyInject(value = "info.list")
    protected String companyInfoList;

    public static final Logger logger = LoggerFactory.getLogger(ProcessSendEmail.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        ArrayList<String> recipientsList = new ArrayList<>();
        ArrayList<String> recipientsListCopy = new ArrayList<>();
        ArrayList<String> recipientsListError = new ArrayList<>();

        String date = "<UNKNOWN DATE>";
        String text = "";

        String fileName = ""; // exchange.getIn().getHeader("fileName", String.class);
        String absPath = ""; // exchange.getIn().getHeader("fileAbsPath", String.class); // absPath= .../.../YYYY-MM-DD/somefile.ext.zip

        String companyName = exchange.getIn().getHeader("name", String.class);

        // Отправка письма если файла нет
        if(exchange.getIn().getHeader("isEmpty", String.class).equals("true")){
            recipientsList = exchange.getIn().getHeader("emailList", ArrayList.class); // получаем список адресатов
            recipientsListCopy = exchange.getIn().getHeader("emailListCopy", ArrayList.class); // получаем список адресатов для копии письма
            recipientsListError = exchange.getIn().getHeader("emailListError", ArrayList.class); // получаем список адресатов для письма ошибки

            date = exchange.getIn().getHeader("yesterdayDate", String.class);
            if(date == null){
                date = exchange.getIn().getHeader("date", String.class);
            }
            absPath = "";
            fileName = "";

            text = "Добрый день!\n" +
                    "Файлов для отправки в этот день не имеется.";

        }
        // отправка письма если файл есть
        else{
            fileName = exchange.getIn().getHeader("fileName", String.class);
            absPath = exchange.getIn().getHeader("fileAbsPath", String.class);

            Gson gson = new GsonBuilder().create();
            CompanyInfoList list = gson.fromJson(String.format("{\"companyInfoList\":%s}", companyInfoList), CompanyInfoList.class);

            for(CompanyInfo item : list.getCompanyInfoList()){
                String name = new String (item.getName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                Pattern pattern = Pattern.compile(".*"+name+".*");
                Matcher matcher = pattern.matcher(absPath);
                if (matcher.find()){
                    // нашли информацию по компании, имя которой значится в пути файла
                    recipientsList = item.getEmailList(); // получаем список адресатов
                    recipientsListCopy = item.getEmailListCopy(); // получаем список адресатов для копии письма
                    recipientsListError = item.getEmailListError(); // получаем список адресатов для письма ошибки
                    companyName = name;
                    break;
                }
            }

            Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
            Matcher matcher = pattern.matcher(absPath);
            if (matcher.find()) {
                date = matcher.group(1); // getting 'YYYY-MM-DD' date from absPath if possible
            }

            text = "Добрый день!\n" +
                    "Высылаем отчет, во вложении";
        }

        try {
            postMail(recipientsList,
                    recipientsListCopy,
                    date + ". Отчет по продажам продуктов " + companyName,
                    fileName,
                    text,
                    absPath
            );
            logger.info("[ProcessSendEmail]: email sent");

        } catch (MessagingException e) {
            logger.error("[ProcessSendEmail]: cant send email with file", e);
            logger.info("[ProcessSendEmail]: try to send error email");
            try {
                postMail(recipientsListError,
                        new ArrayList<>(),
                        date + " Ошибка отправки файла",
                        fileName,
                        "Ошибка отправки файла " + fileName +": \n" + e,
                        ""
                );
                logger.info("[ProcessSendEmail]: error file email sent");
                throw new Exception("Cant send email with file");

            } catch (MessagingException ex){
                logger.error("[ProcessSendEmail]: cant send error email", e);
                throw ex;
            }
        }
    }

    public void postMail(List<String> sendToList, List<String> sendCCList, String subject, String filename, String text, String absPath) throws AddressException, MessagingException
    {
        Properties props = new Properties();
        props.put("mail.smtp.host", bankSMTP);
        props.put("mail.smtp.auth", "false");

        Session session = Session.getInstance(props, null);

        MimeMessage msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(bankMail));

        for (String s : sendToList) {
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(s));
        }
        for (String s : sendCCList) {
            msg.addRecipient(Message.RecipientType.CC, new InternetAddress(s));
        }

        msg.setSubject(subject);

        Multipart multipart = new MimeMultipart();

        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setText(text);

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        if(!absPath.equals("")) {
            DataSource source = new FileDataSource(absPath);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart); // прикрепление файла
        }

        multipart.addBodyPart(textBodyPart); // текст письма

        msg.setContent(multipart);

        Transport trnsport;
        trnsport = session.getTransport("smtp");
        trnsport.connect(); //(bankUser, bankPass);
        msg.saveChanges();
        trnsport.sendMessage(msg, msg.getAllRecipients());
        trnsport.close();
    }
}
