package at.technikum.springrestbackend.repository;

import at.technikum.springrestbackend.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    List<BlogPost> findAllByOrderByCreatedAtDesc();

    Page<BlogPost> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    void deleteByUserId(Long userId);
}