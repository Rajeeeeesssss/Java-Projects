import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class CurrencyConverter extends JFrame {

    private JComboBox<String> fromCurrency;
    private JComboBox<String> toCurrency;
    private JTextField amountField;
    private JLabel resultLabel;

    // ✅ Method 1: API key stored as a constant
    private static final String API_KEY = "64c67a0a76f5e10d81d32ae5";

    private static final String[] currencies = {
            "USD", "EUR", "GBP", "INR", "JPY", "AUD", "CAD", "CHF", "CNY", "NZD", "SGD",
            "SEK", "NOK", "RUB", "MXN", "BRL", "HKD", "ZAR", "KRW", "AED", "SAR", "TRY",
            "THB", "IDR", "MYR", "PHP", "PLN", "HUF", "CZK", "DKK", "ISK"
    };

    public CurrencyConverter() {
        setTitle("Real-Time Currency Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);

        // Layout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Currency Converter", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;

        // Components
        fromCurrency = new JComboBox<>(currencies);
        toCurrency = new JComboBox<>(currencies);
        amountField = new JTextField();
        resultLabel = new JLabel("Converted Amount: ", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton convertButton = new JButton("Convert");
        convertButton.setBackground(new Color(0, 102, 204));
        convertButton.setForeground(Color.WHITE);
        convertButton.setFocusPainted(false);

        // Row 1 - Amount
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        add(amountField, gbc);

        // Row 2 - From Currency
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("From:"), gbc);
        gbc.gridx = 1;
        add(fromCurrency, gbc);

        // Row 3 - To Currency
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("To:"), gbc);
        gbc.gridx = 1;
        add(toCurrency, gbc);

        // Row 4 - Button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(convertButton, gbc);

        // Row 5 - Result
        gbc.gridy = 5;
        add(resultLabel, gbc);

        // Action
        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertCurrency();
            }
        });

        setVisible(true);
    }

    private void convertCurrency() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            String from = (String) fromCurrency.getSelectedItem();
            String to = (String) toCurrency.getSelectedItem();

            // ✅ New API endpoint with API key
            String urlStr = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/" + from;
            URL url = new URI(urlStr).toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            Scanner sc = new Scanner(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonData = new StringBuilder();
            while (sc.hasNext()) {
                jsonData.append(sc.nextLine());
            }
            sc.close();

            JSONObject json = new JSONObject(jsonData.toString());

            if (!json.getString("result").equalsIgnoreCase("success")) {
                resultLabel.setText("Error: API request failed.");
                return;
            }

            double rate = json.getJSONObject("conversion_rates").getDouble(to);
            double result = amount * rate;

            resultLabel.setText("Converted Amount: " + String.format("%.2f", result) + " " + to);

        } catch (NumberFormatException ex) {
            resultLabel.setText("Error: Please enter a valid amount.");
        } catch (Exception ex) {
            resultLabel.setText("Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CurrencyConverter());
    }
}
