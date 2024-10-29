package services;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;
import tools.JenaEngine;

import java.io.InputStream;

@Service
public class CommentService {

    private static final String RDF_FILE_PATH = "data/GestionDechets.owl";
    private Model model;

    private Model loadRDF() {
        model = ModelFactory.createDefaultModel();
        InputStream in = JenaEngine.class.getResourceAsStream("/" + RDF_FILE_PATH);
        if (in == null) {
            throw new IllegalArgumentException("Fichier non trouvé: " + RDF_FILE_PATH);
        }
        model.read(in, null);
        return model;
    }

    private void saveRDF() {
        if (model == null) {
            throw new IllegalStateException("Model is not loaded.");
        }
        JenaEngine.saveModel(model, RDF_FILE_PATH);
    }

    public String queryComments() {
        loadRDF();
        System.out.println("Taille du modèle: " + model.size());

        String queryString =
                "PREFIX ontologie: <http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#> " +
                        "SELECT ?comment ?property ?value " +
                        "WHERE { " +
                        "  ?comment a ontologie:Commentaire . " +
                        "  ?comment ?property ?value . " +
                        "}";

        return JenaEngine.executeQuery(model, queryString);
    }

    public void addComment(String commentId, String content, String author, String createdAt) {
        if (model == null) {
            loadRDF();
        }

        Resource commentResource = model.createResource("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#" + commentId);
        commentResource.addProperty(RDF.type, model.getResource("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#Commentaire"));
        commentResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#contentComment"), content);
        commentResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#author"), author);
        commentResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#comment_created_at"), model.createTypedLiteral(createdAt, XSDDatatype.XSDdateTime));

        saveRDF();
    }

    public void updateComment(String commentId, String newContent, String newAuthor, String newCreatedAt) {
        if (model == null) {
            loadRDF();
        }

        Resource commentResource = model.getResource("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#" + commentId);
        if (commentResource != null) {
            commentResource.removeAll(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#contentComment"));
            commentResource.removeAll(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#author"));
            commentResource.removeAll(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#comment_created_at"));

            commentResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#contentComment"), newContent);
            commentResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#author"), newAuthor);
            commentResource.addProperty(model.getProperty("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#comment_created_at"), model.createTypedLiteral(newCreatedAt, XSDDatatype.XSDdateTime));

            saveRDF();
        } else {
            System.out.println("Commentaire non trouvé: " + commentId);
        }
    }

    public void deleteComment(String commentId) {
        if (model == null) {
            loadRDF();
        }

        Resource commentResource = model.getResource("http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#" + commentId);
        if (commentResource != null) {
            commentResource.removeProperties();
            saveRDF();
        } else {
            System.out.println("Commentaire non trouvé: " + commentId);
        }
    }
}
