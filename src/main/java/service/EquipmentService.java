// src/main/java/service/EquipmentService.java
package service;

import model.Equipment;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EquipmentService {
    private final Path dataFilePath;

    public EquipmentService(Path dataFilePath) {
        this.dataFilePath = dataFilePath;
        createDataFileIfNotExists();
    }

    public Path getDataFilePath() {
        return dataFilePath;
    }

    private void createDataFileIfNotExists() {
        try {
            if (!Files.exists(dataFilePath)) {
                Files.write(dataFilePath, "[]".getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать файл данных", e);
        }
    }

    public List<Equipment> getAllEquipment() throws IOException {
        String jsonData = new String(Files.readAllBytes(dataFilePath));
        JSONArray jsonArray = new JSONArray(jsonData);
        List<Equipment> equipmentList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Equipment equipment = new Equipment();
            equipment.setName(jsonObject.getString("name"));
            equipment.setType(jsonObject.getString("type"));
            equipment.setBrand(jsonObject.getString("brand"));
            equipment.setModel(jsonObject.getString("model"));
            equipment.setYear(jsonObject.getInt("year"));
            equipment.setPrice(jsonObject.getDouble("price"));
            equipment.setCondition(jsonObject.getString("condition"));
            equipmentList.add(equipment);
        }

        return equipmentList;
    }

    public void addEquipment(Equipment equipment) throws IOException, ValidationException {
        validateEquipment(equipment);

        List<Equipment> equipmentList = getAllEquipment();
        equipmentList.add(equipment);

        JSONArray jsonArray = new JSONArray();
        for (Equipment eq : equipmentList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", eq.getName());
            jsonObject.put("type", eq.getType());
            jsonObject.put("brand", eq.getBrand());
            jsonObject.put("model", eq.getModel());
            jsonObject.put("year", eq.getYear());
            jsonObject.put("price", eq.getPrice());
            jsonObject.put("condition", eq.getCondition());
            jsonArray.put(jsonObject);
        }

        Files.write(dataFilePath, jsonArray.toString().getBytes());
    }

    private void validateEquipment(Equipment equipment) throws ValidationException {
        if (equipment.getName() == null || equipment.getName().isEmpty()) {
            throw new ValidationException("Поле name обязательно для заполнения");
        }
        if (equipment.getType() == null || equipment.getType().isEmpty()) {
            throw new ValidationException("Поле type обязательно для заполнения");
        }
        if (equipment.getBrand() == null || equipment.getBrand().isEmpty()) {
            throw new ValidationException("Поле brand обязательно для заполнения");
        }
        if (equipment.getModel() == null || equipment.getModel().isEmpty()) {
            throw new ValidationException("Поле model обязательно для заполнения");
        }
        if (equipment.getYear() < 1900 || equipment.getYear() > 2023) {
            throw new ValidationException("Год выпуска должен быть между 1900 и 2023");
        }
        if (equipment.getPrice() < 0) {
            throw new ValidationException("Цена не может быть отрицательной");
        }
        if (equipment.getCondition() == null || equipment.getCondition().isEmpty()) {
            throw new ValidationException("Поле condition обязательно для заполнения");
        }
    }
}