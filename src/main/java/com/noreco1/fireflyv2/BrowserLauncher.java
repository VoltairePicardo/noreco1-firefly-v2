package com.noreco1.fireflyv2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;

@Component
public class BrowserLauncher implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(BrowserLauncher.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:8080"));
            } else {
                Runtime.getRuntime().exec("cmd /c start http://localhost:8080");
            }
        } catch (Exception e) {
            logger.warn("Could not open browser: {}", e.getMessage());
        }
    }
}
