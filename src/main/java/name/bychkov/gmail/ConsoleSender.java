package name.bychkov.gmail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class ConsoleSender {

	public static void main(String[] args) throws IOException, AddressException, MessagingException {
		String applicationName = System.getProperty("application.name");
		String delegate = System.getProperty("delegate");
		String serviceAccountKey = System.getProperty("service.account.key");
		String emailSubject = System.getProperty("email.subject");
		String emailBody = System.getProperty("email.body");
		String emailRecipient = System.getProperty("email.recipient");

		HttpTransport transport = Utils.getDefaultTransport();
		JsonFactory jsonFactory = Utils.getDefaultJsonFactory();
		GoogleCredential credentials = GoogleCredential
				.fromStream(Files.newInputStream(Paths.get(serviceAccountKey))
						, transport, jsonFactory)
				.createScoped(Arrays.asList(GmailScopes.GMAIL_SEND)).createDelegated(delegate);
		credentials.refreshToken();
		Gmail client = new Gmail.Builder(transport, jsonFactory, credentials).setApplicationName(applicationName)
				.build();

		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		MimeMessage email = new MimeMessage(session);
		email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(emailRecipient));
		email.setSubject(emailSubject);
		email.setText(emailBody);

		Message message = new Message();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		email.writeTo(buffer);
		message.encodeRaw(buffer.toByteArray());

		client.users().messages().send(delegate, message).execute();
	}
}
