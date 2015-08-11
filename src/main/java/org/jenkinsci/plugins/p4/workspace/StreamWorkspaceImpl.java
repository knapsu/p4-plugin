package org.jenkinsci.plugins.p4.workspace;

import hudson.Extension;

import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.server.IOptionsServer;

public class StreamWorkspaceImpl extends Workspace {

	private final String streamName;
	private final String format;

	private static Logger logger = Logger.getLogger(StreamWorkspaceImpl.class
			.getName());

	public String getStreamName() {
		return streamName;
	}

	public String getFormat() {
		return format;
	}

	@Override
	public String getName() {
		return format;
	}

	@Override
	public WorkspaceType getType() {
		return WorkspaceType.STREAM;
	}

	@DataBoundConstructor
	public StreamWorkspaceImpl(String charset, boolean pinHost,
			String streamName, String format) {
		super(charset, pinHost);
		this.streamName = streamName;
		this.format = format;
	}

	@Override
	public IClient setClient(IOptionsServer connection, String user)
			throws Exception {
		// expands Workspace name if formatters are used.
		String clientName = getFullName();

		IClient iclient = connection.getClient(clientName);
		if (iclient == null) {
			logger.info("P4: Creating stream client: " + clientName);
			Client implClient = new Client(connection);
			implClient.setName(clientName);
			connection.createClient(implClient);
			iclient = connection.getClient(clientName);
		}
		// Set owner (not set during create)
		iclient.setOwnerName(user);

		// Expand Stream name
		String streamFullName = getExpand().format(getStreamName(), false);
		iclient.setStream(streamFullName);

		return iclient;
	}

	@Extension
	public static final class DescriptorImpl extends WorkspaceDescriptor {

		public static final String defaultFormat = "jenkins-${NODE_NAME}-${JOB_NAME}";

		@Override
		public String getDisplayName() {
			return "Streams (view generated by Perforce for each node)";
		}
	}
}