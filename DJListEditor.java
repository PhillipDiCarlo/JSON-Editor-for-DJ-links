import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class DJListEditor extends JFrame {
    private JTextArea displayArea;
    private JComboBox<String> typeComboBox;
    private JTextField djNameField;
    private JTextField questLinkField;
    private JTextField nonQuestLinkField;
    private JFileChooser fileChooser;
    private JSONObject jsonObject;

    public DJListEditor() {
        super("DJ List Editor");
        initializeComponents();
        jsonObject = new JSONObject();
        jsonObject.put("DJs", new JSONArray());
        jsonObject.put("VJs", new JSONArray());
    }

    private void initializeComponents() {
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        fileChooser = new JFileChooser();

        JScrollPane scrollPane = new JScrollPane(displayArea);
        this.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2, 10, 10));

        typeComboBox = new JComboBox<>(new String[]{"DJ", "VJ"});
        inputPanel.add(new JLabel("Type:"));
        inputPanel.add(typeComboBox);

        inputPanel.add(new JLabel("DJ Name:"));
        djNameField = new JTextField();
        inputPanel.add(djNameField);

        inputPanel.add(new JLabel("Quest Link:"));
        questLinkField = new JTextField();
        inputPanel.add(questLinkField);

        inputPanel.add(new JLabel("Non-Quest Link:"));
        nonQuestLinkField = new JTextField();
        inputPanel.add(nonQuestLinkField);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addEntry());

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");

        openItem.addActionListener(e -> openFile());
        saveItem.addActionListener(e -> saveFile());

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        menuBar.add(fileMenu);
        this.setJMenuBar(menuBar);

        this.setLayout(new BorderLayout());
        this.add(inputPanel, BorderLayout.NORTH);
        this.add(addButton, BorderLayout.SOUTH);

        this.setSize(800, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void openFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                JSONParser parser = new JSONParser();
                jsonObject = (JSONObject) parser.parse(new FileReader(file));
                displayJsonContent();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to open file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void displayJsonContent() {
        StringBuilder contentBuilder = new StringBuilder();
        for (Object key : jsonObject.keySet()) {
            contentBuilder.append(key).append(": ").append(jsonObject.get(key)).append("\n");
        }
        displayArea.setText(contentBuilder.toString());
    }

    private void addEntry() {
        String type = (String) typeComboBox.getSelectedItem();
        String djName = djNameField.getText();
        String questLink = questLinkField.getText();
        String nonQuestLink = nonQuestLinkField.getText();

        // Link conversion logic
        String convertedNonQuestLink = nonQuestLink;
        String convertedQuestLink = questLink;
        if (questLink.matches("https://stream\\.vrcdn\\.live/live/.*\\.live\\.ts")) {
            convertedNonQuestLink = questLink.replaceFirst("https://stream\\.vrcdn\\.live/live/(.*)\\.live\\.ts", "rtspt://stream.vrcdn.live/live/$1");
        } else if (nonQuestLink.matches("rtspt://stream\\.vrcdn\\.live/live/.*")) {
            convertedQuestLink = nonQuestLink.replaceFirst("rtspt://stream\\.vrcdn\\.live/live/(.*)", "https://stream.vrcdn.live/live/$1.live.ts");
        }

        JSONObject newEntry = new JSONObject();
        newEntry.put("DJ_Name", djName);
        newEntry.put("Quest_Friendly", convertedQuestLink);
        newEntry.put("Non-Quest_Friendly", convertedNonQuestLink);

        JSONArray array = (JSONArray) jsonObject.get(type + "s");
        array.add(newEntry);
        displayJsonContent();
    }

    private void saveFile() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(jsonObject.toJSONString());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to save file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DJListEditor editor = new DJListEditor();
            editor.setVisible(true);
        });
    }
}
