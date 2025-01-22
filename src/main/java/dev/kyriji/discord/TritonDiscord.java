package dev.kyriji.discord;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.Properties;

public class TritonDiscord {
	private static final Logger logger = LoggerFactory.getLogger(TritonDiscord.class);
	public static JDA jda;

	private static String getMongoURI() {
		// Try environment variable first
		String uri = System.getenv("MONGO_URI");
		if (uri != null && !uri.trim().isEmpty()) {
			logger.info("using mongo uri from environment");
			return uri;
		}

		// Fall back to config.properties
		Properties props = new Properties();
		try (InputStream input = TritonDiscord.class.getClassLoader().getResourceAsStream("config.properties")) {
			if (input == null) {
				logger.error("unable to find config.properties in resources");
				return null;
			}
			props.load(input);
			uri = props.getProperty("mongo.uri");
			if (uri != null && !uri.trim().isEmpty()) {
				logger.info("using mongo uri from config.properties");
				return uri;
			}
		} catch (Exception e) {
			logger.error("error loading config.properties", e);
		}

		logger.error("mongo uri not found in environment or config");
		return null;
	}

	public static void main(String[] args) {
		String uri = getMongoURI();
		if (uri == null) return;

		Document document;
		try (MongoClient mongoClient = MongoClients.create(uri)) {
			MongoDatabase database = mongoClient.getDatabase("configuration");
			MongoCollection<Document> collection = database.getCollection("discord");
			document = collection.find().first();
			if (document != null) {
				logger.info(document.toJson());
			} else {
				logger.error("could not find document");
			}
		} catch (Exception e) {
			logger.error("error fetching document", e);
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

		jda = JDABuilder.create(document.getString("discordToken"), intents)
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