package com.chatapp.service;

import com.chatapp.dto.ApiDtos.UserDto;
import com.chatapp.exception.ResourceNotFoundException;
import com.chatapp.mapper.DtoMapper;
import com.chatapp.repository.FriendshipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FriendService {
    private final FriendshipRepository friends; private final CurrentUserService current; private final DtoMapper mapper;
    public FriendService(FriendshipRepository friends, CurrentUserService current, DtoMapper mapper) { this.friends=friends; this.current=current; this.mapper=mapper; }
    @org.springframework.transaction.annotation.Transactional(readOnly=true) public List<UserDto> list(org.springframework.security.core.Authentication auth) {
        Long id=current.get(auth).getId();
        return friends.findAllForUser(id).stream().map(f -> mapper.user(f.getUser1().getId().equals(id) ? f.getUser2() : f.getUser1())).toList();
    }
    @Transactional public void remove(Long userId, org.springframework.security.core.Authentication auth) {
        Long me=current.get(auth).getId(); if (friends.deletePair(me,userId)==0) throw new ResourceNotFoundException("Friendship not found.");
    }
    public boolean areFriends(Long a, Long b) { return !friends.findPair(a,b).isEmpty(); }
}
