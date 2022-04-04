package de.jmonitoring.utils.networking;

import java.awt.Frame;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import de.jmonitoring.Components.SimplePasswordDialog;

/**
 * This class represents a ssh sessionn and tries to create a tunnel
 *
 * @author togro
 */
public class SSHTunnel {

    Session session;
    private final Frame parent;

    /**
     * Construct a new instance of SSHTunnel
     *
     * @param parent The parent frame
     */
    public SSHTunnel(Frame parent) {
        super();
        this.parent = parent;
    }

    /**
     * Try to create a ssh tunnel with the given user data
     *
     * @param host The SSH server
     * @param portString The SSH port (most: 22)
     * @param tunnelRemoteHost The internal IP of the databse server
     * @param tunnelPortString The internal port of the databse server
     * @param localPortString The local port which will be the tunnel endpoint
     * @param user The username of the ssh user
     * @throws Exception
     */
    public void buildTunnel(String host, String portString, String tunnelRemoteHost, String tunnelPortString, String localPortString, String user) throws Exception {
        int port = Integer.parseInt(portString);
        int tunnelLocalPort = Integer.parseInt(localPortString);
        int tunnelRemotePort = Integer.parseInt(tunnelPortString);

        SimplePasswordDialog sd = new SimplePasswordDialog(null, true, host, user);
        sd.setLocationRelativeTo(parent);
        sd.setAlwaysOnTop(true);
        sd.setVisible(true);

        String password = sd.getPassword();
        sd.dispose();
        if (password == null) {
            return;
        }

        JSch jsch = new JSch();
        session = jsch.getSession(user, host, port);
        session.setPassword(password);
        password = "";
        LocalUserInfo lui = new LocalUserInfo();
        session.setUserInfo(lui);
        try {
            session.connect();
            session.setPortForwardingL(tunnelLocalPort, tunnelRemoteHost, tunnelRemotePort);
        } catch (JSchException e) {
            System.out.println("SSH Auth Exeception");
        }

    }

    /**
     * Return the established session
     *
     * @return The {@link Session} or <code>null</code> if to connected
     */
    public Session getSession() {
        return session;
    }

    /**
     * Return the connection status
     *
     * @return <code>true</code> if we are connected
     */
    public boolean isConnected() {
        if (session != null) {
            return session.isConnected();
        } else {
            return false;
        }
    }

    /**
     * Default user info needed by the session
     */
    class LocalUserInfo implements UserInfo {

        String passwd;

        @Override
        public String getPassword() {
            return passwd;
        }

        @Override
        public boolean promptYesNo(String str) {
            return true;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return true;
        }

        @Override
        public boolean promptPassword(String message) {
            return true;
        }

        @Override
        public void showMessage(String message) {
        }
    }
}
