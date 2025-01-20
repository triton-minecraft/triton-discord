package dev.kyriji.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.Properties;

public class TritonDiscord {
	private static final Logger logger = LoggerFactory.getLogger(TritonDiscord.class);
	public static JDA jda;

	private static String getDiscordToken() {
		// Try environment variable first
		String token = System.getenv("DISCORD_TOKEN");
		if (token != null && !token.trim().isEmpty()) {
			logger.info("using discord token from environment");
			return token;
		}

		// Fall back to config.properties
		Properties props = new Properties();
		try (InputStream input = TritonDiscord.class.getClassLoader().getResourceAsStream("config.properties")) {
			if (input == null) {
				logger.error("unable to find config.properties in resources");
				return null;
			}
			props.load(input);
			token = props.getProperty("discord.token");
			if (token != null && !token.trim().isEmpty()) {
				logger.info("using discord token from config.properties");
				return token;
			}
		} catch (Exception e) {
			logger.error("error loading config.properties", e);
		}

		logger.error("discord token not found in environment or config");
		return null;
	}

	public static void main(String[] args) {
		String token = getDiscordToken();
		if (token == null) {
			return;
		}

		EnumSet<GatewayIntent> intents = EnumSet.of(
				GatewayIntent.GUILD_MEMBERS,
				GatewayIntent.GUILD_MESSAGES,
				GatewayIntent.GUILD_PRESENCES,
				GatewayIntent.GUILD_VOICE_STATES,
				GatewayIntent.GUILD_EXPRESSIONS,
				GatewayIntent.SCHEDULED_EVENTS,
				GatewayIntent.MESSAGE_CONTENT
		);

		jda = JDABuilder.create(token, intents)
				.addEventListeners(new TestListener())
				.build();

		CommandListUpdateAction commands = jda.updateCommands();
		commands.addCommands(
				Commands.slash("ping", "Makes the bot say what you tell it to")
		);
		commands.queue();

		try {
			jda.awaitReady();
		} catch (InterruptedException e) {
			logger.error("error while waiting for jda to be ready", e);
			throw new RuntimeException(e);
		}

		logger.info("bot is ready");
	}
}