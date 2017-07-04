package fkg.patch.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.function.BiConsumer;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.api.Response;

import fkg.gui.AppConfig;
import fkg.patch.CommunicateHandler.FKGApiHandler;
import tool.ZLibUtils;

public class Login extends FKGApiHandler {
	public Login() {
		super(getUri());
	}

	public static String getUri() {
		return "/api/v1/user/login";
	}

	@Override
	public void onHeaders(Response proxyResponse, HttpServletResponse httpResponse, BiConsumer<Response, HttpServletResponse> defaultOnHeaders) {}

	@Override
	public void onContent(HttpServletResponse httpResponse, byte[] buffer, int offset, int length) throws IOException {}

	@Override
	public void onSuccess(HttpServletRequest httpRequest, HttpServletResponse httpResponse, ByteArrayOutputStream requestBody, ByteArrayOutputStream responseBody) throws IOException {
		byte[] bytes = responseBody.toByteArray();
		bytes = ZLibUtils.decompress(bytes);
		bytes = Base64.getDecoder().decode(bytes);
		bytes = patch(bytes, AppConfig.getFutuanzhangID());
		bytes = Base64.getEncoder().encode(bytes);
		bytes = ZLibUtils.compress(bytes);

		httpResponse.addHeader("Content-Type", "text/plain");
		httpResponse.addHeader("Content-Length", String.valueOf(bytes.length));
		httpResponse.getOutputStream().write(bytes, 0, bytes.length);
	}

	@Override
	public boolean storeResponseBody() {
		return true;
	}

	private static byte[] patch(byte[] bytes, int target) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		JsonObject json = Json.createReader(new ByteArrayInputStream(replace(bytes, '\\', '#'))).readObject();

		json.forEach((key, value) -> {
			if ("user".equals(key) == false) {
				builder.add(key, value);
			}
		});
		if (json.containsKey("user")) {
			JsonObject user = json.getJsonObject("user");
			JsonObjectBuilder userBuilder = Json.createObjectBuilder();
			user.forEach((key, value) -> {
				if ("deputyLeaderCharacterId".equals(key)) {
					userBuilder.add(key, target);
				} else if ("deputyLeaderUserCharacterId".equals(key)) {
					userBuilder.add(key, 22000000000L);
				} else {
					userBuilder.add(key, value);
				}
			});
			builder.add("user", userBuilder);
		}

		return replace(builder.build().toString().getBytes(), '#', '\\');
	}

	private static byte[] replace(byte[] bytes, char c, char d) {
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == c) {
				bytes[i] = (byte) d;
			}
		}
		return bytes;
	}
}
