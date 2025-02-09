package me.sonam.friendship.persist.repo;



import me.sonam.friendship.persist.entity.Friendship;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FriendshipRepository extends ReactiveCrudRepository<Friendship, UUID> {
    Mono<Boolean>   existsByUserIdAndFriendId(UUID userID, UUID friendId);
    Flux<Friendship> findByUserIdAndFriendId(UUID userID, UUID friendId);
    Mono<Integer> deleteByRequestAcceptedIsFalseAndUserIdAndFriendId(UUID userId, UUID friendId);

    @Query("select * from Friendship where (user_id=:userId or friend_id =:userId) and request_accepted=true and" +
            " response_sent_date is not null order by response_sent_date desc")
    Flux<Friendship> findAcceptedFriendsForUser(@Param("userId")UUID userId);

    Mono<Boolean> existsByUserIdAndFriendIdAndRequestAcceptedIsTrueAndResponseSentDateNotNull(UUID userId, UUID friendId);
}