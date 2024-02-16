import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DJListEditor extends JFrame {
    private JTextPane displayPane;
    private JScrollPane scrollPane;
    private JComboBox<String> typeComboBox;
    private JTextField djNameField, questLinkField, nonQuestLinkField;
    private JsonObject jsonObject; // Use Gson's JsonObject
    private JButton addButton;
    private Gson gson;

    public DJListEditor() {
        super("DJ List Editor");
        gson = new GsonBuilder().setPrettyPrinting().create(); // Gson with pretty printing
        jsonObject = new JsonObject();
        jsonObject.add("DJs", new JsonArray());
        jsonObject.add("VJs", new JsonArray());
        initializeComponents();
        this.setSize(500, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initializeComponents() {
        displayPane = new JTextPane();
        displayPane.setEditable(false); // Make JTextPane non-editable
        scrollPane = new JScrollPane(displayPane);
        scrollPane.setPreferredSize(new Dimension(750, 300));

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

        addButton = new JButton("Add");
        addButton.addActionListener(e -> addEntry());

        // Disable text fields and button initially
        djNameField.setEnabled(false);
        questLinkField.setEnabled(false);
        nonQuestLinkField.setEnabled(false);
        typeComboBox.setEnabled(false); // Disable the combo box as well
        addButton.setEnabled(false);

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
    }

    private void openFile() {
        FileDialog fd = new FileDialog(this, "Open", FileDialog.LOAD);
        fd.setFile("*.json");
        fd.setVisible(true);
        if (fd.getFile() != null) {
            File file = new File(fd.getDirectory(), fd.getFile());
            try (Reader reader = new FileReader(file)) {
                jsonObject = gson.fromJson(reader, JsonObject.class); // Parse JSON file to JsonObject
                displayPane.setText(gson.toJson(jsonObject)); // Display with pretty printing
                
                 // Enable text fields and button after successful load
                djNameField.setEnabled(true);
                questLinkField.setEnabled(true);
                nonQuestLinkField.setEnabled(true);
                typeComboBox.setEnabled(true); // Enable the combo box as well
                addButton.setEnabled(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to open file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // private void displayJsonContent() {
    //     // Assuming jsonObject holds your parsed JSON
    //     // This example simply converts the jsonObject to a string for display
    //     // For real applications, you might format this string for better readability
    //     if (jsonObject != null) {
    //         displayPane.setText(jsonObject.toJSONString());
    //     }
    // }
    
    // This method could be called right after parsing the JSON to update the display
    

    private void addEntry() {
        System.out.println("Add button clicked"); // Debugging line
        try {
            String type = (String) typeComboBox.getSelectedItem();
            String djName = djNameField.getText();
            String questLink = questLinkField.getText();
            String nonQuestLink = nonQuestLinkField.getText();

            // Initialize variables for the converted links
            String convertedQuestLink = "";
            String convertedNonQuestLink = "";

            // Check and convert the quest link 
            if (!questLink.isEmpty()) {
                // Check and convert the quest link if it matches the specified pattern
                if (questLink.matches("https://stream\\.vrcdn\\.live/live/.*\\.live\\.ts")) {
                    convertedNonQuestLink = questLink.replaceFirst("https://stream\\.vrcdn\\.live/live/(.*)\\.live\\.ts", "rtspt://stream.vrcdn.live/live/$1");
                } else {
                    convertedNonQuestLink = questLink;
                }
            } else {
                // Since questLink is empty, check and convert the nonQuestLink if it matches the specified pattern
                if (nonQuestLink.matches("rtspt://stream\\.vrcdn\\.live/live/.*")) {
                    convertedQuestLink = nonQuestLink.replaceFirst("rtspt://stream\\.vrcdn\\.live/live/(.*)", "https://stream.vrcdn.live/live/$1.live.ts");
                }

                else {
                    convertedQuestLink = "None"; // or any other default value or handling
                }
            }

            
            // Prepare the confirmation message with DJ details
            String confirmationMessage = String.format(
                "Are you sure you want to add the following entry?\n\n" +
                "DJ Name: %s\nQuest Link: %s\nNon-Quest Link: %s",
                djName, // Use the DJ name
                convertedQuestLink, // Use the converted quest link
                convertedNonQuestLink // Use the converted non-quest link
            );

            // Display the confirmation dialog
            int response = JOptionPane.showConfirmDialog(
                this, // parent component
                confirmationMessage, // the confirmation message
                "Confirm Add DJ", // title of the dialog
                JOptionPane.YES_NO_OPTION, // option type: yes or no
                JOptionPane.QUESTION_MESSAGE // message type: question
            );  
            // Check the user's response
            if (response == JOptionPane.YES_OPTION) {
                // Create a new JSON object for the entry
                JsonObject newEntry = new JsonObject();
                newEntry.addProperty("DJ_Name", djName);
                newEntry.addProperty("Non-Quest_Friendly", convertedNonQuestLink);
                newEntry.addProperty("Quest_Friendly", convertedQuestLink);

                JsonArray originalArray = jsonObject.getAsJsonArray(type + "s");
                JsonArray newArray = new JsonArray();

                boolean added = false;
                for (int i = 0; i < originalArray.size(); i++) {
                    JsonObject existingEntry = originalArray.get(i).getAsJsonObject();
                    String existingDJName = existingEntry.get("DJ_Name").getAsString();
                    // Insert new entry before the first entry that comes after it alphabetically
                    if (!added && djName.compareToIgnoreCase(existingDJName) < 0) {
                        newArray.add(newEntry);
                        added = true;
                    }
                    newArray.add(existingEntry);
                }

                // If the new entry was not added because it's alphabetically last, add it now
                if (!added) {
                    newArray.add(newEntry);
                }

                // Replace the old array with the new array
                jsonObject.add(type + "s", newArray);

            } else {
                System.out.println("DJ addition cancelled.");
            }

        } catch (Exception e) {
            e.printStackTrace(); // Log exception to standard error
        }
    }

    private void saveFile() {
        FileDialog fd = new FileDialog(this, "Save", FileDialog.SAVE);
        fd.setVisible(true);
        if (fd.getFile() != null) {
            File file = new File(fd.getDirectory(), fd.getFile());
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(jsonObject, writer); // Write JSON with pretty printing
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to save file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DJListEditor().setVisible(true));
    }
}
