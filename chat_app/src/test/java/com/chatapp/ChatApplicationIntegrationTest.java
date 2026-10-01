package com.chatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={"spring.datasource.url=jdbc:h2:mem:chatapp;MODE=MySQL;DB_CLOSE_DELAY=-1", "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false", "jwt.secret=integration-test-secret-key-at-least-32-bytes", "jwt.expiration=86400000"})
@AutoConfigureMockMvc
class ChatApplicationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @LocalServerPort int port;

    @BeforeEach void cleanDatabase() {
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        for (String table : new String[]{"messages","conversation_members","conversations","friendships","friend_requests","users"}) jdbc.execute("TRUNCATE TABLE " + table);
        jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test void registrationLoginAndDuplicateCredentialsAreValidated() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
        mvc.perform(get("/index.html")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML)).andExpect(content().string(org.hamcrest.Matchers.containsString("ChatApp")));
        String payload="{\"username\":\"alice\",\"email\":\"alice@example.com\",\"displayName\":\"Alice\",\"password\":\"Password123\"}";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isCreated()).andExpect(jsonPath("$.user.password").doesNotExist());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"alicia\",\"email\":\"alice@example.com\",\"displayName\":\"Alicia\",\"password\":\"Password123\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"identifier\":\"alice@example.com\",\"password\":\"Password123\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"identifier\":\"alice\",\"password\":\"wrong-password\"}")).andExpect(status().isUnauthorized());
    }

    @Test void friendConversationMessagingAndSeenFlowEnforcesMembership() throws Exception {
        Account alice=register("alice","alice@example.com","Alice");
        Account bob=register("bob","bob@example.com","Bob");
        Account mallory=register("mallory","mallory@example.com","Mallory");

        mvc.perform(get("/api/users/search").param("keyword","bob").header("Authorization",alice.auth()))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].friendshipStatus").value("NONE")).andExpect(jsonPath("$[0].email").doesNotExist()).andExpect(jsonPath("$[0].password").doesNotExist());
        mvc.perform(post("/api/friend-requests/"+bob.id()).header("Authorization",alice.auth())).andExpect(status().isCreated());
        mvc.perform(post("/api/friend-requests/"+bob.id()).header("Authorization",alice.auth())).andExpect(status().isBadRequest());
        MvcResult incoming=mvc.perform(get("/api/friend-requests/received").header("Authorization",bob.auth())).andExpect(status().isOk()).andExpect(jsonPath("$[0].sender.username").value("alice")).andReturn();
        long requestId=json.readTree(incoming.getResponse().getContentAsString()).get(0).get("id").asLong();
        mvc.perform(put("/api/friend-requests/"+requestId+"/accept").header("Authorization",bob.auth())).andExpect(status().isOk());
        mvc.perform(get("/api/friends").header("Authorization",alice.auth())).andExpect(status().isOk()).andExpect(jsonPath("$[0].username").value("bob"));

        JsonNode created=json.readTree(mvc.perform(post("/api/conversations/private/"+bob.id()).header("Authorization",alice.auth())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        long conversationId=created.get("id").asLong();
        mvc.perform(post("/api/conversations/private/"+bob.id()).header("Authorization",alice.auth())).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(conversationId));
        mvc.perform(post("/api/conversations/"+conversationId+"/messages").header("Authorization",alice.auth()).contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Hello Bob\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.senderId").value(alice.id()));
        mvc.perform(get("/api/conversations/"+conversationId+"/messages").header("Authorization",mallory.auth())).andExpect(status().isForbidden());
        MvcResult bobHistory=mvc.perform(get("/api/conversations/"+conversationId+"/messages").header("Authorization",bob.auth())).andExpect(status().isOk()).andReturn();
        long messageId=json.readTree(bobHistory.getResponse().getContentAsString()).get("content").get(0).get("id").asLong();
        mvc.perform(put("/api/messages/"+messageId+"/read").header("Authorization",bob.auth())).andExpect(status().isNoContent());
        mvc.perform(get("/api/conversations/"+conversationId+"/messages").header("Authorization",alice.auth())).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].isRead").value(true));
    }

    @Test void usersCanOnlyUpdateTheirOwnProfile() throws Exception {
        Account alice=register("alice","alice@example.com","Alice");
        Account bob=register("bob","bob@example.com","Bob");
        mvc.perform(put("/api/users/me").header("Authorization",alice.auth()).contentType(MediaType.APPLICATION_JSON).content("{\"displayName\":\"Alice A\",\"avatarUrl\":\"https://example.com/a.png\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.displayName").value("Alice A")).andExpect(jsonPath("$.email").value("alice@example.com"));
        mvc.perform(get("/api/users/me").header("Authorization",bob.auth())).andExpect(status().isOk()).andExpect(jsonPath("$.displayName").value("Bob"));
    }

    @Test void friendRequestsCanBeRejectedCancelledAndFriendshipsRemoved() throws Exception {
        Account alice=register("alice","alice@example.com","Alice");
        Account bob=register("bob","bob@example.com","Bob");
        Account charlie=register("charlie","charlie@example.com","Charlie");
        mvc.perform(post("/api/friend-requests/"+alice.id()).header("Authorization",alice.auth())).andExpect(status().isBadRequest());
        mvc.perform(post("/api/friend-requests/"+bob.id()).header("Authorization",alice.auth())).andExpect(status().isCreated());
        MvcResult request=mvc.perform(get("/api/friend-requests/received").header("Authorization",bob.auth())).andReturn();
        long id=json.readTree(request.getResponse().getContentAsString()).get(0).get("id").asLong();
        mvc.perform(put("/api/friend-requests/"+id+"/accept").header("Authorization",charlie.auth())).andExpect(status().isNotFound());
        mvc.perform(delete("/api/friend-requests/"+id).header("Authorization",charlie.auth())).andExpect(status().isNotFound());
        mvc.perform(delete("/api/friend-requests/"+id).header("Authorization",alice.auth())).andExpect(status().isNoContent());
        mvc.perform(delete("/api/friend-requests/"+id).header("Authorization",alice.auth())).andExpect(status().isBadRequest());

        mvc.perform(post("/api/friend-requests/"+bob.id()).header("Authorization",alice.auth())).andExpect(status().isCreated());
        request=mvc.perform(get("/api/friend-requests/received").header("Authorization",bob.auth())).andReturn();
        id=json.readTree(request.getResponse().getContentAsString()).get(0).get("id").asLong();
        mvc.perform(put("/api/friend-requests/"+id+"/reject").header("Authorization",bob.auth())).andExpect(status().isOk());
        mvc.perform(get("/api/friends").header("Authorization",alice.auth())).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());

        mvc.perform(post("/api/friend-requests/"+bob.id()).header("Authorization",alice.auth())).andExpect(status().isCreated());
        request=mvc.perform(get("/api/friend-requests/received").header("Authorization",bob.auth())).andReturn();
        id=json.readTree(request.getResponse().getContentAsString()).get(0).get("id").asLong();
        mvc.perform(put("/api/friend-requests/"+id+"/accept").header("Authorization",bob.auth())).andExpect(status().isOk());
        mvc.perform(delete("/api/friends/"+bob.id()).header("Authorization",alice.auth())).andExpect(status().isNoContent());
        mvc.perform(delete("/api/friends/"+bob.id()).header("Authorization",charlie.auth())).andExpect(status().isNotFound());
    }

    @Test void authenticatedStompSessionsReceivePersistedPrivateMessages() throws Exception {
        Account alice=register("alice","alice@example.com","Alice");
        Account bob=register("bob","bob@example.com","Bob");
        mvc.perform(post("/api/friend-requests/"+bob.id()).header("Authorization",alice.auth())).andExpect(status().isCreated());
        MvcResult incoming=mvc.perform(get("/api/friend-requests/received").header("Authorization",bob.auth())).andReturn();
        long requestId=json.readTree(incoming.getResponse().getContentAsString()).get(0).get("id").asLong();
        mvc.perform(put("/api/friend-requests/"+requestId+"/accept").header("Authorization",bob.auth())).andExpect(status().isOk());
        long conversationId=json.readTree(mvc.perform(post("/api/conversations/private/"+bob.id()).header("Authorization",alice.auth())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();

        BlockingQueue<Map<?,?>> received=new LinkedBlockingQueue<>();
        BlockingQueue<Map<?,?>> echoed=new LinkedBlockingQueue<>();
        WebSocketStompClient client=new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
        StompSession bobSession=connect(client,bob,received);
        StompSession aliceSession=connect(client,alice,echoed);
        aliceSession.send("/app/chat",Map.of("conversationId",conversationId,"content","Hello Bob"));
        Map<?,?> frame=received.poll(10,TimeUnit.SECONDS);
        org.junit.jupiter.api.Assertions.assertNotNull(frame,"Bob should receive the private STOMP message.");
        org.junit.jupiter.api.Assertions.assertEquals("Hello Bob",frame.get("content"));
        org.junit.jupiter.api.Assertions.assertEquals(alice.id(),((Number)frame.get("senderId")).longValue());
        org.junit.jupiter.api.Assertions.assertEquals("Hello Bob",echoed.poll(10,TimeUnit.SECONDS).get("content"));
        mvc.perform(get("/api/conversations/"+conversationId+"/messages").header("Authorization",bob.auth())).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].content").value("Hello Bob"));
        aliceSession.disconnect(); bobSession.disconnect(); client.stop();
    }

    private StompSession connect(WebSocketStompClient client, Account account, BlockingQueue<Map<?,?>> received) throws Exception {
        StompHeaders connectHeaders=new StompHeaders(); connectHeaders.add("Authorization",account.auth());
        StompSessionHandlerAdapter handler=new StompSessionHandlerAdapter() {
            @Override public void afterConnected(StompSession session, StompHeaders headers) {
                session.subscribe("/user/queue/messages",new StompFrameHandler() {
                    @Override public Type getPayloadType(StompHeaders headers) { return Map.class; }
                    @Override public void handleFrame(StompHeaders headers,Object payload) { received.offer((Map<?,?>)payload); }
                });
            }
        };
        return client.connectAsync("ws://localhost:"+port+"/ws",new WebSocketHttpHeaders(),connectHeaders,handler).get(10,TimeUnit.SECONDS);
    }

    private Account register(String username,String email,String displayName) throws Exception {
        String payload=json.writeValueAsString(java.util.Map.of("username",username,"email",email,"displayName",displayName,"password","Password123"));
        JsonNode result=json.readTree(mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        JsonNode user=result.get("user"); return new Account(user.get("id").asLong(),username,result.get("token").asText());
    }
    private record Account(long id,String username,String token) { String auth() { return "Bearer "+token; } }
}
