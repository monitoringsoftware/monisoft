/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils;

import de.jmonitoring.base.Messages;
import java.io.IOException;

/**
 *
 * @author togro
 */
public class WebBrowser {

     String url;
    //wir starten eine lokale HTML Seite. Modifikation für
    //Webanwendung sollte leicht sein.

    public WebBrowser(String url_in) {
        url = "http://" + url_in;
    }

    public void launch() {
        try {
            if (isWindows()) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else {
                Runtime.getRuntime().exec("firefox " + url);
            }
        } catch (IOException ioe) {
            Messages.showException(ioe);
        }
    }

//Test auf Windows
    public boolean isWindows() {
        String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Windows")) {
            return true;
        } else {
            return false;
        }


    }
}
