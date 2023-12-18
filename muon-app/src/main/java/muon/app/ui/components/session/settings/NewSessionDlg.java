package muon.app.ui.components.session.settings;

import muon.app.App;
import muon.app.ui.components.SkinnedSplitPane;
import muon.app.ui.components.SkinnedTextField;
import muon.app.ui.components.session.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import static muon.app.App.bundle;

public class NewSessionDlg extends JDialog implements ActionListener, SessionTreeListener {

    private static final long serialVersionUID = -1182844921331289546L;

    private SessionTreePanel sessionTreePanel;
    private SessionInfoPanel sessionInfoPanel;
    private JButton btnNewHost, btnDel, btnDup, btnNewFolder, btnExport, btnImport;
    private JButton btnConnect, btnCancel;
    private JTextField txtName;
    private JPanel namePanel;
    private NamedItem selectedInfo;
    private JPanel prgPanel;
    private JPanel pdet;
    private SessionInfo info;
    private JLabel lblName;

    public NewSessionDlg(Window wnd) {
        super(wnd);
        createUI();
    }

    private void createUI() {
        setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        setSize(800, 600);
        setModal(true);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Saving before exit");
                sessionTreePanel.save();
                dispose();
            }
        });

        setTitle(bundle.getString("session_manager"));

        sessionTreePanel = new SessionTreePanel(this);

        btnNewHost = new JButton(bundle.getString("new_site"));
        btnNewHost.addActionListener(this);
        btnNewHost.putClientProperty("button.name", "btnNewHost");
        btnNewFolder = new JButton(bundle.getString("new_folder"));
        btnNewFolder.addActionListener(this);
        btnNewFolder.putClientProperty("button.name", "btnNewFolder");
        btnDel = new JButton(bundle.getString("remove"));
        btnDel.addActionListener(this);
        btnDel.putClientProperty("button.name", "btnDel");
        btnDup = new JButton(bundle.getString("duplicate"));
        btnDup.addActionListener(this);
        btnDup.putClientProperty("button.name", "btnDup");

        btnConnect = new JButton(bundle.getString("connect"));
        btnConnect.addActionListener(this);
        btnConnect.putClientProperty("button.name", "btnConnect");

        btnCancel = new JButton(bundle.getString("cancel"));
        btnCancel.addActionListener(this);
        btnCancel.putClientProperty("button.name", "btnCancel");

        btnExport = new JButton(bundle.getString("export"));
        btnExport.addActionListener(this);
        btnExport.putClientProperty("button.name", "btnExport");

        btnImport = new JButton(bundle.getString("import"));
        btnImport.addActionListener(this);
        btnImport.putClientProperty("button.name", "btnImport");

        normalizeButtonSize();

        Box box1 = Box.createHorizontalBox();
        box1.setBorder(new EmptyBorder(10, 10, 10, 10));
        box1.add(Box.createHorizontalGlue());
        box1.add(Box.createHorizontalStrut(10));
        box1.add(btnConnect);
        box1.add(Box.createHorizontalStrut(10));
        box1.add(btnCancel);

        GridLayout gl = new GridLayout(3, 2, 5, 5);
        JPanel btnPane = new JPanel(gl);
        btnPane.setBorder(new EmptyBorder(10, 0, 0, 0));
        btnPane.add(btnNewHost);
        btnPane.add(btnNewFolder);
        btnPane.add(btnDup);
        btnPane.add(btnDel);
        btnPane.add(btnExport);
        btnPane.add(btnImport);

        JSplitPane splitPane = new SkinnedSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        JPanel treePane = new JPanel(new BorderLayout());
        treePane.setBorder(new EmptyBorder(10, 10, 10, 0));
        treePane.add(sessionTreePanel);
        treePane.add(btnPane, BorderLayout.SOUTH);

        add(treePane, BorderLayout.WEST);

        sessionInfoPanel = new SessionInfoPanel();

        namePanel = new JPanel();

        JPanel pp = new JPanel(new BorderLayout());
        pp.add(namePanel, BorderLayout.NORTH);
        pp.add(sessionInfoPanel);

        pdet = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(pp);
        scrollPane.setBorder(null);
        pdet.add(scrollPane);
        pdet.add(box1, BorderLayout.SOUTH);


        BoxLayout boxLayout = new BoxLayout(namePanel, BoxLayout.PAGE_AXIS);
        namePanel.setLayout(boxLayout);

        namePanel.setBorder(new EmptyBorder(10, 0, 0, 10));

        lblName = new JLabel(bundle.getString("name"));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblName.setHorizontalAlignment(JLabel.LEADING);
        lblName.setBorder(new EmptyBorder(0, 0, 5, 0));


        txtName = new SkinnedTextField(10);
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtName.setHorizontalAlignment(JLabel.LEADING);
        txtName.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                updateName();
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                updateName();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                updateName();
            }

            private void updateName() {
                selectedInfo.setName(txtName.getText());
                sessionTreePanel.updateName(null);
            }
        });

        namePanel.add(lblName);
        namePanel.add(txtName);

        prgPanel = new JPanel();

        JLabel lbl = new JLabel(bundle.getString("connecting"));
        prgPanel.add(lbl);

        splitPane.setLeftComponent(treePane);
        splitPane.setRightComponent(pdet);

        add(splitPane);

        lblName.setVisible(false);
        txtName.setVisible(false);
        sessionInfoPanel.setVisible(false);
        btnConnect.setVisible(false);

        sessionTreePanel.loadTree(SessionStore.load());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton btn = (JButton) e.getSource();

        switch ((String) btn.getClientProperty("button.name")) {
            case "btnNewHost":
                sessionTreePanel.newHost();
                break;
            case "btnNewFolder":
                sessionTreePanel.newFolder();
                break;
            case "btnDel":
                sessionTreePanel.delete();
                break;
            case "btnDup":
                sessionTreePanel.duplicate();
                break;
            case "btnConnect":
                connectClicked();
                break;
            case "btnCancel":
                sessionTreePanel.save();
                dispose();
                break;
            case "btnImport":
                JComboBox<String> cmbImports = new JComboBox<>(
                        new String[]{"Putty", "WinSCP", "Muon session store", "SSH config file"});

                if (JOptionPane.showOptionDialog(this
                        , new Object[]{bundle.getString("import_from"), cmbImports}
                        , bundle.getString("import_sessions")
                        , JOptionPane.OK_CANCEL_OPTION
                        , JOptionPane.PLAIN_MESSAGE
                        , null
                        , null
                        , null
                ) == JOptionPane.OK_OPTION) {
                    if (cmbImports.getSelectedIndex() < 2) {
                        sessionTreePanel.importSessions(cmbImports);
                    } else {
                        if (cmbImports.getSelectedIndex() == 3) {
                            if (SessionExportImport.importSessionsSSHConfig()) {
                                sessionTreePanel.loadTree(SessionStore.load());
                            }
                        } else if (SessionExportImport.importSessions()) {
                            sessionTreePanel.loadTree(SessionStore.load());
                        }
                    }
                }
                break;
            case "btnExport":
                SessionExportImport.exportSessions();
                break;
            default:
                break;
        }
    }

    private void connectClicked() {
        sessionTreePanel.save();
        this.info = (SessionInfo) selectedInfo;
        if (this.info.getHost() == null || this.info.getHost().length() < 1) {
            JOptionPane.showMessageDialog(this, App.bundle.getString("no_hostname"));
            this.info = null;
            System.out.println("Returned");
            return;
        } else {
            System.out.println("Returned disposing");
            dispose();
        }
    }

    public SessionInfo newSession() {
        setLocationRelativeTo(null);
        setVisible(true);
        return this.info;
    }

    @Override
    public void treeValueChanged(Object nodeInfo) {
        if (nodeInfo instanceof SessionInfo) {
            sessionInfoPanel.setVisible(true);
            SessionInfo info = (SessionInfo) nodeInfo;
            sessionInfoPanel.setSessionInfo(info);
            selectedInfo = info;
            txtName.setVisible(true);
            lblName.setVisible(true);
            txtName.setText(selectedInfo.getName());
            btnConnect.setVisible(true);
        } else if (nodeInfo instanceof NamedItem) {
            selectedInfo = (NamedItem) nodeInfo;
            lblName.setVisible(true);
            txtName.setVisible(true);
            txtName.setText(selectedInfo.getName());
            sessionInfoPanel.setVisible(false);
            btnConnect.setVisible(false);
        }

        revalidate();
        repaint();
    }

    @Override
    public Window getWindow() {
        return this;
    }

    @Override
    public void treeDoubleClick() {
        connectClicked();
    }

    private void normalizeButtonSize() {
        int width = Math.max(btnConnect.getPreferredSize().width, btnCancel.getPreferredSize().width);
        btnConnect.setPreferredSize(new Dimension(width, btnConnect.getPreferredSize().height));
        btnCancel.setPreferredSize(new Dimension(width, btnCancel.getPreferredSize().height));
    }
}
