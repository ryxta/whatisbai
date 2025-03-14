package com.whatisbai.Services;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class ModelService {

    @PostConstruct
    public void startPythonServer() {
        try {
            System.out.println("Starting Python server...");
            String pythonScriptPath = "C:/Users/Lnwza007X/OneDrive/Desktop/final_project/whatisbai/script.py";
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Run the process output reading in a separate thread
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Python: " + line);  // Log Python script output
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            System.out.println("Python server started in background.");

        } catch (IOException e) {
            System.err.println("Failed to start Python server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
