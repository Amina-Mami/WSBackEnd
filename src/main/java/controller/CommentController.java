package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.CommentService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getComments() {
        String result = commentService.queryComments();
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> addComment(@RequestBody Map<String, Object> newComment) {
        String commentId = (String) newComment.get("comment_id");
        String content = (String) newComment.get("content");
        String author = (String) newComment.get("author");
        String createdAt = (String) newComment.get("created_at");

        commentService.addComment(commentId, content, author, createdAt);

        Map<String, Object> response = new HashMap<>();
        response.put("comment_id", commentId);
        response.put("content", content);
        response.put("author", author);
        response.put("created_at", createdAt);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateComment(
            @PathVariable String commentId,
            @RequestBody Map<String, Object> updatedComment) {

        String newContent = (String) updatedComment.get("content");
        String newAuthor = (String) updatedComment.get("author");
        String newCreatedAt = (String) updatedComment.get("created_at");

        commentService.updateComment(commentId, newContent, newAuthor, newCreatedAt);

        return ResponseEntity.ok("Commentaire mis à jour avec succès !");
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable String commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok("Commentaire supprimé avec succès: " + commentId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression du commentaire: " + e.getMessage());
        }
    }
}
