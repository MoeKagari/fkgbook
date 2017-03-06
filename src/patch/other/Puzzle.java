package patch.other;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import tool.Downloader;
import tool.FileUtil;

public class Puzzle {

	static Set<String> hints = new TreeSet<>();

	public static void main(String[] args) {
		for (int i = 1; i <= 4; i++) {
			deal(i + ".json");
			for (int j = 1; j <= 11; j++) {
				combineImage(i, j);
			}
		}
		for (int i = 1; i <= 4; i++) {
			for (int j = 1; j <= 6; j++) {
				combineImage(9, i, j);
			}
		}

		hints.forEach(System.out::println);
	}

	private static void combineImage(int root, int i, int j) {
		int r = 3, c = 3;

		String dir = root + "\\" + i;
		int space = 10;
		BufferedImage base = new BufferedImage((space + 100) * c + space, (space + 100) * r + space, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = base.createGraphics();

		int count = 0;
		for (int s = 0; s < r; s++) {
			for (int t = 0; t < c; t++) {
				count++;
				File file = new File(dir + "\\" + j + "\\" + String.format("%02d", count) + ".png");
				try {
					g.drawImage(ImageIO.read(file), space + (space + 100) * t, space + (space + 100) * s, null);
				} catch (IOException e) {
					System.out.println(file.getPath());
				}
			}
		}
		try {
			ImageIO.write(base, "png", FileUtil.create(dir + "\\" + String.format("%02d", j) + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void combineImage(int i, int j) {
		int[] rc = getRowCol(j);
		int r = rc[0], c = rc[1];
		int space = 10;
		BufferedImage base = new BufferedImage((space + 100) * c + space, (space + 100) * r + space, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = base.createGraphics();

		int count = 0;
		for (int s = 0; s < r; s++) {
			for (int t = 0; t < c; t++) {
				if (rc[2] == 4) {
					if (s == 0 && t == 0) continue;
					if (s == 0 && t == c - 1) continue;
					if (s == r - 1 && t == 0) continue;
					if (s == r - 1 && t == c - 1) continue;
				} else if (rc[2] == 1) {
					if (s == r - 1 && t == c - 1) continue;
				}

				count++;
				File file = new File(i + "\\" + j + "\\" + String.format("%02d", count) + ".png");
				try {
					g.drawImage(ImageIO.read(file), space + (space + 100) * t, space + (space + 100) * s, null);
				} catch (IOException e) {
					System.out.println(file.getPath());
				}
			}
		}
		try {
			ImageIO.write(base, "png", FileUtil.create(i + "\\" + String.format("%02d", j) + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int[] getRowCol(int j) {
		return new int[][] {//
				{ 3, 3, 0 }, { 3, 4, 0 }, { 4, 4, 0 }, { 4, 5, 0 }, { 5, 6, 4 }, { 6, 6, 4 }, { 5, 8, 0 }, { 5, 8, 0 },//
				{ 7, 8, 0 }, { 7, 8, 0 }, { 8, 8, 1 }, { 8, 8, 1 }, { 8, 8, 1 }, { 8, 8, 1 }, { 8, 8, 1 }, { 8, 8, 1 },//
		}[j - 1];
	}

	public static void deal(String na) {
		JsonObject json;
		try {
			json = Json.createReader(new FileInputStream(na)).readObject();
		} catch (FileNotFoundException e) {
			System.out.println(na);
			return;
		}

		JsonArray ja = json.getJsonArray("turningCardSheetList");
		Card[] cards = new Card[ja.size()];
		for (int i = 0; i < ja.size(); i++) {
			cards[i] = new Card(ja.getJsonObject(i));
		}

		for (Card card : cards) {
			String filename = card.sheetType + "\\" + card.level + "\\" + String.format("%02d", card.cardOrderNum) + ".png";
			if (card.turningCardSheetGroupId != 8) {
				filename = card.turningCardSheetGroupId + "\\" + filename;
			}
			if (card.hintGroupId != 0) {
				hints.add(card.turningCardItemRateGroupName);
				filename = "hint\\" + filename;
			}
			if (card.textureName.startsWith("000001")) {//金币
				int amount = Integer.parseInt(card.turningCardItemRateGroupName.substring(0, card.turningCardItemRateGroupName.indexOf("\u30b4")));
				BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
				Graphics g = image.createGraphics();
				g.setFont(new Font(null, Font.PLAIN, 15));
				g.drawString(amount + "金币", 10, 50);
				try {
					ImageIO.write(image, "png", FileUtil.create(filename));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				if (new File(filename).exists() == false) {
					byte[] bytes = Downloader.download("http://dugrqaqinbtcq.cloudfront.net/product/images/item/100x100/" + card.textureName);
					if (bytes == null) System.out.println("bytes == null");
					FileUtil.save(filename, bytes);
				}
			}
		}
	}

	public static class Card {
		int id;//???
		int turningCardSheetGroupId;//种类,普通puzzle,按时后续开放puzzle,条件开放puzzle......
		int level;//第几关
		int sheetType;// 4种，左下角人物去不同
		int turningCardItemRateGroupId;
		String turningCardItemRateGroupName;
		int cardOrderNum;//从左到右,从上到下的编号
		String textureName;//内容
		boolean keyItemFlag;//是否为keyitem,不翻到的话,不能进入下一关
		int hintGroupId;//没什么用,关键item左下角人物和荠菜motion不同

		public Card(JsonObject jo) {
			this.id = jo.getInt("id");
			this.turningCardSheetGroupId = jo.getInt("turningCardSheetGroupId");
			this.level = jo.getInt("level");
			this.sheetType = jo.getInt("sheetType");
			this.turningCardItemRateGroupId = jo.getInt("turningCardItemRateGroupId");
			this.turningCardItemRateGroupName = jo.getString("turningCardItemRateGroupName");
			this.cardOrderNum = jo.getInt("cardOrderNum");
			this.textureName = jo.getString("textureName");
			this.keyItemFlag = jo.getInt("keyItemFlag") == 1;
			this.hintGroupId = jo.getInt("hintGroupId");
		}

		// "id": 5869,
		// "turningCardSheetGroupId": 3,
		// "level": 4,
		// "sheetType": 1,
		// "turningCardItemRateGroupId": 84,
		// "turningCardItemRateGroupName": "\u5f37\u5316\u970a\u9752\u306e\u30de\u30cb\u30e520\u624d x1",
		// "cardOrderNum": 8,
		// "textureName": "109918.png",
		//"hintGroupId": 0,//2017.03.06新增
		// "keyItemFlag": 0
	}

}
