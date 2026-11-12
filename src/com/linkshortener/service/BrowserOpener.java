package com.linkshortener.service;

import java.io.IOException;
import java.net.URISyntaxException;

public interface BrowserOpener {

    void open(String url) throws IOException, URISyntaxException;
}
