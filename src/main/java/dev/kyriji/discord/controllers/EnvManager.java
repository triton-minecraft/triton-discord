package dev.kyriji.discord.controllers;

import dev.kyriji.discord.TritonDiscord;
import dev.kyriji.discord.models.EnvData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class EnvManager {
	private static final Logger logger = LoggerFactory.getLogger(EnvManager.class);

	private static EnvData envData;

	public static void init() {
		boolean success = fetchFromEnvironment();
		if (!success) success = fetchFromResource();
		if (!success) throw new RuntimeException("Could not find the necessary environment data");
	}

	public static boolean fetchFromEnvironment() {
		logger.info("attempting to load env data from system environment");

		String uri = System.getenv("MONGO_URI");
		String database = System.getenv("MONGO_DATABASE");
		String collection = System.getenv("MONGO_COLLECTION");

		if (uri == null && database == null && collection == null) {
			logger.info("could not find any environment variables");
			return false;
		} else if (uri == null || uri.trim().isEmpty()) {
			logger.info("could not find env variable MONGO_URI");
			return false;
		} else if(database == null || database.trim().isEmpty()) {
			logger.info("could not find env variable MONGO_DATABASE");
			return false;
		} else if (collection == null || collection.trim().isEmpty()) {
			logger.info("could not find env variable MONGO_COLLECTION");
			return false;
		}

		logger.info("fetched env data from system environment");
		setEnvData(uri, database, collection);
		return true;
	}

	public static boolean fetchFromResource() {
		logger.info("attempting to load env data from config.properties");

		Properties props = new Properties();
		try (InputStream input = TritonDiscord.class.getClassLoader().getResourceAsStream("config.properties")) {
			if (input == null) {
				logger.error("unable to find config.properties in resources. THIS ONLY WORKS LOCALLY");
				return false;
			}
			props.load(input);
		} catch (Exception e) {
			logger.error("error loading config.properties", e);
		}

		String uri = props.getProperty("mongo.uri");
		String database = props.getProperty("mongo.database");
		String collection = props.getProperty("mongo.collection");
		if (uri == null || uri.trim().isEmpty()) {
			logger.info("could not find env variable MONGO_URI");
			return false;
		} else if(database == null || database.trim().isEmpty()) {
			database = "configuration";
		} else if (collection == null || collection.trim().isEmpty()) {
			collection = "discord";
		}

		logger.info("fetched env data from config.properties");
		setEnvData(uri, database, collection);
		return true;
	}

	public static EnvData getEnvData() {
		if (envData == null) throw new RuntimeException();
		return envData;
	}

	public static void setEnvData(String uri, String database, String collection) {
		envData = new EnvData(uri, database, collection);
		logger.info("set env data to: {}", envData);
	}
}
