package pl.arekbednarz.gameshopapi.testcontainers;

import io.vavr.control.Option;
import org.testcontainers.containers.output.OutputFrame;

import java.util.Objects;


public class OutputUtils {

    private OutputUtils(){
    }

    public static void forward(final OutputFrame frame){
        Option.of(frame).map(OutputFrame::getBytes).filter(Objects::nonNull).forEach(OutputUtils::forward);

    }
    private static void forward(byte[] bytes) {
        System.out.println(bytes);
    }
}
