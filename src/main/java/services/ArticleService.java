package services;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;

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
            StringBuilder resultString = new StringBuilder();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String article = solution.get("article").toString();
                String property = solution.get("property").toString();
                String value = solution.get("value").toString();
                resultString.append("Article: ").append(article)
                        .append(", Property: ").append(property)
                        .append(", Value: ").append(value).append("\n");
            }
            return resultString.toString();
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
            StringBuilder resultString = new StringBuilder();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String property = solution.get("property").toString();
                String value = solution.get("value").toString();
                resultString.append("Property: ").append(property)
                        .append(", Value: ").append(value).append("\n");
            }
            return resultString.toString();
        }
    }

    public String queryArticleByTitle(String title) {
        loadRDF();

        // Utiliser un FILTER pour insensibilité à la casse avec "contains"
        String queryString =
                "PREFIX ontologie: <http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#> " +
                        "SELECT ?article ?titleValue ?content ?createdAt " +
                        "WHERE { " +
                        "  ?article a ontologie:Article . " +
                        "  ?article ontologie:title ?titleValue . " +
                        "  ?article ontologie:contentArticle ?content . " +
                        "  ?article ontologie:article_created_at ?createdAt . " +
                        "  FILTER (contains(lcase(str(?titleValue)), lcase(\"" + title + "\"))) " +  // Comparaison insensible à la casse et partielle
                        "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (!results.hasNext()) {
                System.out.println("Aucun résultat trouvé pour le titre: " + title);
                return "Aucun article trouvé avec ce titre";
            }
            StringBuilder resultString = new StringBuilder();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();

                RDFNode articleNode = solution.get("article");
                RDFNode titleNode = solution.get("titleValue");
                RDFNode contentNode = solution.get("content");
                RDFNode createdAtNode = solution.get("createdAt");

                if (articleNode != null && titleNode != null) {
                    resultString.append("Article: ").append(articleNode.toString()).append("\n")
                            .append("Title: ").append(titleNode.toString()).append("\n")
                            .append("Content: ").append(contentNode != null ? contentNode.toString() : "N/A").append("\n")
                            .append("Created At: ").append(createdAtNode != null ? createdAtNode.toString() : "N/A").append("\n\n");
                }
            }
            return resultString.toString();
        }
    }





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
