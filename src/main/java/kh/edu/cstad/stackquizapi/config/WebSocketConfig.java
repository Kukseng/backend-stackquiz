package kh.edu.cstad.stackquizapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
        registry.addEndpoint("/ws-native").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public org.springframework.messaging.Message<?> preSend(
                    org.springframework.messaging.Message<?> message,
                    org.springframework.messaging.MessageChannel channel)
            {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String principalName = null;

                    List<String> participantIds = accessor.getNativeHeader("participantId");
                    if (participantIds != null && !participantIds.isEmpty() && participantIds.get(0) != null && !participantIds.get(0).isBlank()) {
                        principalName = participantIds.get(0);
                    }

                    if (principalName == null || principalName.isBlank()) {
                        List<String> nicks = accessor.getNativeHeader("nickname");
                        if (nicks != null && !nicks.isEmpty() && nicks.get(0) != null && !nicks.get(0).isBlank()) {
                            principalName = nicks.get(0);
                        }
                    }

                    if (principalName == null || principalName.isBlank()) {
                        principalName = accessor.getSessionId();
                    }

                    final String finalPrincipalName = principalName;
                    accessor.setUser(new Principal() {
                        @Override
                        public String getName() {
                            return finalPrincipalName;
                        }
                    });
                }
                return message;
            }
        });
    }
}
