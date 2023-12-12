package muon.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import muon.app.ui.components.session.SavedSessionTree;
import muon.app.ui.components.session.SessionFolder;
import muon.app.ui.components.session.SessionInfo;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.*;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PasswordStore {
    private static KeyStore KEY_STORE;
    private static PasswordStore INSTANCE;

    private final AtomicBoolean unlocked = new AtomicBoolean(false);
    private KeyStore.PasswordProtection protParam;

    private Map<String, char[]> passwordMap = new HashMap<>();

    private PasswordStore() throws KeyStoreException {
        KEY_STORE = KeyStore.getInstance("PKCS12");
    }

    public static final synchronized PasswordStore getSharedInstance() throws Exception {
        if (INSTANCE == null) {
            INSTANCE = new PasswordStore();
        }
        return INSTANCE;
    }

    public final boolean isUnlocked() {
        return unlocked.get();
    }

    public final synchronized void unlockStore(char[] password) throws Exception {
        protParam = new KeyStore.PasswordProtection(password, "PBEWithHmacSHA256AndAES_256", null);
        File filePasswordStore = new File(App.CONFIG_DIR, "passwords.pfx");
        if (!filePasswordStore.exists()) {
            KEY_STORE.load(null, protParam.getPassword());
            unlocked.set(true);
            return;
        }
        try (InputStream in = new FileInputStream(filePasswordStore)) {
            KEY_STORE.load(in, protParam.getPassword());
            loadPasswords();
            unlocked.set(true);
        }
    }

    private final synchronized void loadPasswords() throws Exception {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
        KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry) KEY_STORE.getEntry("passwords", protParam);

        PBEKeySpec keySpec = (PBEKeySpec) factory.getKeySpec(ske.getSecretKey(), PBEKeySpec.class);

        char[] chars = keySpec.getPassword();

        this.passwordMap = deserializePasswordMap(chars);
    }

    private Map<String, char[]> deserializePasswordMap(char[] chars) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(new CharArrayReader(chars), new TypeReference<Map<String, char[]>>() {
        });
    }

    private char[] serializePasswordMap(Map<String, char[]> map) throws Exception {
        CharArrayWriter writer = new CharArrayWriter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(writer, map);
        return writer.toCharArray();
    }

    public final synchronized char[] getSavedPassword(String alias) throws Exception {
        return this.passwordMap.get(alias);
    }

    public final synchronized void savePassword(String alias, char[] password) throws Exception {
        this.passwordMap.put(alias, password);
    }

    public final synchronized void saveKeyStore() throws Exception {

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBE");
        SecretKey generatedSecret = secretKeyFactory
                .generateSecret(new PBEKeySpec(serializePasswordMap(this.passwordMap)));
        KEY_STORE.setEntry("passwords", new SecretKeyEntry(generatedSecret), protParam);

        System.out.println("Password protection: " + protParam.getProtectionAlgorithm());

        try (OutputStream out = new FileOutputStream(new File(App.CONFIG_DIR, "passwords.pfx"))) {
            KEY_STORE.store(out, protParam.getPassword());
        }
    }

    private final boolean unlockStore() {
        if (this.isUnlocked()) {
            return true;
        }

        if (App.getGlobalSettings().isUsingMasterPassword()) {
            return unlockUsingMasterPassword();
        } else {
            try {
                unlockStore(new char[0]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

    }

    public final synchronized void populatePassword(SavedSessionTree savedSessionTree) {
        if (!unlockStore()) {
            return;
        }
        if (savedSessionTree != null) {
            populatePassword(savedSessionTree.getFolder());
        }

    }

    private void populatePassword(SessionFolder folder) {
        for (SessionInfo info : folder.getItems()) {
            try {
                char[] password = this.getSavedPassword(info.getId());
                info.setPassword(new String(password));
            } catch (Exception e) {
            }
        }
        for (SessionFolder f : folder.getFolders()) {
            populatePassword(f);
        }
    }

    public void savePasswords(SavedSessionTree savedSessionTree) {
        if (!this.isUnlocked()) {
            if (App.getGlobalSettings().isUsingMasterPassword()) {
                if (!unlockUsingMasterPassword()) {
                    return;
                }
            } else {
                try {
                    unlockStore(new char[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        savePassword(savedSessionTree.getFolder());
        try {
            saveKeyStore();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePassword(SessionFolder folder) {
        for (SessionInfo info : folder.getItems()) {
            String password = info.getPassword();
            if (password != null && password.length() > 0) {
                try {
                    savePassword(info.getId(), password.toCharArray());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        for (SessionFolder f : folder.getFolders()) {
            savePassword(f);
        }
    }

    private boolean unlockUsingMasterPassword() {
        while (true) {
            try {
                JPasswordField txtPass = new JPasswordField(30);
                if (JOptionPane.showOptionDialog(App.getAppWindow(), new Object[]{"Master password", txtPass},
                        App.bundle.getString("master_password"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
                        null) == JOptionPane.OK_OPTION) {
                    this.unlockStore(txtPass.getPassword());
                    return true;
                }
            } catch (IOException e) {
                if (e.getCause() instanceof UnrecoverableKeyException) {
                    if (JOptionPane.showConfirmDialog(App.getAppWindow(),
                            App.bundle.getString("incorrect_password")) != JOptionPane.YES_OPTION) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (JOptionPane.showConfirmDialog(App.getAppWindow(),
                    App.bundle.getString("error_loading_password")) != JOptionPane.YES_OPTION) {
                break;
            }
        }
        return false;
    }

    public boolean changeStorePassword(char[] newPassword) throws Exception {
        if (!unlockStore()) {
            return false;
        }

        Enumeration<String> aliases = KEY_STORE.aliases();
        Map<String, char[]> passMap = new HashMap<String, char[]>();

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            passMap.put(alias, getSavedPassword(alias));
            KEY_STORE.deleteEntry(alias);
        }

        protParam = new KeyStore.PasswordProtection(newPassword, "PBEWithHmacSHA256AndAES_256", null);
        for (String alias : passMap.keySet()) {
            savePassword(alias, passMap.get(alias));
        }
        saveKeyStore();
        return true;
    }
}
