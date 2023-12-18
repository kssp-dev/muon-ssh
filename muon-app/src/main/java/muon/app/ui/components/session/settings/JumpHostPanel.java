package muon.app.ui.components.session.settings;

import muon.app.ui.components.session.SessionInfo;
import muon.app.ui.components.session.SessionStore;

import javax.swing.*;
import java.awt.*;

public class JumpHostPanel extends JPanel implements SessionTreeListener {
    private SessionTreePanel sessionTreePanel;
    private SessionInfo info;

    public JumpHostPanel() {
        super(new BorderLayout(5, 5));
        JLabel lblTitle = new JLabel("Select intermediate hop (double click to drop)");

        sessionTreePanel = new SessionTreePanel(this);

        this.add(lblTitle, BorderLayout.NORTH);

        this.add(sessionTreePanel);
    }

    public void setInfo(SessionInfo info) {
        this.info = info;

        String id = this.info.getJumpId();

        if (id == null) {
            id = "";
        }

        sessionTreePanel.loadTree(SessionStore.load(), id);
    }

    @Override
    public void treeValueChanged(Object nodeInfo) {
        if (nodeInfo instanceof SessionInfo) {
            SessionInfo sessionInfo = (SessionInfo) nodeInfo;
            this.info.setJumpId(sessionInfo.getId());
        }
    }

    @Override
    public void treeDoubleClick() {
        this.info.setJumpId(null);
        sessionTreePanel.selectNode(null, null);
    }
}
