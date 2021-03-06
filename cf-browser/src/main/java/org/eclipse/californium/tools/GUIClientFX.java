/*******************************************************************************
 * Copyright (c) 2015, 2017 Institute for Pervasive Computing, ETH Zurich and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Martin Lanter - architect and initial implementation
 *    Bosch Software Innovations GmbH - migrate to SLF4J
 ******************************************************************************/
package org.eclipse.californium.tools;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import org.eclipse.californium.cli.ClientBaseConfig;
import org.eclipse.californium.cli.ClientConfig;
import org.eclipse.californium.cli.ClientInitializer;
import org.eclipse.californium.cli.ClientConfig.ContentType;
import org.eclipse.californium.cli.ClientConfig.MessageType;
import org.eclipse.californium.cli.ClientConfig.Payload;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.network.config.NetworkConfigDefaultHandler;
import org.eclipse.californium.elements.util.StandardCharsets;
import org.eclipse.californium.core.network.config.NetworkConfig.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * A JavaFX CoAP Client to communicate with other CoAP resources.
 */
public class GUIClientFX extends Application {

	private static final Logger LOG = LoggerFactory.getLogger(GUIClientFX.class);

	private static final int DEFAULT_MAX_RESOURCE_SIZE = 8192;
	private static final int DEFAULT_BLOCK_SIZE = 1024;

	private static NetworkConfigDefaultHandler DEFAULTS = new NetworkConfigDefaultHandler() {

		@Override
		public void applyDefaults(NetworkConfig config) {
			config.setString(Keys.RESPONSE_MATCHING, "PRINCIPAL");
			config.setInt(Keys.MAX_RESOURCE_BODY_SIZE, DEFAULT_MAX_RESOURCE_SIZE);
			config.setInt(Keys.MAX_MESSAGE_SIZE, DEFAULT_BLOCK_SIZE);
			config.setInt(Keys.PREFERRED_BLOCK_SIZE, DEFAULT_BLOCK_SIZE);
			config.setInt(Keys.MAX_ACTIVE_PEERS, 10);
			config.setInt(Keys.MAX_PEER_INACTIVITY_PERIOD, 60 * 60 * 24); // 24h
			config.setInt(Keys.TCP_CONNECTION_IDLE_TIMEOUT, 60 * 60 * 12); // 12h
			config.setInt(Keys.TCP_CONNECT_TIMEOUT, 20);
			config.setInt(Keys.TCP_WORKER_THREADS, 2);
		}
	};

	@Command(name = "GUIClientFX", version = "(c) 2016, Institute for Pervasive Computing, ETH Zurich and others.")
	public static class GuiClientConfig extends ClientBaseConfig {

		/**
		 * Content type.
		 */
		@ArgGroup(exclusive = true)
		public ContentType contentType;

		/**
		 * Payload.
		 */
		@ArgGroup(exclusive = true)
		public Payload payload;

		/**
		 * Apply {@link String#format(String, Object...)} to payload. The used
		 * parameter depends on the client implementation.
		 */
		@Option(names = "--payload-format", description = "apply format to payload.")
		public boolean payloadFormat;

		/**
		 * Message type.
		 */
		@ArgGroup(exclusive = true)
		public MessageType messageType;

		@Override
		public void register(CommandLine cmd) {
			super.register(cmd);
			cmd.setNegatableOptionTransformer(ClientConfig.messageTypeTransformer);
		}

		@Override
		public void defaults() {
			super.defaults();
			if (contentType != null) {
				contentType.defaults();
			}
			if (payload != null) {
				int max = networkConfig.getInt(Keys.MAX_RESOURCE_BODY_SIZE);
				payload.defaults(max);
			}
		}

	}

	public static void main(String[] args) throws IOException {
		launch(args);
	}

	@Override
	public void stop() throws Exception {
		super.stop();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parameters parameters = getParameters();
		final GuiClientConfig clientConfig = new GuiClientConfig();
		clientConfig.networkConfigDefaultHandler = DEFAULTS;
		ClientInitializer.init(parameters.getRaw().toArray(new String[0]), clientConfig);
		if (clientConfig.helpRequested) {
			System.exit(0);
		}

		URL fxml = getClass().getResource("gui.fxml");
		FXMLLoader loader = new FXMLLoader(fxml);
		loader.setCharset(StandardCharsets.UTF_8);
		Parent root = loader.load();
		GUIController controller = loader.getController();
		controller.initialize(primaryStage, clientConfig);
		PrintStream ps = new PrintStream(controller.getLogStream());
		System.setOut(ps);
		System.setErr(ps);
		LOG.info("MainFX.controller={}", controller);
		primaryStage.setTitle("CoAP Client");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

}
