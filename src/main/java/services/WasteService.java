package services;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.springframework.stereotype.Service;
import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class WasteService {

    private static final String RDF_FILE_PATH = "data/GestionDechets.owl";

    private Model model;


    public Model loadRDF() {
        model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(RDF_FILE_PATH);
        if (in == null) {
            throw new IllegalArgumentException("Fichier non trouvé: " + RDF_FILE_PATH);
        }
        model.read(in, null);
        return model;
    }

    public String queryWastes() {
        loadRDF();
        System.out.println("Taille du modèle: " + model.size());

        String queryString =
                "PREFIX ontologie-dechet: <http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#> " +
                        "SELECT ?waste ?property ?value " +
                        "WHERE { " +
                        "  ?waste a ontologie-dechet:Déchet . " +
                        "  ?waste ?property ?value . " +
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            Map<String, JSONObject> wastesMap = new HashMap<>();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String wasteUrl = solution.getResource("waste").toString();
                String wasteName = wasteUrl.split("#")[1];
                String property = solution.get("property").toString().split("#")[1];
                String value = solution.get("value").toString();


                wastesMap.putIfAbsent(wasteName, new JSONObject());
                wastesMap.get(wasteName).put(property, value);
            }

            JSONObject resultJson = new JSONObject();
            JSONArray wastesArray = new JSONArray();

            for (Map.Entry<String, JSONObject> entry : wastesMap.entrySet()) {
                JSONObject wasteObject = new JSONObject();
                wasteObject.put(entry.getKey(), entry.getValue());
                wastesArray.put(wasteObject);
            }

            resultJson.put("wastes", wastesArray);
            return resultJson.toString();
        }
    }


    public void addWaste(String waste_id, String type, double weight, String status, String createdAt) {
        if (model == null) {
            loadRDF();
        }


        Resource wasteResource = model.createResource("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#" + waste_id);
        wasteResource.addProperty(RDF.type, model.getResource("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#Déchet"));
        wasteResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#hasType"), type);
        wasteResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#hasWeight"), model.createTypedLiteral(weight));
        wasteResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#status"), status);


        wasteResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#createdAt"), model.createTypedLiteral(createdAt, XSDDatatype.XSDdateTime));


        saveRDF();
    }




    public void updateWaste(String wasteId, String newType, double newWeight, String newStatus, String newCreatedAt) {
        if (model == null) {
            loadRDF();
        }

        Resource wasteResource = model.getResource("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#" + wasteId);
        if (wasteResource != null) {

            wasteResource.removeAll(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#hasType"));
            wasteResource.removeAll(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#hasWeight"));
            wasteResource.removeAll(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#status"));
            wasteResource.removeAll(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#createdAt"));


            wasteResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#hasType"), newType);
            wasteResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#hasWeight"), model.createTypedLiteral(newWeight));
            wasteResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#status"), newStatus);
            wasteResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#createdAt"), newCreatedAt);


            saveRDF();
        } else {
            System.out.println("Déchet non trouvé: " + wasteId);
        }
    }

    public void deleteWasteById(String wasteId) {
        if (model == null) {
            loadRDF();
        }

        Resource wasteResource = model.getResource("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#" + wasteId);
        if (wasteResource != null) {

            wasteResource.removeProperties();


            saveRDF();
        } else {
            System.out.println("Déchet non trouvé: " + wasteId);
        }
    }


    private void saveRDF() {
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String queryWastesByType(String type) {
        loadRDF();

        String queryString =
                "PREFIX ontologie-dechet: <http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#> " +
                        "SELECT ?waste ?property ?value " +
                        "WHERE { " +
                        "  ?waste a ontologie-dechet:Déchet . " +
                        "  ?waste ontologie-dechet:hasType \"" + type + "\" . " +
                        "  ?waste ?property ?value . " +
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            Map<String, JSONObject> wastesMap = new HashMap<>();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String wasteUrl = solution.getResource("waste").toString();
                String wasteName = wasteUrl.split("#")[1];
                String property = solution.get("property").toString().split("#")[1];
                String value = solution.get("value").toString();


                wastesMap.putIfAbsent(wasteName, new JSONObject());
                wastesMap.get(wasteName).put(property, value);
            }

            JSONObject resultJson = new JSONObject();
            JSONArray wastesArray = new JSONArray();

            for (Map.Entry<String, JSONObject> entry : wastesMap.entrySet()) {
                JSONObject wasteObject = new JSONObject();
                wasteObject.put(entry.getKey(), entry.getValue());
                wastesArray.put(wasteObject);
            }

            resultJson.put("wastes", wastesArray);
            return resultJson.toString();
        }
    }


}
