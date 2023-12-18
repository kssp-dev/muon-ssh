package muon.app.ui.components.session.settings;

import muon.app.ui.components.session.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.UUID;

import static muon.app.App.bundle;

public class SessionTreePanel extends JScrollPane implements TreeSelectionListener {
    private SessionTreeListener listener;
    private DefaultTreeModel treeModel;
    private JTree tree;
    private DefaultMutableTreeNode rootNode;
    private String lastSelected;

    public SessionTreePanel(SessionTreeListener listener) {
        this.listener = listener;

        treeModel = new DefaultTreeModel(null, true);
        tree = new AutoScrollingJTree(treeModel);
        this.setViewportView(tree);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new TreeTransferHandler());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.getSelectionModel().addTreeSelectionListener(this);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switch (e.getClickCount()) {
                    case 2:
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        if (node == null || node.getAllowsChildren()) return;
                        listener.treeDoubleClick();
                        break;
                }
            }
        });

        tree.setEditable(false);
    }

    public void loadTree(SavedSessionTree stree) {
        loadTree(stree, null);
    }

    public void loadTree(SavedSessionTree stree, String lastSel) {
        if (lastSel == null) {
            this.lastSelected = stree.getLastSelection();
        } else {
            this.lastSelected = lastSel;
        }
        rootNode = SessionStore.getNode(stree.getFolder());
        rootNode.setAllowsChildren(true);
        treeModel.setRoot(rootNode);
        try {
            if (this.lastSelected != null) {
                selectNode(lastSelected, rootNode);
            } else {
                DefaultMutableTreeNode n = null;
                n = findFirstInfoNode(rootNode);
                if (n == null) {
                    SessionInfo sessionInfo = new SessionInfo();
                    sessionInfo.setName(bundle.getString("new_site"));
                    sessionInfo.setId(UUID.randomUUID().toString());
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(sessionInfo);
                    childNode.setUserObject(sessionInfo);
                    childNode.setAllowsChildren(false);
                    treeModel.insertNodeInto(childNode, rootNode, rootNode.getChildCount());
                    n = childNode;
                    tree.scrollPathToVisible(new TreePath(n.getPath()));
                    TreePath path = new TreePath(n.getPath());
                    tree.setSelectionPath(path);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        treeModel.nodeChanged(rootNode);
    }

    public boolean selectNode(String id, DefaultMutableTreeNode node) {
        if (id == null) {
            tree.clearSelection();
            return true;
        }

        if (id.equals((((NamedItem) node.getUserObject()).getId()))) {
            TreePath path = new TreePath(node.getPath());
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            if (selectNode(id, child)) {
                return true;
            }
        }

        return false;
    }

    private DefaultMutableTreeNode findFirstInfoNode(DefaultMutableTreeNode node) {
        if (!node.getAllowsChildren()) {
            return node;
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode child = findFirstInfoNode((DefaultMutableTreeNode) node.getChildAt(i));
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }

    public void newHost() {
        TreePath parentPath = tree.getSelectionPath();
        DefaultMutableTreeNode parentNode = null;

        if (parentPath != null) {
            parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
        }

        if (parentNode == null) {
            parentNode = rootNode;
        }
        Object obj = parentNode.getUserObject();
        if (obj instanceof SessionInfo) {
            parentNode = (DefaultMutableTreeNode) parentNode.getParent();
            obj = parentNode.getUserObject();
        }
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setName(bundle.getString("new_site"));
        sessionInfo.setId(UUID.randomUUID().toString());
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(sessionInfo);
        childNode.setUserObject(sessionInfo);
        childNode.setAllowsChildren(false);
        treeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
        tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        TreePath path = new TreePath(childNode.getPath());
        tree.setSelectionPath(path);
    }

    public void newFolder() {
        TreePath parentPath = tree.getSelectionPath();
        DefaultMutableTreeNode parentNode = null;

        if (parentPath != null) {
            parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
        }

        if (parentNode == null) {
            parentNode = rootNode;
        }
        Object objFolder = parentNode.getUserObject();
        if (objFolder instanceof SessionInfo) {
            parentNode = (DefaultMutableTreeNode) parentNode.getParent();
            objFolder = parentNode.getUserObject();
        }
        SessionFolder folder = new SessionFolder();
        folder.setName(bundle.getString("new_folder"));
        DefaultMutableTreeNode childNode1 = new DefaultMutableTreeNode(folder);
        treeModel.insertNodeInto(childNode1, parentNode, parentNode.getChildCount());
        tree.scrollPathToVisible(new TreePath(childNode1.getPath()));
        TreePath path2 = new TreePath(childNode1.getPath());
        tree.setSelectionPath(path2);
    }

    public void delete() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null && node.getParent() != null) {
            DefaultMutableTreeNode sibling = node.getNextSibling();
            if (sibling != null) {
                String id = ((NamedItem) sibling.getUserObject()).getId();
                selectNode(id, sibling);
            } else {
                DefaultMutableTreeNode parentNode1 = (DefaultMutableTreeNode) node.getParent();
                tree.setSelectionPath(new TreePath(parentNode1.getPath()));
            }
            treeModel.removeNodeFromParent(node);
        }
    }

    public void duplicate() {
        DefaultMutableTreeNode node1 = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node1 != null && node1.getParent() != null && (node1.getUserObject() instanceof SessionInfo)) {
            SessionInfo info = ((SessionInfo) node1.getUserObject()).copy();
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(info);
            child.setAllowsChildren(false);
            treeModel.insertNodeInto(child, (MutableTreeNode) node1.getParent(), node1.getParent().getChildCount());
            selectNode(info.getId(), child);
        } else if (node1 != null && node1.getParent() != null && (node1.getUserObject() instanceof NamedItem)) {
            SessionFolder newFolder = new SessionFolder();
            newFolder.setId(UUID.randomUUID().toString());
            newFolder.setName("Copy of " + ((NamedItem) node1.getUserObject()).getName());
            Enumeration childrens = node1.children();
            DefaultMutableTreeNode newFolderTree = new DefaultMutableTreeNode(newFolder);
            while (childrens.hasMoreElements()) {
                DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) childrens.nextElement();
                if (defaultMutableTreeNode.getUserObject() instanceof SessionInfo) {
                    SessionInfo newCopyInfo = ((SessionInfo) defaultMutableTreeNode.getUserObject()).copy();
                    newCopyInfo.setName("Copy of " + newCopyInfo.getName());
                    DefaultMutableTreeNode subChild = new DefaultMutableTreeNode(newCopyInfo);
                    subChild.setAllowsChildren(false);
                    newFolderTree.add(subChild);
                }
            }
            MutableTreeNode parent = (MutableTreeNode) node1.getParent();
            treeModel.insertNodeInto(newFolderTree, parent, node1.getParent().getChildCount());
            selectNode(newFolder.getId(), newFolderTree);
        }
    }

    public void importSessions(JComboBox<String> cmbImports) {
        TreePath parentPath = tree.getSelectionPath();
        DefaultMutableTreeNode parentNode = null;

        if (parentPath != null) {
            parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
        }

        if (parentNode == null) {
            parentNode = rootNode;
        }
        if (parentNode.getUserObject() instanceof SessionInfo) {
            parentNode = (DefaultMutableTreeNode) parentNode.getParent();
        }

        new ImportDlg(listener.getWindow(), cmbImports.getSelectedIndex(), parentNode).setVisible(true);
        treeModel.nodeStructureChanged(parentNode);
    }

    public TreePath updateName(TreePath parentPath) {
        if (parentPath == null) {
            parentPath = tree.getSelectionPath();
        }
        DefaultMutableTreeNode parentNode = null;

        if (parentPath != null) {
            parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
            if (parentNode != null) {
                treeModel.nodeChanged(parentNode);
            }
        }

        return parentPath;
    }

    @Override
    public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
        System.out.println("value changed");
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (node == null)
            // Nothing is selected.
            return;

        listener.treeValueChanged(node.getUserObject());
    }

    public void save() {
        String id = null;
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            NamedItem item = (NamedItem) node.getUserObject();
            id = item.getId();
        }
        SessionStore.save(SessionStore.convertModelFromTree(rootNode), id);
    }
}
