package kahlua.KahluaProject.dto.post.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentsItemResponse {

    @Schema(description = "댓글 번호", example = "1")
    private Long id;

    @Schema(description = "게시글 번호", example = "1")
    private Long postId;

    @Schema(description = "댓글 작성자", example = "깔루아 홍길동")
    private String user;

    @Schema(description = "댓글 내용", example = "감사합니다.")
    private String content;

    @Schema(description = "대댓글인 경우 부모 댓글 id", example = "네 수고하세요.")
    private Long parentCommentId;

    @Schema(description = "작성한 날짜", example = "2024-08-01T00:00:00")
    private LocalDateTime created_at;

    @Schema(description = "삭제된 날짜", example = "2024.01.01")
    private LocalDateTime deletedAt;
}
