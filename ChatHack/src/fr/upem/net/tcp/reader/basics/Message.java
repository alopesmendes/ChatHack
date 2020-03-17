package fr.upem.net.tcp.reader.basics;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Message {
	private final String login;
	private final String msg;
	
	public Message(String login, String msg) {
		this.login = login;
		this.msg = msg;
	}
	
	public ByteBuffer toByteBuffer() {
		ByteBuffer loginBb = StandardCharsets.UTF_8.encode(login);
		ByteBuffer msgBb = StandardCharsets.UTF_8.encode(msg);
		ByteBuffer buff = ByteBuffer.allocate(2 * Integer.BYTES + loginBb.remaining() + msgBb.remaining());
		
		buff.putInt(loginBb.remaining());
		buff.put(loginBb);
		buff.putInt(msgBb.remaining());
		buff.put(msgBb);
		buff.flip();
		
		return buff;
	}
}
