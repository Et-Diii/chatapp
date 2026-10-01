package com.chatapp.service;

import com.chatapp.dto.ApiDtos.*;
import com.chatapp.entity.User;
import com.chatapp.mapper.DtoMapper;
import com.chatapp.repository.UserRepository;
import com.chatapp.repository.FriendshipRepository;
import com.chatapp.repository.FriendRequestRepository;
import com.chatapp.entity.Enums.RequestStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService {
    private final UserRepository users; private final CurrentUserService current; private final DtoMapper mapper; private final FriendshipRepository friendships; private final FriendRequestRepository requests;
    public UserService(UserRepository users, CurrentUserService current, DtoMapper mapper, FriendshipRepository friendships, FriendRequestRepository requests) { this.users=users; this.current=current; this.mapper=mapper; this.friendships=friendships; this.requests=requests; }
    public ProfileDto me(org.springframework.security.core.Authentication auth) { return mapper.profile(current.get(auth)); }
    public UserDto get(Long id) { return mapper.user(current.byId(id)); }
    @org.springframework.transaction.annotation.Transactional(readOnly=true) public List<SearchUserDto> search(String keyword, org.springframework.security.core.Authentication auth) {
        if (keyword == null || keyword.isBlank()) return List.of();
        Long viewer=current.get(auth).getId();
        return users.search(viewer, keyword.trim(), PageRequest.of(0, 30)).map(user -> {
            String status="NONE"; Long requestId=null;
            if (!friendships.findPair(viewer,user.getId()).isEmpty()) status="FRIENDS";
            else {
                var sent=requests.findBySenderIdAndStatusOrderByCreatedAtDesc(viewer, RequestStatus.PENDING).stream().filter(r -> r.getReceiver().getId().equals(user.getId())).findFirst();
                var received=requests.findBySenderIdAndStatusOrderByCreatedAtDesc(user.getId(), RequestStatus.PENDING).stream().filter(r -> r.getReceiver().getId().equals(viewer)).findFirst();
                if (sent.isPresent()) { status="SENT"; requestId=sent.get().getId(); }
                else if (received.isPresent()) { status="RECEIVED"; requestId=received.get().getId(); }
            }
            return new SearchUserDto(user.getId(), user.getUsername(), user.getDisplayName(), user.getAvatarUrl(), user.getStatus(), status, requestId);
        }).getContent();
    }
    @Transactional public ProfileDto update(ProfileUpdateRequest request, org.springframework.security.core.Authentication auth) {
        User user=current.get(auth); user.setDisplayName(request.displayName().trim()); user.setAvatarUrl(request.avatarUrl() == null || request.avatarUrl().isBlank() ? null : request.avatarUrl().trim());
        return mapper.profile(user);
    }
}
