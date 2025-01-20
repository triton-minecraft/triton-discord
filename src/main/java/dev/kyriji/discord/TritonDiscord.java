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

	public static void main(String[] args) {
		Properties props = new Properties();
		try (InputStream input = TritonDiscord.class.getClassLoader().getResourceAsStream("config.properties")) {
			if (input == null) {
				logger.error("unable to find config.properties in resources");
				return;
			}
			props.load(input);
		} catch (Exception e) {
			logger.error("error loading config.properties", e);
			return;
		}

		String token = props.getProperty("discord.token");
		if (token == null) {
			logger.error("discord token not found in config");
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