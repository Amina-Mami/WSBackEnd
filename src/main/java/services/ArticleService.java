package services;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class ArticleService {

    private static final String RDF_FILE_PATH = "data/GestionDechets.owl";
    private Model model;

    public Model loadRDF() {
        model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(RDF_FILE_PATH);
        if (in == null) {
            throw new IllegalArgumentException("File not found: " + RDF_FILE_PATH);
        }
        model.read(in, null);
        return model;
    }

    public String queryArticles() {
        loadRDF();
        String queryString =
                "PREFIX ontologie: <http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#> " +
                        "SELECT ?article ?property ?value " +
                        "WHERE { " +
                        "  ?article a ontologie:Article . " +
                        "  ?article ?property ?value . " +
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            Map<String, JSONObject> articlesMap = new HashMap<>();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String articleUrl = solution.getResource("article").toString();
                String articleId = articleUrl.split("#")[1];
                String property = solution.get("property").toString().split("#")[1];
                String value = solution.get("value").toString();

                articlesMap.putIfAbsent(articleId, new JSONObject());
                articlesMap.get(articleId).put(property, value);
            }

            JSONObject resultJson = new JSONObject();
            JSONArray articlesArray = new JSONArray();

            for (Map.Entry<String, JSONObject> entry : articlesMap.entrySet()) {
                JSONObject articleObject = new JSONObject();
                articleObject.put("article_id", entry.getKey());
                articleObject.put("details", entry.getValue());
                articlesArray.put(articleObject);
            }

            resultJson.put("articles", articlesArray);
            return resultJson.toString();
        }
    }

    public String queryArticleById(String articleId) {
        loadRDF();
        String queryString =
                "PREFIX ontologie: <http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#> " +
                        "SELECT ?property ?value " +
                        "WHERE { " +
                        "  ontologie:" + articleId + " ?property ?value . " +
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONObject articleJson = new JSONObject();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String property = solution.get("property").toString().split("#")[1];
                String value = solution.get("value").toString();
                articleJson.put(property, value);
            }

            if (articleJson.isEmpty()) {
                return null; // If no properties found for the given articleId
            }

            return new JSONObject().put("article", articleJson).toString();
        }
    }

    public String queryArticleByTitle(String title) {
        loadRDF();
        String queryString =
                "PREFIX ontologie: <http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#> " +
                        "SELECT ?article ?titleValue ?content ?createdAt " +
                        "WHERE { " +
                        "  ?article a ontologie:Article . " +
                        "  ?article ontologie:title ?titleValue . " +
                        "  ?article ontologie:contentArticle ?content . " +
                        "  ?article ontologie:article_created_at ?createdAt . " +
                        "  FILTER (contains(lcase(str(?titleValue)), lcase(\"" + title + "\"))) " +
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            JSONArray articlesArray = new JSONArray();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();

                JSONObject articleJson = new JSONObject();
                RDFNode articleNode = solution.get("article");
                RDFNode titleNode = solution.get("titleValue");
                RDFNode contentNode = solution.get("content");
                RDFNode createdAtNode = solution.get("createdAt");

                articleJson.put("article_id", articleNode.toString().split("#")[1]);
                articleJson.put("title", titleNode != null ? titleNode.toString() : "N/A");
                articleJson.put("content", contentNode != null ? contentNode.toString() : "N/A");
                articleJson.put("created_at", createdAtNode != null ? createdAtNode.toString() : "N/A");

                articlesArray.put(articleJson);
            }

            if (articlesArray.isEmpty()) {
                return "Aucun article trouvé avec ce titre";
            }

            return new JSONObject().put("articles", articlesArray).toString();
        }
    }

    // Existing methods for adding, updating, and deleting articles remain unchanged

    public void addArticle(String articleId, String title, String content, String createdAt) {
        loadRDF();
        Resource articleResource = model.createResource("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#" + articleId);
        articleResource.addProperty(RDF.type, model.getResource("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#Article"));
        articleResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#title"), title);
        articleResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#contentArticle"), content);
        articleResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#article_created_at"), model.createTypedLiteral(createdAt, XSDDatatype.XSDdateTime));
        saveRDF();
    }

    public void updateArticle(String articleId, String newTitle, String newContent, String newCreatedAt) {
        loadRDF();
        Resource articleResource = model.getResource("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#" + articleId);
        if (articleResource != null) {
            articleResource.removeAll(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#title"));
            articleResource.removeAll(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#contentArticle"));
            articleResource.removeAll(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#article_created_at"));
            articleResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#title"), newTitle);
            articleResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#contentArticle"), newContent);
            articleResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#article_created_at"), model.createTypedLiteral(newCreatedAt, XSDDatatype.XSDdateTime));
            saveRDF();
        }
    }

    public void deleteArticle(String articleId) {
        loadRDF();
        Resource articleResource = model.getResource("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#" + articleId);
        if (articleResource != null) {
            articleResource.removeProperties();
            saveRDF();
        }
    }

    private void saveRDF() {
        try (FileOutputStream out = new FileOutputStream(RDF_FILE_PATH)) {
            model.write(out, "RDF/XML");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
