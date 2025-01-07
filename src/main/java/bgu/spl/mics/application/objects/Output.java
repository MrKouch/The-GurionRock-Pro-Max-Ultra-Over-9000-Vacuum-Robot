package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Output {
    private boolean error; // Flag indicating if there was an error
    private String errorMessage; // Optional error message (if error is true)
    private Map<String, Object> data; // Data to include in the output file

    public Output() {
        this.error = false;
        this.data = new HashMap<>();
    }

    public void setError(String errorMessage) {
        this.error = true;
        this.errorMessage = errorMessage;
    }

    public boolean hasError() {
        return error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void addData(String key, Object value) {
        this.data.put(key, value);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void writeToFile(String filePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filePath)) {
            if (error) {
                Map<String, Object> errorOutput = new HashMap<>();
                errorOutput.put("error", true);
                errorOutput.put("errorMessage", errorMessage);
                gson.toJson(errorOutput, writer);
            } else {
                Map<String, Object> successOutput = new HashMap<>(data);
                successOutput.put("error", false);
                gson.toJson(successOutput, writer);
            }
            System.out.println("Output written to: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
