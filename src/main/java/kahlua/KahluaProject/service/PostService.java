package kahlua.KahluaProject.service;

import kahlua.KahluaProject.apipayload.code.status.ErrorStatus;
import kahlua.KahluaProject.converter.PostConverter;
import kahlua.KahluaProject.domain.post.Post;
import kahlua.KahluaProject.domain.post.PostImage;
import kahlua.KahluaProject.domain.post.PostLikes;
import kahlua.KahluaProject.domain.user.User;
import kahlua.KahluaProject.domain.user.UserType;
import kahlua.KahluaProject.dto.post.request.PostCreateRequest;
import kahlua.KahluaProject.dto.post.response.PostCreateResponse;
import kahlua.KahluaProject.dto.post.response.PostGetResponse;
import kahlua.KahluaProject.dto.post.response.PostImageCreateResponse;
import kahlua.KahluaProject.exception.GeneralException;
import kahlua.KahluaProject.repository.PostImageRepository;
import kahlua.KahluaProject.repository.UserRepository;
import kahlua.KahluaProject.repository.post.PostLikesRepository;
import kahlua.KahluaProject.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostLikesRepository postLikesRepository;

    @Transactional
    public PostCreateResponse createPost(PostCreateRequest postCreateRequest, User user) {

        // 공지사항인 경우 admin인지 확인
        if(user.getUserType() != UserType.ADMIN){
            throw new GeneralException(ErrorStatus.UNAUTHORIZED);
        }

        Post post = PostConverter.toPost(postCreateRequest, user);
        postRepository.save(post);

        List<PostImage> imageUrls = PostConverter.toPostImage(postCreateRequest.getImageUrls(), post);
        if(imageUrls.size() > 10) throw new GeneralException(ErrorStatus.IMAGE_NOT_UPLOAD);

        postImageRepository.saveAll(imageUrls);
        List<PostImageCreateResponse> imageUrlResponses = PostConverter.toPostImageCreateResponse(imageUrls);

        PostCreateResponse postCreateResponse = PostConverter.toPostCreateResponse(post, user, imageUrlResponses);

        return postCreateResponse;
    }

    @Transactional
    public boolean createPostLike(User user, Long post_id) {

        // 선택한 게시글이 존재하는 지 확인
        Post existingPost = postRepository.findById(post_id)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_NOT_FOUND));

        // 사용자가 좋아요를 눌렀는지 확인
        Optional<PostLikes> existingLike = postLikesRepository.findByPostAndUser(existingPost, user);

        if (existingLike.isPresent()) {
            postLikesRepository.delete(existingLike.get());
            System.out.println("like"+existingLike.get());

            existingPost.decreaseLikes();
            postRepository.save(existingPost);
            return false;  // 좋아요 취소
        } else {
            PostLikes newLike = PostConverter.toPostLikes(existingPost, user);
            postLikesRepository.save(newLike);

            existingPost.increaseLikes();
            postRepository.save(existingPost);
            return true;  // 좋아요 생성
        }
    }

    @Transactional
    public PostGetResponse viewPost(Long post_id, User user) {

        // 공지사항 조회의 경우 kahlua 혹은 ADMIN인지 확인
        if(user.getUserType() != UserType.KAHLUA && user.getUserType() != UserType.ADMIN){
            throw new GeneralException(ErrorStatus.UNAUTHORIZED);
        }

        // 선택한 게시글이 존재하는 지 확인
        Post existingPost = postRepository.findById(post_id)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_NOT_FOUND));

        return PostConverter.toPostGetResponse(existingPost);
    }
}
