package graphic;

import data.Person;
import data.Things;
import inet.client.Connector;
import managers.InternetCollectionManager;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static inet.client.Connector.createConnector;

/**
 * Created by azamat on 09.10.17.
 */

public class GUI {
    private ArrayList<JComponent> mainColorComponents;
    private InternetCollectionManager collectionManager;
    private Color buttonsColor;
    private CardLayout cardLayout;
    private JFrame mainFrame;
    private JPanel mainPanel;
    private JPanel leftPanel;
    private JImagePanel connectionPanel;
    private JImagePanel mainMenuPanel;
    private JImagePanel commandsPanel;
    private JScrollPane scrollThingsTree;
    private volatile JTree thingsTree;
    private JImagePanel importPanel;
    private DefaultMutableTreeNode selectedNode;
    private JTextField nameField;
    private JTextField ownersNameField;
    private JTextField courageField;
    private JPanel colorPanel;
    private JColorChooser colorChooser;
    private volatile boolean collectionChanged;
    private volatile String panelName;
    private volatile String lastPanelName;

    public GUI() {
        this.collectionChanged = false;
        this.buttonsColor = Color.YELLOW;
        this.colorChooser = new JColorChooser();
        this.mainColorComponents = new ArrayList<>();
        makeGUI();
    }

    private void makeGUI() {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (collectionManager != null) collectionManager.close();
        }));


        //create main Frame
        this.panelName = "Connection panel";
        this.mainFrame = new JFrame("App");
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setResizable(false);

        //create menu bar
        JMenuBar jMenuBar = new JMenuBar();

        JMenu jmFile = new JMenu("File");
        JMenuItem jmiExit = new JMenuItem(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        jmFile.add(jmiExit);

        JMenu jmView = new JMenu("View");
        JMenuItem jmiChooseColor = new JMenuItem(new AbstractAction("Choose color...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "Color panel");
                logPanelChange("Color panel");
            }
        });
        jmView.add(jmiChooseColor);

        JMenu jmHelp = new JMenu("Help");
        JMenuItem jmiSupport = new JMenuItem(new AbstractAction("Support") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(mainFrame, "Support e-mail: azamat.tadeev.support@mail.ru");
            }
        });
        jmHelp.add(jmiSupport);

        jMenuBar.add(jmFile);
        jMenuBar.add(jmView);
        jMenuBar.add(jmHelp);

        mainFrame.setJMenuBar(jMenuBar);

        //create main panel
        this.mainPanel = new JPanel();
        this.cardLayout = new CardLayout();
        this.mainPanel.setLayout(this.cardLayout);
        this.mainFrame.add(mainPanel);

        mainFrame.add(mainPanel);



        //create connection panel
        this.connectionPanel = new JImagePanel();
        try {
            connectionPanel.setBgImage(ImageIO.read(new File("images/connection.jpeg")));
        } catch (IIOException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Images isn't found!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Images isn't accessible!");
        }
        JLabel ipLabel = new JLabel("IP-address");
        ipLabel.setBounds(10, 10, 90, 20);
        JLabel portLabel = new JLabel("Port");
        portLabel.setBounds(10, 40, 90, 20);
        JTextField ipField = new JTextField(15);
        ipField.setBounds(110, 10, 120, 25);
        JTextField portField = new JTextField(4);
        portField.setBounds(110, 40, 120, 25);
        JButton connectButton = new JButton("Connect");
        connectButton.setBounds(75, 70, 100, 25);
        JPanel connectionInnerPanel = new JPanel();
        connectionInnerPanel.setBackground(buttonsColor);
        mainColorComponents.add(connectionInnerPanel);
        connectionInnerPanel.setBounds(277, 200, 250, 105);
        connectionInnerPanel.setLayout(null);
        connectionPanel.setLayout(null);
        connectionPanel.add(connectionInnerPanel);
        connectionInnerPanel.add(ipField);
        connectionInnerPanel.add(ipLabel);
        connectionInnerPanel.add(portLabel);
        connectionInnerPanel.add(portField);
        connectionInnerPanel.add(connectButton);
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(()->{
                try {
                    //88.201.132.194
                    InetAddress serverIP;
                    String textIP = ipField.getText().trim();
                    if (!Connector.isValidIP(textIP)) {
                        JOptionPane.showMessageDialog(mainFrame, "IP-address has incorrect format");
                        return;
                    }
                    if ("localhost".equals(textIP)) {
                        serverIP = InetAddress.getLocalHost();
                    } else {
                        serverIP = InetAddress.getByName(textIP);
                    }
                    int port;
                    try {
                        port = Integer.parseInt(portField.getText());
                    } catch (NumberFormatException nfEx) {
                        JOptionPane.showMessageDialog(mainFrame, "Port is integer number!");
                        return;
                    }
                    Connector connector = createConnector(serverIP, port);
                    if (connector != null) {
                        collectionManager = new InternetCollectionManager(connector);
                        collectionManager.selectAll();
                        makeOtherGUI();
                        cardLayout.show(mainPanel, "Main menu panel");
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Error. Server isn't available. Check IP-address and port or try again.");
                    }
                } catch (UnknownHostException uhEx) {
                    JOptionPane.showMessageDialog(mainFrame, "Server not found. Check IP-address");
                }
            }).start();
            }
        });
        mainPanel.add(connectionPanel, "Connection panel");

        //add choose color panel
        this.colorPanel = new JPanel();
        colorPanel.setLayout(new BorderLayout());
        mainPanel.add(colorPanel, "Color panel");
        colorPanel.add(colorChooser,BorderLayout.CENTER);
        JButton setColorButton = new JButton("OK");
        JButton backFromColorChooser = new JButton("Back");
        JPanel colorButtonsPanel = new JPanel();
        colorPanel.add(colorButtonsPanel, BorderLayout.SOUTH);
        colorButtonsPanel.add(backFromColorChooser);
        colorButtonsPanel.add(setColorButton);
        backFromColorChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel,lastPanelName);
                logPanelChange(lastPanelName);
            }
        });
        setColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonsColor = colorChooser.getColor();
                for(int i = 0; i <= mainColorComponents.size() - 1; i++){
                    mainColorComponents.get(i).setBackground(buttonsColor);
                }
            }
        });
        mainFrame.setVisible(true);
    }


    private void makeOtherGUI(){
        //create main menu panel
        this.mainMenuPanel = new JImagePanel();
        try {
            try {
                mainMenuPanel.setBgImage(ImageIO.read(new File("images/slippers.jpg")));
            } catch (IIOException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Images isn't found");
            }
            JButton disconnectButton = new JButton("DISCONNECT");
            disconnectButton.setBackground(buttonsColor);
            JButton goToCommands = new JButton("START WORK");
            goToCommands.setPreferredSize(new Dimension(200, 70));
            disconnectButton.setPreferredSize(new Dimension(200,70));
            goToCommands.setBackground(this.buttonsColor);
            mainColorComponents.add(goToCommands);
            mainColorComponents.add(disconnectButton);
            disconnectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    collectionManager.close();
                    collectionManager = null;
                    cardLayout.show(mainPanel, "Connection panel");
                }
            });
            goToCommands.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cardLayout.show(mainPanel, "Commands panel");
                    logPanelChange("Commands panel");
                }
            });
            FlowLayout mainMenuPanelLayout = (FlowLayout) mainMenuPanel.getLayout();
            mainMenuPanelLayout.setHgap(300);
            mainMenuPanel.add(goToCommands);
            mainMenuPanel.add(disconnectButton);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.mainPanel.add(mainMenuPanel, "Main menu panel");

        //create commands panel
        this.commandsPanel = new JImagePanel();
        try {
            commandsPanel.setBgImage(ImageIO.read(new File("images/stationery.jpg")));
        }catch (IIOException ex){
            JOptionPane.showMessageDialog(mainFrame,"Images aren't found");
        }catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame,"Images aren't accessible");
        }
        GridLayout gridLayout = new GridLayout(1, 2);
        commandsPanel.setLayout(gridLayout);
        this.leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        JPanel rightPanel = new JPanel();
        this.thingsTree = getThisJTree();
        this.scrollThingsTree = new JScrollPane(thingsTree);
        leftPanel.add(scrollThingsTree);
        leftPanel.setOpaque(false);
        rightPanel.setOpaque(false);
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton deleteLowerButton = new JButton("Delete lower");
        addButton.setBackground(buttonsColor);
        updateButton.setBackground(buttonsColor);
        deleteButton.setBackground(buttonsColor);
        deleteLowerButton.setBackground(buttonsColor);
        mainColorComponents.add(addButton);
        mainColorComponents.add(updateButton);
        mainColorComponents.add(deleteButton);
        mainColorComponents.add(deleteLowerButton);
        addButton.setPreferredSize(new Dimension(150, 40));
        updateButton.setPreferredSize(new Dimension(150, 40));
        deleteButton.setPreferredSize(new Dimension(150, 40));
        deleteLowerButton.setPreferredSize(new Dimension(150, 40));
        rightPanel.setLayout(new FlowLayout(0, 40, 20));
        rightPanel.add(addButton);
        rightPanel.add(updateButton);
        rightPanel.add(deleteButton);
        rightPanel.add(deleteLowerButton);
        JPanel inputPanel = new JPanel();
        inputPanel.setPreferredSize(new Dimension(300, 80));
        inputPanel.setBackground(buttonsColor);
        mainColorComponents.add(inputPanel);
        JLabel nameLabel = new JLabel("Name");
        nameField = new JTextField(10);
        String[] variants = new String[3];
        JLabel ownersNameLabel = new JLabel("Owner's name");
        ownersNameField = new JTextField(10);
        JLabel courageLabel = new JLabel("Owner's courage");
        courageField = new JTextField(10);
        nameLabel.setOpaque(false);
        ownersNameLabel.setOpaque(false);
        courageLabel.setOpaque(false);
        JPanel firstPanel = new JPanel(new GridLayout());
        JPanel secondPanel = new JPanel(new GridLayout());
        JPanel thirdPanel = new JPanel(new GridLayout());
        JPanel fourthPanel = new JPanel(new GridLayout());
        firstPanel.setOpaque(false);
        secondPanel.setOpaque(false);
        thirdPanel.setOpaque(false);
        fourthPanel.setOpaque(false);
        inputPanel.add(firstPanel);
        inputPanel.add(secondPanel);
        inputPanel.add(thirdPanel);
        inputPanel.add(fourthPanel);
        firstPanel.add(nameLabel);
        firstPanel.add(nameField);
        thirdPanel.add(ownersNameLabel);
        thirdPanel.add(ownersNameField);
        fourthPanel.add(courageLabel);
        fourthPanel.add(courageField);
        rightPanel.add(inputPanel);

        JButton infoButton = new JButton("Info");
        JButton importButton = new JButton("Import");
        JButton sortButton = new JButton("Sort");
        JButton filterButton = new JButton("Filter");
        JButton notesButton = new JButton("Notes");
        JButton updateDataButton = new JButton(("Update data"));
        JButton backButton = new JButton("Back to menu");
        infoButton.setBackground(buttonsColor);
        importButton.setBackground(buttonsColor);
        sortButton.setBackground(buttonsColor);
        filterButton.setBackground(buttonsColor);
        backButton.setBackground(buttonsColor);
        updateDataButton.setBackground(buttonsColor);
        notesButton.setBackground(buttonsColor);
        mainColorComponents.add(infoButton);
        mainColorComponents.add(importButton);
        mainColorComponents.add(sortButton);
        mainColorComponents.add(filterButton);
        mainColorComponents.add(backButton);
        mainColorComponents.add(updateDataButton);
        mainColorComponents.add(notesButton);
        infoButton.setPreferredSize(new Dimension(150, 40));
        importButton.setPreferredSize(new Dimension(150, 40));
        sortButton.setPreferredSize(new Dimension(150, 40));
        filterButton.setPreferredSize(new Dimension(150, 40));
        notesButton.setPreferredSize(new Dimension(150,40));
        updateDataButton.setPreferredSize(new Dimension(150, 40));
        backButton.setPreferredSize(new Dimension(340, 40));
        rightPanel.add(infoButton);
        rightPanel.add(importButton);
        rightPanel.add(sortButton);
        rightPanel.add(filterButton);
        rightPanel.add(notesButton);
        rightPanel.add(updateDataButton);
        rightPanel.add(backButton);
        commandsPanel.add(leftPanel);
        commandsPanel.add(rightPanel);
        notesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel,"Notes panel");
                logPanelChange("Notes panel");
            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "Main menu panel");
            }
        });

        //set add button actions
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e){
                Thread addThread = new Thread() {
                    public void run() {
                        String name = nameField.getText();
                        String ownerName = ownersNameField.getText();
                        if (courageField.getText().length() == 0 || name.length() == 0 || ownerName.length() == 0) {
                            JOptionPane.showMessageDialog(mainFrame, "You don't write all values");
                        } else {
                            try {
                                int courage = Integer.parseInt(courageField.getText());
                                String msg = collectionManager.add(new Things(name, new Person(ownerName, courage)));
                                JOptionPane.showMessageDialog(mainFrame, msg);
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(mainFrame, "The courage value must be integer number");
                            }
                        }
                        collectionChanged = true;
                    }
                };
                addThread.start();
            }
        });

        //set info actions
        infoButton.addActionListener((ActionEvent e) -> {
                JOptionPane.showMessageDialog(mainFrame, collectionManager.info());
        });

        //set delete actions
        deleteButton.addActionListener((ActionEvent e)-> {
            Thread deleteThread = new Thread() {
                public void run() {
                    String name = nameField.getText();
                    String ownerName = ownersNameField.getText();
                    if (courageField.getText().

                            length() == 0 || name.length() == 0 || ownerName.length() == 0)

                    {
                        JOptionPane.showMessageDialog(mainFrame, "You don't write all values");
                    } else

                    {
                        try {
                            int courage = Integer.parseInt(courageField.getText());
                            String msg = collectionManager.remove(new Things(name, new Person(ownerName, courage)));
                            JOptionPane.showMessageDialog(mainFrame, msg);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(mainFrame, "The courage value must be integer number");
                        }
                    }
                    collectionChanged = true;
                }
            };
            deleteThread.start();
        });

        deleteLowerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread deleteLowerThread = new Thread() {
                    public void run() {

                        String name = nameField.getText();
                        String ownerName = ownersNameField.getText();
                        if (courageField.getText().

                                length() == 0 || name.length() == 0 || ownerName.length() == 0)

                        {
                            JOptionPane.showMessageDialog(mainFrame, "You don't write all values");
                        } else {
                            try {
                                int courage = Integer.parseInt(courageField.getText());
                                String msg = collectionManager.removeLower(new Things(name, new Person(ownerName, courage)));
                                JOptionPane.showMessageDialog(mainFrame, msg);
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(mainFrame, "The courage value must be integer number");
                            }
                        }
                        collectionChanged = true;
                    }
                };
                deleteLowerThread.start();
            }
        });
        this.mainPanel.add(commandsPanel, "Commands panel");

        //create filter panel
        JImagePanel filterPanel = new JImagePanel();
        try {
            filterPanel.setBgImage(ImageIO.read(new File("images/things.png")));
        }catch (IIOException ex){
            JOptionPane.showMessageDialog(mainFrame,"Images aren't found");
        }catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame,"Images aren't accessible");
        }
        filterPanel.setLayout(null);
        mainPanel.add(filterPanel,"Filter panel");
        JButton backFromFilterButton = new JButton("Back");
        backFromFilterButton.setBackground(buttonsColor);
        mainColorComponents.add(backFromFilterButton);
        filterPanel.add(backFromFilterButton);
        backFromFilterButton.setBounds(0,0,100,40);
        JButton showResultsFilterButton = new JButton("Show results");
        showResultsFilterButton.setBackground(buttonsColor);
        mainColorComponents.add(showResultsFilterButton);
        filterPanel.add(showResultsFilterButton);
        showResultsFilterButton.setBounds(340,350,140,40);
        JPanel filterValuesPanel = new JPanel();
        filterValuesPanel.setBackground(buttonsColor);
        mainColorComponents.add(filterValuesPanel);
        filterPanel.add(filterValuesPanel);
        filterValuesPanel.setLayout(new GridLayout(1,2));
        filterValuesPanel.setBounds(220,200,380,100);
        JPanel filterValuesLeftPanel = new JPanel();
        JPanel filterValuesRightPanel = new JPanel();
        filterValuesLeftPanel.setLayout(new GridLayout(3,1));
        filterValuesRightPanel.setLayout(new GridLayout(3,1));
        filterValuesLeftPanel.setBackground(buttonsColor);
        filterValuesRightPanel.setBackground(buttonsColor);
        mainColorComponents.add(filterValuesLeftPanel);
        mainColorComponents.add(filterValuesRightPanel);
        filterValuesPanel.add(filterValuesLeftPanel);
        filterValuesPanel.add(filterValuesRightPanel);
        filterValuesLeftPanel.setLayout(new GridLayout(4,1));
        filterValuesLeftPanel.add(new JLabel("Name"));
        filterValuesLeftPanel.add(new JLabel("Owner's name"));
        filterValuesLeftPanel.add( new JLabel("Owner's courage"));
        JTextField nameFilterField = new JTextField(12);
        JTextField ownerNameFilterField = new JTextField(16);
        JPanel miniPanel = new JPanel();
        JTextField ownerCourageFilterField = new JTextField(8);
        String[] ranges = {"=",">","<"};
        JComboBox<String> range = new JComboBox<>(ranges);
        miniPanel.add(ownerCourageFilterField);
        miniPanel.add(range);
        filterValuesRightPanel.add(nameFilterField);
        filterValuesRightPanel.add(ownerNameFilterField);
        filterValuesRightPanel.add(miniPanel);

        //add show results filter button actions
        showResultsFilterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String startOfName = nameFilterField.getText();
                String startOfOwnerName = ownerNameFilterField.getText();
                Integer ownerCourage;
                String compareAction = (String) range.getSelectedItem();
                try {
                    if (ownerCourageFilterField.getText().equals("")) {
                        ownerCourage = null;
                    } else {
                        ownerCourage = new Integer(Integer.parseInt(ownerCourageFilterField.getText()));
                    }
                    TreeSet<Things> thingsTreeSet = collectionManager.getTreeSetCopy();
                    Iterator<Things>  iterator = thingsTreeSet.iterator();
                    LinkedList<Things> forRemove = new LinkedList<>();
                    while(iterator.hasNext()){
                        Things thing = iterator.next();
                        if (startOfName.length() > thing.getName().length()){
                            forRemove.add(thing);
                            continue;
                        }
                        if (startOfOwnerName.length() > thing.getOwner().getName().length()){
                            forRemove.add(thing);
                            continue;
                        }
                        String startOfNameForThis = "";
                        String startOfOwnerNameForThis = "";
                        if (startOfName.length() > 0) startOfNameForThis = thing.getName().substring(0,startOfName.length());
                        if (startOfOwnerName.length() > 0) startOfOwnerNameForThis = thing.getOwner().getName().substring(0,startOfOwnerName.length());
                        if (!startOfName.equals(startOfNameForThis) || !startOfOwnerName.equals(startOfOwnerNameForThis)){
                            forRemove.add(thing);
                            continue;
                        }
                        boolean needDelete = false;
                        if (ownerCourage != null){
                            switch (compareAction){
                                case "=":
                                    needDelete = ownerCourage != thing.getOwner().getCourage();
                                    break;
                                case ">":
                                    needDelete = ownerCourage >= thing.getOwner().getCourage();
                                    break;
                                case "<":
                                    needDelete = ownerCourage <= thing.getOwner().getCourage();
                                    break;
                            }
                        }
                        if (needDelete){
                            forRemove.add(thing);
                            continue;
                        }
                    }
                    thingsTreeSet.removeAll(forRemove);
                    Object[] objects = thingsTreeSet.toArray();
                    Things[] thingsForFilter = new Things[objects.length];
                    int count = 0;
                    for(Object ob : objects){
                        thingsForFilter[count++] = (Things) ob;
                    }
                    JTree filteredJTree = getThingsJTree(thingsForFilter);
                    JScrollPane scrollFilteredJTree = new JScrollPane(filteredJTree);
                    JImagePanel filterResultPanel = new JImagePanel();
                    try{
                        filterResultPanel.setBgImage(ImageIO.read(new File("images/dino.jpeg")));
                    }catch (IIOException ex){
                        JOptionPane.showMessageDialog(mainFrame,"Images aren't found");
                    }catch (IOException ex) {
                        JOptionPane.showMessageDialog(mainFrame,"Images aren't accessible");
                    }
                    JButton backToFilterButton = new JButton("Back");
                    backToFilterButton.setBackground(buttonsColor);
                    mainColorComponents.add(backToFilterButton);
                    filterResultPanel.setLayout(null);
                    backToFilterButton.setBounds(0,0,100,40);
                    filterResultPanel.add(backToFilterButton);
                    backToFilterButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            cardLayout.show(mainPanel, "Filter panel");
                            logPanelChange("Filter panel");
                        }
                    });
                    //backToFilterButton.setBounds(0,0,100,40);
                    scrollFilteredJTree.setBounds(275,60,250,450);
                    filterResultPanel.add(scrollFilteredJTree);
                    //filteredJTree.setBounds();

                    mainPanel.add(filterResultPanel,"Filter result panel");
                    cardLayout.show(mainPanel,"Filter result panel");
                    logPanelChange("Filter result panel");
                }catch (NumberFormatException ex){
                    JOptionPane.showMessageDialog(mainFrame,"Courage must be integer number");
                }
            }
        });


        backFromFilterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "Commands panel");
                logPanelChange("Commands panel");
            }
        });

        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "Filter panel");
                logPanelChange("Filter panel");
            }
        });

        //create sort page
        JImagePanel sortingPanel = new JImagePanel();
        try {
            sortingPanel.setBgImage(ImageIO.read(new File("images/blade.jpg")));
        }catch (IIOException ex){
            JOptionPane.showMessageDialog(mainFrame,"Images aren't found");
        }catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame,"Images aren't accessible");
        }
        mainPanel.add(sortingPanel, "Sorting panel");
        sortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "Sorting panel");
                logPanelChange("Sorting panel");
            }
        });
        JButton backFromSortingButton = new JButton("Back");
        backFromSortingButton.setBackground(buttonsColor);
        backFromSortingButton.setBounds(0,0,100,40);
        mainColorComponents.add(backFromSortingButton);
        sortingPanel.add(backFromSortingButton);
        sortingPanel.setLayout(null);
        JPanel sortingOptionsPanel = new JPanel();
        sortingOptionsPanel.setBackground(buttonsColor);
        mainColorComponents.add(sortingOptionsPanel);
        sortingOptionsPanel.setBounds(200,200,400,100);
        sortingPanel.add(sortingOptionsPanel);
        sortingOptionsPanel.setLayout(null);
        JLabel sortOnValueLabel = new JLabel("Sort on ");
        sortOnValueLabel.setBounds(20,15,100,30);
        String[] fields = {"name","owner's name","owner's courage"};
        JComboBox<String> sortValuesComboBox = new JComboBox<>(fields);
        sortValuesComboBox.setBounds(140,20,200,20);
        JButton startSortButton = new JButton("Sort");
        startSortButton.setBounds(150,60,100,30);
        sortingOptionsPanel.add(sortOnValueLabel);
        sortingOptionsPanel.add(sortValuesComboBox);
        sortingOptionsPanel.add(startSortButton);
        backFromSortingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "Commands panel");
                logPanelChange("Commands panel");
            }
        });

        startSortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] objects = collectionManager.getTreeSetCopy().toArray();
                Things[] noSortedThings = new Things[objects.length];
                for (int i = 0; i < objects.length; i++) {
                    noSortedThings[i] = (Things) objects[i];
                }
                Things[] sortedThings = new Things[noSortedThings.length];
                String fieldForSort = (String) sortValuesComboBox.getSelectedItem();
                switch (fieldForSort) {
                    case "name":
                        String[] names = new String[noSortedThings.length];
                        for (int i = 0; i < noSortedThings.length; i++) {
                            names[i] = noSortedThings[i].getName();
                        }
                        Arrays.sort(names);
                        for (int i = 0; i < names.length; i++) {
                            for (int j = i + 1; j < names.length; j++) {
                                if (names[i].equals(names[j])) {
                                    names[j] = null;
                                }
                            }
                        }
                        int thingsCount = 0;
                        for (String name : names) {
                            if (name != null) {
                                for (Things thing : noSortedThings) {
                                    if (name.equals(thing.getName())) {
                                        sortedThings[thingsCount++] = thing;
                                    }
                                }
                            }
                        }
                        break;
                    case "owner's name":
                        String[] ownerNames = new String[noSortedThings.length];
                        for (int i = 0; i < noSortedThings.length; i++) {
                            ownerNames[i] = noSortedThings[i].getOwner().getName();
                        }
                        Arrays.sort(ownerNames);
                        for (int i = 0; i < ownerNames.length; i++) {
                            for (int j = i + 1; j < ownerNames.length; j++) {
                                if(ownerNames[i] != null) {
                                    if (ownerNames[i].equals(ownerNames[j])) {
                                        ownerNames[j] = null;
                                    }
                                }
                            }
                        }
                        int thingsCount1 = 0;
                        for (String name : ownerNames) {
                            if (name != null) {
                                for (Things thing : noSortedThings) {
                                    if (name.equals(thing.getOwner().getName())) {
                                        sortedThings[thingsCount1++] = thing;
                                    }
                                }
                            }
                        }
                        break;
                    case "owner's courage":
                        int thingsCount3 = 0;
                        Integer[] courages = new Integer[noSortedThings.length];
                        for(int i = 0; i < noSortedThings.length; i++){
                            courages[i] = noSortedThings[i].getOwner().getCourage();
                        }
                        Arrays.sort(courages);
                        for(int i = 0; i < courages.length; i++){
                            for (int j = i + 1; j < courages.length; j++) {
                                if (courages[i] != null) {
                                    if (courages[i].equals(courages[j])) courages[j] = null;
                                }
                            }
                        }
                        for(Integer courage : courages){
                            if(courage != null){
                                for(Things thing : noSortedThings){
                                    if (courage.equals(thing.getOwner().getCourage())){
                                        sortedThings[thingsCount3++] = thing;
                                    }
                                }
                            }
                        }
                }
                JTree sortedJTree = getThingsJTree(sortedThings);
                JScrollPane scrollSortedJTree = new JScrollPane(sortedJTree);
                scrollSortedJTree.setBounds(250,60,300,450);
                JImagePanel sortingResultPanel = new JImagePanel();
                try {
                    sortingResultPanel.setBgImage(ImageIO.read(new File("images/earphones.jpg")));
                }catch (IIOException ex){
                    JOptionPane.showMessageDialog(mainFrame,"Images aren't found");
                }catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainFrame,"Images aren't accessible");
                }
                sortingResultPanel.setLayout(null);
                mainPanel.add(sortingResultPanel,"Sorting result panel");
                JButton backFromSortingResultButton = new JButton("Back");
                backFromSortingResultButton.setBackground(buttonsColor);
                mainColorComponents.add(backFromSortingResultButton);
                backFromSortingResultButton.setBounds(0,0,100,40);
                backFromSortingResultButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cardLayout.show(mainPanel,"Sorting panel");
                        logPanelChange("Sorting panel");
                    }
                });
                sortingResultPanel.add(backFromSortingResultButton);
                sortingResultPanel.add(scrollSortedJTree);
                cardLayout.show(mainPanel,"Sorting result panel");
                logPanelChange("Sorting result panel");
            }
        });

        backFromSortingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "Commands panel");
                logPanelChange("Commands panel");
            }
        });

        //create import page
        this.importPanel = new JImagePanel();
        try {
            importPanel.setBgImage(ImageIO.read(new File("images/chest.jpg")));
        }catch (IIOException ex){
            JOptionPane.showMessageDialog(mainFrame,"Images aren't found");
        }catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame,"Images aren't accessible");
        }
        importPanel.setLayout(null);
        JButton backFromImportButton = new JButton("Back");
        backFromImportButton.setBackground(buttonsColor);
        mainColorComponents.add(backFromImportButton);
        importPanel.add(backFromImportButton);
        backFromImportButton.setBounds(0,0,100,40);
        JPanel importFieldPanel = new JPanel();
        importFieldPanel.setBackground(buttonsColor);
        mainColorComponents.add(importFieldPanel);
        importPanel.add(importFieldPanel);
        importFieldPanel.setBounds(160,210,500,40);
        JTextField importPathField = new JTextField(30);
        JButton doImportButton = new JButton("Import");
        importFieldPanel.add(importPathField);
        importFieldPanel.add(doImportButton);
        doImportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread importThread = new Thread() {
                    @Override
                    public void run() {
                        JProgressBar progressBar = new JProgressBar();
                        progressBar.setIndeterminate(true);
                        progressBar.setBounds(330,300,140,15);
                        importPanel.add(progressBar);
                        InputStream inpStr = getFileInputStream(new File(importPathField.getText()));
                        if (inpStr != null)

                        {
                            JOptionPane.showMessageDialog(mainFrame, collectionManager.doImport(inpStr));
                            importPathField.setText("");
                        }
                        importPanel.remove(progressBar);
                        importPanel.revalidate();
                        importPanel.repaint();
                        collectionChanged = true;
                    }
                };
                importThread.start();
            }
        });
        backFromImportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel,"Commands panel");
                logPanelChange("Commands panel");
            }
        });
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel,"Import panel");
                logPanelChange("Import panel");
            }
        });
        this.mainPanel.add(importPanel,"Import panel");


        //add actions for update button
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread updateThread = new Thread() {
                    public void run() {
                        DefaultMutableTreeNode node = getSelectedNode();
                        if (node != null) {
                            String name = (String) node.getUserObject();
                            String ownerName = (String) ((DefaultMutableTreeNode) ((DefaultMutableTreeNode) node.getChildAt(0)).getFirstChild()).getUserObject();
                            Integer ownerCourage = (Integer) ((DefaultMutableTreeNode) ((DefaultMutableTreeNode) node.getChildAt(1)).getFirstChild()).getUserObject();
                            Things forRemove = new Things(name, new Person(ownerName, ownerCourage));
                            name = nameField.getText();
                            ownerName = ownersNameField.getText();
                            if (courageField.getText().length() == 0 || name.length() == 0 || ownerName.length() == 0) {
                                JOptionPane.showMessageDialog(mainFrame, "You don't write all values");
                            } else {
                                try {
                                    int courage = Integer.parseInt(courageField.getText());
                                    Things forAdd = new Things(name, new Person(ownerName, courage));
                                    JOptionPane.showMessageDialog(mainFrame, collectionManager.update(forRemove, forAdd));
                                    nameField.setText("");
                                    courageField.setText("");
                                    ownersNameField.setText("");
                                } catch (NumberFormatException ex) {
                                    JOptionPane.showMessageDialog(mainFrame, "The courage value must be integer number");
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(mainFrame, "You don't chose thing");
                        }
                        collectionChanged = true;
                    }
                };
                updateThread.start();
            }
        });

        JPanel notesPanel = new JPanel();
        notesPanel.setLayout(new BorderLayout());
        JButton backFromNotes = new JButton("Back");
        backFromNotes.setBackground(buttonsColor);
        mainColorComponents.add(backFromNotes);
        notesPanel.add(backFromNotes, BorderLayout.NORTH);
        mainPanel.add(notesPanel, "Notes panel");
        JEditorPane jEditorPane = new JEditorPane();
        notesPanel.add(jEditorPane,BorderLayout.CENTER);
        backFromNotes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel,"Commands panel");
                logPanelChange("Commands panel");
            }
        });

        //add update set update data actions
        updateDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                collectionChanged = true;
            }
        });


        new JTreeUpdater();
    }

    private FileInputStream getFileInputStream(File file){
        try{
            if (!file.exists()) throw new FileNotFoundException("File not found");
            if (!file.isFile()) throw new FileNotFoundException("This isn't a file");
            if (!file.canRead()) throw new FileNotFoundException("File can't be read");
            return new FileInputStream(file);
        }catch (FileNotFoundException ex){
            JOptionPane.showMessageDialog(mainFrame, ex.getMessage());
        }
        return null;
    }

    private JTree getThisJTree(){
        collectionManager.selectAll();
        Things[] thingsArray = collectionManager.getCollectionArray();
        return getThingsJTree(thingsArray);
    }

    private JTree getThingsJTree(Things[] thingsArray){
        DefaultMutableTreeNode thingsNode = new DefaultMutableTreeNode("Things");
        for (Things thing:
             thingsArray) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(thing.getName());
            thingsNode.add(node);
            DefaultMutableTreeNode ownerName = new DefaultMutableTreeNode("Owner's name");
            ownerName.add(new DefaultMutableTreeNode(thing.getOwner().getName()));
            DefaultMutableTreeNode ownerCourage = new DefaultMutableTreeNode("Owner's courage");
            ownerCourage.add(new DefaultMutableTreeNode(thing.getOwner().getCourage()));
            node.add(ownerName);
            node.add(ownerCourage);
        }
        JTree rezTree = new JTree(thingsNode);
        rezTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath treePath = e.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                if(node.getParent() != null) {
                    while (!((DefaultMutableTreeNode) node.getParent()).isRoot()) {
                        node = (DefaultMutableTreeNode) node.getParent();
                    }
                    setSelectedNode(node);
                    String name = (String) node.getUserObject();
                    String ownerName = ((DefaultMutableTreeNode) ((DefaultMutableTreeNode) node.getChildAt(0)).getFirstChild()).getUserObject().toString();
                    Integer ownerCourage = (Integer) ((DefaultMutableTreeNode) ((DefaultMutableTreeNode) node.getChildAt(1)).getFirstChild()).getUserObject();
                    setInputPanel(name, ownerName, ownerCourage);
                }else{
                    setInputPanel(null,null,0);
                }
            }
        });
        return rezTree;
    }

    private void logPanelChange(String panel){
        this.lastPanelName = this.panelName;
        this.panelName = panel;
    }

    JTree getThingsTree(){
        return this.thingsTree;
    }

    void setThingsTree(JTree jTree){
        this.thingsTree = jTree;
    }

    DefaultMutableTreeNode getSelectedNode(){
        return this.selectedNode;
    }

    void setSelectedNode(DefaultMutableTreeNode newNode){
        this.selectedNode = newNode;
    }

    void setInputPanel(String name, String ownerName, int courage){
        if (name != null && ownerName != null) {
            nameField.setText(name);
            ownersNameField.setText(ownerName);
            courageField.setText(courage + "");
        }else{
            nameField.setText("");
            ownersNameField.setText("");
            courageField.setText("");
        }
    }

    JColorChooser getColorChooser(){
        return this.colorChooser;
    }

    JPanel getColorPanel(){
        return colorPanel;
    }

    class JTreeUpdater extends Thread{

        JTreeUpdater(){
            this.setDaemon(true);
            this.start();
        }

        @Override
        public void run(){
            for(;;){
                if(collectionChanged) {
                    leftPanel.remove(scrollThingsTree);
                    scrollThingsTree.remove(thingsTree);
                    setThingsTree(getThisJTree());
                    scrollThingsTree = new JScrollPane(thingsTree);
                    leftPanel.add(scrollThingsTree);
                    setThingsTree(getThisJTree());
                    leftPanel.revalidate();
                    leftPanel.repaint();
                    scrollThingsTree.add(thingsTree);
                    collectionChanged = false;
                    try {
                        Thread.sleep(100);
                    }catch (InterruptedException ex){
                        System.out.println("Interrupted exception in JTreeUpdater");
                    }
                }
            }
        }

    }


}
