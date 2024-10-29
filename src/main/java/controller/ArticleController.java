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
        String result = articleService.queryArticles();
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/{articleId}", produces = "application/json")
    public ResponseEntity<String> getArticleById(@PathVariable String articleId) {
        String result = articleService.queryArticleById(articleId);
        if (result != null && !result.isEmpty()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Article non trouvé");
        }
    }

    @GetMapping(value = "/search", produces = "application/json")
    public ResponseEntity<String> searchArticleByTitle(@RequestParam String title) {
        String result = articleService.queryArticleByTitle(title);
        if (result != null && !result.isEmpty()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucun article trouvé avec ce titre");
        }
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<String> addArticle(@RequestBody Map<String, Object> newArticle) {
        String articleId = (String) newArticle.get("article_id");
        String title = (String) newArticle.get("title");
        String content = (String) newArticle.get("content");
        String createdAt = (String) newArticle.get("created_at");

        try {
            articleService.addArticle(articleId, title, content, createdAt);
            return ResponseEntity.status(HttpStatus.CREATED).body("Article ajouté avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'ajout de l'article : " + e.getMessage());
        }
    }

    @PutMapping(value = "/{articleId}", consumes = "application/json")
    public ResponseEntity<String> updateArticle(@PathVariable String articleId, @RequestBody Map<String, Object> updatedArticle) {
        String newTitle = (String) updatedArticle.get("title");
        String newContent = (String) updatedArticle.get("content");
        String newCreatedAt = (String) updatedArticle.get("created_at");

        try {
            articleService.updateArticle(articleId, newTitle, newContent, newCreatedAt);
            return ResponseEntity.ok("Article mis à jour avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour de l'article : " + e.getMessage());
        }
    }

    @DeleteMapping("/{articleId}")
    public ResponseEntity<String> deleteArticle(@PathVariable String articleId) {
        try {
            articleService.deleteArticle(articleId);
            return ResponseEntity.ok("Article supprimé avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression de l'article : " + e.getMessage());
        }
    }
}
