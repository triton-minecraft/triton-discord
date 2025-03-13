package dev.kyriji.discord.models;

public record EnvData(String uri, String database, String collection) {
	@Override
	public String toString() {
		return "{ uri: \"" + uri + "\", database: \"" + database + "\", collection: \"" + collection + "\" }";
	}
}
