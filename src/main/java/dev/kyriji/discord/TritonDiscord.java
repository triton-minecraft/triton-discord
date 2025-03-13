package dev.kyriji.discord;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.kyriji.discord.controllers.EnvManager;
import dev.kyriji.discord.models.EnvData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class TritonDiscord {
	private static final Logger logger = LoggerFactory.getLogger(TritonDiscord.class);
	public static JDA jda;

	public static void main(String[] args) {
		EnvManager.init();

		EnvData envData = EnvManager.getEnvData();

		Document document;
		try (MongoClient mongoClient = MongoClients.create(envData.uri())) {
			MongoDatabase database = mongoClient.getDatabase(envData.database());
			MongoCollection<Document> collection = database.getCollection(envData.collection());
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
				Commands.slash("ping", "Test ping command")
		).queue();

		try {
			jda.awaitReady();
		} catch (InterruptedException e) {
			logger.error("error while waiting for jda to be ready", e);
			throw new RuntimeException(e);
		}

		logger.info("bot is ready");
	}
}