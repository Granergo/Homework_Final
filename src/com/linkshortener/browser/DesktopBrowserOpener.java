package com.linkshortener.browser;

import com.linkshortener.service.BrowserOpener;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class DesktopBrowserOpener implements BrowserOpener {

    @Override
    public void open(String url) throws IOException, URISyntaxException {

        if (!Desktop.isDesktopSupported()) {
            throw new UnsupportedOperationException(
                    "Работа с рабочим столом не поддерживается."
            );
        }

        Desktop desktop = Desktop.getDesktop();

        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            throw new UnsupportedOperationException(
                    "Открытие браузера не поддерживается."
            );
        }

        URI uri = new URI(url);

        desktop.browse(uri);
    }
}
