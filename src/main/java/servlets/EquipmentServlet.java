// src/main/java/servlets/EquipmentServlet.java
package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import model.Equipment;
import org.json.JSONArray;
import org.json.JSONObject;
import service.EquipmentService;
import service.ValidationException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@WebServlet("/equipment")
public class EquipmentServlet extends HttpServlet {
    private static final String DATA_FILE = "equipment_data.json";
    private EquipmentService equipmentService;

    @Override
    public void init() throws ServletException {
        super.init();
        String appPath = getServletContext().getRealPath("");
        Path dataFilePath = Paths.get(appPath, DATA_FILE);
        this.equipmentService = new EquipmentService(dataFilePath);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            List<Equipment> equipmentList = equipmentService.getAllEquipment();
            JSONArray jsonArray = new JSONArray();

            for (Equipment equipment : equipmentList) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", equipment.getName());
                jsonObject.put("type", equipment.getType());
                jsonObject.put("brand", equipment.getBrand());
                jsonObject.put("model", equipment.getModel());
                jsonObject.put("year", equipment.getYear());
                jsonObject.put("price", equipment.getPrice());
                jsonObject.put("condition", equipment.getCondition());
                jsonArray.put(jsonObject);
            }

            response.getWriter().write(jsonArray.toString());
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

            JSONObject jsonObject = new JSONObject(sb.toString());
            Equipment equipment = new Equipment();
            equipment.setName(jsonObject.getString("name"));
            equipment.setType(jsonObject.getString("type"));
            equipment.setBrand(jsonObject.getString("brand"));
            equipment.setModel(jsonObject.getString("model"));
            equipment.setYear(jsonObject.getInt("year"));
            equipment.setPrice(jsonObject.getDouble("price"));
            equipment.setCondition(jsonObject.getString("condition"));

            equipmentService.addEquipment(equipment);

            JSONObject successResponse = new JSONObject();
            successResponse.put("success", true);
            successResponse.put("message", "Оборудование успешно добавлено");
            response.getWriter().write(successResponse.toString());
        } catch (org.json.JSONException e) {
            sendError(response, "Неверный формат данных", HttpServletResponse.SC_BAD_REQUEST);
        } catch (ValidationException e) {
            sendError(response, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            sendError(response, "Ошибка сохранения данных", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendError(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        response.getWriter().write(errorResponse.toString());
    }
}