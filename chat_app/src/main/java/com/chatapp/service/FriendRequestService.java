package com.chatapp.service;

import com.chatapp.entity.FriendRequest;
import com.chatapp.entity.Friendship;
import com.chatapp.entity.Enums.RequestStatus;
import com.chatapp.exception.BadRequestException;
import com.chatapp.exception.ResourceNotFoundException;
import com.chatapp.mapper.DtoMapper;
import com.chatapp.dto.ApiDtos.FriendRequestDto;
import com.chatapp.repository.FriendRequestRepository;
import com.chatapp.repository.FriendshipRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FriendRequestService {
    private final FriendRequestRepository requests; private final FriendshipRepository friends; private final UserRepository users; private final CurrentUserService current; private final DtoMapper mapper;
    public FriendRequestService(FriendRequestRepository requests, FriendshipRepository friends, UserRepository users, CurrentUserService current, DtoMapper mapper) { this.requests=requests; this.friends=friends; this.users=users; this.current=current; this.mapper=mapper; }
    @Transactional public FriendRequestDto send(Long receiverId, org.springframework.security.core.Authentication auth) {
        var sender=current.get(auth); var receiver=current.byId(receiverId);
        if (sender.getId().equals(receiverId)) throw new BadRequestException("You cannot send a friend request to yourself.");
        if (!friends.findPair(sender.getId(),receiverId).isEmpty()) throw new BadRequestException("You are already friends.");
        if (requests.existsBySenderIdAndReceiverIdAndStatus(sender.getId(),receiverId,RequestStatus.PENDING)) throw new BadRequestException("A friend request is already pending.");
        if (requests.existsBySenderIdAndReceiverIdAndStatus(receiverId,sender.getId(),RequestStatus.PENDING)) throw new BadRequestException("This user has already sent you a request. Accept it from your received requests.");
        FriendRequest request=new FriendRequest(); request.setSender(sender); request.setReceiver(receiver); request.setStatus(RequestStatus.PENDING);
        request.setPendingPairKey(Math.min(sender.getId(),receiverId)+":"+Math.max(sender.getId(),receiverId));
        return mapper.request(requests.save(request));
    }
    @Transactional(readOnly=true) public List<FriendRequestDto> received(org.springframework.security.core.Authentication auth) { return requests.findByReceiverIdAndStatusOrderByCreatedAtDesc(current.get(auth).getId(),RequestStatus.PENDING).stream().map(mapper::request).toList(); }
    @Transactional(readOnly=true) public List<FriendRequestDto> sent(org.springframework.security.core.Authentication auth) { return requests.findBySenderIdAndStatusOrderByCreatedAtDesc(current.get(auth).getId(),RequestStatus.PENDING).stream().map(mapper::request).toList(); }
    @Transactional public FriendRequestDto accept(Long id, org.springframework.security.core.Authentication auth) {
        Long me=current.get(auth).getId(); FriendRequest request=requests.findByIdAndReceiverId(id,me).orElseThrow(() -> new ResourceNotFoundException("Friend request not found."));
        ensurePending(request); request.setStatus(RequestStatus.ACCEPTED); request.setPendingPairKey(null);
        Long a=request.getSender().getId(), b=request.getReceiver().getId();
        if (friends.findPair(a,b).isEmpty()) { Friendship f=new Friendship(); f.setUser1(a < b ? request.getSender() : request.getReceiver()); f.setUser2(a < b ? request.getReceiver() : request.getSender()); friends.save(f); }
        return mapper.request(request);
    }
    @Transactional public FriendRequestDto reject(Long id, org.springframework.security.core.Authentication auth) {
        FriendRequest request=requests.findByIdAndReceiverId(id,current.get(auth).getId()).orElseThrow(() -> new ResourceNotFoundException("Friend request not found.")); ensurePending(request); request.setStatus(RequestStatus.REJECTED); request.setPendingPairKey(null); return mapper.request(request);
    }
    @Transactional public void cancel(Long id, org.springframework.security.core.Authentication auth) {
        FriendRequest request=requests.findByIdAndSenderId(id,current.get(auth).getId()).orElseThrow(() -> new ResourceNotFoundException("Friend request not found.")); ensurePending(request); request.setStatus(RequestStatus.CANCELLED); request.setPendingPairKey(null);
    }
    private void ensurePending(FriendRequest request) { if (request.getStatus()!=RequestStatus.PENDING) throw new BadRequestException("This friend request is no longer pending."); }
}
