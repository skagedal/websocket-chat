package se.kry.chat;

public record DeployedChatVerticle(
    ChatVerticle verticle,
    String deploymentId
) {
}
