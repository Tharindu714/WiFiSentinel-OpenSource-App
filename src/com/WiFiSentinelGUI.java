package com;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.OutputStream;
import java.net.URI;

public class WiFiSentinelGUI extends JFrame {

    private final JProgressBar signalStrengthBar;
    private final JLabel wifiNameLabel, signalStrengthLabel, downloadSpeedLabel, uploadSpeedLabel;
    private Timer updateTimer;

    public WiFiSentinelGUI() {
        JFrame frame = new JFrame("WiFiSentinel 🌐 - Wi-Fi Security Auditor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 350);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(30, 30, 30)); // Darker background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // WiFi Icon + Name Panel
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        wifiNameLabel = new JLabel("🌐 Wi-Fi Network: Fetching SSID...");
        wifiNameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        wifiNameLabel.setForeground(Color.CYAN);
        mainPanel.add(wifiNameLabel, gbc);

        // Image on the left side
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        ImageIcon icon = new ImageIcon("");
        JLabel imageLabel = new JLabel(icon);
        mainPanel.add(imageLabel, gbc);

        // Signal Strength Label + Percentage
        gbc.gridx = 1;
        JLabel signalLabel = new JLabel("📶 Signal Strength:");
        signalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        signalLabel.setForeground(Color.LIGHT_GRAY);
        mainPanel.add(signalLabel, gbc);

        gbc.gridx = 2;
        signalStrengthLabel = new JLabel("0%");
        signalStrengthLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        signalStrengthLabel.setForeground(Color.ORANGE);
        mainPanel.add(signalStrengthLabel, gbc);

        // Signal Strength Bar
        gbc.gridy = 2;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        signalStrengthBar = new JProgressBar(0, 100);
        signalStrengthBar.setStringPainted(true);
        signalStrengthBar.setForeground(Color.MAGENTA);
        mainPanel.add(signalStrengthBar, gbc);

        // Download Speed
        gbc.gridy = 3;
        downloadSpeedLabel = new JLabel("⬇️ Download Speed: Testing... 🚀");
        downloadSpeedLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        downloadSpeedLabel.setForeground(Color.GREEN);
        mainPanel.add(downloadSpeedLabel, gbc);

        // Upload Speed
        gbc.gridy = 4;
        uploadSpeedLabel = new JLabel("⬆️ Upload Speed: Testing... 📡");
        uploadSpeedLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        uploadSpeedLabel.setForeground(Color.YELLOW);
        mainPanel.add(uploadSpeedLabel, gbc);

        // Footer Section
        gbc.gridy = 5;
        gbc.insets = new Insets(16, 0, 0, 0);
        JLabel footerLabel = new JLabel("© 2025 Developed By Tharindu Chanka >> Visit me 🌐");
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footerLabel.setForeground(Color.WHITE);

        // Make "tharindu.me" a clickable link
        footerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        footerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://tharindu714.github.io/tharinduc.me/"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        mainPanel.add(footerLabel, gbc);

        frame.add(mainPanel);
        frame.setVisible(true);
        setupUpdateTimer();
    }

    private void setupUpdateTimer() {
        updateTimer = new Timer(5000, (ActionEvent e) -> {
            updateWiFiData();
        });
        updateTimer.start();
    }

    private void updateWiFiData() {
        String wifiName = getWiFiSSID();
        wifiNameLabel.setText("Wi-Fi Network: " + (wifiName != null ? wifiName : "Fetching SSID..."));

        int signalStrength = getRealSignalStrength();
        signalStrengthBar.setValue(signalStrength);
        signalStrengthLabel.setText(signalStrength + "%");

        // Get real download & upload speed
        double downloadSpeed = getRealDownloadSpeed();
        double uploadSpeed = getRealUploadSpeed();
        downloadSpeedLabel.setText(String.format("Download Speed: %.2f Mbps", downloadSpeed));
        uploadSpeedLabel.setText(String.format("Upload Speed: %.2f Mbps", uploadSpeed));
    }

    private String getWiFiSSID() {
        String ssid = null;
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process;
            if (os.contains("win")) {
                process = Runtime.getRuntime().exec("netsh wlan show interfaces");
            } else {
                process = Runtime.getRuntime().exec("iwgetid -r");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (os.contains("win") && line.trim().startsWith("SSID")) {
                    ssid = line.split(":")[1].trim();
                    break;
                } else if (!os.contains("win")) {
                    ssid = line.trim();
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssid;
    }

    private int getRealSignalStrength() {
        int signalStrength = -1;
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process;
            if (os.contains("win")) {
                process = Runtime.getRuntime().exec("netsh wlan show interfaces");
            } else {
                process = Runtime.getRuntime().exec("iwconfig wlan0");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (os.contains("win") && line.trim().startsWith("Signal")) {
                    signalStrength = Integer.parseInt(line.split(":")[1].trim().replace("%", ""));
                    break;
                } else if (!os.contains("win") && line.contains("Signal level=")) {
                    signalStrength = Integer.parseInt(line.split("=")[1].split(" ")[0]);
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signalStrength;
    }

    private double getRealDownloadSpeed() {
        try {
            String fileUrl = "http://speed.hetzner.de/100MB.bin"; // Large file for speed test
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set timeouts
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);

            // Set User-Agent (Some servers block Java default user-agent)
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Check for successful response
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Error: Server responded with code " + responseCode);
                return 0;
            }

            // Start measuring time AFTER connection is established
            long startTime = System.nanoTime();

            InputStream in = new BufferedInputStream(connection.getInputStream());
            byte[] buffer = new byte[16 * 1024]; // 16 KB buffer for faster reading
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                totalBytes += bytesRead;
            }

            // Close resources
            in.close();
            connection.disconnect();

            // End time
            long endTime = System.nanoTime();
            double timeTaken = (endTime - startTime) / 1_000_000_000.0; // Convert nanoseconds to seconds

            // Calculate speed (Convert bytes → bits → Megabits)
            double speedMbps = (totalBytes * 8.0) / (1024 * 1024) / timeTaken;

            return Math.round(speedMbps * 100.0) / 100.0; // Round to 2 decimal places
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private double getRealUploadSpeed() {
        try {
            long startTime = System.currentTimeMillis();

            byte[] testData = new byte[50 * 1024 * 1024]; // 10MB file // 512 KB of random data
            new java.util.Random().nextBytes(testData);

            URL url = new URL("http://speedtest.tele2.net/upload.php"); // Replace with a real test server
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("Content-Length", String.valueOf(testData.length));

            OutputStream out = connection.getOutputStream();
            out.write(testData);
            out.flush();
            out.close();

            long endTime = System.currentTimeMillis();
            double timeTaken = (endTime - startTime) / 1000.0;
            double speed = (testData.length / (1024.0 * 1024.0)) / timeTaken;

            return Math.max(0, Math.round(speed * 100.0) / 100.0); // Fix negatives & round
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Return 0 instead of -1
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            new WiFiSentinelGUI();
        });
    }
}
