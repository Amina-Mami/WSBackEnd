import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import tools.JenaEngine;

public class ArticleSparql {
    public static void main(String[] args) {
        String NS = "";


        Model model = JenaEngine.readModel("data/GestionDechets.owl");

        if (model != null) {

            NS = model.getNsPrefixURI("ontologie");
            if (NS == null) {
                System.out.println("Namespace 'ontologie' not found, using hardcoded namespace.");
                NS = "http://www.semanticweb.org/user/ontologies/2024/8/untitled-ontology-5#";
            }


            Model inferredModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");


            String sparqlSelect =
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ontologie: <" + NS + "> " +
                            "SELECT ?article ?title ?content " +
                            "WHERE { " +
                            "    ?article rdf:type ontologie:Article . " +
                            "    ?article ontologie:title ?title . " +
                            "    ?article ontologie:content ?content . " +
                            "}";

            // Execute SPARQL query and print results
            try (QueryExecution qexec = QueryExecutionFactory.create(sparqlSelect, inferredModel)) {
                ResultSet resultSet = qexec.execSelect();

                while (resultSet.hasNext()) {
                    var solution = resultSet.nextSolution();
                    RDFNode articleNode = solution.get("article");
                    RDFNode titleNode = solution.get("title");
                    RDFNode contentNode = solution.get("content");

                    System.out.println("Article: " + articleNode.toString() +
                            ", Title: " + titleNode.toString() +
                            ", Content: " + contentNode.toString());
                }
            } catch (Exception e) {
                System.err.println("Erreur pendant l'exécution de la requête SPARQL: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Erreur lors de la lecture du modèle depuis l'ontologie");
        }
    }
}
