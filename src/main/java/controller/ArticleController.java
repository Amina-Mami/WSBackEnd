package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.ArticleService;

import java.util.Map;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<String> getArticles() {
        try {
            String result = articleService.queryArticles();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération des articles : " + e.getMessage());
        }
    }

    @GetMapping(value = "/{articleId}", produces = "application/json")
    public ResponseEntity<String> getArticleById(@PathVariable String articleId) {
        try {
            String result = articleService.queryArticleById(articleId);
            if (result != null && !result.isEmpty()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Article non trouvé");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération de l'article : " + e.getMessage());
        }
    }

    @GetMapping(value = "/search", produces = "application/json")
    public ResponseEntity<String> searchArticleByTitle(@RequestParam String title) {
        try {
            String result = articleService.queryArticleByTitle(title);
            if (result != null && !result.isEmpty()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucun article trouvé avec ce titre");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la recherche de l'article : " + e.getMessage());
        }
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> addArticle(@RequestBody Map<String, Object> newArticle) {
        String articleId = (String) newArticle.get("article_id");
        String title = (String) newArticle.get("title");
        String content = (String) newArticle.get("content");
        String createdAt = (String) newArticle.get("created_at");

        try {
            articleService.addArticle(articleId, title, content, createdAt);
            return ResponseEntity.status(HttpStatus.CREATED).body("{\"message\": \"Article ajouté avec succès\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Erreur lors de l'ajout de l'article : " + e.getMessage() + "\"}");
        }
    }

    @PutMapping(value = "/{articleId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> updateArticle(@PathVariable String articleId, @RequestBody Map<String, Object> updatedArticle) {
        String newTitle = (String) updatedArticle.get("title");
        String newContent = (String) updatedArticle.get("content");
        String newCreatedAt = (String) updatedArticle.get("created_at");

        try {
            articleService.updateArticle(articleId, newTitle, newContent, newCreatedAt);
            return ResponseEntity.ok("{\"message\": \"Article mis à jour avec succès\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Erreur lors de la mise à jour de l'article : " + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping(value = "/{articleId}", produces = "application/json")
    public ResponseEntity<String> deleteArticle(@PathVariable String articleId) {
        try {
            articleService.deleteArticle(articleId);
            return ResponseEntity.ok("{\"message\": \"Article supprimé avec succès\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Erreur lors de la suppression de l'article : " + e.getMessage() + "\"}");
        }
    }
}
