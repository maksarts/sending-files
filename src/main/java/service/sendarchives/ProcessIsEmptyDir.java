package service.sendarchives;

import com.sun.org.slf4j.internal.LoggerFactory;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessIsEmptyDir implements Processor {

    public static final Logger logger = LoggerFactory.getLogger(ProcessIsEmptyDir.class);

    /**
     * Required header "path" and "name"
     * @work: sets header.isEmpty == true if there are no files by "name" in path,
     * else sets header.isEmpty == false
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        exchange.getIn().setHeader("isEmpty", "true");

        String companyName = exchange.getIn().getHeader("name", String.class);
        if(companyName == null){
            throw new Exception("[ProcessIsEmptyDir]: header \"name\" is null");
        }
        String strPath = exchange.getIn().getHeader("path", String.class);
        if(strPath == null){
            throw new Exception("[ProcessIsEmptyDir]: header \"strPath\" is null");
        }

        Path directory = Paths.get(strPath);
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            Iterator<Path> iterator = dirStream.iterator();
            boolean isEmpty = true;

            while(iterator.hasNext()){
                String path = iterator.next().toAbsolutePath().toString();

                Pattern pattern = Pattern.compile(".*("+companyName+").*");
                Matcher matcher = pattern.matcher(path);
                if(matcher.find()){
                    isEmpty = false;
                    break;
                }
            }

            exchange.getIn().setHeader("isEmpty", isEmpty);
            if(isEmpty){
                logger.info("[ProcessIsEmptyDir]: directory '" + strPath + "' has no " + companyName + " files, sending email");
            }
        } catch (Exception ex){
            logger.info("[ProcessIsEmptyDir]: directory '" + strPath + "' does not exist, sending email");
        }
    }
}
