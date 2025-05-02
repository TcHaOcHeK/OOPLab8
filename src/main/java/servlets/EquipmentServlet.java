package servlets;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.json.*;

@WebServlet("/equipment")
public class EquipmentServlet extends HttpServlet {
    private static final String DATA_FILE = "equipment_data.json";
    private Path dataFilePath;

    @Override
    public void init() throws ServletException {
        super.init();
        String appPath = getServletContext().getRealPath("");
        dataFilePath = Paths.get(appPath, DATA_FILE);
        createDataFileIfNotExists();
    }

    private void createDataFileIfNotExists() throws ServletException {
        try {
            if (!Files.exists(dataFilePath)) {
                Files.write(dataFilePath, "[]".getBytes());
            }
        } catch (IOException e) {
            throw new ServletException("Не удалось создать файл данных", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String jsonData = new String(Files.readAllBytes(dataFilePath));
            response.getWriter().write(jsonData);
        } catch (IOException e) {
            sendError(response, "Ошибка чтения данных", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject newEquipment = new JSONObject(sb.toString());
            validateEquipment(newEquipment);

            JSONArray equipmentArray = readExistingData();
            equipmentArray.put(newEquipment);

            saveData(equipmentArray);

            JSONObject successResponse = new JSONObject();
            successResponse.put("success", true);
            successResponse.put("message", "Оборудование успешно добавлено");
            response.getWriter().write(successResponse.toString());
        } catch (JSONException e) {
            sendError(response, "Неверный формат данных", HttpServletResponse.SC_BAD_REQUEST);
        } catch (ValidationException e) {
            sendError(response, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            sendError(response, "Ошибка сохранения данных", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private JSONArray readExistingData() throws IOException, JSONException {
        String jsonData = new String(Files.readAllBytes(dataFilePath));
        return new JSONArray(jsonData);
    }

    private void saveData(JSONArray data) throws IOException {
        Files.write(dataFilePath, data.toString().getBytes());
    }

    private void validateEquipment(JSONObject equipment) throws ValidationException {
        String[] requiredFields = {"name", "type", "brand", "model", "year", "price", "condition"};

        for (String field : requiredFields) {
            if (!equipment.has(field) || equipment.getString(field).isEmpty()) {
                throw new ValidationException("Поле " + field + " обязательно для заполнения");
            }
        }

        try {
            int year = equipment.getInt("year");
            if (year < 1900 || year > 2023) {
                throw new ValidationException("Год выпуска должен быть между 1900 и 2023");
            }

            double price = equipment.getDouble("price");
            if (price < 0) {
                throw new ValidationException("Цена не может быть отрицательной");
            }
        } catch (JSONException e) {
            throw new ValidationException("Неверный формат числовых данных");
        }
    }

    private void sendError(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        response.getWriter().write(errorResponse.toString());
    }

    private static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }
}