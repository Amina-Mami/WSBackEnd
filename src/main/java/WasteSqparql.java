import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import tools.JenaEngine;

public class WasteSqparql {
    public static void main(String[] args) {
        String NS = "";


        Model model = JenaEngine.readModel("data/GestionDechets.owl");

        if (model != null) {

            NS = model.getNsPrefixURI("");


            Model inferredModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");


            String sparqlSelect =
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                            "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                            "SELECT ?waste ?type ?weight " +
                            "WHERE { " +
                            "    ?waste rdf:type <" + NS + "Déchet> . " +
                            "    ?waste <" + NS + "type> ?type . " +
                            "    ?waste <" + NS + "weight> ?weight . " +
                            "}";


            try (QueryExecution qexec = QueryExecutionFactory.create(sparqlSelect, inferredModel)) {
                ResultSet resultSet = qexec.execSelect();


                while (resultSet.hasNext()) {

                    var solution = resultSet.nextSolution();
                    RDFNode wasteNode = solution.get("waste");
                    RDFNode typeNode = solution.get("type");
                    RDFNode weightNode = solution.get("weight");


                    System.out.println("Déchet: " + wasteNode.toString() +
                            ", Type: " + typeNode.toString() +
                            ", Poids: " + weightNode.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Erreur lors de la lecture du modèle depuis l'ontologie");
        }
    }
}
