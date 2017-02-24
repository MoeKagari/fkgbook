package tool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class GameUtils {
	private static final String pass = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

	private static int byteToInteger(byte b) {
		return b < 0 ? (b + 128) : b;
	}

	//�ַ�����  ->(indexOf)  ����4����  ->  �任  ->  ���
	public static byte[] decrypt(byte[] source) {
		int len = 4;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			for (int index = 0; index < source.length; index += len) {
				int[] a = new int[len];
				int i = 0;
				while (i < len && index + i < source.length) {
					a[i] = pass.indexOf(byteToInteger(source[index + i]));
					i++;
				}

				int[] b = decompress(a);
				int j = 0;
				while (j < b.length) {
					if (a[j + 1] == 64) break;
					baos.write(b[j]);
					j++;
				}
			}
			return baos.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	private static int[] decompress(int[] a) {
		int[] b = new int[3];

		b[0] = (a[0] << 2) + ((a[1] & 48) >> 4);
		b[1] = ((a[1] & 15) << 4) + ((a[2] & 60) >> 2);
		b[2] = ((a[2] & 3) << 6) + a[3];

		return b;
	}

	private static int[] compress(int[] b) {
		int[] a = new int[4];

		a[0] = (b[0] & 252) >> 2;
		a[1] = ((b[0] & 3) << 4) | (b[1] >> 4);
		a[2] = ((b[1] & 15) << 2) | (b[2] >> 6);
		a[3] = b[2] & 63;

		return a;
	}

	//��������  ->  ����3����  ->  �任  ->(charAt)  ���
	public static byte[] encrypt(byte[] source) {
		int len = 3;
		StringBuilder sb = new StringBuilder();
		for (int index = 0; index < source.length; index += len) {
			int[] a = new int[len];
			int i = 0;
			while (i < len && index + i < source.length) {
				a[i] = byteToInteger(source[index + i]);
				i++;
			}

			int b[] = compress(a);

			int j = i;
			while (j < len) {
				b[j + 1] = 64;
				j++;
			}

			for (int oneint : b) {
				sb.append(pass.charAt(oneint));
			}
		}
		return sb.toString().getBytes(Charset.forName("UTF-8"));
	}

}